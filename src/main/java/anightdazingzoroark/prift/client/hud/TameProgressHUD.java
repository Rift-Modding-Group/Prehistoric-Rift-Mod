package anightdazingzoroark.prift.client.hud;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.hitbox.RiftLibCollisionHitbox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class TameProgressHUD {
    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int BAR_WIDTH = 66;
    private static final int BAR_HEIGHT = 10;
    private static final int EMPTY_BAR_TEXTURE_Y = 65;
    private static final int FILLED_BAR_TEXTURE_Y = 75;

    public static void renderTamingProgress(Minecraft minecraft, int scaledWidth, int scaledHeight) {
        RiftCreature creature = getCreature(minecraft.objectMouseOver.entityHit);
        if (creature == null || creature.isTamed() || creature.getCreatureType().getDomestication() == null) return;

        float progress = creature.getTamingProgress();
        if (progress <= 0f) return;

        int left = scaledWidth / 2 - BAR_WIDTH / 2;
        int top = scaledHeight / 2 + 60;
        int filledWidth = Math.round(BAR_WIDTH * Math.clamp(progress, 0f, 1f));

        minecraft.getTextureManager().bindTexture(HUD_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0, EMPTY_BAR_TEXTURE_Y, BAR_WIDTH, BAR_HEIGHT, 256, 256);
        if (filledWidth > 0) {
            Gui.drawModalRectWithCustomSizedTexture(left, top, 0, FILLED_BAR_TEXTURE_Y, filledWidth, BAR_HEIGHT, 256, 256);
        }
        GlStateManager.disableBlend();

        FontRenderer fontRenderer = minecraft.fontRenderer;
        String progressText = Math.round(progress * 100f) + "%";
        fontRenderer.drawStringWithShadow(
                progressText,
                scaledWidth / 2f - fontRenderer.getStringWidth(progressText) / 2f,
                top - fontRenderer.FONT_HEIGHT - 2,
                0xFFFFFF
        );
    }

    @Nullable
    private static RiftCreature getCreature(@Nullable Entity hitEntity) {
        if (hitEntity instanceof RiftCreature creature) return creature;
        else if (hitEntity instanceof RiftLibCollisionHitbox<?> hitbox && hitbox.parent instanceof RiftCreature creature) return creature;
        else return null;
    }
}
