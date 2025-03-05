package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.List;

public class RiftLungeMove extends RiftCreatureMove {
    private BlockPos targetPosForLunge;
    private int lungeTime;
    private int maxLungeTime;
    private double lungeDirectionToPosX;
    private double lungeDirectionToPosZ;

    public RiftLungeMove() {
        super(CreatureMove.LUNGE);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.getDistance(target) > user.attackWidth() + 1 && user.getDistance(target) <= user.rangedWidth() / 2 && (user.onGround || user.isInWater())) {
            return MovePriority.HIGH;
        }
        return MovePriority.NONE;
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user.onGround || user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.disableCanRotateMounted();
        if (target != null) this.targetPosForLunge = new BlockPos(target.posX, user.posY, target.posZ);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

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

        if (breakBlocksFlag || !chargedIntoEntities.isEmpty() || this.lungeTime >= this.maxLungeTime) {
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

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            user.motionX = this.lungeDirectionToPosX * 8;
            user.motionZ = this.lungeDirectionToPosZ * 8;
            user.velocityChanged = true;
            this.lungeTime++;
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (this.targetPosForLunge != null) {
            //get charge distance
            double unnormalizedDirectionX = this.targetPosForLunge.getX() - user.posX;
            double unnormalizedDirectionZ = this.targetPosForLunge.getZ() - user.posZ;
            double angleToTarget = Math.atan2(unnormalizedDirectionZ, unnormalizedDirectionX);
            double chargeDistX = Math.min(user.rangedWidth() * Math.cos(angleToTarget), unnormalizedDirectionX);
            double chargeDistZ = Math.min(user.rangedWidth() * Math.sin(angleToTarget), unnormalizedDirectionZ);

            //get charge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(unnormalizedDirectionX, 2) + Math.pow(unnormalizedDirectionZ, 2));
            this.lungeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
            this.lungeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;

            //get charge time
            this.maxLungeTime = (int)Math.round(Math.sqrt(chargeDistX * chargeDistX + chargeDistZ * chargeDistZ) * 1.5D / 8);
        }
        else {
            //get lunge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(user.getLookVec().x, 2) + Math.pow(user.getLookVec().z, 2));
            this.lungeDirectionToPosX = user.getLookVec().x / unnormalizedMagnitude;
            this.lungeDirectionToPosZ = user.getLookVec().z / unnormalizedMagnitude;

            //get lunge time
            this.maxLungeTime = (int) Math.ceil(user.rangedWidth() * 1.5D / 8);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.enableCanRotateMounted();
    }
}
