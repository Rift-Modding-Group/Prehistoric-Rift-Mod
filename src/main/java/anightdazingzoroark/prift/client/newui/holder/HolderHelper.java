package anightdazingzoroark.prift.client.newui.holder;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import net.minecraft.entity.player.EntityPlayer;

public class HolderHelper {
    public static void setSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature, CreatureNBT creatureNBT) {
        if (player == null || player.world.isRemote) return;

        if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            PlayerPartyProperties playerPartyProperties = PlayerPartyHelper.getPlayerParty(player);
            FixedSizeList<CreatureNBT> playerParty = playerPartyProperties.getPlayerParty();
            playerParty.set(selectedCreature.pos[0], creatureNBT);
            playerPartyProperties.setPlayerParty(playerParty);
        }
    }

    public static CreatureNBT getSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature) {
        if (player == null) return new CreatureNBT();
        if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            return PlayerPartyHelper.getPlayerParty(player).getPartyMember(selectedCreature.pos[0]);
        }
        return new CreatureNBT();
    }
}
