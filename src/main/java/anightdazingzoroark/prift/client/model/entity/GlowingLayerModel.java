package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.client.renderer.layer.AutoGlowingTexture;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

import java.util.function.Function;

//a good chunk of this code and everything else related to it is based on code from Chocolate Quest Repoured
//https://github.com/TeamChocoQuest/ChocolateQuestRepoured/blob/1.12.2/src/main/java/team/cqr/cqrepoured/client/render/entity/RenderCQREntityGeo.java
public class GlowingLayerModel<T extends EntityLiving & IAnimatable> extends GeoLayerRenderer<T> {
    protected final Function<T, ResourceLocation> funcGetCurrentTexture;
    protected final Function<T, ResourceLocation> funcGetCurrentModel;
    protected GeoEntityRenderer<T> geoRendererInstance;

    public GlowingLayerModel(GeoEntityRenderer<T> renderer, Function<T, ResourceLocation> funcGetCurrentTexture, Function<T, ResourceLocation> funcGetCurrentModel) {
        super(renderer);
        this.geoRendererInstance = renderer;
        this.funcGetCurrentTexture = funcGetCurrentTexture;
        this.funcGetCurrentModel = funcGetCurrentModel;
    }

    @Override
    public void render(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, Color renderColor) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        this.geoRendererInstance.bindTexture(AutoGlowingTexture.get(this.funcGetCurrentTexture.apply(entitylivingbaseIn)));

        this.reRenderCurrentModelInRenderer(entitylivingbaseIn, partialTicks, renderColor);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }

    @Override
    public void doRenderLayer(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    protected void reRenderCurrentModelInRenderer(T entity, float partialTicks, Color renderColor) {
        this.entityRenderer.render(
                this.getEntityModel().getModel(this.funcGetCurrentModel.apply(entity)),
                entity,
                partialTicks,
                (float) renderColor.getRed() / 255f,
                (float) renderColor.getBlue() / 255f,
                (float) renderColor.getGreen() / 255f,
                (float) renderColor.getAlpha() / 255
        );
    }
}