package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMillstone;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RiftMillstoneModel extends AnimatedGeoModel<TileEntityMillstone> {
    @Override
    public ResourceLocation getModelLocation(TileEntityMillstone tileEntityMillstone) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/millstone.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityMillstone tileEntityMillstone) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/millstone.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityMillstone tileEntityMillstone) {
        return null;
    }
}
