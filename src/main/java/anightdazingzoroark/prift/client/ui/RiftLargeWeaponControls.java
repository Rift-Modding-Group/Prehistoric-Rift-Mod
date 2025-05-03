package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RiftLargeWeaponControls {
    private static final ResourceLocation leftMouseIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/left_mouse_icon.png");
    private final float iconScale = 0.75f;
    private final float textScale = 0.5f;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();
        ScaledResolution resolution = event.getResolution();

        if (entity instanceof RiftLargeWeapon) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                RiftLargeWeapon largeWeapon = (RiftLargeWeapon) entity;
                this.showLeftMouseControls(largeWeapon, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
            }
        }
    }

    private void showLeftMouseControls(RiftLargeWeapon largeWeapon, int width, int height, int yOffset) {
        //show left mouse button icon
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        Minecraft.getMinecraft().getTextureManager().bindTexture(leftMouseIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        RiftLargeWeaponType weaponType = largeWeapon.weaponType;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("large_weapon_control.left_mouse."+weaponType.toString().toLowerCase());
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }
}
