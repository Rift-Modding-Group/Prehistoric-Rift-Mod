package anightdazingzoroark.rift.client.renderer;

import anightdazingzoroark.rift.client.renderer.entity.ThrownStegoPlateRenderer;
import anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import anightdazingzoroark.rift.client.renderer.entity.RiftEggRenderer;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlate;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenderer {
    public static void registerRenderers() {
        //creatures
        for (RiftCreatureType creature : RiftCreatureType.values()) RenderingRegistry.registerEntityRenderingHandler(creature.getCreature(), creature.getRenderFactory());

        //everythin else
        RenderingRegistry.registerEntityRenderingHandler(RiftEgg.class, RiftEggRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ThrownStegoPlate.class, ThrownStegoPlateRenderer::new);
    }
}
