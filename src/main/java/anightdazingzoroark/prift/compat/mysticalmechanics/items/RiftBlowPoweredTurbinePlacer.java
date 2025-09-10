package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockBlowPoweredTurbinePart;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbinePart;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class RiftBlowPoweredTurbinePlacer extends ItemBlock {
    public RiftBlowPoweredTurbinePlacer() {
        super(RiftMMBlocks.BLOW_POWERED_TURBINE);
        this.setMaxDamage(0);
        this.maxStackSize = 1;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        EnumFacing facing = player.getHorizontalFacing().getOpposite();
        boolean flag = true;

        //should check for 3 x 1 x 3 space
        loop: for (int width = -1; width <= 1; width++) {
            for (int height = -1; height <= 1; height++) {
                int widthX = facing.getAxis().equals(EnumFacing.Axis.Z) ? width : facing.getAxisDirection().equals(EnumFacing.AxisDirection.POSITIVE) ? -1 : 1;
                int widthZ = facing.getAxis().equals(EnumFacing.Axis.X) ? width : facing.getAxisDirection().equals(EnumFacing.AxisDirection.POSITIVE) ? -1 : 1;
                BlockPos testPos = pos.offset(facing).add(widthX, height, widthZ);
                if (!world.isAirBlock(testPos)) {
                    flag = false;
                    break loop;
                }
            }
        }

        //if insufficient space, do not continue
        if (!flag) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_place_turbine"), false);
            return false;
        }

        //go on as usual
        if (!world.setBlockState(pos, newState, 11)) return false;
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() == this.block) {
            //rotate state
            state = state.withProperty(BlockHorizontal.FACING, facing);

            //place main turbine block
            ItemBlock.setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
            world.setBlockState(pos, state);

            //place turbine parts
            for (int width = -1; width <= 1; width++) {
                for (int height = -1; height <= 1; height++) {
                    int widthX = facing.getAxis().equals(EnumFacing.Axis.Z) ? width : facing.getAxisDirection().equals(EnumFacing.AxisDirection.POSITIVE) ? -1 : 1;
                    int widthZ = facing.getAxis().equals(EnumFacing.Axis.X) ? width : facing.getAxisDirection().equals(EnumFacing.AxisDirection.POSITIVE) ? -1 : 1;
                    BlockPos placePos = pos.offset(facing).add(widthX, height, widthZ);

                    if (!pos.equals(placePos)) {
                        Block turbinePart = RiftMMBlocks.BLOW_POWERED_TURBINE_PART;
                        IBlockState partState = turbinePart.getDefaultState();
                        turbinePart.onBlockPlacedBy(world, placePos, partState, player, stack);
                        world.setBlockState(placePos, partState);

                        //for associated tile entity
                        TileEntityBlowPoweredTurbinePart turbinePartTE = (TileEntityBlowPoweredTurbinePart)world.getTileEntity(placePos);
                        if (turbinePartTE != null) turbinePartTE.setCenterBlockPos(pos);
                    }
                }
            }

            if (player instanceof EntityPlayerMP) CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        }

        return true;
    }
}
