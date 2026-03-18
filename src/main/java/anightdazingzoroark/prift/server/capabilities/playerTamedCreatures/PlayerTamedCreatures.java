package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Deprecated
public class PlayerTamedCreatures implements IPlayerTamedCreatures {
    private FixedSizeList<CreatureNBT> partyCreatures = new FixedSizeList(6, new CreatureNBT());
    private CreatureBoxStorage boxCreatures = new CreatureBoxStorage();
    private int lastSelected = 0;
    private int partyLastOpenedTime = 0;
    private int boxLastOpenedTime = 0;
    private int lastOpenedBox = 0;
    private int selectedPosInOverlay = 0;

    @Override
    public void setLastSelected(int value) {
        this.lastSelected = value;
    }

    @Override
    public int getLastSelected() {
        return this.lastSelected;
    }

    @Override
    public void setPartyLastOpenedTime(int value) {
        this.partyLastOpenedTime = value;
    }

    @Override
    public int getPartyLastOpenedTime() {
        return this.partyLastOpenedTime;
    }

    @Override
    public boolean partyHasNotDeployed() {
        if (this.getPartyNBT().isEmpty()) return false;

        for (CreatureNBT creatureNBT : this.getPartyNBT().getList()) {
            if (creatureNBT.nbtIsEmpty()) continue;

            if (creatureNBT.getDeploymentType() == DeploymentType.PARTY_INACTIVE) return true;
        }
        return false;
    }

    @Override
    public void setBoxLastOpenedTime(int value) {
        this.boxLastOpenedTime = value;
    }

    @Override
    public int getBoxLastOpenedTime() {
        return this.boxLastOpenedTime;
    }

    @Override
    public void setLastOpenedBox(int value) {
        this.lastOpenedBox = value;
    }

    @Override
    public int getLastOpenedBox() {
        return this.lastOpenedBox;
    }

    @Override
    public int getSelectedPosInOverlay() {
        return this.selectedPosInOverlay;
    }

    @Override
    public void setSelectedPosInOverlay(int value) {
        this.selectedPosInOverlay = value;
    }

    @Override
    public void setPartyNBT(FixedSizeList<CreatureNBT> compound) {
        this.partyCreatures = compound;
    }

    @Override
    public FixedSizeList<CreatureNBT> getPartyNBT() {
        return this.partyCreatures;
    }

    //there is potential for race conditions to fuck up a party creature
    @Override
    public void setPartyMemNBT(int index, CreatureNBT compound) {
        this.partyCreatures.set(index, compound);
    }

    @Override
    public void addToPartyNBT(CreatureNBT compound) {
        this.partyCreatures.add(compound);
    }

    @Override
    public void setBoxNBT(CreatureBoxStorage compound) {
        this.boxCreatures = compound;
    }

    @Override
    public CreatureBoxStorage getBoxNBT() {
        return this.boxCreatures;
    }

    @Override
    public void addToBoxNBT(CreatureNBT compound) {
        this.boxCreatures.addCreatureToBox(compound);
    }

    @Override
    public void setBoxMemNBT(int box, int posInBox, CreatureNBT newCompound) {
        this.boxCreatures.getBoxContents(box).set(posInBox, newCompound);
    }

    public enum DeploymentType {
        NONE, //default
        PARTY_INACTIVE,
        PARTY, //with player in party
        BASE, //wandering around box
        BASE_INACTIVE; //sitting in box
    }
}
