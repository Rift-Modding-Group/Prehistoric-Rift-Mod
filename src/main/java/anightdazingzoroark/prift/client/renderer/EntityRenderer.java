package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.client.renderer.entity.*;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCannon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
import anightdazingzoroark.prift.server.entity.projectile.*;
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
        RenderingRegistry.registerEntityRenderingHandler(RiftSac.class, RiftSacRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftCannon.class, RiftLargeWeaponRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftMortar.class, RiftLargeWeaponRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(RiftCatapult.class, RiftCatapultRenderer::new);

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
        RenderingRegistry.registerEntityRenderingHandler(RiftCannonball.class, new IRenderFactory<RiftCannonball>() {
            @Override
            public Render<? super RiftCannonball> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftCannonball>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectiles.CANNONBALL.setTileEntityItemStackRenderer(new WeaponProjectileAnimatorRenderer(RiftLargeWeaponType.CANNON));
        RenderingRegistry.registerEntityRenderingHandler(RiftMortarShell.class, new IRenderFactory<RiftMortarShell>() {
            @Override
            public Render<? super RiftMortarShell> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftMortarShell>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectiles.MORTAR_SHELL.setTileEntityItemStackRenderer(new WeaponProjectileAnimatorRenderer(RiftLargeWeaponType.MORTAR));
        RenderingRegistry.registerEntityRenderingHandler(RiftCatapultBoulder.class, new IRenderFactory<RiftCatapultBoulder>() {
            @Override
            public Render<? super RiftCatapultBoulder> createRenderFor(RenderManager manager) {
                return new ProjectileRenderer<RiftCatapultBoulder>(manager, Minecraft.getMinecraft().getRenderItem(), null);
            }
        });
        RiftProjectiles.CATAPULT_BOULDER.setTileEntityItemStackRenderer(new WeaponProjectileAnimatorRenderer(RiftLargeWeaponType.CATAPULT));
    }
}
