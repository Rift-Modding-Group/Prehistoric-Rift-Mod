package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSetEntityMotion;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

public class RiftChargeMove extends RiftCreatureMove {
    private BlockPos lookAtPosition;
    private BlockPos targetPosForCharge;
    private int chargeTime;
    private int maxChargeTime;
    private double chargeDirectionToPosX;
    private double chargeDirectionToPosZ;
    private final int chargeSpeed = 4;

    public RiftChargeMove() {
        super(CreatureMove.CHARGE);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();

        //this is only relevant when unmounted
        if (target != null) this.targetPosForCharge = new BlockPos(target);
    }

    @Override
    public void onEndChargeUp(RiftCreature user, int useAmount) {
        user.setCanMove(true);

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
            this.maxChargeTime = (int)Math.round(Math.sqrt(chargeDistX * chargeDistX + chargeDistZ * chargeDistZ) / (this.chargeSpeed * 2));
        }
        //this is only relevant when mounted
        else {
            //get charge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(user.getLookVec().x, 2) + Math.pow(user.getLookVec().z, 2));
            this.chargeDirectionToPosX = user.getLookVec().x / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = user.getLookVec().z / unnormalizedMagnitude;

            //get charge time
            this.maxChargeTime = (int) Math.ceil(RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 0, user.rangedWidth()) / this.chargeSpeed);
        }
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        AxisAlignedBB chargerDetectHitbox = user.getEntityBoundingBox();
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

        if (hitBlocksFlag || !chargedIntoEntities.isEmpty() || this.chargeTime >= this.maxChargeTime) {
            user.motionX = 0;
            user.motionZ = 0;
            user.velocityChanged = true;

            //damage all entities it charged into
            if (!chargedIntoEntities.isEmpty()) {
                List<Entity> entitiesToDamage = user.world.getEntitiesWithinAABB(Entity.class, chargerEffectHitbox, this.generalEntityPredicate(user));
                for (Entity entity : entitiesToDamage) {
                    user.attackEntityAsMob(entity);
                }
            }

            //destroy all breakable blocks it has hit
            //this can skip the breakBlocksFlagw
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
            //for some reason when being ridden and tryin to make a creature
            //charge/lunge into a wall theres a good chance they'll stop prematurely
            //so there's this shit instead
            if (user.isBeingRidden() && user.getControllingPassenger() != null)
                RiftMessages.WRAPPER.sendToAll(new RiftSetEntityMotion(user, this.chargeDirectionToPosX * this.chargeSpeed, this.chargeDirectionToPosZ * this.chargeSpeed));
            else
                user.move(MoverType.SELF, this.chargeDirectionToPosX * this.chargeSpeed, user.motionY, this.chargeDirectionToPosZ * this.chargeSpeed);

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
        if (this.lookAtPosition != null) user.getLookHelper().setLookPosition(this.lookAtPosition.getX(), this.lookAtPosition.getY(), this.lookAtPosition.getZ(), 180.0F, 30.0F);
        else this.lookAtPosition = new BlockPos(target);
    }
}
