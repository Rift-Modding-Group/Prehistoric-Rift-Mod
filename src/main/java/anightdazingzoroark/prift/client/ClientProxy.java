package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.client.hud.TameProgressHUD;
import anightdazingzoroark.prift.client.rendering.ItemRenderer;
import anightdazingzoroark.prift.client.rendering.block.SmokenutBushBlockRenderer;
import anightdazingzoroark.prift.client.rendering.entity.RiftCreatureRenderer;
import anightdazingzoroark.prift.client.rendering.entity.RiftProjectileRenderer;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class ClientProxy extends ServerProxy {
    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        //register entity renderers
        RenderingRegistry.registerEntityRenderingHandler(RiftCreature.class, RiftCreatureRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftProjectile.class, RiftProjectileRenderer::new);

        //register block renderers
        GeoBlockRenderer.registerBlockRenderer(RiftBlocks.SMOKENUT_BUSH, new SmokenutBushBlockRenderer());

        //register HUDs
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);

        ClientRegistry.registerKeyBinding(RiftControls.SWITCH_PARTY_MEMBER_UP);
        ClientRegistry.registerKeyBinding(RiftControls.SWITCH_PARTY_MEMBER_DOWN);
        ClientRegistry.registerKeyBinding(RiftControls.DEPLOY_PARTY_MEMBER);

        //register item renderers
        ItemRenderer.registerItemRenderer();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
}
