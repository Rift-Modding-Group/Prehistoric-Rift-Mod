package anightdazingzoroark.prift.client.ui.screens.player;

import anightdazingzoroark.prift.client.ui.UIPanelNames;
import anightdazingzoroark.prift.client.ui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureBoxUI;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureScreen;
import com.cleanroommc.modularui.factory.ClientGUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class PlayerUIHelper {
    public static void openUI(EntityPlayer player, String uiName) {
        if (!player.world.isRemote) return;
        if (uiName.equals(UIPanelNames.PARTY_SCREEN)) ClientGUI.open(new RiftPartyScreen());
        else if (uiName.equals(UIPanelNames.JOURNAL_SCREEN)) ClientGUI.open(new RiftJournalScreen());
    }

    public static void openCreatureScreen(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo, int pageToOpenTo) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(
                player, selectedCreatureInfo,
                pageToOpenTo
        ));
    }

    public static void openCreatureBoxUI(EntityPlayer player, BlockPos creatureBoxPos) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureBoxUI(player, creatureBoxPos));
    }
}
