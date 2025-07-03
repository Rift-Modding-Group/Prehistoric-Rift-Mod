package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class RiftMortarShellModel extends AnimatedGeoModel<RiftMortarShell> {
    @Override
    public ResourceLocation getModelLocation(RiftMortarShell riftMortarShell) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/mortar_shell.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftMortarShell riftMortarShell) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/mortar_shell.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftMortarShell riftMortarShell) {
        return null;
    }
}
