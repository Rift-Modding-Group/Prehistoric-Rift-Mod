package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

public class RiftChargeMove extends RiftCreatureMove {
    private BlockPos lookAtPosition;
    private BlockPos targetPosForCharge;

    public RiftChargeMove() {
        super(CreatureMove.CHARGE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.onGround && !user.isInWater();
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user) {
        return user.onGround && !user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();

        //only relevant if creature is ridden
        //if the creature is being ridden, targets pos is where it charges to
        if (target != null) this.targetPosForCharge = new BlockPos(target);
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        user.setCanMove(true);
        //if the creature is being ridden, targets pos is where it charges to
        if (this.targetPosForCharge != null) user.chargeToPos(this.targetPosForCharge, 8D);
        //otherwise, use position forward based on where it lookin at as charge pos
        //distance to charge by is based on useAmount and the creatures ranged attack reach
        else {
            Vec3d lookVecNoY = new Vec3d(user.getLookVec().x, 0, user.getLookVec().z);
            double slopeResForCharge = RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 0, user.rangedWidth());
            double chargeDirectionToPosX = lookVecNoY.normalize().x * slopeResForCharge;
            double chargeDirectionToPosZ = lookVecNoY.normalize().z * slopeResForCharge;
            this.targetPosForCharge = user.getPosition().add(chargeDirectionToPosX, 0, chargeDirectionToPosZ);
        }
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        AxisAlignedBB chargerDetectHitbox = user.frontOfCreatureAABB();
        AxisAlignedBB chargerEffectHitbox = chargerDetectHitbox.grow(2D);

        //stop if it hits a mob
        List<Entity> chargedIntoEntities = user.world.getEntitiesWithinAABB(Entity.class, chargerDetectHitbox.grow(1D), this.generalEntityPredicate(user));

        //stop if it hits a block
        boolean hitBlocksFlag = false;
        breakBlocksLoop: for (int x = MathHelper.floor(chargerDetectHitbox.minX); x < MathHelper.ceil(chargerDetectHitbox.maxX); x++) {
            for (int z = MathHelper.floor(chargerDetectHitbox.minZ); z < MathHelper.ceil(chargerDetectHitbox.maxZ); z++) {
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
                List<Entity> entitiesToDamage = user.world.getEntitiesWithinAABB(Entity.class, chargerEffectHitbox, this.generalEntityPredicate(user));
                for (Entity entity : entitiesToDamage) {
                    user.attackEntityAsMob(entity);
                }
            }

            //destroy all breakable blocks it has hit
            //this can skip the breakBlocksFlag
            //first get blocks in the detection list
            List<BlockPos> blockBreakList = new ArrayList<>();
            for (int x = MathHelper.floor(chargerEffectHitbox.minX); x < MathHelper.ceil(chargerEffectHitbox.maxX); x++) {
                for (int y = MathHelper.floor(chargerEffectHitbox.minY); y < MathHelper.ceil(chargerEffectHitbox.maxY); y++) {
                    for (int z = MathHelper.floor(chargerEffectHitbox.minZ); z < MathHelper.ceil(chargerEffectHitbox.maxZ); z++) {
                        BlockPos blockpos = new BlockPos(x, y, z);
                        IBlockState iblockstate = user.world.getBlockState(blockpos);
                        if (iblockstate.getMaterial() != Material.AIR && y >= user.posY) {
                            if (user.checkIfCanBreakBlock(iblockstate)) blockBreakList.add(blockpos);
                        }
                    }
                }
            }

            //now break the blocks
            for (BlockPos posToBreak : blockBreakList) {
                IBlockState blockState = user.world.getBlockState(posToBreak);

                //break block and put the items in the creatures inventory
                if (user.checkIfCanBreakBlock(blockState)) {
                    List<ItemStack> drops = blockState.getBlock().getDrops(user.world, posToBreak, blockState, 0);
                    if (user.isTamed()) for (ItemStack stack : drops) user.creatureInventory.addItem(stack);
                    user.world.destroyBlock(posToBreak, !user.isTamed());
                }
            }

            this.breakBlocksInFront(user);

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            //now charge to final charge position
            //note that it is meant to be executed every tick
            if (this.targetPosForCharge != null) user.chargeToPos(this.targetPosForCharge, 8D);

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
        user.endCharge();
        user.enableCanRotateMounted();
    }

    @Override
    public void lookAtTarget(RiftCreature user, Entity target) {
        if (this.lookAtPosition != null) user.getLookHelper().setLookPosition(this.lookAtPosition.getX(), this.lookAtPosition.getY(), this.lookAtPosition.getZ(), 180.0F, 30.0F);
        else this.lookAtPosition = new BlockPos(target);
    }
}
