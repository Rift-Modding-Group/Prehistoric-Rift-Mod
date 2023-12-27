package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.inventory.CreatureContainer;
import anightdazingzoroark.prift.server.inventory.WeaponContainer;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.recipes.RiftRecipes;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ServerProxy implements IGuiHandler {
    public static final int GUI_EGG = 0;
    public static final int GUI_DIAL = 1;
    public static final int GUI_CREATURE_INVENTORY = 2;
    public static final int GUI_MENU_FROM_RADIAL = 3;
    public static final int GUI_WEAPON_INVENTORY = 4;

    public void preInit(FMLPreInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(RiftInitialize.instance, this);
        RiftItems.registerItems();
        RiftItems.registerOreDicTags();
        RiftRecipes.registerSmelting();
        MinecraftForge.EVENT_BUS.register(new RiftItems());
        RiftEntities.registerEntities();
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void spawnParticle(String name, double x, double y, double z, double motX, double motY, double motZ) {}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {}

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {}

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Entity entity = world.getEntityByID(x);
        if (id == GUI_CREATURE_INVENTORY) {
            if (entity instanceof RiftCreature) {
                return new CreatureContainer((RiftCreature) entity, player);
            }
        }
        else if (id == GUI_MENU_FROM_RADIAL) {
            return new CreatureContainer((RiftCreature) entity, player);
        }
        else if (id == GUI_WEAPON_INVENTORY) {
            if (entity instanceof RiftLargeWeapon) {
                return new WeaponContainer((RiftLargeWeapon) entity, player);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public int get3rdPersonView() {
        return 0;
    }

    public void set3rdPersonView(int view) {}

    public void setPreviousViewType(int view) {}

    public int getPreviousViewType() {
        return 0;
    }
}
