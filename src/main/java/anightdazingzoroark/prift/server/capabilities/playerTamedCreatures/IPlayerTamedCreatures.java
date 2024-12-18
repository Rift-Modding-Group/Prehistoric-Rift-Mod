package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public interface IPlayerTamedCreatures {
    //for direct values
    void setPartyNBT(List<NBTTagCompound> compound);
    List<NBTTagCompound> getPartyNBT();
    void addToPartyNBT(NBTTagCompound compound);
    void removeFromPartyNBT(NBTTagCompound compound);
    void setBoxNBT(List<NBTTagCompound> compound);
    List<NBTTagCompound> getBoxNBT();
    void addToBoxNBT(NBTTagCompound compound);
    void removeFromBoxNBT(NBTTagCompound compound);
    void setLastSelected(int value);
    int getLastSelected();
    int getMaxPartySize();
    void setPartySizeLevel(int value);
    int getPartySizeLevel();
    int getMaxBoxSize();
    void setBoxSizeLevel(int value);
    int getBoxSizeLevel();
    void setPartyLastOpenedTime(int value);
    int getPartyLastOpenedTime();
    void setBoxLastOpenedTime(int value);
    int getBoxLastOpenedTime();

    //for indirect values
    void addToPartyCreatures(RiftCreature creature);
    List<RiftCreature> getPartyCreatures(World world);
    void addToBoxCreatures(RiftCreature creature);
    List<RiftCreature> getBoxCreatures(World world);

    void rearrangePartyCreatures(int posSelected, int posToSwap);
    void rearrangeBoxCreatures(int posSelected, int posToSwap);
    void rearrangeDeployedBoxCreatures(World world, BlockPos pos, int posSelected, int posToSwap);

    void partyCreatureToBoxCreature(int partyPosSelected, int boxPosSelected);
    void partyCreatureToBox(int partyPosSelected);
    void partyCreatureToBoxCreatureDeployed(World world, BlockPos pos, int posSelected, int posToSwap);
    void partyCreatureToBoxDeployed(World world, BlockPos pos, int partyPosSelected);

    void boxCreatureToPartyCreature(int boxPosSelected, int partyPosSelected);
    void boxCreatureToParty(int boxPosSelected);
    void boxCreatureToBoxCreatureDeployed(World world, BlockPos pos, int posSelected, int posToSwap);
    void boxCreatureToBoxDeployed(World world, BlockPos pos, int boxPosSelected);

    void boxCreatureDeployedToPartyCreature(World world, BlockPos pos, int posSelected, int posToSwap);
    void boxCreatureDeployedToParty(World world, BlockPos pos, int boxDepPosSelected);
    void boxCreatureDeployedToBoxCreature(World world, BlockPos pos, int posSelected, int posToSwap);
    void boxCreatureDeployedToBox(World world, BlockPos pos, int boxDepPosSelected);

    void updateCreatures(RiftCreature creature);
    void modifyCreature(UUID uuid, NBTTagCompound compound);
    void replaceCreature(UUID uuid, NBTTagCompound compound);
    void removeCreature(UUID uuid);
    void removeCreatureFromBoxDeployed(World world, BlockPos pos, UUID uuid);

    void removePartyCreatureInventory(int partyPos);
    void removeBoxCreatureDeployedInventory(World world, BlockPos pos, int partyPos);
}
