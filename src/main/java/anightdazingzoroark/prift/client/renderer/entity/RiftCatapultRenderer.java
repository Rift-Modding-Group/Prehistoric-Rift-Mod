package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class RiftCatapultRenderer extends RiftLargeWeaponRenderer {
    public RiftCatapultRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(GeoModel model, RiftLargeWeapon animatable, float partialTicks, float red, float green, float blue, float alpha) {
        RiftCatapult catapult = (RiftCatapult) animatable;
        model.getBone("catapultBoulder").get().setHidden(!catapult.isLoaded());
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
