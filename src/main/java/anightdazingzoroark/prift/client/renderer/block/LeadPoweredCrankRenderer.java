package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftLeadPoweredCrankModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import net.minecraft.util.EnumFacing;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class LeadPoweredCrankRenderer extends GeoBlockRenderer<TileEntityLeadPoweredCrank> {
    public LeadPoweredCrankRenderer() {
        super(new RiftLeadPoweredCrankModel());
    }

    @Override
    public void render(GeoModel model, TileEntityLeadPoweredCrank animatable, float partialTicks, float red, float green, float blue, float alpha) {
        model.getBone("lead").get().setHidden(!animatable.getHasLead());

        GeckoLibCache.getInstance().parser.setValue("rotation", animatable.getRotation());

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    @Override
    protected void rotateBlock(EnumFacing facing) {}
}
