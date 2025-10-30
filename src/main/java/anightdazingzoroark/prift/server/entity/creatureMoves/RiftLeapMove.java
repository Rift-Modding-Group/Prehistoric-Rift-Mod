package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RiftLeapMove extends RiftCreatureMove {
    private BlockPos targetPosForLeap;

    public RiftLeapMove() {
        super(CreatureMove.POUNCE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return false;
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user) {
        return user.onGround && !user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.disableCanRotateMounted();
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        //use position forward based on where it lookin at as leap pos
        //distance to leap by is based on the creatures ranged attack reach
        Vec3d lookVecNoY = new Vec3d(user.getLookVec().x, 0, user.getLookVec().z);
        double leapDirectionToPosX = lookVecNoY.normalize().x * user.rangedWidth();
        double leapDirectionToPosZ = lookVecNoY.normalize().z * user.rangedWidth();
        this.targetPosForLeap = user.getPosition().add(leapDirectionToPosX, 0, leapDirectionToPosZ);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        AxisAlignedBB leapDetectHitbox = user.frontOfCreatureAABB();

        //stop if it hits a block
        boolean hitBlocksFlag = false;
        breakBlocksLoop: for (int x = MathHelper.floor(leapDetectHitbox.minX); x < MathHelper.ceil(leapDetectHitbox.maxX); x++) {
            for (int z = MathHelper.floor(leapDetectHitbox.minZ); z < MathHelper.ceil(leapDetectHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR) {
                    hitBlocksFlag = true;
                    break breakBlocksLoop;
                }
            }
        }

        if (hitBlocksFlag || user.stopLeapFlag || user.isInWater()) {
            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            //now leap to final leap position
            //note that it is meant to be executed every tick
            if (this.targetPosForLeap != null) user.leapToPos(this.targetPosForLeap, 8D);
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
