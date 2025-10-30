package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class RiftPounceMove extends RiftCreatureMove {
    private BlockPos targetPosForPounce;

    public RiftPounceMove() {
        super(CreatureMove.POUNCE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.getDistance(target) < user.rangedWidth() && user.getDistance(target) > user.rangedWidth() * 0.25;
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user) {
        return user.onGround && !user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.disableCanRotateMounted();

        //only relevant if creature is not ridden
        //if the creature is not being ridden, targets pos is where it charges to
        if (target != null) this.targetPosForPounce = new BlockPos(target);
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        //if the creature is not being ridden, targets pos is where it leaps to
        if (this.targetPosForPounce != null) user.leapToPos(this.targetPosForPounce, 8D);
        //otherwise, use position forward based on where it lookin at as leap pos
        //distance to leap by is based on the creatures ranged attack reach
        else {
            Vec3d lookVecNoY = new Vec3d(user.getLookVec().x, 0, user.getLookVec().z);
            double leapDirectionToPosX = lookVecNoY.normalize().x * user.rangedWidth();
            double leapDirectionToPosZ = lookVecNoY.normalize().z * user.rangedWidth();
            this.targetPosForPounce = user.getPosition().add(leapDirectionToPosX, 0, leapDirectionToPosZ);
        }
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        AxisAlignedBB leapDetectEntityHitbox = user.getEntityBoundingBox();
        AxisAlignedBB leapEffectEntityHitbox = leapDetectEntityHitbox.grow(2D);
        AxisAlignedBB leapDetectBlockHitbox = user.frontOfCreatureAABB();

        //stop if it hits a mob
        List<Entity> leapedIntoEntities = user.world.getEntitiesWithinAABB(Entity.class, leapDetectEntityHitbox.grow(1D), this.generalEntityPredicate(user));

        //stop if it hits a block
        boolean hitBlocksFlag = false;
        breakBlocksLoop: for (int x = MathHelper.floor(leapDetectBlockHitbox.minX); x < MathHelper.ceil(leapDetectBlockHitbox.maxX); x++) {
            for (int z = MathHelper.floor(leapDetectBlockHitbox.minZ); z < MathHelper.ceil(leapDetectBlockHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR) {
                    hitBlocksFlag = true;
                    break breakBlocksLoop;
                }
            }
        }

        if (hitBlocksFlag || !leapedIntoEntities.isEmpty() || user.stopLeapFlag || user.isInWater()) {
            //damage all entities it leaped into
            if (!leapedIntoEntities.isEmpty()) {
                List<Entity> entitiesToDamage = user.world.getEntitiesWithinAABB(Entity.class, leapEffectEntityHitbox, this.generalEntityPredicate(user));
                for (Entity entity : entitiesToDamage) {
                    user.attackEntityAsMob(entity);
                }
            }

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            //now leap to final leap position
            //note that it is meant to be executed every tick
            if (this.targetPosForPounce != null) user.leapToPos(this.targetPosForPounce, 8D);
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.endLeap();
        user.enableCanRotateMounted();
    }
}
