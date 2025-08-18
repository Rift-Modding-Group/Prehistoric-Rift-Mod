package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
only server side gets truly edited
this means that all getter functions on client side update client to sync w server info first before returning value
and on server side they just return the value
while setter functions update only server side
*/
public class NewPlayerTamedCreaturesHelper {
    public static final int maxPartySize = 6;

    //party stuff starts here
    public static IPlayerTamedCreatures getPlayerTamedCreatures(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    public static void updateAllPartyMems(EntityPlayer player) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftNewUpdatePartyDeployed(player));
    }

    public static void forceSyncPartyNBT(EntityPlayer player) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
    }

    public static FixedSizeList<CreatureNBT> getPlayerPartyNBT(EntityPlayer player) {
        if (player == null) return new FixedSizeList<>(maxPartySize, new CreatureNBT());
        if (player.world.isRemote) {
            RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
        }
        return getPlayerTamedCreatures(player).getPartyNBT();
    }

    public static boolean canAddToParty(EntityPlayer player) {
        if (player == null) return false;
        if (player.world.isRemote) {
            RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
        }
        for (int x = 0; x < getPlayerPartyNBT(player).size(); x++) {
            if (getPlayerPartyNBT(player).get(x).nbtIsEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void setPlayerPartyNBT(EntityPlayer player, FixedSizeList<CreatureNBT> tagCompounds) {
        if (player == null || tagCompounds == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player, tagCompounds));
    }

    public static void updateIndividualPartyMem(EntityPlayer player, RiftCreature creature) {
        if (player == null || creature == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftUpdateIndividualCreature(player, creature));
    }

    public static void addCreatureToParty(EntityPlayer player, RiftCreature creature) {
        if (player == null || creature == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftNewAddToParty(player, creature));
    }

    public static int getMaxPartySize(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getMaxPartySize();
    }

    public static void deployCreatureFromParty(EntityPlayer player, int position, boolean deploy) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftDeployPartyMem(player, position, deploy));
    }

    public static boolean canBeDeployed(EntityPlayer player, int position) {
        if (player == null) return false;
        RiftCreature creature = getPlayerPartyNBT(player).get(position).getCreatureAsNBT(player.world);
        if (creature == null) return false;
        if (creature instanceof RiftWaterCreature) {
            if (player.world.getBlockState(player.getPosition()).getMaterial() == Material.WATER) return true;
            else if (player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.AIR
                    && ((RiftWaterCreature)creature).isAmphibious()) return true;
        }
        else {
            if (player.world.getBlockState(player.getPosition().down()).getMaterial() == Material.AIR
                    && player.isRiding()) return true;
            else if (player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.AIR) return true;
        }
        return false;
    }

    public static RiftCreature createCreatureFromNBT(World world, NBTTagCompound compound) {
        if (!compound.hasKey("CreatureType")) return null;
        RiftCreatureType creatureType = RiftCreatureType.values()[compound.getByte("CreatureType")];
        UUID uniqueID = compound.getUniqueId("UniqueID");
        String customName = compound.getString("CustomName");

        RiftCreature creature = creatureType.invokeClass(world);

        //attributes and creature health dont carry over on client side, this should be a workaround
        if (world.isRemote) {
            creature.setHealth(compound.getFloat("Health"));
            SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), compound.getTagList("Attributes", 10));
        }

        creature.readEntityFromNBT(compound);
        creature.setUniqueId(uniqueID);
        creature.setCustomNameTag(customName);
        return creature;
    }

    public static NBTTagCompound createNBTFromCreature(RiftCreature creature) {
        NBTTagCompound compound = new NBTTagCompound();
        //get data that doesnt get saved into nbt properly for some reason
        compound.setUniqueId("UniqueID", creature.getUniqueID());
        compound.setString("CustomName", creature.getCustomNameTag());
        creature.writeEntityToNBT(compound);
        return compound;
    }

    public static void updateAfterOpenPartyScreen(EntityPlayer player, int lastOpenedTime) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftUpdatePartyAfterOpenPartyScreen(player, lastOpenedTime));
    }

    public static int getSelectedPartyPosFromOverlay(EntityPlayer player) {
        if (player == null) return -1;
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncSelectedPartyPosFromOverlay(player));
        return getPlayerTamedCreatures(player).getSelectedPosInOverlay();
    }

    public static void setSelectedPartyPosFromOverlay(EntityPlayer player, int value) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSetSelectedPartyPosFromOverlay(player, value));
    }
    //party stuff ends here

    //box stuff starts here
    public static void forceSyncBoxNBT(EntityPlayer player) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxNBT(player));
    }

    public static void forceSyncLastOpenedBox(EntityPlayer player) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftForceSyncLastOpenedBox(player));
    }

    public static void addCreatureToBox(EntityPlayer player, RiftCreature creature) {
        if (player == null || creature == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftNewAddToBox(player, creature));
    }

    public static boolean canAddCreatureToBox(EntityPlayer player) {
        if (player == null) return false;
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxNBT(player));
        for (int x = 0; x < CreatureBoxStorage.maxBoxAmnt; x++) {
            int validSpace = getCreatureBoxStorage(player).validSpaceInBox(x);
            if (validSpace >= 0) return true;
        }
        return false;
    }

    public static CreatureBoxStorage getCreatureBoxStorage(EntityPlayer player) {
        if (player == null) return new CreatureBoxStorage();
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxNBT());
        return getPlayerTamedCreatures(player).getBoxNBT();
    }

    public static void setLastOpenedBox(EntityPlayer player, int value) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSetLastOpenedBox(player, value));
    }

    public static int getLastOpenedBox(EntityPlayer player) {
        if (player == null) return 0;
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncLastOpenedBox(player));
        return getPlayerTamedCreatures(player).getLastOpenedBox();
    }
    //box stuff ends here

    //getter stuff for SelectedCreatureInfo class starts here
    public static CreatureNBT getCreatureNBTFromSelected(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        if (player == null || selectedCreatureInfo == null) return new CreatureNBT();
        if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            return getPlayerPartyNBT(player).get(selectedCreatureInfo.pos[0]);
        }
        else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
            return getCreatureBoxStorage(player).getBoxContents(selectedCreatureInfo.pos[0]).get(selectedCreatureInfo.pos[1]);
        }
        return new CreatureNBT();
    }
    //getter stuff for SelectedCreatureInfo class ends here

    //swapping related stuff starts here
    public static void rearrangePartyCreatures(EntityPlayer player, int posSelected, int posToSwap) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftNewChangePartyOrBoxOrder(player, posSelected, posToSwap));
    }

    public static void rearrangeBoxCreatures(EntityPlayer player, int boxSelected, int posSelected, int boxToSwap, int posToSwap) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftNewChangePartyOrBoxOrder(RiftNewChangePartyOrBoxOrder.BoxSwapType.REARRANGE_BOX, player, boxSelected, posSelected, boxToSwap, posToSwap));
    }

    public static void boxPartySwap(EntityPlayer player, int boxSelected, int posSelected, int partyPosToSwap) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftNewChangePartyOrBoxOrder(RiftNewChangePartyOrBoxOrder.BoxSwapType.BOX_TO_PARTY, player, boxSelected, posSelected, -1, partyPosToSwap));
    }
    //swapping related stuff ends here

    //move swapping related stuff starts here
    public static void partyMemSwapLearntMoveWithLearnableMove(EntityPlayer player, int partyMemPos, int learntMovePos, String learnableMove) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftChangeLearntMoveWithLearnableMove(player, partyMemPos, learntMovePos, learnableMove));
    }

    public static void swapCreatureMoves(EntityPlayer player, SelectedCreatureInfo selectedCreature, SelectedMoveInfo moveSelected, SelectedMoveInfo moveToSwap) {
        if (player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSwapCreatureMoves(player, selectedCreature, moveSelected, moveToSwap));
    }
    //move swapping related stuff ends here

    //helper functions for debugging
    public static List<CreatureMove> getMoveListFromNBT(NBTTagList moveListNBT) {
        List<CreatureMove> toReturn = new ArrayList<>();

        if (moveListNBT != null && !moveListNBT.isEmpty()) {
            for (int x = 0; x < moveListNBT.tagCount(); x++) {
                NBTTagCompound moveNBT = moveListNBT.getCompoundTagAt(x);
                CreatureMove move = CreatureMove.values()[moveNBT.getInteger("Move")];
                toReturn.add(move);
            }
        }

        return toReturn;
    }
}
