package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.widgets.CreatureStorageGrid;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData.RevivalSnapshot;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxTileEntity;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureBoxRecovery;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer.Result;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorageTransfer.Slot;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.util.RiftUtil;
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
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

public class RiftCreatureBoxUI {
    @NotNull
    public static ModularPanel show(@NotNull CreatureBoxGuiData data, @NotNull PanelSyncManager syncManager, @NotNull UISettings settings) {
        settings.getRecipeViewerSettings().disable();

        PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(data.getPlayer());
        PlayerPartyProperties party = PlayerPartyProperties.get(data.getPlayer());
        if (boxes == null || party == null) throw new IllegalArgumentException("Player storage is unavailable!");
        if (!(data.getWorld().getTileEntity(data.getBlockPos()) instanceof CreatureBoxTileEntity tile)) {
            throw new IllegalArgumentException("Creature box storage is unavailable!");
        }
        boxes.updateBox(data.selectedBox, true);
        // Apply every displayed timer atomically; the client never uses its own world clock.
        syncManager.syncValue("revival", GenericSyncValue.builder(RevivalSnapshot.class)
                .getter(() -> {
                    if (data.isClient()) return data.revivalSnapshot;
                    return RevivalSnapshot.capture(boxes.getBox(data.selectedBox), CreatureBoxRecovery.currentTime(data.getPlayer()));
                })
                .setter(snapshot -> {
                    if (data.isClient()) data.revivalSnapshot = snapshot;
                })
                .serializer(RevivalSnapshot::write).deserializer(RevivalSnapshot::read)
                .copyImmutable().build());
        IPanelHandler inventoryWarning = RiftConfirmationUI.register(syncManager, "gui.prift.box.drop_warning", () -> {
            if (data.pendingCreature == null || data.pendingSource == null || data.pendingDestination == null
                    || !CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) return;
            Result result = CreatureStorageTransfer.swap(
                    data.getPlayer(), data.pendingSource, data.pendingDestination,
                    data.pendingCreature, data.pendingDestinationCreature, true
            );
            data.pendingCreature = null;
            if (result != Result.SUCCESS) {
                data.getPlayer().sendStatusMessage(new TextComponentTranslation(switch (result) {
                    case REVIVING -> "gui.prift.box.reviving";
                    case NO_DEPLOYMENT_SPACE -> "gui.prift.box.no_deployment_space";
                    default -> "gui.prift.box.transfer_failed";
                }), false);
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
                                                        && CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)
                                                ) {
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
            data.selectedStorage = "";
            data.selectedSlot = -1;
            data.selectedCreature = null;
            data.pendingCreature = null;
        }));
        syncManager.syncValue("selected_storage", new StringSyncValue(() -> data.selectedStorage, value -> {
            if (data.isClient()) data.selectedStorage = value;
        }));
        syncManager.syncValue("selected_slot", new IntSyncValue(() -> data.selectedSlot, value -> {
            if (data.isClient()) data.selectedSlot = value;
        }));
        return new ModularPanel("rift_creature_box").size(392, 200)
                .background(IDrawable.EMPTY).disableHoverBackground()
                //draw the side panels first so the main panel covers their joining edges
                .child(Flow.column().size(78, 120).left(22).top(8)
                        .padding(2, 16, 6, 6).childPadding(3)
                        .background(GuiTextures.MC_BACKGROUND)
                        .child(IKey.lang("gui.prift.party.title").scale(0.75f).asWidget().widthRel(1f).height(15))
                        .child(new CreatureStorageGrid(
                                data, CreatureStorageGrid.syncValue(party::getCreatureStorage),
                                2, 26, inventoryWarning
                        ))
                )
                .child(Flow.column().size(100, 132).left(292).top(8)
                        .padding(14, 6, 6, 6).childPadding(3)
                        .background(GuiTextures.MC_BACKGROUND)
                        .child(IKey.lang("gui.prift.box.deployed").scale(0.65f).asWidget().widthRel(1f).height(15))
                        .child(new CreatureStorageGrid(
                                data, CreatureStorageGrid.syncValue(tile::getDeployedCreatures),
                                4, 18, inventoryWarning
                        ))
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
                                .child(new CreatureStorageGrid(
                                        data, CreatureStorageGrid.syncValue(() -> boxes.getBox(data.selectedBox)),
                                        5, 34, inventoryWarning
                                ).minElementMargin(2))
                        )
                );
    }

    public static void openCreaturePage(@NotNull CreatureBoxGuiData data, @NotNull CreatureStorage storage, int slot, int page) {
        if (!data.isClient() || data.swapping || slot < 0 || slot >= storage.getSize() || page < 1 || page > 3) return;
        CreatureNBT stored = storage.getCreature(slot);
        if (stored.nbtTagCompound().isEmpty() || !stored.isOwner(data.getPlayer())) return;
        RiftCreatureGuiData details;
        switch (storage.getIdentifier()) {
            case PlayerPartyProperties.STORAGE_IDENTIFIER -> {
                Entity entity = RiftUtil.getEntityWithUUID(data.getWorld(), stored.getUniqueID());
                IRiftCreature creature = entity instanceof RiftCreature deployed ? deployed : stored;
                details = new RiftCreatureGuiData(data.getPlayer(), creature, slot, page);
            }
            case CreatureBoxTileEntity.STORAGE_IDENTIFIER -> {
                Entity entity = RiftUtil.getEntityWithUUID(data.getWorld(), stored.getUniqueID());
                IRiftCreature creature = entity instanceof RiftCreature deployed ? deployed : stored;
                details = new RiftCreatureGuiData(data.getPlayer(), creature, slot, page, data.getBlockPos());
            }
            default -> {
                int box = PlayerCreatureBoxProperties.getBoxIndex(storage.getIdentifier());
                if (box < 0) return;
                details = new RiftCreatureGuiData(data.getPlayer(), stored, box, slot, page, data.getBlockPos());
            }
        }
        GuiManager.openFromClient(RiftCreatureGuiFactory.INSTANCE, details);
    }

    public static void selectSlot(@NotNull CreatureBoxGuiData data, @NotNull CreatureStorage displayedStorage, int slot,
                                  @NotNull IPanelHandler inventoryWarning) {
        if (!data.swapping) return;
        if (!data.isClient() && !CreatureBoxGuiFactory.INSTANCE.canInteractWith(data.getPlayer(), data)) return;
        String storage = displayedStorage.getIdentifier();
        CreatureStorage creatureStorage = displayedStorage;
        if (!data.isClient()) {
            PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(data.getPlayer());
            PlayerPartyProperties party = PlayerPartyProperties.get(data.getPlayer());
            if (boxes == null || party == null) return;
            if (CreatureBoxTileEntity.STORAGE_IDENTIFIER.equals(storage)) {
                if (!(data.getWorld().getTileEntity(data.getBlockPos()) instanceof CreatureBoxTileEntity tile)) return;
                creatureStorage = tile.getDeployedCreatures();
            }
            else if (PlayerPartyProperties.STORAGE_IDENTIFIER.equals(storage)) creatureStorage = party.getCreatureStorage();
            else {
                int box = PlayerCreatureBoxProperties.getBoxIndex(storage);
                if (box < 0) return;
                creatureStorage = boxes.getBox(box);
            }
        }
        if (slot < 0 || slot >= creatureStorage.getSize()) return;
        CreatureNBT creature = creatureStorage.getCreature(slot);
        if (data.selectedSlot < 0) {
            if (creature.nbtTagCompound().isEmpty()) return;
            data.selectedStorage = storage;
            data.selectedSlot = slot;
            data.selectedCreature = creature.getUniqueID();
            return;
        }
        if ((!data.selectedStorage.equals(storage) || data.selectedSlot != slot) && !data.isClient() && data.selectedCreature != null) {
            Slot first = Slot.fromStorageId(data.selectedStorage, data.selectedSlot, data.getBlockPos());
            Slot second = Slot.fromStorageId(storage, slot, data.getBlockPos());
            Result result = CreatureStorageTransfer.swap(
                    data.getPlayer(), first, second, data.selectedCreature,
                    creature.nbtTagCompound().isEmpty() ? null : creature.getUniqueID(), false
            );
            if (result == Result.NEEDS_CONFIRMATION) {
                data.pendingSource = first;
                data.pendingDestination = second;
                data.pendingCreature = data.selectedCreature;
                data.pendingDestinationCreature = creature.nbtTagCompound().isEmpty() ? null : creature.getUniqueID();
                RiftConfirmationUI.open(inventoryWarning);
            }
            else if (result != Result.SUCCESS) {
                data.getPlayer().sendStatusMessage(new TextComponentTranslation(switch (result) {
                    case REVIVING -> "gui.prift.box.reviving";
                    case NO_DEPLOYMENT_SPACE -> "gui.prift.box.no_deployment_space";
                    default -> "gui.prift.box.transfer_failed";
                }), false);
            }
        }
        data.selectedStorage = "";
        data.selectedSlot = -1;
        data.selectedCreature = null;
    }
}
