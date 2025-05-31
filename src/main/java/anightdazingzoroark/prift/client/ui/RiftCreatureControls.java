package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.items.RiftItems;
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
public class RiftCreatureControls {
    private static final ResourceLocation leftMouseIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/left_mouse_icon.png");
    private static final ResourceLocation rightMouseIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/right_mouse_icon.png");
    private static final ResourceLocation middleMouseIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/middle_mouse_icon.png");
    private static final ResourceLocation spacebarIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/spacebar_icon.png");
    private static final ResourceLocation genericButtonIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/generic_button_icon.png");
    private final float iconScale = 0.75f;
    private final float textScale = 0.5f;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();
        ScaledResolution resolution = event.getResolution();

        if (entity instanceof RiftCreature) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                RiftCreature creature = (RiftCreature) entity;
                if (creature.creatureType.canHoldLargeWeapon
                        && creature.getLargeWeapon() != RiftLargeWeaponType.NONE
                        && player.getHeldItemMainhand().getItem() == RiftItems.COMMAND_CONSOLE) {
                    this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 20, true);
                }
                else {
                    for (int x = 0; x < creature.getLearnedMoves().size(); x++) {
                        if (x == 0) this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0, false);
                        if (x == 1) this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 20);
                        if (x == 2) this.showMiddleMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 40);
                    }
                }

                //for changing y pos thru spacebar
                if (creature instanceof RiftWaterCreature && creature.isInWater()) {
                    this.showSpacebarControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 20);
                }

                //for toggling between attack and block break
                this.showToggleAttackOrBreakControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 40);
            }
        }
    }

    private void showLeftMouseControls(RiftCreature creature, int width, int height, int yOffset, boolean forLargeWeapons) {
        //show left mouse button icon
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        float alpha = ((creature.getMoveOneCooldown() > 0) ||
                (creature.getLearnedMoves().get(0).chargeType == CreatureMove.ChargeType.BUILDUP && creature.getMoveThreeUse() < creature.getLearnedMoves().get(0).maxUse))
                ? 0.2f : 1f;

        Minecraft.getMinecraft().getTextureManager().bindTexture(leftMouseIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = forLargeWeapons ? I18n.format("large_weapon_control."+creature.getLargeWeapon().toString().toLowerCase()) : I18n.format("creature_move."+creature.getLearnedMoves().get(0).toString().toLowerCase()+".name");
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);
        int textColorHex = ((int)(alpha * 255f) << 24) | (255 << 16) | (255 << 8) | 255;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, textColorHex);
        GlStateManager.popMatrix();

        //use bar
        if (forLargeWeapons) {
            if (creature.getLargeWeaponUse() > 0 || creature.getLargeWeaponCooldown() > 0) {
                int useBarPosX = (int)((width - 48) / 2D) + 120;
                int useBarPosY = (int)((height - 1) / 2D) + 70 + yOffset;
                int useBarFill = (int)RiftUtil.slopeResult(creature.getLargeWeaponCooldown() > 0 ? creature.getLargeWeaponCooldown() : creature.getLargeWeaponUse(),
                        true,
                        0,
                        creature.getLargeWeaponCooldown() > 0 ? creature.getLargeWeapon().maxCooldown : creature.getLargeWeapon().maxUse,
                        0, 48);
                int useBarColor = (255 << 24) | ((creature.getLargeWeaponCooldown() > 0 ? 128 : 255) << 16) | ((creature.getLargeWeaponCooldown() > 0 ? 128 : 255) << 8) | (creature.getLargeWeaponCooldown() > 0 ? 128 : 255);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                Gui.drawRect(useBarPosX, useBarPosY, useBarPosX + useBarFill, useBarPosY + 1, useBarColor);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
        else {
            if (creature.getMoveOneUse() > 0 || creature.getMoveOneCooldown() > 0) {
                int useBarPosX = (int)((width - 48) / 2D) + 120;
                int useBarPosY = (int)((height - 1) / 2D) + 70 + yOffset;
                int useBarFill = (int)RiftUtil.slopeResult(creature.getMoveOneCooldown() > 0 ? creature.getMoveOneCooldown() : creature.getMoveOneUse(),
                        true,
                        0,
                        creature.getMoveOneCooldown() > 0 ? creature.getLearnedMoves().get(0).maxCooldown : creature.getLearnedMoves().get(0).maxUse,
                        0, 48);
                int useBarColor = (255 << 24) | ((creature.getMoveOneCooldown() > 0 ? 128 : 255) << 16) | ((creature.getMoveOneCooldown() > 0 ? 128 : 255) << 8) | (creature.getMoveOneCooldown() > 0 ? 128 : 255);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                Gui.drawRect(useBarPosX, useBarPosY, useBarPosX + useBarFill, useBarPosY + 1, useBarColor);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private void showRightMouseControls(RiftCreature creature, int width, int height, int yOffset) {
        //show right mouse button icon
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        float alpha = ((creature.getMoveTwoCooldown() > 0) ||
                (creature.getLearnedMoves().get(1).chargeType == CreatureMove.ChargeType.BUILDUP && creature.getMoveThreeUse() < creature.getLearnedMoves().get(1).maxUse))
                ? 0.2f : 1f;

        Minecraft.getMinecraft().getTextureManager().bindTexture(rightMouseIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_move."+creature.getLearnedMoves().get(1).toString().toLowerCase()+".name");
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);
        int textColorHex = ((int)(alpha * 255) << 24) | (255 << 16) | (255 << 8) | 255;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, textColorHex);
        GlStateManager.popMatrix();

        //use bar
        if (creature.getMoveTwoUse() > 0 || creature.getMoveTwoCooldown() > 0) {
            int useBarPosX = (int)((width - 48) / 2D) + 120;
            int useBarPosY = (int)((height - 1) / 2D) + 70 + yOffset;
            int useBarFill = (int)RiftUtil.slopeResult(creature.getMoveTwoCooldown() > 0 ? creature.getMoveTwoCooldown() : creature.getMoveTwoUse(),
                    true,
                    0,
                    creature.getMoveTwoCooldown() > 0 ? creature.getLearnedMoves().get(1).maxCooldown : creature.getLearnedMoves().get(1).maxUse,
                    0, 48);
            int useBarColor = (255 << 24) | ((creature.getMoveTwoCooldown() > 0 ? 128 : 255) << 16) | ((creature.getMoveTwoCooldown() > 0 ? 128 : 255) << 8) | (creature.getMoveTwoCooldown() > 0 ? 128 : 255);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            Gui.drawRect(useBarPosX, useBarPosY, useBarPosX + useBarFill, useBarPosY + 1, useBarColor);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private void showMiddleMouseControls(RiftCreature creature, int width, int height, int yOffset) {
        //show middle mouse button icon
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        float alpha = ((creature.getMoveThreeCooldown() > 0) ||
                (creature.getLearnedMoves().get(2).chargeType == CreatureMove.ChargeType.BUILDUP && creature.getMoveThreeUse() < creature.getLearnedMoves().get(2).maxUse))
                ? 0.2f : 1f;

        Minecraft.getMinecraft().getTextureManager().bindTexture(middleMouseIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_move."+creature.getLearnedMoves().get(2).toString().toLowerCase()+".name");
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);
        int textColorHex = ((int)(alpha * 255f) << 24) | (255 << 16) | (255 << 8) | 255;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.enableBlend();
        fontRenderer.renderString(controlName, textPosX, textPosY, textColorHex, false);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //use bar
        if (creature.getMoveThreeUse() > 0 || creature.getMoveThreeCooldown() > 0) {
            int useBarPosX = (int)((width - 48) / 2D) + 120;
            int useBarPosY = (int)((height - 1) / 2D) + 70 + yOffset;
            int useBarFill = (int)RiftUtil.slopeResult(creature.getMoveThreeCooldown() > 0 ? creature.getMoveThreeCooldown() : creature.getMoveThreeUse(),
                    true,
                    0,
                    creature.getMoveThreeCooldown() > 0 ? creature.getLearnedMoves().get(2).maxCooldown : creature.getLearnedMoves().get(2).maxUse,
                    0, 48);
            int useBarColor = (255 << 24) | ((creature.getMoveThreeCooldown() > 0 ? 128 : 255) << 16) | ((creature.getMoveThreeCooldown() > 0 ? 128 : 255) << 8) | (creature.getMoveThreeCooldown() > 0 ? 128 : 255);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            Gui.drawRect(useBarPosX, useBarPosY, useBarPosX + useBarFill, useBarPosY + 1, useBarColor);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private void showSpacebarControls(RiftCreature creature, int width, int height, int yOffset) {
        //show left mouse button icon
        int iconXPos = (int) (((width - 24 * this.iconScale) / 2D - 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        Minecraft.getMinecraft().getTextureManager().bindTexture(spacebarIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 24, 16, 24, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_control.spacebar."+(creature instanceof RiftWaterCreature ? "water" : "air"));
        int textPosX = (int) ((width / 2D) / this.textScale - 440 * this.textScale) - 4 - fontRenderer.getStringWidth(controlName);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }

    private void showToggleAttackOrBreakControls(RiftCreature creature, int width, int height, int yOffset) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        //show generic control key button texture
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D - 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        Minecraft.getMinecraft().getTextureManager().bindTexture(genericButtonIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show control key
        String controlKey = RiftControls.toggleAttackOrBlockBreak.getDisplayName();
        int controlPosX = (int) ((width / 2D) / this.textScale - 400 * this.textScale) - fontRenderer.getStringWidth(controlKey);
        int controlPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 121 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlKey, controlPosX, controlPosY, 0x000000);
        GlStateManager.popMatrix();

        //show text
        String controlName = I18n.format("creature_control.block_break_mode."+(creature.inBlockBreakMode() ? "disable" : "enable"));
        int textPosX = (int) ((width / 2D) / this.textScale - 440 * this.textScale) - 4 - fontRenderer.getStringWidth(controlName);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }
}
