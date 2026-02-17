package anightdazingzoroark.prift.client.newui.holder;

import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class HolderHelper {
    public static void setSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature, CreatureNBT creatureNBT) {
        if (player == null || player.world.isRemote) return;

        if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            IPlayerParty playerParty = PlayerPartyHelper.getPlayerParty(player);
            playerParty.setPartyMember(selectedCreature.pos[0], creatureNBT);
        }
    }

    public static void setSelectedCreatureParam(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo, NBTTagCompound param) {
        if (player == null) return;
        if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            IPlayerParty playerParty = PlayerPartyHelper.getPlayerParty(player);
            CreatureNBT creatureNBT = playerParty.getPartyMember(selectedCreatureInfo.pos[0]);
            param.merge(creatureNBT.getCreatureNBT());
            CreatureNBT newCreatureNBT = new CreatureNBT(param);
            playerParty.setPartyMember(selectedCreatureInfo.pos[0], newCreatureNBT);
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
