package anightdazingzoroark.rift.client;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.renderer.EntityRenderer;
import anightdazingzoroark.rift.client.ui.RiftCreatureInvMenu;
import anightdazingzoroark.rift.client.ui.RiftDialMenu;
import anightdazingzoroark.rift.client.ui.RiftEggMenu;
import anightdazingzoroark.rift.client.ui.RiftMountEnergyBar;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
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
    private int thirdPersonView = 0;
    private int previousViewType = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        EntityRenderer.registerRenderers();
        MinecraftForge.EVENT_BUS.register(new RiftMountEnergyBar());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        registerItemRenderer();
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
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
        Entity entity = world.getEntityByID(x);
        if (id == GUI_EGG) {
            return new RiftEggMenu();
        }
        else if (id == GUI_DIAL) {
            return new RiftDialMenu((RiftCreature) entity);
        }
        else if (id == GUI_CREATURE_INVENTORY) {
            IInventory playerInventory = player.inventory;
            return new RiftCreatureInvMenu(playerInventory, (RiftCreature) entity);
        }
        return null;
    }

    public void set3rdPersonView(int view) {
        thirdPersonView = view;
    }

    public int get3rdPersonView() {
        return thirdPersonView;
    }

    public void setPreviousViewType(int view) {
        previousViewType = view;
    }

    public int getPreviousViewType() {
        return previousViewType;
    }
}
