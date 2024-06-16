package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualHammerer;
import anightdazingzoroark.prift.server.ServerProxy;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.item.spi.ItemTongsFullBase;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.util.BloomHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class BlockSemiManualHammerer extends BlockSemiManualBase {
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySemiManualHammerer();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            if (playerIn.getHeldItem(hand).getItem() instanceof ItemTongsFullBase) {
                //get bloom nbt
                NBTTagCompound nbtTagCompound = playerIn.getHeldItem(hand).getTagCompound();
                NBTTagCompound bloomTagCompound = nbtTagCompound != null ? nbtTagCompound.getCompoundTag("BlockEntityTag") : null;
                ItemStack bloomStack = BloomHelper.createBloomAsItemStack(new ItemStack(ModuleTechBloomery.Blocks.BLOOM), bloomTagCompound);
                TileEntitySemiManualHammerer te = (TileEntitySemiManualHammerer) worldIn.getTileEntity(pos);
                if (te != null && te.getInputItem() != null && te.getInputItem().isEmpty()) {
                    IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    itemHandler.insertItem(0, bloomStack, false);
                    playerIn.setHeldItem(hand, BloomHelper.createItemTongsEmpty(playerIn.getHeldItem(hand), !playerIn.isCreative()));
                }
            }
            else playerIn.openGui(RiftInitialize.instance, ServerProxy.GUI_SEMI_MANUAL_HAMMERER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(RiftMMItems.SEMI_MANUAL_HAMMERER);
    }
}
