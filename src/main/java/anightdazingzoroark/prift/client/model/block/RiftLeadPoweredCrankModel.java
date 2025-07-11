package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftLeadPoweredCrankModel extends AnimatedGeoModel<TileEntityLeadPoweredCrank> {
    @Override
    public ResourceLocation getModelLocation(TileEntityLeadPoweredCrank tileEntityLeadPoweredCrank) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/lead_powered_crank.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityLeadPoweredCrank tileEntityLeadPoweredCrank) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/lead_powered_crank.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityLeadPoweredCrank tileEntityLeadPoweredCrank) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/lead_powered_crank.animation.json");
    }
}
