package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.client.ui.*;
import anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen.RiftCreatureBoxInfoScreen;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.RiftCreatureBoxScreen;
import anightdazingzoroark.prift.client.ui.journalScreen.RiftJournalScreen;
import anightdazingzoroark.prift.client.ui.movesScreen.RiftMovesScreen;
import anightdazingzoroark.prift.client.ui.partyScreen.RiftPartyScreen;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.*;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.inventory.FeedingTroughContainer;
import anightdazingzoroark.prift.server.inventory.WeaponContainer;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import anightdazingzoroark.riftlib.ui.RiftLibUIRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class RiftGui implements IGuiHandler {
    //NOTE: soon all of this will be replaced with modularui stuff
    public static final int GUI_MENU_FROM_RADIAL = 3;
    public static final int GUI_WEAPON_INVENTORY = 4;
    public static final int GUI_FEEDING_TROUGH = 5;
    public static final int GUI_SEMI_MANUAL_EXTRACTOR = 6;
    public static final int GUI_SEMI_MANUAL_PRESSER = 7;
    public static final int GUI_SEMI_MANUAL_EXTRUDER = 8;
    public static final int GUI_SEMI_MANUAL_HAMMERER = 9;
    public static final int GUI_MILLSTONE = 10;
    public static final int GUI_MECHANICAL_FILTER = 11;

    public static final String CREATURE_BOX_SCREEN = "creatureBoxScreen";
    public static final String JOURNAL_SCREEN = "journalScreen";
    public static final String MOVES_SCREEN = "movesScreen";
    public static final String PARTY_SCREEN = "partyScreen";
    public static final String CREATURE_BOX_INFO_SCREEN = "creatureBoxInfoScreen";
    public static final String EGG_SCREEN = "eggScreen";

    public static void registerUI() {
        RiftLibUIRegistry.registerUI(CREATURE_BOX_SCREEN, RiftCreatureBoxScreen.class);
        RiftLibUIRegistry.registerUI(JOURNAL_SCREEN, RiftJournalScreen.class);
        RiftLibUIRegistry.registerUI(MOVES_SCREEN, RiftMovesScreen.class);
        RiftLibUIRegistry.registerUI(PARTY_SCREEN, RiftPartyScreen.class);
        RiftLibUIRegistry.registerUI(CREATURE_BOX_INFO_SCREEN, RiftCreatureBoxInfoScreen.class);
        RiftLibUIRegistry.registerUI(EGG_SCREEN, RiftEggScreen.class);
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Entity entity = world.getEntityByID(x);
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GUI_WEAPON_INVENTORY) {
            if (entity instanceof RiftLargeWeapon) {
                return new WeaponContainer((RiftLargeWeapon) entity, player);
            }
        }
        else if (id == GUI_FEEDING_TROUGH) {
            if (tileEntity instanceof RiftTileEntityFeedingTrough) {
                return new FeedingTroughContainer((RiftTileEntityFeedingTrough)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_EXTRACTOR) {
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
        else if (id == GUI_MILLSTONE) {
            if (tileEntity instanceof TileEntityMillstone) {
                return new MillstoneContainer((TileEntityMillstone)tileEntity, player);
            }
        }
        else if (id == GUI_MECHANICAL_FILTER) {
            if (tileEntity instanceof TileEntityMechanicalFilter) {
                return new MechanicalFilterContainer((TileEntityMechanicalFilter)tileEntity, player);
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Entity entity = world.getEntityByID(x);
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GUI_WEAPON_INVENTORY) {
            IInventory playerInventory = player.inventory;
            return new RiftWeaponInvMenu(playerInventory, (RiftLargeWeapon) entity);
        }
        else if (id == GUI_FEEDING_TROUGH) {
            if (tileEntity instanceof RiftTileEntityFeedingTrough) {
                return new RiftFeedingTroughInvMenu((RiftTileEntityFeedingTrough) tileEntity, player.inventory);
            }
        }
        else if (id == GUI_SEMI_MANUAL_EXTRACTOR) {
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
        else if (id == GUI_MILLSTONE) {
            if (tileEntity instanceof TileEntityMillstone) {
                return new RiftMillstoneMenu((TileEntityMillstone) tileEntity, player.inventory);
            }
        }
        else if (id == GUI_MECHANICAL_FILTER) {
            if (tileEntity instanceof TileEntityMechanicalFilter) {
                return new RiftMechanicalFilterMenu((TileEntityMechanicalFilter) tileEntity, player.inventory);
            }
        }
        return null;
    }
}
