package anightdazingzoroark.prift.client.model;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlateAnimator;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ThrownStegoPlateModel extends AnimatedGeoModel<ThrownStegoPlateAnimator> {
    private int variant;

    public ThrownStegoPlateModel(int variant) {
        this.variant = variant;
    }

    @Override
    public ResourceLocation getModelLocation(ThrownStegoPlateAnimator object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/thrown_stegosaurus_plate.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownStegoPlateAnimator object) {
        if (this.variant >= 0 && this.variant <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/stegosaurus/thrown_stegosaurus_plate_"+(this.variant + 1)+".png");
        }
        else {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/stegosaurus/thrown_stegosaurus_plate_1.png");
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ThrownStegoPlateAnimator animatable) {
        return null;
    }
}
