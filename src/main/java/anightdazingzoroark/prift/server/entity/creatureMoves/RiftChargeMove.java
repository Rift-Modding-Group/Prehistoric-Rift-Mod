package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RiftChargeMove extends RiftCreatureMove {
    private double chargePosX;
    private double chargePosZ;
    private double chargeDirectionToPosX;
    private double chargeDirectionToPosZ;

    public RiftChargeMove() {
        super(CreatureMove.CHARGE);
    }

    @Override
    public MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target) {
        if (user.getDistance(target) > user.attackWidth() + 1 && user.getDistance(target) <= user.rangedWidth()) {
            return MovePriority.HIGH;
        }
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user, EntityLivingBase target) {
        user.removeSpeed();
        if (target != null) {
            double unnormalizedDirectionX = target.posX - user.posX;
            double unnormalizedDirectionZ = target.posZ - user.posZ;
            double angleToTarget = Math.atan2(unnormalizedDirectionZ, unnormalizedDirectionX);
            this.chargePosX = user.rangedWidth() * Math.cos(Math.toRadians(angleToTarget));
            this.chargePosZ = user.rangedWidth() * Math.sin(Math.toRadians(angleToTarget));

            double unnormalizedMagnitude = Math.sqrt(Math.pow(unnormalizedDirectionX, 2) + Math.pow(unnormalizedDirectionZ, 2));
            this.chargeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;
        }
        else {
            this.chargePosX = -user.rangedWidth() * Math.sin(Math.toRadians(user.rotationYaw));
            this.chargePosZ = user.rangedWidth() * Math.cos(Math.toRadians(user.rotationYaw));
            double unnormalizedDirectionX = this.chargePosX - user.posX;
            double unnormalizedDirectionZ = this.chargePosZ - user.posZ;

            double unnormalizedMagnitude = Math.sqrt(Math.pow(unnormalizedDirectionX, 2) + Math.pow(unnormalizedDirectionZ, 2));
            this.chargeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;
        }
    }

    @Override
    public void whileExecuting(RiftCreature user) {
        //stop if it hits a mob
        AxisAlignedBB chargerHitbox = user.getEntityBoundingBox().grow(1.5D);
        List<Entity> chargedIntoEntities = user.world.getEntitiesWithinAABB(Entity.class, chargerHitbox, new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity instanceof RiftCreaturePart) {
                    RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                    return !parent.equals(user) && RiftUtil.checkForNoAssociations(user, parent);
                }
                else if (entity instanceof EntityLivingBase) return RiftUtil.checkForNoAssociations(user, entity);
                else return false;
            }
        });
        chargedIntoEntities.remove(user);

        //stop if it hits a block
        boolean breakBlocksFlag = false;
        breakBlocksLoop: for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
            for (int z = MathHelper.floor(chargerHitbox.minZ); z < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR) {
                    breakBlocksFlag = true;
                    break breakBlocksLoop;
                }
            }
        }

        if (breakBlocksFlag || !chargedIntoEntities.isEmpty() || this.atSpotToChargeTo(user)) {
            //stop charging
            user.motionX = 0;
            user.motionZ = 0;

            //damage all entities it charged into
            if (!chargedIntoEntities.isEmpty()) for (Entity entity : chargedIntoEntities) {
                if (entity instanceof RiftCreaturePart) {
                    RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                    user.attackEntityAsMob(parent);
                }
                user.attackEntityAsMob(entity);
            }

            //destroy all breakable blocks it has hit
            boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(user.world, user);
            if (breakBlocksFlag && canBreak) {
                List<BlockPos> toBreak = new ArrayList<>();
                for (int x = MathHelper.floor(chargerHitbox.minX); x < MathHelper.ceil(chargerHitbox.maxX); x++) {
                    for (int y = MathHelper.floor(chargerHitbox.minY); y < MathHelper.ceil(chargerHitbox.maxY); y++) {
                        for (int z = MathHelper.floor(chargerHitbox.minZ); z < MathHelper.ceil(chargerHitbox.maxZ); z++) {
                            BlockPos blockpos = new BlockPos(x, y, z);
                            IBlockState iblockstate = user.world.getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR && y >= user.posY) {
                                if (user.checkBasedOnStrength(iblockstate)) toBreak.add(blockpos);
                            }
                        }
                    }
                }
                for (BlockPos blockPos : toBreak) user.world.destroyBlock(blockPos, false);
            }

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            user.motionX = this.chargeDirectionToPosX * 4;
            user.motionZ = this.chargeDirectionToPosZ * 4;
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target, int useAmount) {

    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }

    @Override
    public void onHitEntity(RiftCreature user, EntityLivingBase target) {

    }

    @Override
    public void onHitBlock(RiftCreature user, BlockPos targetPos) {

    }

    private boolean atSpotToChargeTo(RiftCreature user) {
        return user.posX >= this.chargePosX - 1
                && user.posX <= this.chargePosX + 1
                && user.posZ >= this.chargePosZ - 1
                && user.posZ <= this.chargePosZ + 1;
    }
}
