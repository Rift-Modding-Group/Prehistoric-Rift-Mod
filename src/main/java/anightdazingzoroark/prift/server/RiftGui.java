package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.client.ui.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class RiftGui implements IGuiHandler {
    //NOTE: soon all of this will be replaced with modularui stuff
    public static final int GUI_SEMI_MANUAL_EXTRACTOR = 6;
    public static final int GUI_SEMI_MANUAL_PRESSER = 7;
    public static final int GUI_SEMI_MANUAL_EXTRUDER = 8;
    public static final int GUI_SEMI_MANUAL_HAMMERER = 9;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GUI_SEMI_MANUAL_EXTRACTOR) {
            if (tileEntity instanceof TileEntitySemiManualExtractor) {
                return new SemiManualExtractorContainer((TileEntitySemiManualExtractor)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_PRESSER) {
            if (tileEntity instanceof TileEntitySemiManualPresser) {
                return new SemiManualPresserContainer((TileEntitySemiManualPresser)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_EXTRUDER) {
            if (tileEntity instanceof TileEntitySemiManualExtruder) {
                return new SemiManualExtruderContainer((TileEntitySemiManualExtruder)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_HAMMERER) {
            if (tileEntity instanceof TileEntitySemiManualHammerer) {
                return new SemiManualHammererContainer((TileEntitySemiManualHammerer)tileEntity, player);
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GUI_SEMI_MANUAL_EXTRACTOR) {
            if (tileEntity instanceof TileEntitySemiManualExtractor) {
                return new RiftSemiManualExtractorMenu((TileEntitySemiManualExtractor) tileEntity, player.inventory);
            }
        }
        else if (id == GUI_SEMI_MANUAL_PRESSER) {
            if (tileEntity instanceof TileEntitySemiManualPresser) {
                return new RiftSemiManualPresserMenu((TileEntitySemiManualPresser) tileEntity, player.inventory);
            }
        }
        else if (id == GUI_SEMI_MANUAL_EXTRUDER) {
            if (tileEntity instanceof TileEntitySemiManualExtruder) {
                return new RiftSemiManualExtruderMenu((TileEntitySemiManualExtruder) tileEntity, player.inventory);
            }
        }
        else if (id == GUI_SEMI_MANUAL_HAMMERER) {
            if (tileEntity instanceof TileEntitySemiManualHammerer) {
                return new RiftSemiManualHammererMenu((TileEntitySemiManualHammerer) tileEntity, player.inventory);
            }
        }
        return null;
    }
}
