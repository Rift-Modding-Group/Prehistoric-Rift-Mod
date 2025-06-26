package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftBlowPoweredTurbineModel extends AnimatedGeoModel<TileEntityBlowPoweredTurbine> {
    @Override
    public ResourceLocation getModelLocation(TileEntityBlowPoweredTurbine tileEntityBlowPoweredTurbine) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/blow_powered_turbine.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityBlowPoweredTurbine tileEntityBlowPoweredTurbine) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/blow_powered_turbine.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityBlowPoweredTurbine tileEntityBlowPoweredTurbine) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/blow_powered_turbine.animation.json");
    }
}
