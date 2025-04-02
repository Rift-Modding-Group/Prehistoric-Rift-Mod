package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.particle.*;
import anightdazingzoroark.prift.client.renderer.BlockRenderer;
import anightdazingzoroark.prift.client.renderer.EntityRenderer;
import anightdazingzoroark.prift.client.renderer.FluidRenderer;
import anightdazingzoroark.prift.client.renderer.ItemRenderer;
import anightdazingzoroark.prift.client.ui.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.*;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.client.data.GlowingMetadataSection;
import anightdazingzoroark.prift.client.data.GlowingMetadataSectionSerializer;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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

import java.util.UUID;

@Mod.EventBusSubscriber
public class ClientProxy extends ServerProxy {
    @Mod.Instance(RiftInitialize.MODID)
    public static Object popupFromRadial;
    public static Object swapTypeForPopup;
    public static UUID creatureUUID;
    public static BlockPos creatureBoxBlockPos;
    public static int creatureIdForWorkstation = -1;
    public static boolean settingCreatureWorkstation = false;
    private int thirdPersonView = 0;
    private int previousViewType = 0;
    private RiftParticleSpawner particleSpawner;

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        EntityRenderer.registerRenderers();
        BlockRenderer.registerRenderers();
        MinecraftForge.EVENT_BUS.register(new RiftCreatureControls());
        MinecraftForge.EVENT_BUS.register(new RiftLargeWeaponControls());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ItemRenderer.registerItemRenderer();
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
        switch (name) {
            case "bleed":
                particle = new RiftBleedParticle(world, x, y, z, motX, motY, motZ);
                break;
            case "snow":
                particle = new RiftSnowParticle(world, x, y, z, motX, motY, motZ);
                break;
            case "detect":
                particle = new RiftDetectParticle(world, x, y, z);
                break;
            case "chest_detect":
                particle = new RiftChestDetectParticle(world, x, y, z);
                break;
            case "pregnancy":
                particle = new RiftPregnancyParticle(world, x, y, z, motX, motY, motZ);
                break;
        }
        if (particle != null) this.particleSpawner.spawnParticle(particle, false, false, false, x, y, z);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        FluidRenderer.registerRenderers();
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
