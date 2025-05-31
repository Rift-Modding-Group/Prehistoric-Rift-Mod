package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.TileChoppingBlock;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.spi.TileAnvilBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

    public abstract void onStopExecuting(RiftCreature user);

    public void lookAtTarget(RiftCreature user, Entity target) {
        if (target != null) user.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
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
    public void breakBlocks(RiftCreature user) {
        //get all blocks in front of user based on width, melee reach, and height
        //currently this only gets a straight line, will figure out soon how to
        //make this
        List<BlockPos> blockBreakList = new ArrayList<>();

        Vec3d look = user.getLookVec().normalize();
        double perpX = -look.z;
        double perpZ = look.x;
        double offset = Math.ceil(user.width / 2.0) + 0.5;

        BlockPos firstPos = new BlockPos(
                user.posX + look.x - perpX * offset,
                user.posY,
                user.posZ + look.z - perpZ * offset
        );
        BlockPos lastPos = new BlockPos(
                user.posX + (user.attackWidth() + user.width) * look.x + perpX * offset,
                user.posY + user.height + ((user.isBeingRidden() && user.getControllingPassenger() != null) ? user.getControllingPassenger().height : 0),
                user.posZ + (user.attackWidth() + user.width) * look.z + perpZ * offset
        );

        //for loops wouldn't work in this situation, so its
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
                for (ItemStack stack : drops) user.creatureInventory.addItem(stack);
                user.world.destroyBlock(posToBreak, false);
            }
        }
    }
}
