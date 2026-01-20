package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftFeedingTroughModel;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

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
