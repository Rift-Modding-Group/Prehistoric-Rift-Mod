package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftPetalStorm;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityNonLivingRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class RiftPetalStormRenderer extends GeoEntityNonLivingRenderer<RiftPetalStorm> {
    public RiftPetalStormRenderer(@NotNull RenderManager renderManager) {
        super(renderManager, new AnimatedGeoModel<RiftPetalStorm>() {
            @Override
            @NotNull
            public String getModId() {
                return RiftInitialize.MODID;
            }

            @Override
            @NotNull
            public String getModelIdentifier(@NotNull RiftPetalStorm entity) {
                return "geometry.petal_storm";
            }

            @Override
            @NotNull
            public String getTextureLocation(@NotNull RiftPetalStorm entity) {
                return "icons/empty_icon.png";
            }
        });
    }

    @Override
    public void repositionAnimatedLocators(@NotNull RiftPetalStorm entity) {
        // The stationary storm's base locator must stay at its ground position.
        GeoEntityRenderHelper.repositionAnimatedLocators(entity, 1f);
    }
}
