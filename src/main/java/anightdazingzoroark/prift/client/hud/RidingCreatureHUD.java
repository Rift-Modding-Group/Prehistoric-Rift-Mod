package anightdazingzoroark.prift.client.hud;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureLeapHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("SuspiciousNameCombination")
public class RidingCreatureHUD {
    private static final ResourceLocation CONTROLS_TEXTURE = new ResourceLocation(RiftInitialize.MODID, "textures/ui/controls.png");
    private static final ResourceLocation HUD_ICONS_TEXTURE = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_SPACING = 2;
    private static final int CONTROL_ICON_COLUMN_WIDTH = 24;
    private static final float CONTROLS_SCALE = 0.75f;

    public void renderLeapChargeBar(
            @NotNull Minecraft minecraft, int chargeTicks, int cooldownTicks,
            int cooldownDisplayChargeTicks, int width, int height
    ) {
        int barWidth = 182;
        int left = width / 2 - barWidth / 2;
        int top = height - 32;
        int fillWidth;
        //chargeup fill
        if (chargeTicks > 0) {
            fillWidth = Math.round(MathUtil.slopeResult(
                    chargeTicks, true, 0, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS, 0, barWidth
            ));
        }
        //cooldown fill
        else {
            double oldChargeFill = MathUtil.slopeResult(
                    cooldownDisplayChargeTicks, true,
                    0, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS,
                    0, barWidth
            );
            fillWidth = (int) Math.round(MathUtil.slopeResult(
                    cooldownTicks, true,
                    0, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_COOLDOWN_TICKS,
                    0, oldChargeFill
            ));
        }

        minecraft.getTextureManager().bindTexture(HUD_ICONS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0, 9, barWidth, 5, 256, 256);
        if (fillWidth > 0) {
            Gui.drawModalRectWithCustomSizedTexture(left, top, 0, 14, fillWidth, 5, 256, 256);
        }
        GlStateManager.disableBlend();
        minecraft.getTextureManager().bindTexture(Gui.ICONS);
    }

    public void renderMoveHotbar(@NotNull Minecraft minecraft, @NotNull RiftCreature creature, int selectedMove, int width, int height) {
        List<ImmutablePair<String, CreatureMoveBuilder>> moves = creature.getCreatureMoves().getUsableMoves();
        if (moves.isEmpty()) return;

        int hotbarWidth = moves.size() * SLOT_SIZE + (moves.size() - 1) * SLOT_SPACING;
        int left = (width - hotbarWidth) / 2;
        int top = height - SLOT_SIZE - 3;
        CreatureMoveStorage moveStorage = creature.getCreatureMoves();

        //go over each available move
        for (int moveIndex = 0; moveIndex < moves.size(); moveIndex++) {
            ImmutablePair<String, CreatureMoveBuilder> move = moves.get(moveIndex);
            int slotLeft = left + moveIndex * (SLOT_SIZE + SLOT_SPACING);
            Gui.drawRect(
                    slotLeft, top, slotLeft + SLOT_SIZE, top + SLOT_SIZE,
                    moveIndex == selectedMove ? 0xFFFFFFFF : 0xFFC0C0C0
            );
            Gui.drawRect(slotLeft + 1, top + 1, slotLeft + SLOT_SIZE - 1, top + SLOT_SIZE - 1, 0xC0101010);

            String translatedName = I18n.format("move.creature." + move.getKey());
            String abbreviation = translatedName.substring(0, Math.min(2, translatedName.length())).toUpperCase(Locale.ROOT);
            FontRenderer fontRenderer = minecraft.fontRenderer;
            fontRenderer.drawStringWithShadow(
                    abbreviation,
                    slotLeft + (SLOT_SIZE - fontRenderer.getStringWidth(abbreviation)) / 2f,
                    top + (SLOT_SIZE - fontRenderer.FONT_HEIGHT) / 2f,
                    0xFFFFFF
            );
            fontRenderer.drawString(Integer.toString(moveIndex + 1), slotLeft + 2, top + 2, 0xB0B0B0);

            //show cooldown and time
            int cooldown = moveStorage.moveCurrentCooldown(move.getKey());
            int maximumCooldown = moveStorage.moveMaximumCooldown(move.getKey());
            if (cooldown > 0 && maximumCooldown > 0) {
                int cooldownHeight = (int) Math.ceil(MathUtil.slopeResult(
                        cooldown, true, 0, maximumCooldown, 0, SLOT_SIZE - 2
                ));
                Gui.drawRect(
                        slotLeft + 1, top + SLOT_SIZE - 1 - cooldownHeight,
                        slotLeft + SLOT_SIZE - 1, top + SLOT_SIZE - 1,
                        0xA0808080
                );
                String cooldownSeconds = Integer.toString((int) Math.ceil(cooldown / 20D));
                fontRenderer.drawStringWithShadow(
                        cooldownSeconds,
                        slotLeft + SLOT_SIZE - fontRenderer.getStringWidth(cooldownSeconds) - 2,
                        top + SLOT_SIZE - fontRenderer.FONT_HEIGHT - 2,
                        0xFFFFFF
                );
            }

            //show small charge up bar
            if (move.getKey().equals(moveStorage.getCurrentMove()) && move.getValue().getMoveChargeupBuilder() != null) {
                int maximumCharge = move.getValue().getMoveChargeupBuilder().getMaxChargeUp();
                int chargeWidth = Math.round(MathUtil.slopeResult(
                        moveStorage.getCurrentMoveBuildup(), true, 0, maximumCharge, 0, SLOT_SIZE - 2
                ));
                Gui.drawRect(
                        slotLeft + 1, top + SLOT_SIZE - 3,
                        slotLeft + 1 + chargeWidth, top + SLOT_SIZE - 1,
                        0xFF60E060
                );
            }
        }

        //show currently selected move above move hotbar
        String selectedMoveName = I18n.format("move.creature." + moves.get(selectedMove).getKey());
        minecraft.fontRenderer.drawStringWithShadow(
                selectedMoveName,
                (width - minecraft.fontRenderer.getStringWidth(selectedMoveName)) / 2f,
                top - minecraft.fontRenderer.FONT_HEIGHT - 12,
                0xFFFFFF
        );
    }

