package anightdazingzoroark.prift.client.hud;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.player.PlayerPartyProperties;
import anightdazingzoroark.prift.util.RiftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class PlayerPartyHUD {
    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int INFO_BOX_WIDTH = 80;
    private static final int INFO_BOX_HEIGHT = 27;
    private static final int BAR_WIDTH = 50;
    private static final int BAR_HEIGHT = 3;
    private static final int PARTY_SLOT_TEXTURE_Y = 19;
    private static final int PARTY_ARROW_TEXTURE_Y = 57;

    public static void renderPlayerParty(Minecraft minecraft, int scaledWidth, int scaledHeight) {
        PlayerPartyProperties playerParty = PlayerPartyProperties.get(minecraft.player);
        if (playerParty == null) return;

        CreatureStorage creatureStorage = playerParty.getCreatureStorage();
        int selectedPosition = playerParty.getSelectedPosition();
        int previousPosition = selectedPosition > 0 ? selectedPosition - 1 : creatureStorage.getSize() - 1;
        int nextPosition = selectedPosition + 1 < creatureStorage.getSize() ? selectedPosition + 1 : 0;

        CreatureNBT previousStoredCreature = creatureStorage.getCreature(previousPosition);
        CreatureNBT selectedStoredCreature = creatureStorage.getCreature(selectedPosition);
        CreatureNBT nextStoredCreature = creatureStorage.getCreature(nextPosition);

        Entity previousEntity = previousStoredCreature.nbtTagCompound().isEmpty() ? null
                : RiftUtil.getEntityWithUUID(minecraft.world, previousStoredCreature.getUniqueID());
        Entity selectedEntity = selectedStoredCreature.nbtTagCompound().isEmpty() ? null
                : RiftUtil.getEntityWithUUID(minecraft.world, selectedStoredCreature.getUniqueID());
        Entity nextEntity = nextStoredCreature.nbtTagCompound().isEmpty() ? null
                : RiftUtil.getEntityWithUUID(minecraft.world, nextStoredCreature.getUniqueID());

        IRiftCreature previousCreature = previousEntity instanceof RiftCreature creature ? creature : previousStoredCreature;
        IRiftCreature selectedCreature = selectedEntity instanceof RiftCreature creature ? creature : selectedStoredCreature;
        IRiftCreature nextCreature = nextEntity instanceof RiftCreature creature ? creature : nextStoredCreature;

        float selectedIconScale = 0.75f;
        int maximumInformationLeftOffset = Math.round(
                (scaledWidth - selectedIconScale * 38f) / 2f - (1f - selectedIconScale) * 19f
        );
        int informationHorizontalOffset = Math.max(-maximumInformationLeftOffset, -210);
        int informationLeft = Math.round(scaledWidth / 2f + informationHorizontalOffset + 10f);
        int informationTop = Math.max(0, scaledHeight / 2 - INFO_BOX_HEIGHT / 2 - 20);
        Gui.drawRect(
                informationLeft, informationTop,
                informationLeft + INFO_BOX_WIDTH, informationTop + INFO_BOX_HEIGHT,
                0xC0000000
        );

        if (!selectedStoredCreature.nbtTagCompound().isEmpty()) {
            FontRenderer fontRenderer = minecraft.fontRenderer;
            float textScale = 0.5f;
            String creatureName = fontRenderer.trimStringToWidth(
                    selectedCreature.getName(), Math.round((INFO_BOX_WIDTH - 10) / textScale)
            );

            GlStateManager.pushMatrix();
            GlStateManager.scale(textScale, textScale, textScale);
            fontRenderer.drawString(
                    creatureName,
                    Math.round((informationLeft + 10) / textScale),
                    Math.round((informationTop + 3) / textScale),
                    0xFFFFFF
            );
            GlStateManager.popMatrix();

            float healthPercentage = selectedCreature.getHealth() / selectedCreature.getMaxHealth();
            float staminaPercentage = selectedCreature.getStamina() / selectedCreature.getMaxStamina();
            drawStatusBar(informationLeft + 10, informationTop + 13, healthPercentage, 0xFFFF0000);
            drawStatusBar(informationLeft + 10, informationTop + 20, staminaPercentage, 0xFFFFFF00);
        }

        drawArrow(minecraft, scaledWidth, scaledHeight, true);
        addCreatureIcon(
                minecraft, previousCreature, previousStoredCreature.nbtTagCompound().isEmpty(),
                previousStoredCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY,
                scaledWidth, scaledHeight, 0.5f, -30
        );
        addCreatureIcon(
                minecraft, selectedCreature, selectedStoredCreature.nbtTagCompound().isEmpty(),
                selectedStoredCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY,
                scaledWidth, scaledHeight, selectedIconScale, 0
        );
        addCreatureIcon(
                minecraft, nextCreature, nextStoredCreature.nbtTagCompound().isEmpty(),
                nextStoredCreature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY,
                scaledWidth, scaledHeight, 0.5f, 30
        );
        drawArrow(minecraft, scaledWidth, scaledHeight, false);
    }

    private static void addCreatureIcon(
            @NotNull Minecraft minecraft, @NotNull IRiftCreature creature, boolean empty, boolean deployed,
            int scaledWidth, int scaledHeight, float scale, int verticalOffset
    ) {
        int maximumLeftOffset = Math.round((scaledWidth - scale * 38f) / 2f - (1f - scale) * 19f);
        int horizontalOffset = Math.max(-maximumLeftOffset, -210);
        int verticalBaseOffset = -20;
        float unscaledBackgroundX = (scaledWidth - 38f * scale) / 2f + horizontalOffset;
        float unscaledBackgroundY = (scaledHeight - 38f * scale) / 2f + verticalBaseOffset + verticalOffset;
        int backgroundX = Math.round(Math.max((1f - scale) * 19f, unscaledBackgroundX) / scale);
        int backgroundY = Math.round(Math.max(0f, unscaledBackgroundY) / scale);
        int backgroundTextureX = !empty && creature.getHealth() <= 0f ? 76 : !empty && deployed ? 38 : 0;

        minecraft.getTextureManager().bindTexture(HUD_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        Gui.drawModalRectWithCustomSizedTexture(
                backgroundX, backgroundY, backgroundTextureX, PARTY_SLOT_TEXTURE_Y, 38, 38, 256, 256
        );
        GlStateManager.popMatrix();

        if (!empty && creature.getCreatureType() != null) {
            minecraft.getTextureManager().bindTexture(new ResourceLocation(
                    RiftInitialize.MODID, "textures/icons/" + creature.getCreatureType().getName() + "_icon.png"
            ));
            float unscaledIconX = (scaledWidth - 24f * scale) / 2f + horizontalOffset;
            float unscaledIconY = (scaledHeight - 24f * scale) / 2f + verticalBaseOffset + verticalOffset;
            int iconX = Math.round(Math.max((1f - scale) * 12f, unscaledIconX) / scale);
            int iconY = Math.round(Math.max(0f, unscaledIconY) / scale);

            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            Gui.drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, 24, 24, 24, 24);
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();
    }

    private static void drawArrow(@NotNull Minecraft minecraft, int scaledWidth, int scaledHeight, boolean upward) {
        int maximumLeftOffset = Math.round((scaledWidth - 32f) / 2f - 42f);
        int horizontalOffset = Math.max(-maximumLeftOffset, -168);
        int verticalOffset = -20 + (upward ? -20 : 20);
        int left = Math.round(Math.max(0f, (scaledWidth - 32f) / 2f + horizontalOffset));
        int top = Math.round(Math.max(0f, (scaledHeight - 8f) / 2f + verticalOffset));

        minecraft.getTextureManager().bindTexture(HUD_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(
                left, top, upward ? 0 : 32, PARTY_ARROW_TEXTURE_Y, 32, 8, 256, 256
        );
        GlStateManager.disableBlend();
    }

    private static void drawStatusBar(int left, int top, float percentage, int color) {
        Gui.drawRect(left, top, left + BAR_WIDTH, top + BAR_HEIGHT, 0xFF868686);
        int filledWidth = Math.round(BAR_WIDTH * percentage);
        if (filledWidth > 0) Gui.drawRect(left, top, left + filledWidth, top + BAR_HEIGHT, color);
    }
}
