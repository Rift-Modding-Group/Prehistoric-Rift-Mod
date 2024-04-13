package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.particle.RiftBleedParticle;
import anightdazingzoroark.prift.client.particle.RiftParticleSpawner;
import anightdazingzoroark.prift.client.particle.RiftSnowParticle;
import anightdazingzoroark.prift.client.renderer.BlockRenderer;
import anightdazingzoroark.prift.client.renderer.EntityRenderer;
import anightdazingzoroark.prift.client.ui.*;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.client.data.GlowingMetadataSection;
import anightdazingzoroark.prift.client.data.GlowingMetadataSectionSerializer;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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

import static anightdazingzoroark.prift.client.renderer.ItemRenderer.registerItemRenderer;

@Mod.EventBusSubscriber
public class ClientProxy extends ServerProxy {
    @Mod.Instance(RiftInitialize.MODID)
    public static Object EGG;
    public static Object popupFromRadial;
    private int thirdPersonView = 0;
    private int previousViewType = 0;
    private RiftParticleSpawner particleSpawner;

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        EntityRenderer.registerRenderers();
        BlockRenderer.registerRenderers();
        MinecraftForge.EVENT_BUS.register(new RiftMountEnergyBar());
        MinecraftForge.EVENT_BUS.register(new RiftRightClickChargeBar());
        MinecraftForge.EVENT_BUS.register(new RiftLeftClickChargeBar());
        MinecraftForge.EVENT_BUS.register(new RiftSpacebarChargeBar());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        registerItemRenderer();
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        this.particleSpawner = new RiftParticleSpawner();
        Minecraft.getMinecraft().metadataSerializer.registerMetadataSectionType(new GlowingMetadataSectionSerializer(), GlowingMetadataSection.class);
        RiftControls.init();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void spawnParticle(String name, double x, double y, double z, double motX, double motY, double motZ) {
        World world = Minecraft.getMinecraft().world;
        Particle particle = null;
        if (world == null) return;
        if (name.equals("bleed")) particle = new RiftBleedParticle(world, x, y, z, motX, motY, motZ);
        else if (name.equals("snow")) particle = new RiftSnowParticle(world, x, y, z, motX, motY, motZ);
        if (particle != null) particleSpawner.spawnParticle(particle, false, false, false, x, y, z);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {}

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Entity entity = world.getEntityByID(x);
        if (id == GUI_EGG) return new RiftEggMenu();
        else if (id == GUI_DIAL) return new RiftDialMenu((RiftCreature) entity);
        else if (id == GUI_CREATURE_INVENTORY) {
            IInventory playerInventory = player.inventory;
            return new RiftCreatureInvMenu(playerInventory, (RiftCreature) entity);
        }
        else if (id == GUI_MENU_FROM_RADIAL) return new RiftPopupFromRadial((RiftCreature) entity);
        else if (id == GUI_WEAPON_INVENTORY) {
            IInventory playerInventory = player.inventory;
            return new RiftWeaponInvMenu(playerInventory, (RiftLargeWeapon) entity);
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
