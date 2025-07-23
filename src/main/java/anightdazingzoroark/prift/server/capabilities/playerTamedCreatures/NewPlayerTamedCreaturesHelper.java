package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

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

    //player party and creature box
    public static IPlayerTamedCreatures getPlayerTamedCreatures(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    public static void updateAllPartyMems(EntityPlayer player) {
        RiftMessages.WRAPPER.sendToServer(new RiftNewUpdatePartyDeployed(player));
    }

    public static FixedSizeList<NBTTagCompound> getPlayerPartyNBT(EntityPlayer player) {
        if (player.world.isRemote) {
            RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
        }
        return getPlayerTamedCreatures(player).getPartyNBT();
    }

    public static boolean canAddToParty(EntityPlayer player) {
        if (player.world.isRemote) {
            RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
        }
        int pos = -1;

        for (int x = 0; x < getPlayerPartyNBT(player).size(); x++) {
            if (getPlayerPartyNBT(player).get(x).isEmpty()) {
                pos = x;
                break;
            }
        }
        return pos >= 0;
    }

    public static void setPlayerPartyNBT(EntityPlayer player, FixedSizeList<NBTTagCompound> tagCompounds) {
        RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player, tagCompounds));
    }

    public static void addCreatureToParty(EntityPlayer player, RiftCreature creature) {
        RiftMessages.WRAPPER.sendToServer(new RiftNewAddToParty(player, creature));
    }

    public static int getMaxPartySize(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getMaxPartySize();
    }

    public static void deployCreatureFromParty(EntityPlayer player, int position, boolean deploy) {
        RiftMessages.WRAPPER.sendToServer(new RiftDeployPartyMem(player, position, deploy));
    }

    public static boolean canBeDeployed(EntityPlayer player, int position) {
        if (player == null) return false;
        RiftCreature creature = createCreatureFromNBT(player.world, getPlayerPartyNBT(player).get(position));
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
        RiftMessages.WRAPPER.sendToServer(new RiftUpdatePartyAfterOpenPartyScreen(player, lastOpenedTime));
    }

    public static void updateWhilePartyScreenOpen(EntityPlayer player, boolean isSingleplayer) {}

    //swapping related stuff starts here
    public static void rearrangePartyCreatures(EntityPlayer player, int posSelected, int posToSwap) {
        RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_PARTY, player, posSelected, posToSwap));
    }
    //swapping related stuff ends here

    //move swapping related stuff starts here
    public static void partyMemSwapLearntMoves(EntityPlayer player, int partyMemPos, int posSelected, int posToSwap) {
        RiftMessages.WRAPPER.sendToServer(new RiftChangeLearntMovesOrder(player, partyMemPos, posSelected, posToSwap));
    }

    public static void partyMemSwapLearntMoveWithLearnableMove(EntityPlayer player, int partyMemPos, int learntMovePos, String learnableMove) {
        RiftMessages.WRAPPER.sendToServer(new RiftChangeLearntMoveWithLearnableMove(player, partyMemPos, learntMovePos, learnableMove));
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
