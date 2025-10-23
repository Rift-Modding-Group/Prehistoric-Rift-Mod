package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class RiftTackleMove extends RiftCreatureMove {
    private BlockPos targetPosForTackle;

    public RiftTackleMove() {
        super(CreatureMove.TACKLE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.onGround && !user.isInWater();
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user.onGround && !user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();

        //only relevant if creature is ridden
        //if the creature is being ridden, targets pos is where it charges to
        if (target != null) this.targetPosForTackle = new BlockPos(target);
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        user.setCanMove(true);
        //if the creature is being ridden, targets pos is where it charges to
        if (this.targetPosForTackle != null) user.chargeToPos(this.targetPosForTackle, 4D);
        //otherwise, use position forward based on where it lookin at as charge pos
        //distance to charge by is based on useAmount and the creatures ranged attack reach
        else {
            Vec3d lookVecNoY = new Vec3d(user.getLookVec().x, 0, user.getLookVec().z);
            double chargeDirectionToPosX = lookVecNoY.normalize().x * 4;
            double chargeDirectionToPosZ = lookVecNoY.normalize().z * 4;
            this.targetPosForTackle = user.getPosition().add(chargeDirectionToPosX, 0, chargeDirectionToPosZ);
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
        boolean hitBlocksFlag = false;
        breakBlocksLoop: for (int x = MathHelper.floor(tackleDetectHitbox.minX); x < MathHelper.ceil(tackleDetectHitbox.maxX); x++) {
            for (int z = MathHelper.floor(tackleDetectHitbox.minZ); z < MathHelper.ceil(tackleDetectHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR) {
                    hitBlocksFlag = true;
                    break breakBlocksLoop;
                }
            }
        }

        if (hitBlocksFlag || !chargedIntoEntities.isEmpty() || user.stopChargeFlag || user.isInWater()) {
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
            if (this.targetPosForTackle != null) user.chargeToPos(this.targetPosForTackle, 4D);

            if (this.useValue > 0) this.useValue--; //this only matters when using while mounted
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.endCharge();
        user.enableCanRotateMounted();
    }
}
