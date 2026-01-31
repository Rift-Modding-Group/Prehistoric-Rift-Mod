package anightdazingzoroark.prift.client.newui.custom;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class EntityWidget<T extends EntityWidget<T>> extends Widget<T> {
    private final Entity entity;
    private final float entityScale;
    private float yawRotationAngle = 180f;

    public EntityWidget(Entity entity, float entityScale) {
        this.entity = entity;
        this.entityScale = entityScale;
    }

    public EntityWidget<T> yawRotationAngle(float value) {
        this.yawRotationAngle = value;
        return this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        //-----calculate positions based on area-----
        float posX = this.getArea().paddedWidth() / 2f;
        float posY = this.getArea().paddedHeight();

        //-----entity rendering-----
        //start
        GlStateManager.pushMatrix();

        //prevent leaking from other gl states
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        //render setup and transforms
        GlStateManager.enableColorMaterial();
        GlStateManager.enableDepth();
        GlStateManager.translate(posX, posY, 64);
        GlStateManager.scale(this.getCreatureScale(), this.getCreatureScale(), this.getCreatureScale());
        GlStateManager.rotate(180f, 1f, 0f, 0f);
        GlStateManager.rotate(this.yawRotationAngle, 0f, 1f, 0f);

        //fullbright lightmap
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        RenderHelper.enableStandardItemLighting();

        //render entity
        Minecraft.getMinecraft().getRenderManager().setPlayerViewY(180f);
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(false);
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().getRenderManager().renderEntity(this.entity, 0D, 0D, 0D, 0f, 0f, false);
        Minecraft.getMinecraft().getRenderManager().setRenderShadow(true);
        RenderHelper.disableStandardItemLighting();

        //exit
        GlStateManager.disableDepth();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    private float getCreatureScale() {
        if (!(this.entity instanceof RiftCreature)) return this.entityScale;
        return ((RiftCreature) this.entity).scale() * this.entityScale;
    }
}
