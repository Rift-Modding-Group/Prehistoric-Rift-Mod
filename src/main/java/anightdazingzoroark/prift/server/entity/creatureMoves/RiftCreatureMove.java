package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class RiftCreatureMove {
    public final CreatureMove creatureMove;
    public boolean forceStopFlag = false;
    protected int useValue;

    public RiftCreatureMove(CreatureMove creatureMove) {
        this.creatureMove = creatureMove;
    }

    //this section is for moves when used in battle
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return true;
    }

    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return true;
    }

    public String cannotExecuteMountedMessage() {
        return null;
    }

    public void onStartExecuting(RiftCreature user) {
        this.onStartExecuting(user, null);
    }

    public abstract void onStartExecuting(RiftCreature user, Entity target);

    public void onEndChargeUp(RiftCreature user, int useAmount) {}

    public abstract void whileChargingUp(RiftCreature user);

    public abstract void whileExecuting(RiftCreature user);

    public void onReachUsePoint(RiftCreature user, Entity target) {
        this.onReachUsePoint(user, target, 0);
    }

    public abstract void onReachUsePoint(RiftCreature user, Entity target, int useAmount);

    public void onBeforeStopExecuting(RiftCreature user) {}

    public abstract void onStopExecuting(RiftCreature user);

    public void lookAtTarget(RiftCreature user, Entity target) {
        if (target != null) user.getLookHelper().setLookPositionWithEntity(target, 180F, 180F);
    }

    //this is for advanced anim time controls when managing animations for moves used while creature is mounted
    //the use value of the move is set here
    public void setUseValue(int value) {
        this.useValue = value;
    }

    //if the move is disrupted, the use value left goes here to be subtracted from the anim times
    public int getUseValue() {
        return this.useValue;
    }

    public int[] unmountedChargeBounds() {
        return new int[]{(int)(this.creatureMove.maxUse * 0.3), this.creatureMove.maxUse};
    }

    //for use in when break block mode is active
    public void breakBlocksInFront(RiftCreature user) {
        //get all blocks in front of user based on width, melee reach, and height
        List<BlockPos> blockBreakList = new ArrayList<>();

        Vec3d look = user.getLookVec().normalize();
        double offset = Math.ceil(user.width / 2.0) + 0.5;
        double creatureHeight = user.height + ((user.isBeingRidden() && user.getControllingPassenger() != null) ? user.getControllingPassenger().height : 0);
        double firstX, firstY, firstZ, lastX, lastY, lastZ;

        //for looking up
        if (user.rotationPitch <= -22.5f) {
            firstX = user.posX - ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    offset : creatureHeight/2D);
            firstY = user.posY + user.height;
            firstZ = user.posZ - ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    creatureHeight/2D : offset);

            lastX = user.posX + ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    offset : creatureHeight/2D);
            lastY = user.posY + user.height + user.attackWidth() + user.width;
            lastZ = user.posZ + ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    creatureHeight/2D : offset);
        }
        //for looking down
        else if (user.rotationPitch >= 22.5f) {
            firstX = user.posX - ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    offset : creatureHeight/2D);
            firstY = user.posY - 1;
            firstZ = user.posZ - ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    creatureHeight/2D : offset);

            lastX = user.posX + ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    offset : creatureHeight/2D);
            lastY = user.posY - 2; //to make sure that digging downwards doesn't result in a hole they cannot get out of
            lastZ = user.posZ + ((user.creatureHorizontalDirection() == EnumFacing.NORTH || user.creatureHorizontalDirection() == EnumFacing.SOUTH) ?
                    creatureHeight/2D : offset);
        }
        //for looking forward
        else {
            firstX = user.posX + look.x - (-look.z) * offset;
            firstY = user.posY;
            firstZ = user.posZ + look.z - look.x * offset;

            lastX = user.posX + (user.attackWidth() + user.width) * look.x + (-look.z) * offset;
            lastY = user.posY + user.height + ((user.isBeingRidden() && user.getControllingPassenger() != null) ? user.getControllingPassenger().height : 0);
            lastZ = user.posZ + (user.attackWidth() + user.width) * look.z + look.x * offset;
        }

        BlockPos firstPos = new BlockPos(
                firstX,
                firstY,
                firstZ
        );
        BlockPos lastPos = new BlockPos(
                lastX,
                lastY,
                lastZ
        );

        //for loops wouldn't work in this situation, so it's
        //time to do nested while loops
        int x = firstPos.getX();
        int y = firstPos.getY();
        int z = firstPos.getZ();

        //outer loop is x
        while ((lastPos.getX() - firstPos.getX() >= 0) == (x <= lastPos.getX())) {
            //middle loop is z
            while ((lastPos.getZ() - firstPos.getZ() >= 0) == (z <= lastPos.getZ())) {
                //inner loop is y
                while ((lastPos.getY() - firstPos.getY() >= 0) == (y <= lastPos.getY())) {

                    //this is where all the block detection happens
                    BlockPos testPos = new BlockPos(x, y, z);
                    IBlockState blockState = user.world.getBlockState(testPos);
                    if (user.checkIfCanBreakBlock(blockState) && !blockBreakList.contains(testPos)) {
                        blockBreakList.add(testPos);
                    }

                    //update y after every iteration
                    if (lastPos.getY() - firstPos.getY() >= 0) y++;
                    else y--;
                }
                //reset y when the loop ends
                y = firstPos.getY();

                //update z after every iteration
                if (lastPos.getZ() - firstPos.getZ() >= 0) z++;
                else z--;
            }

            //reset z when the loop ends
            z = firstPos.getZ();

            //update x after every iteration
            if (lastPos.getX() - firstPos.getX() >= 0) x++;
            else x--;
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
    }

    protected Predicate<Entity> generalEntityPredicate(RiftCreature user) {
        return this.generalEntityPredicate(user, false);
    }

    protected Predicate<Entity> generalEntityPredicate(RiftCreature user, boolean considerSize) {
        return new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity instanceof EntityLivingBase || entity instanceof MultiPartEntityPart)
                    return RiftUtil.checkForNoAssociations(user, entity)
                            && RiftUtil.checkForNoHerdAssociations(user, entity)
                            && (considerSize ? RiftUtil.isAppropriateSizeNotEqual(entity, RiftUtil.getMobSize(user)) : true);
                else return false;
            }
        };
    }
}
