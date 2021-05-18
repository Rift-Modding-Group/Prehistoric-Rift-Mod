package com.anightdazingzoroark.rift.client.renderer;

import com.anightdazingzoroark.rift.client.models.TyrannosaurusModel;
import com.anightdazingzoroark.rift.entities.TyrannosaurusEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;

public class TyrannosaurusRenderer extends GeoEntityRenderer<TyrannosaurusEntity> {
    public TyrannosaurusRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new TyrannosaurusModel());
        this.shadowRadius = 0.7F;
    }
}
