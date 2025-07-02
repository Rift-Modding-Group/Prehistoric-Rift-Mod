package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.client.renderer.entity.*;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.creature.*;
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
        RenderingRegistry.registerEntityRenderingHandler(ThrownStegoPlate.class, new IRenderFactory<ThrownStegoPlate>() {
            @Override
            public Render<? super ThrownStegoPlate> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<ThrownStegoPlate>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_ONE.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(0));
        RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_TWO.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(1));
        RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_THREE.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(2));
        RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_FOUR.setTileEntityItemStackRenderer(new ThrownStegoPlateAnimatorRenderer(3));

        RenderingRegistry.registerEntityRenderingHandler(RiftCannonball.class, new IRenderFactory<RiftCannonball>() {
            @Override
            public Render<? super RiftCannonball> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftCannonball>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.CANNONBALL.setTileEntityItemStackRenderer(new WeaponProjectileAnimatorRenderer(RiftLargeWeaponType.CANNON));

        RenderingRegistry.registerEntityRenderingHandler(RiftMortarShell.class, new IRenderFactory<RiftMortarShell>() {
            @Override
            public Render<? super RiftMortarShell> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftMortarShell>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.MORTAR_SHELL.setTileEntityItemStackRenderer(new WeaponProjectileAnimatorRenderer(RiftLargeWeaponType.MORTAR));

        RenderingRegistry.registerEntityRenderingHandler(RiftCatapultBoulder.class, new IRenderFactory<RiftCatapultBoulder>() {
            @Override
            public Render<? super RiftCatapultBoulder> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftCatapultBoulder>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.CATAPULT_BOULDER.setTileEntityItemStackRenderer(new WeaponProjectileAnimatorRenderer(RiftLargeWeaponType.CATAPULT));

        RenderingRegistry.registerEntityRenderingHandler(ThrownBola.class, new IRenderFactory<ThrownBola>() {
            @Override
            public Render<? super ThrownBola> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<ThrownBola>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.THROWN_BOLA.setTileEntityItemStackRenderer(new ThrownBolaAnimatorRenderer());

        RenderingRegistry.registerEntityRenderingHandler(DilophosaurusSpit.class, new IRenderFactory<DilophosaurusSpit>() {
            @Override
            public Render<? super DilophosaurusSpit> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<DilophosaurusSpit>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.DILOPHOSAURUS_SPIT.setTileEntityItemStackRenderer(new DilophosaurusSpitRenderer());

        RenderingRegistry.registerEntityRenderingHandler(VenomBomb.class, new IRenderFactory<VenomBomb>() {
            @Override
            public Render<? super VenomBomb> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<VenomBomb>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.VENOM_BOMB.setTileEntityItemStackRenderer(new VenomBombRenderer());

        RenderingRegistry.registerEntityRenderingHandler(Mudball.class, new IRenderFactory<Mudball>() {
            @Override
            public Render<? super Mudball> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<Mudball>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectileAnimatorRegistry.MUDBALL.setTileEntityItemStackRenderer(new MudballRenderer());


        RenderingRegistry.registerEntityRenderingHandler(RiftTrap.class, new IRenderFactory<RiftTrap>() {
            @Override
            public Render<? super RiftTrap> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftTrap>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
    }
}
