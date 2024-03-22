package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ApatosaurusRenderer extends GeoEntityRenderer<RiftCreature> {
    public ApatosaurusRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1.75f;
    }

    @Override
    public boolean shouldRender(RiftCreature livingEntity, ICamera camera, double camX, double camY, double camZ) {
        Apatosaurus apatosaurus = (Apatosaurus) livingEntity;
        return super.shouldRender(apatosaurus, camera, camX, camY, camZ) || apatosaurus.shouldRender(camera) || Minecraft.getMinecraft().player.isRidingOrBeingRiddenBy(apatosaurus);
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.35f, 2.25f);
        Apatosaurus apatosaurus = (Apatosaurus) animatable;

        //hide saddle stuff
        model.getBone("platform").get().setHidden(!animatable.isSaddled());
        model.getBone("neck0Saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("neck1Saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("neck2Saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("furnace").get().setHidden(true);
        model.getBone("craftingTable").get().setHidden(true);
        model.getBone("leftCannon").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CANNON));
        model.getBone("rightCannon").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CANNON));
        model.getBone("leftMortar").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.MORTAR));
        model.getBone("rightMortar").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.MORTAR));
        model.getBone("leftCatapult").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT));
        model.getBone("rightCatapult").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT));
        model.getBone("leftCatapultBoulder").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT) || !apatosaurus.isLoaded());
        model.getBone("rightCatapultBoulder").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT) || !apatosaurus.isLoaded());


        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
