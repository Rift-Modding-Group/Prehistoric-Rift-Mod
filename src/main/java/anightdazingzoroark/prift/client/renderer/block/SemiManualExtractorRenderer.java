package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.block.RiftSemiManualExtractorModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractor;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractorTop;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SemiManualExtractorRenderer extends GeoBlockRenderer<TileEntitySemiManualExtractor> {
    public SemiManualExtractorRenderer() {
        super(new RiftSemiManualExtractorModel());
    }

    @Override
    public void render(GeoModel model, TileEntitySemiManualExtractor animatable, float partialTicks, float red, float green, float blue, float alpha) {
        TileEntitySemiManualExtractorTop topTE = (TileEntitySemiManualExtractorTop)animatable.getTopTEntity();
        float recipeTRatio = (float)topTE.getTimeHeld()/(float)topTE.getMaxRecipeTime();
        if (recipeTRatio > 0) {
            model.getBone("arm").get().setPositionY(-155f * recipeTRatio);
            model.getBone("arm").get().setScaleY(5f * recipeTRatio + 1f);
            model.getBone("squeezer").get().setPositionY(-14f * recipeTRatio);
        }
        if (topTE.getMustBeReset()) {
            model.getBone("arm").get().setPositionY(-155f);
            model.getBone("arm").get().setScaleY(6f);
            model.getBone("squeezer").get().setPositionY(-14f);
        }
        if (animatable.canDoResetAnim()) {
            model.getBone("arm").get().setPositionY(RiftUtil.clamp(31f * animatable.getResetAnimTime() - 310f, -155f, 0));
            model.getBone("arm").get().setScaleY(RiftUtil.clamp(-animatable.getResetAnimTime() + 11f, 1f, 6f));
            model.getBone("squeezer").get().setPositionY(RiftUtil.clamp(2.8f * animatable.getResetAnimTime() - 28f, -14f, 0));
        }
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    private void renderLiquid() {}

    private void renderItem() {}
}