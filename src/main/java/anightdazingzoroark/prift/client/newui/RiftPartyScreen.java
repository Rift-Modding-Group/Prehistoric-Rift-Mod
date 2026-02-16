package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.data.PlayerGuiData;
import anightdazingzoroark.prift.client.newui.sync.CreatureSwapInfoSyncValue;
import anightdazingzoroark.prift.client.newui.sync.PlayerPartySyncValue;
import anightdazingzoroark.prift.client.newui.widget.PaddedGrid;
import anightdazingzoroark.prift.client.newui.widget.PartyMemberButtonWidget;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
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

        return new ModularPanel(UIPanelNames.PARTY_SCREEN)
                .coverChildren().padding(7, 7)
                .child(new Column().coverChildren()
                        .childPadding(5)
                        .child(new ParentWidget<>().coverChildrenHeight().widthRel(1f)
                                .child(IKey.lang("journal.party_label.party").asWidget().align(Alignment.CenterLeft))
                                .child(new ToggleButton().overlay()
                                        .value(new BoolValue.Dynamic(
                                                isCreatureSwitching::getBoolValue,
                                                isCreatureSwitching::setBoolValue
                                        ))
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
                );
    }
}
