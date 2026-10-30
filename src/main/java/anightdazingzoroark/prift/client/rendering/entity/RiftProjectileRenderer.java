package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import org.jetbrains.annotations.NotNull;

public class RiftProjectileRenderer extends GeoProjectileRenderer<RiftProjectile> {
    public RiftProjectileRenderer(RenderManager renderManager) {
        super(renderManager, new AnimatedGeoModel<RiftProjectile>() {
            @Override
            @NotNull
            public String getModId() {
                return RiftInitialize.MODID;
            }

            @Override
            @NotNull
            public String getModelIdentifier(RiftProjectile riftProjectile) {
                return switch (riftProjectile.getProjectileModel()) {
                    case CUBE -> "geometry.generic_cube_projectile";
                    case FLAT -> "geometry.generic_flat_projectile";
                    case NONE -> "geometry.generic_no_model_projectile";
                    case CUSTOM -> "geometry."+riftProjectile.getName();
                };
            }

            @Override
            @NotNull
            public String getTextureLocation(RiftProjectile riftProjectile) {
                if (riftProjectile.getProjectileModel() == ProjectileBuilder.ProjectileModel.NONE) {
                    return "icons/empty_icon.png";
                }
                return "entities/projectiles/"+riftProjectile.getName()+".png";
            }
        });
    }
}
