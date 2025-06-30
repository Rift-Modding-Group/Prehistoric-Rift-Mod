package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.Ankylosaurus;
import anightdazingzoroark.prift.server.entity.creature.Anomalocaris;
import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;
import anightdazingzoroark.riftlib.resource.RiftLibCache;

public class RiftCreatureRenderer extends GeoEntityRenderer<RiftCreature> {
    public RiftCreatureRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1f;
    }

    /*
    @Override
    public boolean shouldRender(RiftCreature livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ) || livingEntity.shouldRender(camera) || Minecraft.getMinecraft().player.isRidingOrBeingRiddenBy(livingEntity);
    }
     */

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //other conditions where the creature must not render
        if (animatable.isBurrowing()) return;

        //variables
        RiftLibCache.getInstance().parser.setValue("creature_move_use", Math.min(animatable.getCurrentMoveUse(), 100));

        //translucensy for cloaking
        float translucency = animatable.isCloaked() ? 0.2f : alpha;

        //hide saddle stuff
        if (model.getBone("saddle").isPresent()) model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        if (model.getBone("headSaddle").isPresent()) model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        if (model.getBone("chest").isPresent()) model.getBone("chest").get().setHidden(true);
        if (model.getBone("hiddenBySaddle").isPresent()) model.getBone("hiddenBySaddle").get().setHidden(animatable.isSaddled());

        if (animatable instanceof Apatosaurus) {
            Apatosaurus apatosaurus = (Apatosaurus) animatable;
            model.getBone("platform").get().setHidden(!animatable.isSaddled());
            model.getBone("neck0Saddle").get().setHidden(!animatable.isSaddled());
            model.getBone("neck1Saddle").get().setHidden(!animatable.isSaddled());
            model.getBone("neck2Saddle").get().setHidden(!animatable.isSaddled());
            model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
            model.getBone("furnace").get().setHidden(true);
            model.getBone("craftingTable").get().setHidden(true);
            model.getBone("leftCannon").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.CANNON));
            model.getBone("rightCannon").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.CANNON));
            model.getBone("leftMortar").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.MORTAR));
            model.getBone("rightMortar").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.MORTAR));
            model.getBone("leftCatapult").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.CATAPULT));
            model.getBone("rightCatapult").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.CATAPULT));
            model.getBone("leftCatapultBoulder").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.CATAPULT) || !apatosaurus.isLoaded());
            model.getBone("rightCatapultBoulder").get().setHidden(!apatosaurus.getLargeWeapon().equals(RiftLargeWeaponType.CATAPULT) || !apatosaurus.isLoaded());
        }
        if (animatable instanceof Anomalocaris) {
            model.getBone("leftAppendageSaddle").get().setHidden(!animatable.isSaddled());
            model.getBone("rightAppendageSaddle").get().setHidden(!animatable.isSaddled());
        }
        if (animatable instanceof Ankylosaurus) {
            model.getBone("spikeSaddle").get().setHidden(!animatable.isSaddled());
            model.getBone("spike36").get().setHidden(animatable.isSaddled());
            model.getBone("spike37").get().setHidden(animatable.isSaddled());
            model.getBone("spike38").get().setHidden(animatable.isSaddled());
            model.getBone("spike39").get().setHidden(animatable.isSaddled());
            model.getBone("spike42").get().setHidden(animatable.isSaddled());
            model.getBone("spike43").get().setHidden(animatable.isSaddled());
        }

        //other parts to hide
        if (model.getBone("antlers").isPresent()) model.getBone("antlers").get().setHidden(animatable.isBaby());

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, translucency);
        super.render(model, animatable, partialTicks, red, green, blue, translucency);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
