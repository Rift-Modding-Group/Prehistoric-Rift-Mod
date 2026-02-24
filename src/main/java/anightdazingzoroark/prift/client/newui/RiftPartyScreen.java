package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.data.PlayerGuiData;
import anightdazingzoroark.prift.client.newui.panel.ModularPanelExitAffectable;
import anightdazingzoroark.prift.client.newui.sync.CreatureSwapInfoSyncValue;
import anightdazingzoroark.prift.client.newui.sync.PlayerPartySyncValue;
import anightdazingzoroark.prift.client.newui.widget.PaddedGrid;
import anightdazingzoroark.prift.client.newui.widget.PartyMemberButtonWidget;
import anightdazingzoroark.prift.client.newui.widget.SideButton;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Grid;
import net.minecraft.init.Items;

import java.util.function.Function;

public class RiftPartyScreen {
    public static ModularPanel build(PlayerGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disable();

        PlayerPartySyncValue playerParty = new PlayerPartySyncValue(
                data.getPlayer(),
                () -> data.playerParty,
                value -> data.playerParty = value
        );
        syncManager.syncValue("playerPartySynced", playerParty);

        BooleanSyncValue isCreatureSwitching = new BooleanSyncValue(() -> data.isMoveSwitchingUI, value -> data.isMoveSwitchingUI = value);
        syncManager.syncValue("creatureSwitching", isCreatureSwitching);

        CreatureSwapInfoSyncValue swapInfo = new CreatureSwapInfoSyncValue(
                () -> data.creatureSwapInfo,
                value -> data.creatureSwapInfo = value
        );
        syncManager.syncValue("creatureSwapInfo", swapInfo);

        return new ModularPanelExitAffectable(UIPanelNames.PARTY_SCREEN)
                .onEscPressed(new Function<ModularPanelExitAffectable, Boolean>() {
                    @Override
                    public Boolean apply(ModularPanelExitAffectable panel) {
                        if (!this.hasOpenedDropdown(panel)) return false;
                        this.closeAllPartyPanels(panel);
                        return true;
                    }

                    private boolean hasOpenedDropdown(ModularPanelExitAffectable panel) {
                        if (panel == null || !panel.isValid()) return false;
                        PaddedGrid grid = this.getGrid(panel);
                        if (grid == null || !grid.isValid()) return false;

                        for (IWidget child : grid.getChildren()) {
                            if (child instanceof PartyMemberButtonWidget partyMemberButton) {
                                if (partyMemberButton.getButtonMenu() == null) continue;
                                if (!partyMemberButton.getButtonMenu().getPanel().isValid()) continue;

                                if (partyMemberButton.getButtonMenu().getPanel().isOpen()) return true;
                            }
                        }
                        return false;
                    }

                    private void closeAllPartyPanels(ModularPanelExitAffectable panel) {
                        if (panel == null || !panel.isValid()) return;
                        PaddedGrid grid = this.getGrid(panel);
                        if (grid == null || !grid.isValid()) return;

                        for (IWidget child : grid.getChildren()) {
                            if (child instanceof PartyMemberButtonWidget partyMemberButton) {
                                if (partyMemberButton.getButtonMenu() == null) continue;
                                if (!partyMemberButton.getButtonMenu().getPanel().isValid()) continue;

                                partyMemberButton.closeButtonMenu();
                                //partyMemberButton.getButtonMenu().getPanel().closeIfOpen();
                            }
                        }
                    }

                    private PaddedGrid getGrid(IWidget panel) {
                        if (panel == null || !panel.isValid()) return null;
                        for (IWidget child : panel.getChildren()) {
                            //search in self
                            if (child instanceof PaddedGrid grid) return grid;

                            //search in children too
                            IWidget recursionRes = this.getGrid(child);
                            if (recursionRes instanceof PaddedGrid recursionResGrid) return recursionResGrid;
                        }
                        return null;
                    }
                })
                .width(180).coverChildrenHeight()
                .child(new Column().coverChildren()
                        .leftRel(0f, 4, 1f)
                        .child(new SideButton()
                                .overlay(new ItemDrawable(Items.BOOK).asIcon())
                                .onMousePressed(button -> {
                                    PlayerUIHelper.openUI(data.getPlayer(), UIPanelNames.JOURNAL_SCREEN);
                                    return true;
                                })
                                .addTooltipElement(IKey.lang("party.open_journal"))
                                .tab(GuiTextures.TAB_LEFT, -1)
                        )
                )
                .child(new ParentWidget<>().padding(7, 7)
                        .coverChildren()
                        .child(new Column().coverChildren()
                                .childPadding(5)
                                .child(new ParentWidget<>().coverChildrenHeight().widthRel(1f)
                                        .child(IKey.lang("party.label").asWidget().align(Alignment.CenterLeft))
                                        .child(new ToggleButton().overlay()
                                                .value(new BoolValue.Dynamic(
                                                        isCreatureSwitching::getBoolValue,
                                                        isCreatureSwitching::setBoolValue
                                                ))
                                                .addTooltipElement(IKey.lang("party.swap_creatures"))
                                                .overlay(GuiTextures.REVERSE.asIcon().size(12))
                                                .size(12).right(0)
                                        )
                                )
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                2, PlayerPartyHelper.maxSize,
                                                index -> new PartyMemberButtonWidget(index, playerParty, isCreatureSwitching, swapInfo)
                                        ))
                                        .padding(4)
                                )
                        )
                );
    }
}
