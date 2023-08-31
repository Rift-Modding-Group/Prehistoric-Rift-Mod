package anightdazingzoroark.rift.client;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.renderer.EntityRenderer;
import anightdazingzoroark.rift.client.ui.RiftCreatureInventory;
import anightdazingzoroark.rift.client.ui.RiftDialMenu;
import anightdazingzoroark.rift.client.ui.RiftEggMenu;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.TameBehaviorType;
import anightdazingzoroark.rift.server.entity.TameStatusType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static anightdazingzoroark.rift.client.renderer.ItemRenderer.registerItemRenderer;

@Mod.EventBusSubscriber
public class ClientProxy extends ServerProxy {
    @Mod.Instance(RiftInitialize.MODID)
    public static Object EGG;
    @Mod.Instance(RiftInitialize.MODID)
    public static Object CREATURE;

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        EntityRenderer.registerRenderers();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        registerItemRenderer();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {}

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_EGG) {
            return new RiftEggMenu();
        }
        else if (id == GUI_DIAL) {
            return new RiftDialMenu();
        }
        else if (id == GUI_CREATURE_INVENTORY) {
            IInventory playerInventory = player.inventory;
            IInventory creatureInventory = ((RiftCreature) CREATURE).creatureInventory;
            return new RiftCreatureInventory(playerInventory, creatureInventory, (RiftCreature) CREATURE);
        }
        return null;
    }
}
