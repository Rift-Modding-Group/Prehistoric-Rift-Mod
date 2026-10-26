package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.client.ui.RiftCreatureBoxUI;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiData;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.util.RiftUtil;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.menu.ContextMenuButton;
import com.cleanroommc.modularui.widgets.menu.Menu;
import com.cleanroommc.modularui.widgets.menu.MenuPanel;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Rounded creature cards share the party screen's colors and context-menu interaction.
 * */
public class CreatureBoxSlot extends ContextMenuButton<CreatureBoxSlot> {
    private final CreatureBoxGuiData data;
    private final int storage;
    private final int index;
    private final boolean openRight;
    private final boolean openUp;
    @Nullable
    private IPanelHandler hoverInfo;

    public CreatureBoxSlot(@NotNull CreatureBoxGuiData data, int storage, int index, @NotNull IPanelHandler inventoryWarning) {
        super("box_creature_" + storage + "_" + index);
        this.data = data;
        this.storage = storage;
        this.index = index;
        this.openRight = storage == -1 ? index % 2 == 0 : index % 5 < 2;
        this.openUp = storage == -1 ? index >= 4 : index >= 10;
        Direction menuDirection;
        if (this.openRight) menuDirection = this.openUp ? Direction.RIGHT_UP : Direction.RIGHT_DOWN;
        else menuDirection = this.openUp ? Direction.LEFT_UP : Direction.LEFT_DOWN;
        this.setSyncOrValue(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                    RiftCreatureBoxUI.selectSlot(data, storage, index, inventoryWarning);
        }));
        this.requiresClick().size(storage == -1 ? 26 : 34).disableHoverBackground()
                .direction(menuDirection)
                .menu(new Menu<>().width(78).coverChildrenHeight()
                        .child(Flow.column().widthRel(1f).coverChildrenHeight()
                                .children(3, page -> new ButtonWidget<>().widthRel(1f).height(18)
                                        .child(IKey.lang(switch (page) {
                                            case 0 -> "gui.prift.party.dropdown.settings";
                                            case 1 -> "gui.prift.party.dropdown.summary";
                                            default -> "gui.prift.party.dropdown.moves";
                                        }).scale(0.65f).asWidget().sizeRel(1f).textAlign(Alignment.CENTER))
                                        .onMousePressed(button -> {
                                            this.closeMenu(false);
                                            CreatureNBT stored = this.getStoredCreature();
                                            if (data.swapping || stored.nbtTagCompound().isEmpty()) return true;
                                            RiftCreatureGuiData details;
                                            if (storage == -1) {
                                                Entity entity = RiftUtil.getEntityWithUUID(data.getWorld(), stored.getUniqueID());
                                                IRiftCreature creature = entity instanceof RiftCreature deployed ? deployed : stored;
                                                details = new RiftCreatureGuiData(data.getPlayer(), creature, index, page + 1);
                                            }
                                            else {
                                                details = new RiftCreatureGuiData(data.getPlayer(), stored, storage, index, page + 1, data.getBlockPos());
                                            }
                                            GuiManager.openFromClient(RiftCreatureGuiFactory.INSTANCE, details);
                                            return true;
                                        }))
                        ));
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(InteractionSyncHandler.class);
    }

    @NotNull
    private CreatureNBT getStoredCreature() {
        if (this.storage == -1) {
            PlayerPartyProperties party = PlayerPartyProperties.get(this.data.getPlayer());
            return party == null ? CreatureNBT.EMPTY_NBT : party.getCreatureStorage().getCreature(this.index);
        }
        PlayerCreatureBoxProperties boxes = PlayerCreatureBoxProperties.get(this.data.getPlayer());
        return boxes == null ? CreatureNBT.EMPTY_NBT : boxes.getBox(this.storage).getCreature(this.index);
    }

    @Nullable
    private IRiftCreature getDisplayedCreature() {
        CreatureNBT stored = this.getStoredCreature();
        if (stored.nbtTagCompound().isEmpty()) return null;
        if (this.storage == -1) {
            Entity entity = RiftUtil.getEntityWithUUID(this.data.getWorld(), stored.getUniqueID());
            if (entity instanceof RiftCreature creature && !creature.isDead) return creature;
        }
        return stored;
    }

    @Override
    public void onMouseEnterArea() {
        super.onMouseEnterArea();
        if (this.getDisplayedCreature() == null || this.isOpen()) return;
        if (this.hoverInfo == null) {
            this.hoverInfo = IPanelHandler.simple(this.getPanel(), (parent, player) ->
                    new MenuPanel(this.getName() + "_info", new CreatureBoxInfo(
                            this, this::getDisplayedCreature, this.openRight, this.openUp)), true);
        }
        this.hoverInfo.openPanel();
    }

    @Override
    public void onMouseLeaveArea() {
        super.onMouseLeaveArea();
        if (this.hoverInfo != null) this.hoverInfo.closePanel();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.data.swapping && this.isOpen()) this.closeMenu(false);
        if (this.hoverInfo != null && (this.isOpen() || this.getDisplayedCreature() == null)) this.hoverInfo.closePanel();
    }

    @Override
    @NotNull
    public Result onMousePressed(int mouseButton) {
        if (this.hoverInfo != null) this.hoverInfo.closePanel();
        if (this.data.swapping) {
            ((InteractionSyncHandler) this.getSyncHandler()).onMousePressed(mouseButton);
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        if (this.getStoredCreature().nbtTagCompound().isEmpty()) return Result.ACCEPT;
        Interactable.playButtonClickSound();
        return super.onMousePressed(mouseButton);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());
        CreatureNBT creature = this.getStoredCreature();
        boolean empty = creature.nbtTagCompound().isEmpty();
        boolean selected = this.data.swapping && this.data.selectedStorage == this.storage && this.data.selectedSlot == this.index;
        int borderColor = 0xFF000000;
        if (selected) borderColor = 0xFFFFD54F;
        else if (this.isHovering()) borderColor = 0xFFFFFFFF;
        int fillColor = empty ? 0xFF555555 : 0xFFC6C6C6;
        if (!empty && creature.getHealth() <= 0f) fillColor = 0xFFF33F3F;
        else if (!empty && this.storage == -1 && creature.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) fillColor = 0xFF208620;
        int size = this.getArea().width;
        int radius = this.storage == -1 ? 5 : 6;
        int iconSize = this.storage == -1 ? 18 : 24;
        int iconOffset = (size - iconSize) / 2;
        new Rectangle().color(borderColor).cornerRadius(radius).draw(context, 0, 0, size, size, theme);
        new Rectangle().color(fillColor).cornerRadius(radius - 1).draw(context, 1, 1, size - 2, size - 2, theme);
        if (!empty) CreatureIconTextures.getTextureForCreature(creature).draw(context, iconOffset, iconOffset, iconSize, iconSize, theme);
        if (!empty && this.storage >= 0) {
            CreatureBoxGuiData.RevivalCountdown countdown = this.data.revivalSnapshot.countdowns().get(this.index);
            if (countdown.seconds() > 0 && creature.getUniqueID().equals(countdown.creature())) {
                String seconds = Integer.toString(countdown.seconds() % 60);
                if (countdown.seconds() % 60 < 10) seconds = "0" + seconds;
                new Rectangle().color(0xC0000000).cornerRadius(2).draw(context, 1, size / 2 - 5, size - 2, 10, theme);
                IKey.str(countdown.seconds() / 60 + ":" + seconds).color(0xFFFFFFFF).scale(0.65f)
                        .alignment(Alignment.CENTER).draw(context, 1, size / 2 - 5, size - 2, 10, theme);
            }
        }
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFallback();
    }
}
