package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.data.PlayerGuiData;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenPlayerScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerUIHelper {
    public static void openUI(EntityPlayer player, String uiName) {
        RiftMessages.WRAPPER.sendToServer(new RiftOpenPlayerScreen(player, uiName));
    }
}
