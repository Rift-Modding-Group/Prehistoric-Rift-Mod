package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.client.renderer.entity.*;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCannon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
import anightdazingzoroark.prift.server.entity.other.RiftEmbryo;
import anightdazingzoroark.prift.server.entity.other.RiftTrap;
import anightdazingzoroark.prift.server.entity.projectile.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRenderer {
    public static void registerRenderers() {
        //creatures
        for (RiftCreatureType creatureType : RiftCreatureType.values()) {
            if (creatureType == RiftCreatureType.SAUROPHAGANAX)
                RenderingRegistry.registerEntityRenderingHandler(creatureType.getCreature(), RiftCreatureGlowingRenderer::new);
            else
                RenderingRegistry.registerEntityRenderingHandler(creatureType.getCreature(), RiftCreatureRenderer::new);
        }

        //everythin else
        RenderingRegistry.registerEntityRenderingHandler(RiftEgg.class, RiftEggRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftSac.class, RiftSacRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftCannon.class, RiftLargeWeaponRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftMortar.class, RiftLargeWeaponRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftCatapult.class, RiftCatapultRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftEmbryo.class, RiftEmbryoRenderer::new);

        //projectiles
        RenderingRegistry.registerEntityRenderingHandler(ThrownStegoPlate.class, ThrownStegoPlateRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftCannonball.class, RiftCannonballRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftMortarShell.class, RiftMortarShellRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftCatapultBoulder.class, RiftCatapultBoulderRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ThrownBola.class, ThrownBolaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(DilophosaurusSpit.class, DilophosaurusSpitRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(VenomBomb.class, new IRenderFactory<VenomBomb>() {
            @Override
            public Render<? super VenomBomb> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<VenomBomb>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.VENOM_BOMB.setTileEntityItemStackRenderer(new VenomBombRenderer());

        RenderingRegistry.registerEntityRenderingHandler(Mudball.class, MudballRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(RiftTrap.class, new IRenderFactory<RiftTrap>() {
            @Override
            public Render<? super RiftTrap> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftTrap>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
    }
}
