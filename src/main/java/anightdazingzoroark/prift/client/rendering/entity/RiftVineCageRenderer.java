package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftVineCage;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityNonLivingRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class RiftVineCageRenderer extends GeoEntityNonLivingRenderer<RiftVineCage> {
    public RiftVineCageRenderer(@NotNull RenderManager renderManager) {
        super(renderManager, new AnimatedGeoModel<RiftVineCage>() {
            @Override
            @NotNull
            public String getModId() {
                return RiftInitialize.MODID;
            }

            @Override
            @NotNull
            public String getModelIdentifier(RiftVineCage entity) {
                return "geometry.vine_cage";
            }

            @Override
            @NotNull
            public String getTextureLocation(RiftVineCage entity) {
                return "entities/move_graphics/vine_cage.png";
            }
        });
    }
}
