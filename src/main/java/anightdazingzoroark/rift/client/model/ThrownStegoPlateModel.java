package anightdazingzoroark.rift.client.model;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ThrownStegoPlateModel extends AnimatedGeoModel<ThrownStegoPlate> {
    @Override
    public ResourceLocation getModelLocation(ThrownStegoPlate object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/thrown_stegosaurus_plate.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownStegoPlate object) {
        if (object.getVariant() >= 0 && object.getVariant() <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/stegosaurus/thrown_stegosaurus_plate_"+(object.getVariant()+1)+".png");
        }
        else {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/stegosaurus/thrown_stegosaurus_plate_1.png");
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ThrownStegoPlate animatable) {
        return null;
    }
}
