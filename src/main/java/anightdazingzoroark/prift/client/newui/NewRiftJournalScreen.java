package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.data.PlayerGuiData;
import anightdazingzoroark.prift.client.newui.panel.ModularPanelExitAffectable;
import anightdazingzoroark.prift.client.newui.sync.NullableEnumSyncValue;
import anightdazingzoroark.prift.client.newui.sync.PlayerJournalProgressSyncValue;
import anightdazingzoroark.prift.client.newui.widget.JournalLeftPageWidget;
import anightdazingzoroark.prift.client.newui.widget.JournalRightPageWidget;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;

public class NewRiftJournalScreen {
    public static ModularPanel build(PlayerGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disable();

        //sync values
        PlayerJournalProgressSyncValue journalProgress = new PlayerJournalProgressSyncValue(
                data.getPlayer(),
                () -> data.playerJournalProgress,
                value -> data.playerJournalProgress = value
        );
        syncManager.syncValue("playerJournalProgressSynced", journalProgress);

        NullableEnumSyncValue<RiftCreatureType> currentCreature = new NullableEnumSyncValue<>(
                RiftCreatureType.class,
                () -> data.creatureType,
                value -> data.creatureType = value
        );
        syncManager.syncValue("currentCreatureEntry", currentCreature);

        return new ModularPanelExitAffectable(UIPanelNames.JOURNAL_SCREEN)
                .onEscPressed(panel -> {
                    PlayerUIHelper.openUI(data.getPlayer(), UIPanelNames.PARTY_SCREEN);
                    return true;
                })
                .widgetTheme(UIThemes.JOURNAL_PANEL)
                //-----left page-----
                .child(new JournalLeftPageWidget(journalProgress, currentCreature)
                        .name("leftPage").size(189, 225).left(8).top(8)
                )
                //-----right page-----
                .child(new JournalRightPageWidget(journalProgress, currentCreature)
                        .name("rightPage").size(189, 225).right(8).top(8)
                );
    }

    private static IKey str(String value) {
        return IKey.str(value).scale(0.75f);
    }

    private static void tryAddChild(ParentWidget<?> parentWidget, IWidget childToAdd) {
        if (!parentWidget.getChildren().contains(childToAdd)) {
            parentWidget.child(childToAdd);
            parentWidget.scheduleResize();
        }
    }

    private static void tryRemoveChild(ParentWidget<?> parentWidget, IWidget childToRemove) {
        if (parentWidget.getChildren().contains(childToRemove)) {
            parentWidget.remove(childToRemove);
            parentWidget.scheduleResize();
        }
    }
}
