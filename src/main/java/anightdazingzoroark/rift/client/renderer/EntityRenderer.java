package anightdazingzoroark.rift.client.renderer;

import anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenderer {
    public static void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(Tyrannosaurus.class, TyrannosaurusRenderer::new);
    }
}
