package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    void setBoxMemNBT(int box, int posInBox, CreatureNBT compound);
    void setLastSelected(int value);
    int getLastSelected();
    //party extra info
    void setPartyLastOpenedTime(int value);
    int getPartyLastOpenedTime();
    boolean partyHasNotDeployed();
    //box extra info
    void setLastOpenedBox(int value);
    int getLastOpenedBox();
    void setBoxLastOpenedTime(int value);
    int getBoxLastOpenedTime();

    //for selected party creature from ui overlay
    int getSelectedPosInOverlay();
    void setSelectedPosInOverlay(int value);
}
