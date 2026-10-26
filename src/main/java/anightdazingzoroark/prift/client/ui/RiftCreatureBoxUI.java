package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.widgets.CreatureBoxSlot;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties.StoragePosition;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

public class RiftCreatureBoxUI {
    @NotNull
    public static ModularPanel show(@NotNull CreatureBoxGuiData data, @NotNull PanelSyncManager syncManager, @NotNull UISettings settings) {
        settings.getRecipeViewerSettings().disable();
        PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(data.getPlayer());
        PlayerPartyProperties party = PlayerPartyProperties.get(data.getPlayer());
        if (boxes == null || party == null) throw new IllegalArgumentException("Player storage is unavailable!");
        IPanelHandler inventoryWarning = RiftConfirmationUI.register(syncManager, "gui.prift.box.drop_warning", () -> {
            if (data.pendingCreature == null || data.pendingSource == null || data.pendingDestination == null
                    || !CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) return;
            PlayerCreatureBoxProperties.DepositResult result = PlayerCreatureBoxProperties.swapCreatures(
                    data.getPlayer(), data.pendingSource, data.pendingDestination,
                    data.pendingCreature, data.pendingDestinationCreature, true
            );
            data.pendingCreature = null;
            if (result != PlayerCreatureBoxProperties.DepositResult.SUCCESS) {
                data.getPlayer().sendStatusMessage(new TextComponentTranslation("gui.prift.box.transfer_failed"), false);
            }
        }, () -> data.pendingCreature = null);
        IPanelHandler rename = syncManager.syncedPanel("rename_box", true, (panelSync, handler) -> {
            panelSync.syncValue("draft", new StringSyncValue(() -> data.renameDraft, value -> data.renameDraft = value));
            return new ModularPanel("rename_box") {
                @Override
                public boolean disablePanelsBelow() {
                    return true;
                }
            }.size(220, 100)
                    .child(Flow.column().sizeRel(1f).padding(9).childPadding(8)
                            .child(IKey.lang("gui.prift.box.rename").asWidget().height(12))
                            .child(new TextFieldWidget().syncHandler("draft").setMaxLength(32).autoUpdateOnChange(true)
                                    .hintText(IKey.lang("gui.prift.box.default_name", data.selectedBox + 1).get())
                                    .widthRel(1f).height(20))
                            .child(Flow.row().widthRel(1f).height(20).childPadding(6)
                                    .child(new ButtonWidget<>().expanded().height(20).overlay(IKey.lang("gui.prift.box.save"))
                                            .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                                                if (!mouse.isClient() && handler.isPanelOpen()
                                                        && CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) {
                                                    boxes.setBoxName(data.selectedBox, data.renameDraft);
                                                    handler.closePanel();
                                                }
                                            })))
                                    .child(new ButtonWidget<>().expanded().height(20).overlay(IKey.lang("gui.prift.box.cancel"))
                                            .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                                                if (!mouse.isClient()) handler.closePanel();
                                            })))
                            )
                    );
        });
        syncManager.syncValue("swapping", new BooleanSyncValue(() -> data.swapping, value -> {
            if (data.swapping == value) return;
            data.swapping = value;
            data.selectedStorage = -2;
            data.selectedSlot = -1;
            data.selectedCreature = null;
            data.pendingCreature = null;
        }));
        syncManager.syncValue("selected_storage", new IntSyncValue(() -> data.selectedStorage, value -> {
            if (data.isClient()) data.selectedStorage = value;
        }));
        syncManager.syncValue("selected_slot", new IntSyncValue(() -> data.selectedSlot, value -> {
            if (data.isClient()) data.selectedSlot = value;
        }));
        return new ModularPanel("rift_creature_box").size(302, 200)
                .background(IDrawable.EMPTY).disableHoverBackground()
                // Draw the party panel first so the main panel covers its right edge.
                .child(Flow.column().size(78, 120).left(22).top(8)
                        .padding(2, 16, 6, 6).childPadding(3)
                        .background(GuiTextures.MC_BACKGROUND)
                        .child(IKey.lang("gui.prift.party.title").scale(0.75f).asWidget().widthRel(1f).height(15))
                        .child(new Grid().coverChildren().mapTo(2, PlayerPartyProperties.MAX_SIZE,
                                index -> new CreatureBoxSlot(data, -1, index, inventoryWarning).margin(1)))
                )
                .child(Flow.column().size(214, 200).left(88).top(0)
                        .padding(8).childPadding(4)
                        .background(GuiTextures.MC_BACKGROUND)
                        .child(Flow.row().widthRel(1f).height(20).childPadding(2)
                                .children(3, index -> {
                                    if (index == 1) {
                                        return new ButtonWidget<>().widthRelOffset(1f, -60).height(20)
                                                .background((context, x, y, width, height, theme) -> {
                                                    new Rectangle().color(0xFF555555).cornerRadius(6)
                                                            .draw(context, x, y, width, height, theme);
                                                    new Rectangle().color(0xFFC6C6C6).cornerRadius(5)
                                                            .draw(context, x + 1, y + 1, width - 2, height - 2, theme);
                                                }).disableHoverBackground()
                                                .child(IKey.dynamic(() -> {
                                                    String name = boxes.getBoxName(data.selectedBox);
                                                    return name.isEmpty() ? IKey.lang("gui.prift.box.default_name", data.selectedBox + 1).get() : name;
                                                }).scale(0.8f).asWidget().sizeRel(1f).textAlign(Alignment.CENTER))
                                                .addTooltipLine(IKey.lang("gui.prift.box.rename_tooltip"))
                                                .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                                                    if (!mouse.isClient() && CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) {
                                                        data.renameDraft = boxes.getBoxName(data.selectedBox);
                                                        RiftConfirmationUI.open(rename);
                                                    }
                                                }));
                                    }
                                    return new ButtonWidget<>().size(18, 20).overlay(IKey.str(index == 0 ? "<" : ">"))
                                            .addTooltipLine(IKey.lang(index == 0 ? "gui.prift.box.previous" : "gui.prift.box.next"))
                                            .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                                                if (mouse.isClient() || !CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) return;
                                                CreatureBoxGuiData next = new CreatureBoxGuiData(data.getPlayer(), data.getBlockPos(),
                                                        Math.floorMod(data.selectedBox + (index == 0 ? -1 : 1), PlayerCreatureBoxProperties.BOX_COUNT));
                                                next.swapping = data.swapping;
                                                next.selectedStorage = data.selectedStorage;
                                                next.selectedSlot = data.selectedSlot;
                                                next.selectedCreature = data.selectedCreature;
                                                GuiManager.open(CreatureBoxGuiFactory.INSTANCE, next, (EntityPlayerMP) data.getPlayer());
                                            }));
                                })
                                .child(new CycleButtonWidget().syncHandler("swapping")
                                        .stateBackground(false, GuiTextures.MC_BUTTON)
                                        .stateBackground(true, GuiTextures.MC_BUTTON_PRESSED)
                                        .stateHoverBackground(false, GuiTextures.MC_BUTTON_HOVERED)
                                        .stateHoverBackground(true, GuiTextures.MC_BUTTON_HOVERED_PRESSED)
                                        .size(18, 20).overlay(GuiTextures.REFRESH.asIcon().size(12))
                                        .addTooltipLine(IKey.lang("gui.prift.box.swap")))
                        )
                        .child(Flow.column().widthRel(1f).height(160).padding(4)
                                .background((context, x, y, width, height, theme) -> {
                                    Platform.setupDrawColor();
                                    Platform.startDrawing(Platform.DrawMode.TRIANGLE_STRIP, Platform.VertexFormat.POS_COLOR, buffer -> {
                                        for (int corner = 0; corner < 4; corner++) {
                                            double centerX = corner < 2 ? x + width - 7 : x + 7;
                                            double centerY = corner == 1 || corner == 2 ? y + height - 7 : y + 7;
                                            for (int segment = 0; segment <= 6; segment++) {
                                                double angle = Math.toRadians(corner * 90 - 90 + segment * 15);
                                                double cosine = Math.cos(angle);
                                                double sine = Math.sin(angle);
                                                buffer.pos(centerX + cosine * 7, centerY + sine * 7, 0)
                                                        .color(0, 0, 0, 255).endVertex();
                                                buffer.pos(centerX + cosine * 6, centerY + sine * 6, 0)
                                                        .color(0, 0, 0, 255).endVertex();
                                            }
                                        }
                                        buffer.pos(x + width - 7, y, 0).color(0, 0, 0, 255).endVertex();
                                        buffer.pos(x + width - 7, y + 1, 0).color(0, 0, 0, 255).endVertex();
                                    });
                                })
                                .child(new Grid().coverChildren().mapTo(5, PlayerCreatureBoxProperties.BOX_SIZE,
                                        index -> new CreatureBoxSlot(data, data.selectedBox, index, inventoryWarning).margin(2)))
                        )
                );
    }

    public static void selectSlot(@NotNull CreatureBoxGuiData data, int storage, int slot, @NotNull IPanelHandler inventoryWarning) {
        if (!data.swapping) return;
        if (!data.isClient() && !CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) return;
        PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(data.getPlayer());
        PlayerPartyProperties party = PlayerPartyProperties.get(data.getPlayer());
        if (boxes == null || party == null) return;
        CreatureNBT creature = storage == -1 ? party.getCreatureStorage().getCreature(slot) : boxes.getBox(storage).getCreature(slot);
        if (data.selectedSlot < 0) {
            if (creature.nbtTagCompound().isEmpty()) return;
            data.selectedStorage = storage;
            data.selectedSlot = slot;
            data.selectedCreature = creature.getUniqueID();
            return;
        }
        if ((data.selectedStorage != storage || data.selectedSlot != slot) && !data.isClient() && data.selectedCreature != null) {
            StoragePosition first = new StoragePosition(data.selectedStorage, data.selectedSlot);
            StoragePosition second = new StoragePosition(storage, slot);
            PlayerCreatureBoxProperties.DepositResult result = PlayerCreatureBoxProperties.swapCreatures(
                    data.getPlayer(), first, second, data.selectedCreature,
                    creature.nbtTagCompound().isEmpty() ? null : creature.getUniqueID(), false
            );
            if (result == PlayerCreatureBoxProperties.DepositResult.NEEDS_CONFIRMATION) {
                data.pendingSource = first;
                data.pendingDestination = second;
                data.pendingCreature = data.selectedCreature;
                data.pendingDestinationCreature = creature.nbtTagCompound().isEmpty() ? null : creature.getUniqueID();
                RiftConfirmationUI.open(inventoryWarning);
            }
            else if (result != PlayerCreatureBoxProperties.DepositResult.SUCCESS) {
                data.getPlayer().sendStatusMessage(new TextComponentTranslation("gui.prift.box.transfer_failed"), false);
            }
        }
        data.selectedStorage = -2;
        data.selectedSlot = -1;
        data.selectedCreature = null;
    }
}
