package anightdazingzoroark.prift.client.camera;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder.RiddenCrosshairType;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * Draws the configured ridden crosshair using the shared aiming dimensions.
 * */
@SideOnly(Side.CLIENT)
public class RidingAimCrosshair {
    @NotNull
    private static final ResourceLocation JAWS_CROSSHAIR = new ResourceLocation("prift", "textures/ui/jaws_crosshair.png");
    @NotNull
    private static final ResourceLocation LEAP_CROSSHAIR = new ResourceLocation("prift", "textures/ui/leap_crosshair.png");

    public static void renderLeapCrosshair(@NotNull Minecraft minecraft, boolean validTarget, boolean ready, int screenWidth, int screenHeight) {
        if (!ready) GlStateManager.color(1f, 1f, 1f, 0.22f);
        else if (validTarget) GlStateManager.color(0.4f, 1f, 0.4f, 0.65f);
        else GlStateManager.color(1f, 0.4f, 0.4f, 0.65f);
        minecraft.getTextureManager().bindTexture(LEAP_CROSSHAIR);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        Gui.drawModalRectWithCustomSizedTexture(screenWidth / 2 - 12, screenHeight / 2 - 12,
                0f, 0f, 24, 24, 24, 24);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public static void renderWideCrosshair(@NotNull Minecraft minecraft, @NotNull RiddenCrosshairType crosshairType, int screenWidth, int screenHeight) {
        int crosshairLeft = screenWidth / 2 - 7;
        int crosshairTop = screenHeight / 2 - 7;
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        if (crosshairType == RiddenCrosshairType.WIDE) {
            // Fit the hit ellipse between this PNG's tooth tips, keeping the artwork at its 2:1 aspect ratio.
            int textureWidth = (int) Math.round(screenHeight * RiddenAimArea.HORIZONTAL_SCREEN_RADIUS / 0.30D);
            int textureHeight = (int) Math.round(screenHeight * RiddenAimArea.VERTICAL_SCREEN_RADIUS / 0.20D);
            minecraft.getTextureManager().bindTexture(JAWS_CROSSHAIR);
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            Gui.drawModalRectWithCustomSizedTexture(
                    (screenWidth - textureWidth) / 2, (screenHeight - textureHeight) / 2,
                    0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight
            );
        }
        else {
            minecraft.getTextureManager().bindTexture(Gui.ICONS);
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            Gui.drawModalRectWithCustomSizedTexture(crosshairLeft, crosshairTop, 0, 0, 16, 16, 256, 256);
        }
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
    }
}