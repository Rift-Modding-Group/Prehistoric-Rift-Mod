package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
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

                switch (creature.creatureType) {
                    case TYRANNOSAURUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case STEGOSAURUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case TRICERATOPS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case UTAHRAPTOR:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        this.showMiddleMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 30);
                        this.showSpacebarControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 45);
                        break;
                    case APATOSAURUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        this.showMiddleMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 30);
                        break;
                    case PARASAUROLOPHUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case SARCOSUCHUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case ANOMALOCARIS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        this.showMiddleMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 30);
                        break;
                    case SAUROPHAGANAX:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case DIREWOLF:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        this.showMiddleMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 30);
                        break;
                    case MEGALOCEROS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        this.showSpacebarControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 30);
                        break;
                    case BARYONYX:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case ANKYLOSAURUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                    case DILOPHOSAURUS:
                        this.showLeftMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 0);
                        this.showRightMouseControls(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), 15);
                        break;
                }
            }
        }
    }

    private void showLeftMouseControls(RiftCreature creature, int width, int height, int yOffset) {
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
        RiftCreatureType creatureType = creature.creatureType;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_control.left_mouse."+creatureType.toString().toLowerCase());
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }

    private void showRightMouseControls(RiftCreature creature, int width, int height, int yOffset) {
        //show left mouse button icon
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        Minecraft.getMinecraft().getTextureManager().bindTexture(rightMouseIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        RiftCreatureType creatureType = creature.creatureType;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_control.right_mouse."+creatureType.toString().toLowerCase());
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }

    private void showMiddleMouseControls(RiftCreature creature, int width, int height, int yOffset) {
        //show left mouse button icon
        int iconXPos = (int) (((width - 16 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
        int iconYPos = (int) (((height - 16 * this.iconScale) / 2D + 80 * this.iconScale + yOffset) / this.iconScale);
        Minecraft.getMinecraft().getTextureManager().bindTexture(middleMouseIcon);
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.iconScale, this.iconScale, this.iconScale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        //show text
        RiftCreatureType creatureType = creature.creatureType;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_control.middle_mouse."+creatureType.toString().toLowerCase());
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale);
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }

    private void showSpacebarControls(RiftCreature creature, int width, int height, int yOffset) {
        //show left mouse button icon
        int iconXPos = (int) (((width - 24 * this.iconScale) / 2D + 135 * this.iconScale) / this.iconScale);
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
        RiftCreatureType creatureType = creature.creatureType;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String controlName = I18n.format("creature_control.spacebar."+creatureType.toString().toLowerCase());
        int textPosX = (int) ((width / 2D) / this.textScale + 440 * this.textScale) + 4;
        int textPosY = (int) (((height - fontRenderer.FONT_HEIGHT * this.textScale) / 2D + 120 * this.textScale + yOffset) / this.textScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        fontRenderer.drawString(controlName, textPosX, textPosY, 0xffffff);
        GlStateManager.popMatrix();
    }
}
