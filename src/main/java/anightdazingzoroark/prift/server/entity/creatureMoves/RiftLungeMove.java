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

public class RiftLungeMove extends RiftCreatureMove {
    private BlockPos targetPosForLunge;

    public RiftLungeMove() {
        super(CreatureMove.LUNGE);
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user.onGround || user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();
        if (target != null) this.targetPosForLunge = new BlockPos(target.posX, user.posY, target.posZ);
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        user.setCanMove(true);
        //if the creature is being ridden, targets pos is where it charges to
        if (this.targetPosForLunge != null) user.chargeToPos(this.targetPosForLunge, 8D);
            //otherwise, use position forward based on where it lookin at as charge pos
            //distance to charge by is based on useAmount and the creatures ranged attack reach
        else {
            Vec3d lookVec = user.getLookVec();
            Vec3d lungeVec = lookVec.normalize().scale(user.rangedWidth());
            this.targetPosForLunge = user.getPosition().add(lungeVec.x, lungeVec.y, lungeVec.z);
        }
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        AxisAlignedBB tackleDetectHitbox = user.frontOfCreatureAABB();
        AxisAlignedBB tackleEffectHitbox = tackleDetectHitbox.grow(2D);

        //stop if it hits a mob
        List<Entity> chargedIntoEntities = user.world.getEntitiesWithinAABB(Entity.class, tackleDetectHitbox.grow(1D), this.generalEntityPredicate(user));

        //stop if it hits a block
        boolean hitBlocksFlag = user.isInWater() ? this.hitBlocksInWater(user, tackleDetectHitbox) : this.hitBlocksOutOfWater(user, tackleDetectHitbox);

        if (hitBlocksFlag || !chargedIntoEntities.isEmpty() || user.stopChargeFlag) {
            //damage all entities it charged into
            if (!chargedIntoEntities.isEmpty()) {
                List<Entity> entitiesToDamage = user.world.getEntitiesWithinAABB(Entity.class, tackleEffectHitbox, this.generalEntityPredicate(user));
                for (Entity entity : entitiesToDamage) {
                    user.attackEntityAsMob(entity);
                }
            }

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            //now charge to final charge position
            //note that it is meant to be executed every tick
            if (this.targetPosForLunge != null) user.chargeToPos(this.targetPosForLunge, 8D);
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {
        this.targetPosForLunge = null;
        user.endCharge();
        user.enableCanRotateMounted();
    }

    private boolean hitBlocksOutOfWater(RiftCreature user, AxisAlignedBB tackleDetectHitbox) {
        for (int x = MathHelper.floor(tackleDetectHitbox.minX); x < MathHelper.ceil(tackleDetectHitbox.maxX); x++) {
            for (int z = MathHelper.floor(tackleDetectHitbox.minZ); z < MathHelper.ceil(tackleDetectHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR
                        && state.getMaterial() != Material.WATER && stateUp.getMaterial() != Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hitBlocksInWater(RiftCreature user, AxisAlignedBB tackleDetectHitbox) {
        for (int x = MathHelper.floor(tackleDetectHitbox.minX); x < MathHelper.ceil(tackleDetectHitbox.maxX); x++) {
            for (int y = MathHelper.floor(tackleDetectHitbox.minY - 1); y < MathHelper.ceil(tackleDetectHitbox.maxY + 1); y++) {
                for (int z = MathHelper.floor(tackleDetectHitbox.minZ); z < MathHelper.ceil(tackleDetectHitbox.maxZ); z++) {
                    IBlockState state = user.world.getBlockState(new BlockPos(x, y, z));

                    if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
