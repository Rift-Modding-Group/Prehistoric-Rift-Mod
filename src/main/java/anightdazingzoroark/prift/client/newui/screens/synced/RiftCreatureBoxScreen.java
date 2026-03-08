package anightdazingzoroark.prift.client.newui.screens.synced;

import anightdazingzoroark.prift.client.newui.UIPanelNames;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public class RiftCreatureBoxScreen {
    public static ModularPanel buildCreatureBoxUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disable();

        return new ModularPanel(UIPanelNames.CREATURE_BOX_SCREEN);
    }
}
