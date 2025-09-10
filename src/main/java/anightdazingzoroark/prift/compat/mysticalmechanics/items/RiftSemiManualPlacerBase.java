package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class RiftSemiManualPlacerBase extends ItemBlock {
    private final Block topBlock;

    public RiftSemiManualPlacerBase(Block toPlace, Block topBlock) {
        super(toPlace);
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.topBlock = topBlock;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        EnumFacing facing = player.getHorizontalFacing().getOpposite();

        //if upper block is not air, do not continue
        if (!world.isAirBlock(pos.up())) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_place_semi_manual_machine"), false);
            return false;
        }


        //go on as usual
        if (!world.setBlockState(pos, newState, 11)) return false;
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() == this.block) {
            //rotate state
            state = state.withProperty(BlockHorizontal.FACING, facing);

            //bottom
            ItemBlock.setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
            world.setBlockState(pos, state);

            //top
            IBlockState stateTop = this.topBlock.getDefaultState().withProperty(BlockHorizontal.FACING, facing);
            this.topBlock.onBlockPlacedBy(world, pos.up(), stateTop, player, stack);
            world.setBlockState(pos.up(), stateTop);

            if (player instanceof EntityPlayerMP) CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
        }

        return true;
    }
}
