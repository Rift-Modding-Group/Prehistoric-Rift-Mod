package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CreaturePartRenderer extends Render<RiftCreaturePart> {
    protected CreaturePartRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(RiftCreaturePart entity) {
        return null;
    }

    public static class Factory implements IRenderFactory<RiftCreaturePart> {
        @Override
        public Render<? super RiftCreaturePart> createRenderFor(RenderManager manager) {
            return new CreaturePartRenderer(manager);
        }
    }
}
