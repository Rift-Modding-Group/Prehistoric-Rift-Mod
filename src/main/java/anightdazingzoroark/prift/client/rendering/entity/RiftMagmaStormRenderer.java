package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftMagmaStorm;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityNonLivingRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class RiftMagmaStormRenderer extends GeoEntityNonLivingRenderer<RiftMagmaStorm> {
    public RiftMagmaStormRenderer(@NotNull RenderManager renderManager) {
        super(renderManager, new AnimatedGeoModel<RiftMagmaStorm>() {
            @Override
            @NotNull
            public String getModId() {
                return RiftInitialize.MODID;
            }

            @Override
            @NotNull
            public String getModelIdentifier(RiftMagmaStorm entity) {
                return "geometry.magma_storm";
            }

            @Override
            @NotNull
            public String getTextureLocation(RiftMagmaStorm entity) {
                return "icons/empty_icon.png";
            }
        });
    }
}
