package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftFeedingTroughModel;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class FeedingTroughRenderer extends GeoBlockRenderer<RiftTileEntityFeedingTrough> {
    public FeedingTroughRenderer() {
        super(new RiftFeedingTroughModel());
    }

    @Override
    public void render(GeoModel model, RiftTileEntityFeedingTrough animatable, float partialTicks, float red, float green, float blue, float alpha) {
        model.getBone("contents").get().setHidden(animatable.inventoryIsEmpty());
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
