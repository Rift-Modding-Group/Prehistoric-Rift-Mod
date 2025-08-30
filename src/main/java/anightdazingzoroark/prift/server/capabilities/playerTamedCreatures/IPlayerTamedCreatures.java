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
    void setPartyNBT(FixedSizeList<CreatureNBT> compound);
    FixedSizeList<CreatureNBT> getPartyNBT();
    void setPartyMemNBT(int index, CreatureNBT compound);
    void addToPartyNBT(CreatureNBT compound);
    //box stuff
    void setBoxNBT(CreatureBoxStorage creatureBoxStorage);
    CreatureBoxStorage getBoxNBT();
    void addToBoxNBT(CreatureNBT compound);
    void setLastSelected(int value);
    int getLastSelected();
    default int getMaxPartySize() {
        return 6;
    }
    @Deprecated
    int getMaxBoxSize();
    @Deprecated
    void setBoxSizeLevel(int value);
    @Deprecated
    int getBoxSizeLevel();
    //party extra info
    void setPartyLastOpenedTime(int value);
    int getPartyLastOpenedTime();
    //box extra info
    void setLastOpenedBox(int value);
    int getLastOpenedBox();
    void setBoxLastOpenedTime(int value);
    int getBoxLastOpenedTime();

    //for selected party creature from ui overlay
    int getSelectedPosInOverlay();
    void setSelectedPosInOverlay(int value);

    //for indirect values
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
    void modifyCreature(UUID uuid, NBTTagCompound compound);

    void removePartyCreatureInventory(int partyPos);
}
