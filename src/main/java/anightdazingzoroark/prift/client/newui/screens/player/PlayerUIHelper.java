package anightdazingzoroark.prift.client.newui.screens.player;

import anightdazingzoroark.prift.client.newui.UIPanelNames;
import com.cleanroommc.modularui.factory.ClientGUI;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerUIHelper {
    public static void openUI(EntityPlayer player, String uiName) {
        if (!player.world.isRemote) return;
        if (uiName.equals(UIPanelNames.PARTY_SCREEN)) ClientGUI.open(new RiftPartyScreen());
        else if (uiName.equals(UIPanelNames.JOURNAL_SCREEN)) ClientGUI.open(new NewRiftJournalScreen());
    }
}
