package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.client.renderer.entity.ThrownStegoPlateAnimatorRenderer;
import anightdazingzoroark.prift.client.renderer.entity.RiftEggRenderer;
import anightdazingzoroark.prift.client.renderer.entity.ProjectileRenderer;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectiles;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenderer {
    public static void registerRenderers() {
        //creatures
        for (RiftCreatureType creature : RiftCreatureType.values()) RenderingRegistry.registerEntityRenderingHandler(creature.getCreature(), creature.getRenderFactory());

        //everythin else
        RenderingRegistry.registerEntityRenderingHandler(RiftEgg.class, RiftEggRenderer::new);

        //projectiles
        RenderingRegistry.registerEntityRenderingHandler(ThrownStegoPlate.class, new IRenderFactory<ThrownStegoPlate>() {
            @Override
            public Render<? super ThrownStegoPlate> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<ThrownStegoPlate>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectiles.THROWN_STEGOSAURUS_PLATE_ONE.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(0));
        RiftProjectiles.THROWN_STEGOSAURUS_PLATE_TWO.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(1));
        RiftProjectiles.THROWN_STEGOSAURUS_PLATE_THREE.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(2));
        RiftProjectiles.THROWN_STEGOSAURUS_PLATE_FOUR.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(3));
    }
}
