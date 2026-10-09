package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.client.hud.PlayerPartyHUD;
import anightdazingzoroark.prift.client.hud.RidingCreatureHUD;
import anightdazingzoroark.prift.client.hud.TameProgressHUD;
import anightdazingzoroark.prift.client.ui.RiftPartyScreen;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder.RiddenAimingType;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureLeapHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureSprintHelper;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftPartyActionMessage;
import anightdazingzoroark.prift.server.message.RiftRidingAimMessage;
import anightdazingzoroark.prift.server.message.RiftRidingActionMessage;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.ClientGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ClientEvents {
    @NotNull
    private final RidingCreatureHUD ridingCreatureHUD = new RidingCreatureHUD();
    @NotNull
    private final CameraHandler cameraHandler = new CameraHandler(this.ridingCreatureHUD);
    private int riddenCreatureId = -1;
    private boolean moveHotbarActive;
    private boolean usingRidingMove;
    private boolean ridingAimActive;
    private int ridingAimMove = -1;
    private int selectedRidingMove;
    private int riddenLeapChargeStartTick = -1;
    private int riddenLeapCooldownDisplayChargeTicks;
    private int riddenSprintStartTick = -1;
    private int riddenSprintCooldownDisplayTicks;

    /**
     * block default inventory opening when riding on a creature
     * and open creature inventory instead
     * */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRiddenCreatureInventoryOpen(GuiOpenEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!(event.getGui() instanceof GuiInventory) || minecraft.player == null) return;
        if (!(minecraft.player.getRidingEntity() instanceof RiftCreature creature) || creature.getControllingPassenger() != minecraft.player) return;

        if (this.usingRidingMove) this.releaseSelectedRidingMove();
        this.stopRidingAim();
        event.setCanceled(true);
        GuiManager.openFromClient(RiftCreatureGuiFactory.INSTANCE, new RiftCreatureGuiData(minecraft.player, creature));
    }

    /**
     * keybinds are dealt with here
     * */
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null) return;

        //---for party HUD---
        if (RiftControls.SWITCH_PARTY_MEMBER_UP.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(RiftPartyActionMessage.Action.SELECT_PREVIOUS));
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
        if (RiftControls.SWITCH_PARTY_MEMBER_DOWN.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(RiftPartyActionMessage.Action.SELECT_NEXT));
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
        if (RiftControls.DEPLOY_PARTY_MEMBER.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(RiftPartyActionMessage.Action.DEPLOY_OR_DISMISS));
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
        }
        if (RiftControls.OPEN_PARTY_MENU.isPressed() && minecraft.currentScreen == null) {
            ClientGUI.open(new RiftPartyScreen());
        }

        //---for riding controls---
        RiftCreature riddenCreature = this.updateRidingState(minecraft);
        if (riddenCreature == null) return;

        //for changing between item hotbar and move hotbar
        if (RiftControls.TOGGLE_RIDING_MOVE_HOTBAR.isPressed()) {
            if (this.moveHotbarActive && this.usingRidingMove) this.releaseSelectedRidingMove();
            if (this.moveHotbarActive) this.stopRidingAim();

            this.moveHotbarActive = !this.moveHotbarActive;
            //block default mouse actions
            if (this.moveHotbarActive) {
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindAttack.getKeyCode(), false);
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindUseItem.getKeyCode(), false);
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindPickBlock.getKeyCode(), false);
            }
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
        }

        //toggle block breaking with ridden melee moves
        boolean toggleBlockBreakPressed = RiftControls.TOGGLE_RIDING_BLOCK_BREAK.isPressed();
        if (this.moveHotbarActive && toggleBlockBreakPressed) {
            RiftMessages.WRAPPER.sendToServer(new RiftRidingActionMessage(
                    RiftRidingActionMessage.Action.SET_BLOCK_BREAK,
                    this.selectedRidingMove,
                    !riddenCreature.getUseBlockBreak()
            ));
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
        }

        int eventKey = Keyboard.getEventKey();
        boolean keyPressed = Keyboard.getEventKeyState();

        //use keybinds for hotbar for manual move selection
        if (this.moveHotbarActive && minecraft.currentScreen == null && keyPressed) {
            int moveCount = riddenCreature.getCreatureMoves().getUsableMoves().size();
            for (int hotbarSlot = 0; hotbarSlot < minecraft.gameSettings.keyBindsHotbar.length; hotbarSlot++) {
                KeyBinding hotbarBinding = minecraft.gameSettings.keyBindsHotbar[hotbarSlot];
                if (!hotbarBinding.isActiveAndMatches(eventKey)) continue;

                hotbarBinding.isPressed();
                if (hotbarSlot < moveCount && hotbarSlot != this.selectedRidingMove) {
                    this.stopRidingAim();
                    this.selectedRidingMove = hotbarSlot;
                }
                break;
            }
        }

        //make ridden creature jump
        if (eventKey == minecraft.gameSettings.keyBindJump.getKeyCode()) {
            if (keyPressed) {
                if (this.riddenLeapChargeStartTick < 0 && riddenCreature.canChargeRiddenLeap(minecraft.player)) {
                    this.riddenLeapChargeStartTick = minecraft.player.ticksExisted;
                }
            }
            else if (this.riddenLeapChargeStartTick >= 0) {
                int chargeTicks = Math.clamp(
                        minecraft.player.ticksExisted - this.riddenLeapChargeStartTick + 1,
                        1, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS
                );
                boolean movingForward = minecraft.gameSettings.keyBindForward.isKeyDown() || minecraft.player.movementInput.moveForward > 0f;
                this.riddenLeapCooldownDisplayChargeTicks = chargeTicks;
                riddenCreature.jumpFromRider(minecraft.player, chargeTicks, movingForward);
                RiftMessages.WRAPPER.sendToServer(new RiftRidingActionMessage(RiftRidingActionMessage.Action.JUMP, chargeTicks, movingForward));
                this.riddenLeapChargeStartTick = -1;
            }
        }

        //make ridden creature sprint
        if (eventKey == minecraft.gameSettings.keyBindSprint.getKeyCode()) {
            RiftMessages.WRAPPER.sendToServer(new RiftRidingActionMessage(
                    RiftRidingActionMessage.Action.SET_SPRINTING, this.selectedRidingMove, keyPressed
            ));
        }
    }

    /**
     * operations that require constant updates happen here
     * */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        RiftCreature riddenCreature = this.updateRidingState(minecraft);

        //---tick custom camera stuff---
        if (this.ridingAimActive && (!this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature) || riddenCreature == null)) {
            this.stopRidingAim();
        }
        else if (this.ridingAimActive) {
            List<ImmutablePair<String, CreatureMoveBuilder>> moves = riddenCreature.getCreatureMoves().getUsableMoves();
            if (!this.moveHotbarActive || minecraft.currentScreen != null
                    || this.ridingAimMove < 0 || this.ridingAimMove >= moves.size()
                    || moves.get(this.ridingAimMove).getValue().getRiddenAimingType() == RiddenAimingType.NONE
            ) {
                this.stopRidingAim();
            }
        }
        this.cameraHandler.update(minecraft, riddenCreature, this.moveHotbarActive, this.ridingAimActive);
        if (riddenCreature != null && this.ridingAimActive) {
            this.cameraHandler.sendRidingAimUpdate(minecraft, riddenCreature, this.ridingAimMove);
        }

        //---disable sprint key when on sprint cooldown---
        if (riddenCreature != null && !riddenCreature.isSprinting()) minecraft.player.setSprinting(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRidingCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RiftCreature riddenCreature = this.updateRidingState(minecraft);
        CameraHandler.RidingCameraAngles cameraAngles = this.cameraHandler.setupRidingCamera(
                minecraft, riddenCreature, event.getEntity(), event.getRenderPartialTicks()
        );
        if (cameraAngles == null) return;
        event.setYaw(cameraAngles.yaw());
        event.setPitch(cameraAngles.pitch());
    }

    /**
     * handle fov change when aiming
     * */
    @SubscribeEvent
    public void onRidingCameraFov(EntityViewRenderEvent.FOVModifier event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RiftCreature riddenCreature = this.updateRidingState(minecraft);
        event.setFOV(this.cameraHandler.modifyRidingCameraFov(
                minecraft, riddenCreature, event.getEntity(),
                event.getRenderPartialTicks(), event.getFOV()
        ));
    }

    /**
     * all mousebound controls happen here
     * */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRidingMouseInput(MouseEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.currentScreen != null || !this.moveHotbarActive) return;

        RiftCreature riddenCreature = this.updateRidingState(minecraft);
        if (riddenCreature == null) return;
        int moveCount = riddenCreature.getCreatureMoves().getUsableMoves().size();
        if (moveCount == 0) return;

        //make scroll switch between moves
        if (event.getDwheel() != 0) {
            int nextMove = Math.floorMod(this.selectedRidingMove + (event.getDwheel() > 0 ? -1 : 1), moveCount);
            if (nextMove != this.selectedRidingMove) this.stopRidingAim();
            this.selectedRidingMove = nextMove;
            event.setCanceled(true);
        }

        //make left click use selected move
        if (event.getButton() == 0) {
            if (event.isButtonstate()) {
                this.usingRidingMove = true;
                RiftMessages.WRAPPER.sendToServer(new RiftRidingActionMessage(
                        RiftRidingActionMessage.Action.START_MOVE, this.selectedRidingMove, false
                ));
            }
            else this.releaseSelectedRidingMove();
            event.setCanceled(true);
        }
        //hold right click to aim moves that opt into ridden aiming
        else if (event.getButton() == 1 && !riddenCreature.isLeaping()) {
            CreatureMoveBuilder selectedMove = riddenCreature.getCreatureMoves().getUsableMoves().get(this.selectedRidingMove).getValue();
            if (this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature) && selectedMove.getRiddenAimingType() != RiddenAimingType.NONE) {
                if (event.isButtonstate()) {
                    this.ridingAimActive = true;
                    this.ridingAimMove = this.selectedRidingMove;
                    this.riddenLeapChargeStartTick = -1;
                    this.cameraHandler.beginRidingAim(riddenCreature, this.ridingAimMove);
                    riddenCreature.setRiddenAimingFromClient(true);
                    this.cameraHandler.sendRidingAimUpdate(minecraft, riddenCreature, this.ridingAimMove);
                }
                else {
                    CreatureMoveChargeupBuilder chargeupBuilder = selectedMove.getMoveChargeupBuilder();
                    if (this.usingRidingMove && chargeupBuilder != null && chargeupBuilder.getChargeUpWhileUse()) {
                        this.releaseSelectedRidingMove();
                    }
                    this.stopRidingAim();
                }
            }
            event.setCanceled(true);
        }
        //clicking middle mouse doesn't do anything beyond move selection via scrolling
        else if (event.getButton() == 2) {
            event.setCanceled(true);
        }
    }

    /**
     * show player party and ridden creature huds
     * */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTextOverlayRender(RenderGameOverlayEvent.Text event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.hideGUI) return;

        int width = event.getResolution().getScaledWidth();
        int height = event.getResolution().getScaledHeight();
        PlayerPartyHUD.renderPlayerParty(minecraft, width, height);

        RiftCreature riddenCreature = this.updateRidingState(minecraft);
        if (riddenCreature != null) {
            //exclusively for creatures that jump
            if (riddenCreature.getNavigationBuilder().getCanLeap()) {
                int chargeTicks = this.riddenLeapChargeStartTick < 0 ? 0 : Math.clamp(
                        minecraft.player.ticksExisted - this.riddenLeapChargeStartTick + 1,
                        0, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS
                );
                boolean riddenLeapPendingOrActive = riddenCreature.isRiddenLeapPendingOrActive();
                int leapCooldownTicks = riddenCreature.getLeapCooldown();
                if (chargeTicks == 0 && !riddenLeapPendingOrActive && leapCooldownTicks == 0) {
                    this.riddenLeapCooldownDisplayChargeTicks = 0;
                }
                int displayedChargeTicks = chargeTicks;
                if (displayedChargeTicks == 0 && riddenLeapPendingOrActive) {
                    displayedChargeTicks = this.riddenLeapCooldownDisplayChargeTicks;
                }

                this.ridingCreatureHUD.setJumpState(
                        displayedChargeTicks, leapCooldownTicks, this.riddenLeapCooldownDisplayChargeTicks
                );
            }

            int currentTick = minecraft.player.ticksExisted;
            if (riddenCreature.isSprinting()) {
                if (this.riddenSprintStartTick < 0) this.riddenSprintStartTick = currentTick;
            }
            else if (this.riddenSprintStartTick >= 0) {
                this.riddenSprintCooldownDisplayTicks = Math.clamp(
                        currentTick - this.riddenSprintStartTick + 1,
                        1, RiftCreatureSprintHelper.MAXIMUM_SPRINT_TICKS
                );
                this.riddenSprintStartTick = -1;
            }

            int sprintTicks = this.riddenSprintStartTick < 0 ? 0 : Math.clamp(
                    currentTick - this.riddenSprintStartTick + 1,
                    0, RiftCreatureSprintHelper.MAXIMUM_SPRINT_TICKS
            );
            int sprintCooldownTicks = riddenCreature.getSprintCooldown();
            if (sprintTicks == 0 && sprintCooldownTicks == 0) this.riddenSprintCooldownDisplayTicks = 0;
            this.ridingCreatureHUD.setSprintState(
                    sprintTicks, sprintCooldownTicks, this.riddenSprintCooldownDisplayTicks
            );

            this.ridingCreatureHUD.renderControls(
                    minecraft, riddenCreature, this.moveHotbarActive, this.ridingAimActive,
                    this.selectedRidingMove, width, height
            );
        }
    }

    /**
     * for completely custom hud stuff above hotbar
     * */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRidingOverlayRender(RenderGameOverlayEvent.Pre event) {
        RenderGameOverlayEvent.ElementType elementType = event.getType();
        if (elementType != RenderGameOverlayEvent.ElementType.HEALTH
                && elementType != RenderGameOverlayEvent.ElementType.FOOD
                && elementType != RenderGameOverlayEvent.ElementType.HEALTHMOUNT
                && elementType != RenderGameOverlayEvent.ElementType.HOTBAR
                && elementType != RenderGameOverlayEvent.ElementType.EXPERIENCE
        ) return;

        //get player and settings
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.hideGUI) return;

        RiftCreature riddenCreature = this.updateRidingState(minecraft);

        //hide vanilla mount health and replace it with creature stamina or player hunger
        if (elementType == RenderGameOverlayEvent.ElementType.HEALTHMOUNT) {
            if ((riddenCreature == null || !this.moveHotbarActive) && minecraft.ingameGUI instanceof GuiIngameForge guiIngameForge) {
                guiIngameForge.renderFood(event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
            }
            event.setCanceled(true);
            return;
        }

        if (riddenCreature == null) return;

        //hide player health and replace it with mounted creature health when in view moves view
        if (elementType == RenderGameOverlayEvent.ElementType.HEALTH && this.moveHotbarActive) {
            event.setCanceled(true);
            return;
        }

        //hide player hunger and experience when in view moves view
        if ((elementType == RenderGameOverlayEvent.ElementType.FOOD || elementType == RenderGameOverlayEvent.ElementType.EXPERIENCE)
                && this.moveHotbarActive
        ) {
            event.setCanceled(true);
            return;
        }

        //show move hotbar when riding a creature and when it is set to be shown
        if (elementType == RenderGameOverlayEvent.ElementType.HOTBAR && this.moveHotbarActive) {
            int width = event.getResolution().getScaledWidth();
            int height = event.getResolution().getScaledHeight();
            this.ridingCreatureHUD.renderCreatureHealth(minecraft, riddenCreature, width, height);
            this.ridingCreatureHUD.renderStamina(minecraft, riddenCreature, width, height);
            this.ridingCreatureHUD.renderMoveHotbar(
                    minecraft, riddenCreature, this.selectedRidingMove,
                    width, height
            );
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onOverlayPostRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        RiftCreature riddenCreature = this.updateRidingState(minecraft);
        int screenWidth = event.getResolution().getScaledWidth();
        int screenHeight = event.getResolution().getScaledHeight();

        //render aim crosshair
        this.cameraHandler.renderRidingAimCrosshair(minecraft, riddenCreature, screenWidth, screenHeight);
        if (minecraft.player == null || minecraft.gameSettings.hideGUI || minecraft.objectMouseOver == null) return;

        //render tame progress hud
        TameProgressHUD.renderTamingProgress(minecraft, screenWidth, screenHeight);
    }

    //-----helpers-----
    private void releaseSelectedRidingMove() {
        RiftMessages.WRAPPER.sendToServer(new RiftRidingActionMessage(
                RiftRidingActionMessage.Action.RELEASE_MOVE, this.selectedRidingMove, false
        ));
        this.usingRidingMove = false;
    }

    private void stopRidingAim() {
        if (!this.ridingAimActive) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player != null && minecraft.player.getRidingEntity() instanceof RiftCreature creature) {
            creature.setRiddenAimingFromClient(false);
        }
        if (minecraft.getConnection() != null) {
            RiftMessages.WRAPPER.sendToServer(new RiftRidingAimMessage(
                    false, this.ridingAimMove, -1, 0D, 0D, 0D, null, null
            ));
        }
        this.cameraHandler.endRidingAim();
        this.ridingAimActive = false;
        this.ridingAimMove = -1;
    }

    @Nullable
    private RiftCreature updateRidingState(Minecraft minecraft) {
        RiftCreature riddenCreature = minecraft.player != null
                && minecraft.player.getRidingEntity() instanceof RiftCreature creature
                && creature.getControllingPassenger() == minecraft.player
                ? creature : null;
        int currentRiddenCreatureId = riddenCreature == null ? -1 : riddenCreature.getEntityId();

        if (currentRiddenCreatureId != this.riddenCreatureId) {
            this.stopRidingAim();
            this.riddenCreatureId = currentRiddenCreatureId;
            this.moveHotbarActive = false;
            this.usingRidingMove = false;
            this.selectedRidingMove = 0;
            this.riddenLeapChargeStartTick = -1;
            this.riddenLeapCooldownDisplayChargeTicks = 0;
            this.riddenSprintStartTick = -1;
            this.riddenSprintCooldownDisplayTicks = riddenCreature != null && riddenCreature.getSprintCooldown() > 0
                    ? RiftCreatureSprintHelper.MAXIMUM_SPRINT_TICKS : 0;
        }

        if (riddenCreature != null) {
            int moveCount = riddenCreature.getCreatureMoves().getUsableMoves().size();
            this.selectedRidingMove = moveCount == 0 ? 0 : Math.clamp(this.selectedRidingMove, 0, moveCount - 1);
        }
        return riddenCreature;
    }
}
