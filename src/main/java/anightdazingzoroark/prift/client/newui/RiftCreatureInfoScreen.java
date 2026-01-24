package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;

//this one class will be used to display information about a creature
//in the form of a pop-up, and will be shared amongst various uis
public class RiftCreatureInfoScreen {
    public static ModularPanel build(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo, PanelSyncManager syncManager, UISettings settings) {
        //get creature from selected creature info
        CreatureNBT selectedCreature = selectedCreatureInfo.getCreatureNBT(player);

        //get creature strictly for rendering purposes
        RiftCreature creature = selectedCreature.getCreatureAsNBT(player.world);

        return new ModularPanel(UIPanelNames.CREATURE_INFO_SCREEN);
    }
}
