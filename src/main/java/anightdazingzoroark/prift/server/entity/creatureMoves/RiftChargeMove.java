package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
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
    private BlockPos lookAtPosition;
    private BlockPos targetPosForCharge;
    private int chargeTime;
    private int maxChargeTime;
    private double chargeDirectionToPosX;
    private double chargeDirectionToPosZ;

    public RiftChargeMove() {
        super(CreatureMove.CHARGE);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();
        //this is only relevant when unmounted
        if (target != null) this.targetPosForCharge = new BlockPos(target.posX, user.posY, target.posZ);
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        //this is only relevant when unmounted
        if (this.targetPosForCharge != null) {
            //get charge distance
            double unnormalizedDirectionX = this.targetPosForCharge.getX() - user.posX;
            double unnormalizedDirectionZ = this.targetPosForCharge.getZ() - user.posZ;
            double angleToTarget = Math.atan2(unnormalizedDirectionZ, unnormalizedDirectionX);
            double chargeDistX = user.rangedWidth() * Math.cos(angleToTarget);
            double chargeDistZ = user.rangedWidth() * Math.sin(angleToTarget);

            //get charge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(unnormalizedDirectionX, 2) + Math.pow(unnormalizedDirectionZ, 2));
            this.chargeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;

            //get charge time
            //the point at which it stops when unmounted is doubled, so the max charge time here
            //is to be halved to make it consistent with when mounted
            this.maxChargeTime = (int)Math.round(Math.sqrt(chargeDistX * chargeDistX + chargeDistZ * chargeDistZ) * 1.5D / 16);
        }
        //this is only relevant when mounted
        else {
            //get charge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(user.getLookVec().x, 2) + Math.pow(user.getLookVec().z, 2));
            this.chargeDirectionToPosX = user.getLookVec().x / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = user.getLookVec().z / unnormalizedMagnitude;

            //get charge time
            this.maxChargeTime = (int) Math.ceil(RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 0, user.rangedWidth()) * 1.5D / 8);
        }
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        AxisAlignedBB chargerHitbox = user.getEntityBoundingBox().grow(1.5D);

        //stop if it hits a mob
        List<Entity> chargedIntoEntities = user.world.getEntitiesWithinAABB(Entity.class, chargerHitbox, new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity instanceof RiftCreaturePart) {
                    RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                    return !parent.equals(user)
                            && RiftUtil.checkForNoAssociations(user, parent)
                            && ((parent instanceof IHerder && user instanceof IHerder) ?
                            ((IHerder)user).getHerdLeader() != null && ((IHerder)parent).getHerdLeader() != null && !((IHerder)user).getHerdLeader().equals(((IHerder)parent).getHerdLeader())
                            : true);
                }
                else if (entity instanceof RiftCreature) {
                    return  !entity.equals(user)
                            && RiftUtil.checkForNoAssociations(user, entity)
                            && ((entity instanceof IHerder && user instanceof IHerder) ?
                            ((IHerder)user).getHerdLeader() != null && ((IHerder)entity).getHerdLeader() != null && !((IHerder)user).getHerdLeader().equals(((IHerder)entity).getHerdLeader())
                            : true);
                }
                else if (entity instanceof EntityLivingBase) return RiftUtil.checkForNoAssociations(user, entity) && !entity.equals(user);
                else return false;
            }
        });

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

        if (breakBlocksFlag || !chargedIntoEntities.isEmpty() || this.chargeTime >= this.maxChargeTime) {
            //stop charging
            user.motionX = 0;
            user.motionZ = 0;
            user.velocityChanged = true;

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
                                if (user.checkIfCanBreakBlock(iblockstate)) toBreak.add(blockpos);
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
            user.motionX = this.chargeDirectionToPosX * 8;
            user.motionZ = this.chargeDirectionToPosZ * 8;
            user.velocityChanged = true;
            this.chargeTime++;
            if (this.useValue > 0) this.useValue--; //this only matters when using while mounted
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        this.setUseValue(useAmount);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }

    @Override
    public void lookAtTarget(RiftCreature user, Entity target) {
        if (this.lookAtPosition != null) user.getLookHelper().setLookPosition(this.lookAtPosition.getX(), this.lookAtPosition.getY(), this.lookAtPosition.getZ(), 30.0F, 30.0F);
        else this.lookAtPosition = target.getPosition();
    }
}
