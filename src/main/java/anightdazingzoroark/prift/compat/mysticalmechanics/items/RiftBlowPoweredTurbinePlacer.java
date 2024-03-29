package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class RiftBlowPoweredTurbinePlacer extends Item {
    public RiftBlowPoweredTurbinePlacer() {
        super();
        this.setMaxDamage(0);
        this.maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        boolean flag = this.placeTurbine(world, player, pos, player.getHeldItem(hand));
        if (flag && !player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        else if (!flag){
            if (!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_place_turbine"), false);
        }
        return flag ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    protected boolean placeTurbine(World world, EntityPlayer player, BlockPos pos, ItemStack stack) {
        EnumFacing facing = player.getHorizontalFacing().getOpposite();
        BlockPos newPos = pos.offset(facing);
        boolean flag = true;

        if (!world.isRemote) {
            //should check for 3 x 1 x 3 space
            loop: for (int width = -1; width <= 1; width++) {
                for (int height = -1; height <= 1; height++) {
                    int widthX = facing.getAxis().equals(EnumFacing.Axis.X) ? width : 0;
                    int widthZ = facing.getAxis().equals(EnumFacing.Axis.Z) ? width : 0;
                    BlockPos testPos = newPos.offset(facing).add(widthX, height, widthZ);
                    if (!world.isAirBlock(testPos)) {
                        flag = false;
                        break loop;
                    }
                }
            }

            //place block
            if (flag) {
                Block turbine = RiftMMBlocks.BLOW_POWERED_TURBINE;
                IBlockState state = turbine.getDefaultState().withProperty(BlockHorizontal.FACING, facing);
                turbine.onBlockPlacedBy(world, newPos, state, player, stack);
                world.setBlockState(newPos, state);
            }
        }
        return flag;
    }
}
