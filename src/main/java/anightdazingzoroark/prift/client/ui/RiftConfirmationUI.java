package anightdazingzoroark.prift.client.ui;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

public class RiftConfirmationUI {
    public static void open(@NotNull IPanelHandler handler) {
        handler.openPanel();
        //ModularUI's server-initiated open needs the matching client notification
        if (handler instanceof PanelSyncHandler synced) synced.syncToClient(PanelSyncHandler.SYNC_OPEN);
    }

    @NotNull
    public static IPanelHandler register(
            @NotNull PanelSyncManager syncManager, @NotNull String key,
            @NotNull Runnable confirm, @NotNull Runnable cancel
    ) {
        return syncManager.syncedPanel(key, true, (panelSync, handler) ->
                new ModularPanel(key) {
                    @Override
                    public boolean disablePanelsBelow() {
                        return true;
                    }
                }.size(236, 112)
                        .child(Flow.column().sizeRel(1f).padding(9).childPadding(7)
                                .child(IKey.lang(key).asWidget().widthRel(1f).height(62).textAlign(Alignment.CENTER))
                                .child(Flow.row().widthRel(1f).height(20).childPadding(8)
                                        .child(new ButtonWidget<>().expanded().height(20)
                                                .overlay(IKey.lang("gui.prift.yes"))
                                                .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                                                    if (!mouse.isClient() && handler.isPanelOpen()) {
                                                        handler.closePanel();
                                                        confirm.run();
                                                    }
                                                })))
                                        .child(new ButtonWidget<>().expanded().height(20)
                                                .overlay(IKey.lang("gui.prift.no"))
                                                .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouse -> {
                                                    if (!mouse.isClient() && handler.isPanelOpen()) {
                                                        cancel.run();
                                                        handler.closePanel();
                                                    }
                                                })))
                                )
                        ));
    }
}
