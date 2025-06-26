package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftHandCrankModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityHandCrank;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import anightdazingzoroark.riftlib.resource.RiftLibCache;

public class HandCrankRenderer extends GeoBlockRenderer<TileEntityHandCrank> {
    public HandCrankRenderer() {
        super(new RiftHandCrankModel());
    }

    @Override
    public void render(GeoModel model, TileEntityHandCrank animatable, float partialTicks, float red, float green, float blue, float alpha) {
        RiftLibCache.getInstance().parser.setValue("rotation", animatable.getRotation());
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    protected void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case NORTH:
                break;
            case EAST:
                GlStateManager.rotate(270, 0, 1, 0);
                break;
            case UP:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.translate(0, -0.5, -0.5);
                break;
            case DOWN:
                GlStateManager.rotate(90, -1, 0, 0);
                GlStateManager.translate(0, -0.5, 0.5);
                break;
        }
    }
}
