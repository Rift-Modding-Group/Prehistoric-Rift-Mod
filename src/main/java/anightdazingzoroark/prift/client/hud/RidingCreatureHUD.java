package anightdazingzoroark.prift.client.hud;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureLeapHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureSprintHelper;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class RidingCreatureHUD {
    private static final ResourceLocation CONTROLS_TEXTURE = new ResourceLocation(RiftInitialize.MODID, "textures/ui/controls.png");
    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_SPACING = 2;
    private static final int CONTROL_ICON_COLUMN_WIDTH = 24;
    private static final int STATUS_ICON_SIZE = 9;
    private static final int STATUS_ICON_SPACING = 8;
    private static final int STATUS_ROW_HEIGHT = 10;
    private static final int HEALTH_ICON_COUNT = 20;
    private static final int STAMINA_ICON_COUNT = 20;
    private static final int EMPTY_STAMINA_TEXTURE_X = 0;
    private static final int FULL_STAMINA_TEXTURE_X = 9;
    private static final int HALF_STAMINA_TEXTURE_X = 18;
    private static final float CONTROLS_SCALE = 0.75f;

    private int jumpChargeTicks;
    private int jumpCooldownTicks;
    private int jumpCooldownDisplayChargeTicks;
    private int sprintTicks;
    private int sprintCooldownTicks;
    private int sprintCooldownDisplayTicks;

    //---setters---
    public void setJumpState(int chargeTicks, int cooldownTicks, int cooldownDisplayChargeTicks) {
        this.jumpChargeTicks = Math.max(0, chargeTicks);
        this.jumpCooldownTicks = Math.max(0, cooldownTicks);
        this.jumpCooldownDisplayChargeTicks = Math.max(0, cooldownDisplayChargeTicks);
    }

    public void setSprintState(int sprintTicks, int cooldownTicks, int cooldownDisplayTicks) {
        this.sprintTicks = Math.max(0, sprintTicks);
        this.sprintCooldownTicks = Math.max(0, cooldownTicks);
        this.sprintCooldownDisplayTicks = Math.max(0, cooldownDisplayTicks);
    }

    //---render---
    //reimplementation of ridden creature health that uses creature health percentage
    public void renderCreatureHealth(@NotNull Minecraft minecraft, @NotNull RiftCreature creature, int width, int height) {
        int healthLevel = 0;
        if (creature.getMaxHealth() > 0f) {
            healthLevel = (int) Math.round(MathUtil.slopeResult(
                    creature.getHealth(), true,
                    0D, creature.getMaxHealth(),
                    0D, HEALTH_ICON_COUNT * 2D
            ));
        }

        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;
        int healthRowCount = (HEALTH_ICON_COUNT + 9) / 10;

        minecraft.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        for (int heartIndex = 0; heartIndex < HEALTH_ICON_COUNT; heartIndex++) {
            int heartValue = heartIndex * 2 + 1;
            int heartLeft = left + heartIndex % 10 * STATUS_ICON_SPACING;
            int heartTop = top - heartIndex / 10 * STATUS_ROW_HEIGHT;
            Gui.drawModalRectWithCustomSizedTexture(
                    heartLeft, heartTop, 52, 9, STATUS_ICON_SIZE, STATUS_ICON_SIZE, 256, 256
            );

            if (heartValue < healthLevel) {
                Gui.drawModalRectWithCustomSizedTexture(
                        heartLeft, heartTop, 88, 9, STATUS_ICON_SIZE, STATUS_ICON_SIZE, 256, 256
                );
            }
            else if (heartValue == healthLevel) {
                //flip half heart icon
                GlStateManager.pushMatrix();
                GlStateManager.translate(heartLeft + STATUS_ICON_SIZE, 0f, 0f);
                GlStateManager.scale(-1f, 1f, 1f);
                Gui.drawModalRectWithCustomSizedTexture(
                        0, heartTop, 97, 9, STATUS_ICON_SIZE, STATUS_ICON_SIZE, 256, 256
                );
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.disableBlend();

        GuiIngameForge.left_height += healthRowCount * STATUS_ROW_HEIGHT;
    }

    public void renderStamina(@NotNull Minecraft minecraft, @NotNull RiftCreature creature, int width, int height) {
        int staminaLevel = 0;
        if (creature.getMaxStamina() > 0f) {
            staminaLevel = (int) Math.round(MathUtil.slopeResult(
                    creature.getStamina(), true,
                    0D, creature.getMaxStamina(),
                    0D, STAMINA_ICON_COUNT * 2D
            ));
        }

        int right = width / 2 + 91;
        int top = height - GuiIngameForge.right_height;
        minecraft.getTextureManager().bindTexture(HUD_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        for (int staminaIndex = 0; staminaIndex < STAMINA_ICON_COUNT; staminaIndex++) {
            int staminaValue = staminaIndex * 2 + 1;
            int staminaLeft = right - staminaIndex % 10 * STATUS_ICON_SPACING - STATUS_ICON_SIZE;
            int staminaTop = top - staminaIndex / 10 * STATUS_ROW_HEIGHT;
            Gui.drawModalRectWithCustomSizedTexture(
                    staminaLeft, staminaTop, EMPTY_STAMINA_TEXTURE_X, 0,
                    STATUS_ICON_SIZE, STATUS_ICON_SIZE, 256, 256
            );

            if (staminaValue < staminaLevel) {
                Gui.drawModalRectWithCustomSizedTexture(
                        staminaLeft, staminaTop, FULL_STAMINA_TEXTURE_X, 0,
                        STATUS_ICON_SIZE, STATUS_ICON_SIZE, 256, 256
                );
            }
            else if (staminaValue == staminaLevel) {
                Gui.drawModalRectWithCustomSizedTexture(
                        staminaLeft, staminaTop, HALF_STAMINA_TEXTURE_X, 0,
                        STATUS_ICON_SIZE, STATUS_ICON_SIZE, 256, 256
                );
            }
        }
        GlStateManager.disableBlend();

        GuiIngameForge.right_height += (STAMINA_ICON_COUNT + 9) / 10 * STATUS_ROW_HEIGHT;
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
        int selectedMoveNameLeft = (width - minecraft.fontRenderer.getStringWidth(selectedMoveName)) / 2;
        minecraft.fontRenderer.drawStringWithShadow(
                selectedMoveName,
                selectedMoveNameLeft,
                top - minecraft.fontRenderer.FONT_HEIGHT - 30,
                0xFFFFFF
        );

        //separator line between moves and creature health and stamina
        int separatorLeft = width / 2 - 91;
        int separatorTop = top - 5;
        Gui.drawRect(
                separatorLeft, separatorTop,
                separatorLeft + 182, separatorTop + 1,
                0xFF808080
        );
    }

    public void renderControls(
            @NotNull Minecraft minecraft, @NotNull RiftCreature creature, boolean moveHotbarActive,
            int selectedMove, int width, int height
    ) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0f);
        GlStateManager.scale(CONTROLS_SCALE, CONTROLS_SCALE, CONTROLS_SCALE);

        //-----right side-----
        //---switch between item hotbar and move hotbar---
        int rightRow = 0;
        String hotbarSwapString;
        if (moveHotbarActive) hotbarSwapString = I18n.format("hud.prift.riding.show_item_hotbar");
        else hotbarSwapString = I18n.format("hud.prift.riding.show_move_hotbar");
        this.drawControl(
                minecraft, RiftControls.TOGGLE_RIDING_MOVE_HOTBAR.getKeyCode(),
                hotbarSwapString, true, rightRow++
        );

        //---ctrl for sprinting---
        int maximumSprintCooldown = Math.max(0, creature.getCreatureType().getSprintCooldown());
        double sprintProgress;
        if (this.sprintTicks > 0) {
            sprintProgress = (double) this.sprintTicks / RiftCreatureSprintHelper.MAXIMUM_SPRINT_TICKS;
        }
        else if (this.sprintCooldownTicks > 0 && maximumSprintCooldown > 0) {
            double sprintDisplayProgress = MathUtil.slopeResult(
                    this.sprintCooldownDisplayTicks, true,
                    0, RiftCreatureSprintHelper.MAXIMUM_SPRINT_TICKS,
                    0D, 1D
            );
            sprintProgress = MathUtil.slopeResult(
                    this.sprintCooldownTicks, true,
                    0, maximumSprintCooldown,
                    0D, sprintDisplayProgress
            );
        }
        else sprintProgress = 0D;
        int sprintColor = this.sprintTicks == 0 && this.sprintCooldownTicks > 0 ? 0xAAAAAA : 0xFFFFFF;
        this.drawControl(
                minecraft, minecraft.gameSettings.keyBindSprint.getKeyCode(),
                I18n.format("hud.prift.riding.sprint"), sprintColor, sprintProgress, true, rightRow++
        );

        //---spacebar for jumping---
        if (creature.getNavigationBuilder().getCanLeap()) {
            double leapProgress;
            if (this.jumpChargeTicks > 0) {
                leapProgress = (double) this.jumpChargeTicks / RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS;
            }
            else if (this.jumpCooldownTicks > 0) {
                double leapDisplayProgress = MathUtil.slopeResult(
                        this.jumpCooldownDisplayChargeTicks, true,
                        0, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS,
                        0D, 1D
                );
                leapProgress = MathUtil.slopeResult(
                        this.jumpCooldownTicks, true,
                        0, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_COOLDOWN_TICKS,
                        0D, leapDisplayProgress
                );
            }
            else leapProgress = 0D;
            int leapColor = this.jumpCooldownTicks > 0 ? 0xAAAAAA : 0xFFFFFF;
            this.drawControl(
                    minecraft, minecraft.gameSettings.keyBindJump.getKeyCode(),
                    I18n.format("hud.prift.riding.jump"), leapColor, leapProgress, true, rightRow++
            );
        }

        //-----left side-----
        if (moveHotbarActive) {
            int leftRow = 0;

            //---move use instruction by left clicking---
            String moveUseInstruction;
            if (!creature.getCreatureMoves().getUsableMoves().isEmpty()
                    && creature.getCreatureMoves().getUsableMoves().get(selectedMove).getValue().getMoveChargeupBuilder() != null
            ) {
                moveUseInstruction = I18n.format("hud.prift.riding.charge_move");
            }
            else moveUseInstruction = I18n.format("hud.prift.riding.use_move");
            this.drawControl(
                    minecraft, minecraft.gameSettings.keyBindAttack.getKeyCode(),
                    moveUseInstruction, false, leftRow++
            );

            //---move selection by scrolling---
            this.drawControl(
                    minecraft, RiftControls.MIDDLE_MOUSE,
                    I18n.format("hud.prift.riding.select_move"), false, leftRow++
            );

            //---block break---
            String blockBreakString;
            if (creature.getUseBlockBreak()) blockBreakString = I18n.format("hud.prift.riding.block_break_disable");
            else blockBreakString = I18n.format("hud.prift.riding.block_break_enable");
            this.drawControl(
                    minecraft, RiftControls.TOGGLE_RIDING_BLOCK_BREAK.getKeyCode(),
                    blockBreakString, false, leftRow
            );
        }

        GlStateManager.popMatrix();
    }

    //well
    private void drawControl(
            @NotNull Minecraft minecraft, int keyCode,
            @NotNull String purpose, boolean rightSide, int row
    ) {
        this.drawControl(minecraft, keyCode, purpose, 0xFFFFFF, -1D, rightSide, row);
    }

    //universal, with special case param for sprint and leap
    private void drawControl(
            @NotNull Minecraft minecraft, int keyCode,
            @NotNull String purpose, int purposeTextColor, double progress, boolean rightSide, int row
    ) {
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
        int iconColumnLeft;
        if (rightSide) iconColumnLeft = 140;
        else iconColumnLeft = -140 - CONTROL_ICON_COLUMN_WIDTH;
        int top = 80 + row * 20;
        int iconLeft = iconColumnLeft + (CONTROL_ICON_COLUMN_WIDTH - iconWidth) / 2;
        minecraft.getTextureManager().bindTexture(CONTROLS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(iconLeft, top, textureX, textureY, iconWidth, 16, 64, 48);
        GlStateManager.disableBlend();

        if (textureX == 0 && textureY == 0) {
            String keyName = Keyboard.getKeyName(keyCode);

            //special case for lctrl
            if (keyCode == Keyboard.KEY_LCONTROL) keyName = "LCtrl";
            float keyCharScale = (keyCode == Keyboard.KEY_LCONTROL) ? 0.5f : 1f;

            if (keyCharScale < 1f) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(keyCharScale, keyCharScale, 1);
            }
            minecraft.fontRenderer.drawString(
                    keyName,
                    (iconLeft + (iconWidth - minecraft.fontRenderer.getStringWidth(keyName) * keyCharScale) / 2f + 0.5f) / keyCharScale,
                    (top + (16 - minecraft.fontRenderer.FONT_HEIGHT * keyCharScale) / 2f + 0.5f) / keyCharScale,
                    0x202020,
                    false
            );
            if (keyCharScale < 1f) GlStateManager.popMatrix();
        }

        //draw action
        float actionPosX;
        if (rightSide) actionPosX = iconColumnLeft + CONTROL_ICON_COLUMN_WIDTH + 4;
        else actionPosX = iconColumnLeft - minecraft.fontRenderer.getStringWidth(purpose) - 4;
        float actionPosY = top + (16 - minecraft.fontRenderer.FONT_HEIGHT) / 2f;
        minecraft.fontRenderer.drawStringWithShadow(purpose, actionPosX, actionPosY, purposeTextColor);

        if (progress >= 0D) {
            int lineTop = Math.round(actionPosY) + minecraft.fontRenderer.FONT_HEIGHT + 1;
            int fillWidth = (int) Math.round(MathUtil.slopeResult(
                    Math.clamp(progress, 0D, 1D), true,
                    0D, 1D, 0, 80
            ));

            if (fillWidth > 0) {
                int lineLeft;
                int lineRight;
                if (rightSide) {
                    lineLeft = iconColumnLeft + CONTROL_ICON_COLUMN_WIDTH + 4;
                    lineRight = lineLeft + fillWidth;
                }
                else {
                    lineRight = iconColumnLeft - 4;
                    lineLeft = lineRight - fillWidth;
                }
                Gui.drawRect(lineLeft, lineTop, lineRight, lineTop + 2, 0xFF000000 | purposeTextColor);
            }
        }
    }
}
