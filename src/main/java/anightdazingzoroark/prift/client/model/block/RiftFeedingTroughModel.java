package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RiftFeedingTroughModel extends AnimatedGeoModel<RiftTileEntityFeedingTrough> {
    @Override
    public ResourceLocation getModelLocation(RiftTileEntityFeedingTrough riftTileEntityFeedingTrough) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/feeding_trough.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftTileEntityFeedingTrough riftTileEntityFeedingTrough) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/feeding_trough.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftTileEntityFeedingTrough riftTileEntityFeedingTrough) {
        return null;
    }
}
