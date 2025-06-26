package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityHandCrank;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftHandCrankModel extends AnimatedGeoModel<TileEntityHandCrank> {
    @Override
    public ResourceLocation getModelLocation(TileEntityHandCrank tileEntityHandCrank) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/hand_crank.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityHandCrank tileEntityHandCrank) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/hand_crank.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityHandCrank tileEntityHandCrank) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/hand_crank.animation.json");
    }
}
