package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftControls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class RiftControlButtonDisplay {
    private static final ResourceLocation controlsUI = new ResourceLocation(RiftInitialize.MODID, "textures/ui/controls.png");

    public static void draw(int keyBinding, int xScreenSize, int yScreenSize, int xOffset, int yOffset) {
        draw(keyBinding, xScreenSize, yScreenSize, xOffset, yOffset, 1f);
    }

    public static void draw(int keyBinding, int xScreenSize, int yScreenSize, int xOffset, int yOffset, float alpha) {
        final float iconScale = 0.75f;
        float unscaledXPos = (xScreenSize - (iconScale * getSizeForControl(keyBinding)[0])) / 2 + xOffset;
        float unscaledYPos = (yScreenSize - (iconScale * getSizeForControl(keyBinding)[1])) / 2 + yOffset;

        //texture first
        Minecraft.getMinecraft().getTextureManager().bindTexture(controlsUI);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        GlStateManager.pushMatrix();
        GlStateManager.scale(iconScale, iconScale, iconScale);
        Gui.drawModalRectWithCustomSizedTexture(
                (int) (unscaledXPos / iconScale),
                (int) (unscaledYPos / iconScale),
                getUVsForControl(keyBinding)[0],
                getUVsForControl(keyBinding)[1],
                getSizeForControl(keyBinding)[0],
                getSizeForControl(keyBinding)[1],
                64,
                48
        );
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();

        //put on text, only if eligible
        if (eligibleForText(keyBinding)) {
            final float textScale = 0.5f;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            String textOnKey = Keyboard.getKeyName(keyBinding);
            int textWidth = fontRenderer.getStringWidth(textOnKey);
            float unscaledTextXPos = (xScreenSize - (textWidth * textScale)) / 2 + xOffset;
            float unscaledTextYPos = (yScreenSize - (fontRenderer.FONT_HEIGHT * textScale)) / 2 + yOffset;

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            fontRenderer.drawString(
                    textOnKey,
                    (int) (unscaledTextXPos / textScale),
                    (int) (unscaledTextYPos / textScale),
                    0x000000
            );
            GlStateManager.popMatrix();
        }
    }

    private static int[] getUVsForControl(int keyBinding) {
        switch (keyBinding) {
            case RiftControls.LEFT_MOUSE:
                return new int[]{16, 0};
            case RiftControls.RIGHT_MOUSE:
                return new int[]{32, 0};
            case RiftControls.MIDDLE_MOUSE:
                return new int[]{48, 0};
            case Keyboard.KEY_SPACE:
                return new int[]{0, 16};
            case Keyboard.KEY_UP:
                return new int[]{0, 32};
            case Keyboard.KEY_RIGHT:
                return new int[]{16, 32};
            case Keyboard.KEY_DOWN:
                return new int[]{32, 32};
            case Keyboard.KEY_LEFT:
                return new int[]{48, 32};
        }
        return new int[]{0, 0};
    }

    private static int[] getSizeForControl(int keyBinding) {
        if (keyBinding == Keyboard.KEY_SPACE) return new int[]{24, 16};
        return new int[]{16, 16};
    }

    private static boolean eligibleForText(int keyBinding) {
        return getUVsForControl(keyBinding)[0] == 0 && getUVsForControl(keyBinding)[1] == 0;
    }
}
