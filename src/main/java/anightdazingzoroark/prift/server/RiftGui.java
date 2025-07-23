package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.client.ui.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.*;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.inventory.CreatureContainer;
import anightdazingzoroark.prift.server.inventory.FeedingTroughContainer;
import anightdazingzoroark.prift.server.inventory.WeaponContainer;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
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
    public static final int GUI_EGG = 0;
    public static final int GUI_DIAL = 1;
    public static final int GUI_CREATURE_INVENTORY = 2;
    public static final int GUI_MENU_FROM_RADIAL = 3;
    public static final int GUI_WEAPON_INVENTORY = 4;
    public static final int GUI_JOURNAL = 5;
    public static final int GUI_PARTY = 6;
    public static final int GUI_MOVES = 7;
    public static final int GUI_MENU_FROM_PARTY = 8;
    public static final int GUI_FEEDING_TROUGH = 9;
    public static final int GUI_SEMI_MANUAL_EXTRACTOR = 10;
    public static final int GUI_SEMI_MANUAL_PRESSER = 11;
    public static final int GUI_SEMI_MANUAL_EXTRUDER = 12;
    public static final int GUI_SEMI_MANUAL_HAMMERER = 13;
    public static final int GUI_MILLSTONE = 14;
    public static final int GUI_MECHANICAL_FILTER = 15;
    public static final int GUI_CREATURE_BOX = 16;
    public static final int GUI_MENU_FROM_CREATURE_BOX = 17;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Entity entity = world.getEntityByID(x);
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GUI_CREATURE_INVENTORY) {
            if (entity instanceof RiftCreature) {
                return new CreatureContainer((RiftCreature) entity, player);
            }
        }
        else if (id == GUI_WEAPON_INVENTORY) {
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
        if (id == GUI_EGG) {
            if (entity instanceof RiftEgg) return new RiftEggMenu((RiftEgg)entity);
            else if (entity instanceof RiftSac) return new RiftEggMenu((RiftSac)entity);
            else if (entity instanceof RiftCreature && ((RiftCreature)entity).canBePregnant()) return new RiftEggMenu((RiftCreature)entity);
        }
        else if (id == GUI_DIAL) return new RiftDialMenu((RiftCreature) entity);
        else if (id == GUI_CREATURE_INVENTORY) {
            IInventory playerInventory = player.inventory;
            return new RiftCreatureInvMenu(playerInventory, (RiftCreature) entity);
        }
        else if (id == GUI_WEAPON_INVENTORY) {
            IInventory playerInventory = player.inventory;
            return new RiftWeaponInvMenu(playerInventory, (RiftLargeWeapon) entity);
        }
        else if (id == GUI_PARTY) {
            return new RiftPartyScreen(x, y);
        }
        else if (id == GUI_MOVES) {
            return new RiftMovesScreen(x);
        }
        else if (id == GUI_MENU_FROM_PARTY) {
            return new RiftPopupFromPlayerParty(x);
        }
        else if (id == GUI_JOURNAL) {
            return new RiftJournalScreen();
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
        else if (id == GUI_CREATURE_BOX) {
            return new RiftCreatureBoxMenu(x, y, z);
        }
        else if (id == GUI_MENU_FROM_CREATURE_BOX) {
            return new RiftPopupFromCreatureBox(y, z);
        }
        return null;
    }
}
