package anightdazingzoroark.prift.client.newui.holder;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class HolderHelper {
    public static void setSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature, CreatureNBT creatureNBT) {
        if (player == null || player.world.isRemote) return;

        if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            PlayerPartyProperties playerPartyProperties = PlayerPartyHelper.getPlayerParty(player);
            FixedSizeList<CreatureNBT> playerParty = playerPartyProperties.getPlayerParty();
            playerParty.set(selectedCreature.getIndex(), creatureNBT);
            playerPartyProperties.setPlayerParty(playerParty);
        }
        else if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
            PlayerCreatureBoxProperties playerCreatureBoxProperties = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);
            CreatureBoxStorage storage = playerCreatureBoxProperties.getCreatureBoxStorage();
            storage.setBoxCreature(
                    selectedCreature.getBoxIndex(),
                    selectedCreature.getIndex(),
                    creatureNBT
            );
            playerCreatureBoxProperties.setCreatureBoxStorage(storage);
        }
    }

    public static CreatureNBT getSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature) {
        if (player == null) return new CreatureNBT();
        if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            return PlayerPartyHelper.getPlayerParty(player).getPartyMember(selectedCreature.getIndex());
        }
        else if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
            return PlayerCreatureBoxHelper.getPlayerCreatureBox(player)
                    .getCreatureBoxStorage()
                    .getBoxContents(selectedCreature.getBoxIndex())
                    .get(selectedCreature.getIndex());
        }
        else if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            TileEntity tileEntity = player.world.getTileEntity(selectedCreature.getCreatureBoxPos());
            if (!(tileEntity instanceof RiftTileEntityCreatureBox teCreatureBox)) return new CreatureNBT();
            return teCreatureBox.getDeployedCreatures().get(selectedCreature.getIndex());
        }
        return new CreatureNBT();
    }
}
