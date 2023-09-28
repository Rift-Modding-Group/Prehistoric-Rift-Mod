package anightdazingzoroark.rift.client.renderer;

import anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import anightdazingzoroark.rift.client.renderer.entity.RiftEggRenderer;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenderer {
    public static void registerRenderers() {
        for (int x = 0; x < RiftCreatureType.values().length; x++) {
            RiftCreatureType creature = RiftCreatureType.values()[x];
            RenderingRegistry.registerEntityRenderingHandler(creature.getCreature(), creature.getRenderFactory());
        }
        RenderingRegistry.registerEntityRenderingHandler(RiftEgg.class, RiftEggRenderer::new);
    }
}
