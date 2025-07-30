package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public interface IPlayerTamedCreatures {
    //for direct values
    //party stuff
    void setPartyNBT(FixedSizeList<NBTTagCompound> compound);
    FixedSizeList<NBTTagCompound> getPartyNBT();
    void setPartyMemNBT(int index, NBTTagCompound compound);
    void addToPartyNBT(NBTTagCompound compound);
    void removeFromPartyNBT(NBTTagCompound compound);
    //box stuff
    void setBoxNBT(CreatureBoxStorage creatureBoxStorage);
    CreatureBoxStorage getBoxNBT();
    void addToBoxNBT(NBTTagCompound compound);
    void setLastSelected(int value);
    int getLastSelected();
    default int getMaxPartySize() {
        return 6;
    }
    int getMaxBoxSize();
    void setBoxSizeLevel(int value);
    int getBoxSizeLevel();
    void setPartyLastOpenedTime(int value);
    int getPartyLastOpenedTime();
    void setBoxLastOpenedTime(int value);
    int getBoxLastOpenedTime();
    NBTTagCompound getPartyMemberTag(UUID uuid);

    //for selected party creature from ui overlay
    int getSelectedPosInOverlay();
    void setSelectedPosInOverlay(int value);

    //for indirect values
    @Deprecated
    void addToPartyCreatures(RiftCreature creature);
    @Deprecated
    List<RiftCreature> getPartyCreatures(World world);
    @Deprecated
    void addToBoxCreatures(RiftCreature creature);
    @Deprecated
    List<RiftCreature> getBoxCreatures(World world);

    //swapping related stuff
    void rearrangePartyCreatures(int posSelected, int posToSwap);
    void rearrangeBoxCreatures(int selectedBox, int posSelected, int boxToSwapWith, int posToSwap);
    void rearrangeDeployedBoxCreatures(World world, BlockPos pos, int posSelected, int posToSwap);
    void boxPartySwap(int selectedBox, int boxPosSelected, int partyPosToSwap);
    void boxDeployedPartySwap(World world, BlockPos creatureBoxPos, int boxDepPosSelected, int partyPosToSwap);
    void boxDeployedBoxSwap(World world, BlockPos creatureBoxPos, int boxDepPosSelected, int boxToSwapWith, int boxPosToSwap);

    @Deprecated
    void partyCreatureToBoxCreature(int partyPosSelected, int boxPosSelected);
    @Deprecated
    void partyCreatureToBox(int partyPosSelected);
    @Deprecated
    void partyCreatureToBoxCreatureDeployed(World world, BlockPos pos, int posSelected, int posToSwap);
    @Deprecated
    void partyCreatureToBoxDeployed(World world, BlockPos pos, int partyPosSelected);

    @Deprecated
    void boxCreatureToPartyCreature(int boxPosSelected, int partyPosSelected);
    @Deprecated
    void boxCreatureToParty(int boxPosSelected);
    @Deprecated
    void boxCreatureToBoxCreatureDeployed(World world, BlockPos pos, int posSelected, int posToSwap);
    @Deprecated
    void boxCreatureToBoxDeployed(World world, BlockPos pos, int boxPosSelected);

    @Deprecated
    void boxCreatureDeployedToPartyCreature(World world, BlockPos pos, int posSelected, int posToSwap);
    @Deprecated
    void boxCreatureDeployedToParty(World world, BlockPos pos, int boxDepPosSelected);
    @Deprecated
    void boxCreatureDeployedToBoxCreature(World world, BlockPos pos, int posSelected, int posToSwap);
    @Deprecated
    void boxCreatureDeployedToBox(World world, BlockPos pos, int boxDepPosSelected);

    @Deprecated
    void modifyCreature(UUID uuid, NBTTagCompound compound);
    void removeCreature(UUID uuid);
    void removeCreatureFromBoxDeployed(World world, BlockPos pos, UUID uuid);

    void removePartyCreatureInventory(int partyPos);
    void removeBoxCreatureDeployedInventory(World world, BlockPos pos, int partyPos);
}
