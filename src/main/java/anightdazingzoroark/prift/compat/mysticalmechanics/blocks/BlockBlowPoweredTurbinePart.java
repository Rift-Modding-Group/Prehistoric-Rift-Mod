package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbinePart;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBlowPoweredTurbinePart extends Block implements ITileEntityProvider {
    public BlockBlowPoweredTurbinePart() {
        super(Material.IRON);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 2);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBlowPoweredTurbinePart();
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            TileEntityBlowPoweredTurbinePart part = (TileEntityBlowPoweredTurbinePart) worldIn.getTileEntity(pos);
            if (part != null) {
                BlockBlowPoweredTurbine blowPoweredTurbine = (BlockBlowPoweredTurbine)worldIn.getBlockState(part.getCenterBlockPos()).getBlock();
                if (blowPoweredTurbine != null) {
                    blowPoweredTurbine.onBlockHarvested(worldIn, part.getCenterBlockPos(), worldIn.getBlockState(part.getCenterBlockPos()), player);
                    worldIn.destroyBlock(part.getCenterBlockPos(), false);
                }
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(RiftMMItems.BLOW_POWERED_TURBINE);
    }
}