    public void renderControls(
            @NotNull Minecraft minecraft, @NotNull RiftCreature creature, boolean moveHotbarActive,
            int selectedMove, int width, int height
    ) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0f);
        GlStateManager.scale(CONTROLS_SCALE, CONTROLS_SCALE, CONTROLS_SCALE);

        //switch between item hotbar and move hotbar
        int row = 0;
        this.drawControl(
                minecraft, RiftControls.TOGGLE_RIDING_MOVE_HOTBAR.getKeyCode(),
                I18n.format("hud.prift.riding.toggle_hotbar"), row++
        );
        if (moveHotbarActive) {
            //move selection by scrolling
            this.drawControl(
                    minecraft, RiftControls.MIDDLE_MOUSE,
                    I18n.format("hud.prift.riding.select_move"), row++
            );

            //move use instruction by left clicking
            String moveUseInstruction;
            if (!creature.getCreatureMoves().getUsableMoves().isEmpty()
                    && creature.getCreatureMoves().getUsableMoves().get(selectedMove).getValue().getMoveChargeupBuilder() != null
            ) {
                moveUseInstruction = I18n.format("hud.prift.riding.charge_move");
            }
            else moveUseInstruction = I18n.format("hud.prift.riding.use_move");
            this.drawControl(
                    minecraft, minecraft.gameSettings.keyBindAttack.getKeyCode(),
                    moveUseInstruction, row++
            );
        }
        //spacebar for jumping
        if (creature.getNavigationBuilder().getCanLeap()) {
            this.drawControl(
                    minecraft, minecraft.gameSettings.keyBindJump.getKeyCode(),
                    I18n.format("hud.prift.riding.jump"), row
            );
        }
        GlStateManager.popMatrix();
    }

    private void drawControl(@NotNull Minecraft minecraft, int keyCode, @NotNull String purpose, int row) {
        //---set uvs from keybind textures---
        int textureX;
        int textureY;
        if (keyCode == minecraft.gameSettings.keyBindAttack.getKeyCode()) {
            textureX = 16;
            textureY = 0;
        }
        else if (keyCode == minecraft.gameSettings.keyBindUseItem.getKeyCode()) {
            textureX = 32;
            textureY = 0;
        }
        else if (keyCode == RiftControls.MIDDLE_MOUSE) {
            textureX = 48;
            textureY = 0;
        }
        else if (keyCode == Keyboard.KEY_SPACE) {
            textureX = 0;
            textureY = 16;
        }
        else {
            textureX = 0;
            textureY = 0;
        }

        //---create control guides---
        int iconWidth = keyCode == Keyboard.KEY_SPACE ? 24 : 16;
        int left = 140;
        int top = 80 + row * 20;
        int iconLeft = left + (CONTROL_ICON_COLUMN_WIDTH - iconWidth) / 2;
        minecraft.getTextureManager().bindTexture(CONTROLS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(iconLeft, top, textureX, textureY, iconWidth, 16, 64, 48);
        GlStateManager.disableBlend();

        if (textureX == 0 && textureY == 0) {
            String keyName = Keyboard.getKeyName(keyCode);
            minecraft.fontRenderer.drawString(
                    keyName,
                    iconLeft + (iconWidth - minecraft.fontRenderer.getStringWidth(keyName)) / 2f + 0.5f,
                    top + (16 - minecraft.fontRenderer.FONT_HEIGHT) / 2f + 0.5f,
                    0x202020,
                    false
            );
        }
        minecraft.fontRenderer.drawStringWithShadow(
                purpose,
                left + CONTROL_ICON_COLUMN_WIDTH + 4,
                top + (16 - minecraft.fontRenderer.FONT_HEIGHT) / 2f,
                0xFFFFFF
        );
    }
}
