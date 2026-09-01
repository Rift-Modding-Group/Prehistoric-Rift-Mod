package anightdazingzoroark.prift.client.model;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProjectileModel extends AnimatedGeoModel<RiftProjectile> {
    @Override
    @NotNull
    public String getModId() {
        return RiftInitialize.MODID;
    }

    @Override
    public String getModelIdentifier(RiftProjectile riftProjectile) {
        return "geometry."+riftProjectile.getName();
    }

    @Override
    public String getTextureLocation(RiftProjectile riftProjectile) {
        return "entities/projectiles/"+riftProjectile.getName()+".png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(RiftProjectile riftProjectile) {
        return List.of();
    }
}
