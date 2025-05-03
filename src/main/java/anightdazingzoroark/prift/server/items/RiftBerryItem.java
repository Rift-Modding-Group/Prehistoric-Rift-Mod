package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.server.blocks.RiftBerryBush;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftBerryItem extends ItemFood {
    private final Block bushBlock;

    public RiftBerryItem(Block bushBlock) {
        super(1, 0.2f, false);
        this.bushBlock = bushBlock;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        boolean flag = this.placeBush(worldIn, player, pos, facing, player.getHeldItem(hand));
        if (flag && !player.capabilities.isCreativeMode) {
            player.getHeldItem(hand).shrink(1);
        }
        //if (flag) player.swingArm(hand);
        return flag ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    private boolean placeBush(World world, EntityPlayer player, BlockPos pos, EnumFacing facing, ItemStack stack) {
        IBlockState iblockstate = world.getBlockState(pos);
        Block oldBlock = iblockstate.getBlock();
        if (!oldBlock.isReplaceable(world, pos)) pos = pos.offset(facing);

        if (!world.isRemote) {
            IBlockState state = this.bushBlock.getDefaultState();
            if (this.bushBlock.canPlaceBlockAt(world, pos)) {
                this.bushBlock.onBlockPlacedBy(world, pos, state, player, stack);
                world.setBlockState(pos, state);
                return true;
            }
        }
        return false;
    }
}
