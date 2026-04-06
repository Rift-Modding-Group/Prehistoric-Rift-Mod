package anightdazingzoroark.prift.client.ui.holder;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftReleaseCreature;
import anightdazingzoroark.prift.server.message.RiftSetSelectedCreature;
import anightdazingzoroark.prift.server.message.RiftSetSelectedCreatureMultiple;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

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
        else if (selectedCreature.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            TileEntity tileEntity = player.world.getTileEntity(selectedCreature.getCreatureBoxPos());
            if (!(tileEntity instanceof RiftTileEntityCreatureBox teCreatureBox)) return;

            FixedSizeList<CreatureNBT> deployedFromBox = teCreatureBox.getDeployedCreatures();
            deployedFromBox.set(selectedCreature.getIndex(), creatureNBT);
            teCreatureBox.setDeployedCreatures(deployedFromBox);
        }
    }

    public static void setSelectedCreatureClient(EntityPlayer player, SelectedCreatureInfo selectedCreature, CreatureNBT creatureNBT) {
        if (player == null || !player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSetSelectedCreature(player, selectedCreature, creatureNBT));
    }

    public static void setSelectedCreatureClient(EntityPlayer player, List<ImmutablePair<SelectedCreatureInfo, CreatureNBT>> positionsToSet) {
        if (player == null || !player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSetSelectedCreatureMultiple(player, positionsToSet));
    }

    public static void releaseCreatureClient(EntityPlayer player, SelectedCreatureInfo selectedCreature) {
        if (player == null || !player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftReleaseCreature(player, selectedCreature));
    }

    public static CreatureNBT getSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature) {
        if (player == null || selectedCreature == null) return new CreatureNBT();
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
