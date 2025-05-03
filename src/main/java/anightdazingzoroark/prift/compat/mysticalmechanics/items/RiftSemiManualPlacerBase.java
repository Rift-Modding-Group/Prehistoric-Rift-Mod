package anightdazingzoroark.prift.compat.mysticalmechanics.items;

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

public class RiftSemiManualPlacerBase extends Item {
    private final Block toPlace;
    private final Block topBlock;

    public RiftSemiManualPlacerBase(Block toPlace, Block topBlock) {
        super();
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.toPlace = toPlace;
        this.topBlock = topBlock;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        boolean flag = this.placeSemiManMech(world, player, pos, facing, player.getHeldItem(hand));
        if (flag && !player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        else if (!flag){
            if (!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_place_turbine"), false);
        }
        return flag ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    protected boolean placeSemiManMech(World world, EntityPlayer player, BlockPos pos, EnumFacing facing, ItemStack stack) {
        EnumFacing playerFacing = player.getHorizontalFacing().getOpposite();
        IBlockState iblockstate = world.getBlockState(pos);
        Block oldBlock = iblockstate.getBlock();

        if (!oldBlock.isReplaceable(world, pos)) pos = pos.offset(facing);
        boolean flag = true;
        if (!world.isRemote) {
            if (!world.isAirBlock(pos.up())) flag = false;
            if (flag) {
                //bottom
                IBlockState stateBottom = this.toPlace.getDefaultState().withProperty(BlockHorizontal.FACING, playerFacing);
                this.toPlace.onBlockPlacedBy(world, pos, stateBottom, player, stack);
                world.setBlockState(pos, stateBottom);
                //top
                IBlockState stateTop = this.topBlock.getDefaultState().withProperty(BlockHorizontal.FACING, playerFacing);
                this.toPlace.onBlockPlacedBy(world, pos.up(), stateTop, player, stack);
                world.setBlockState(pos.up(), stateTop);
            }
        }
        return flag;
    }
}
