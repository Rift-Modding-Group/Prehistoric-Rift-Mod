package anightdazingzoroark.prift.client.newui.custom;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;

public class EntityWidget<T extends EntityWidget<T>> extends Widget<T> {
    private final Entity entity;
    private float yawRotationAngle;

    public EntityWidget(Entity entity) {
        this.entity = entity;
    }

    public EntityWidget<T> yawRotationAngle(float value) {
        this.yawRotationAngle = value;
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        //calculate positions based on entity size and area
        float posX = (this.getArea().paddedWidth()) / 2f;
        float posY = (this.getArea().paddedHeight() + this.getArea().paddedHeight() * this.entity.height) / 2f;

        //entity rendering
        GlStateManager.pushMatrix();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableDepth();
        GlStateManager.translate(posX, posY, 0);
        GlStateManager.scale(this.getArea().paddedWidth(), this.getArea().paddedHeight(), this.getArea().paddedWidth());
        GlStateManager.rotate(180f, 1f, 0f, 0f);
        GlStateManager.rotate(this.yawRotationAngle, 0f, 1f, 0f);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderManager().setPlayerViewY(180f);
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getRenderManager().renderEntity(this.entity, 0D, 0D, 0D, 0f, 0f, false);
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(true);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
        GlStateManager.color(1f, 1f, 1f, 1f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
