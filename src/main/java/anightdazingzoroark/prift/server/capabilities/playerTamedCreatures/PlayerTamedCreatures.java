package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public void rearrangePartyCreatures(int posSelected, int posToSwap) {
        if (posSelected == posToSwap) return;
        CreatureNBT compoundSelected = this.partyCreatures.get(posSelected);
        CreatureNBT compoundToSwap = this.partyCreatures.get(posToSwap);
        this.partyCreatures.set(posSelected, compoundToSwap);
        this.partyCreatures.set(posToSwap, compoundSelected);
    }

    @Override
    public void rearrangeBoxCreatures(int selectedBox, int posSelected, int boxToSwapWith, int posToSwap) {
        if (selectedBox == boxToSwapWith && posSelected == posToSwap) return;
        CreatureNBT compoundSelected = this.boxCreatures.getBoxContents(selectedBox).get(posSelected);
        CreatureNBT compoundToSwap = this.boxCreatures.getBoxContents(boxToSwapWith).get(posToSwap);
        this.boxCreatures.setBoxCreature(boxToSwapWith, posToSwap, compoundSelected);
        this.boxCreatures.setBoxCreature(selectedBox, posSelected, compoundToSwap);
    }

    public void rearrangeDeployedBoxCreatures(World world, BlockPos creatureBoxPos, int posSelected, int posToSwap) {
        if (posSelected == posToSwap) return;
        TileEntity tileEntity = world.getTileEntity(creatureBoxPos);
        if (!(tileEntity instanceof RiftTileEntityCreatureBox)) return;
        RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;

        CreatureNBT compoundSelected = teCreatureBox.getDeployedCreatures().get(posSelected);
        CreatureNBT compoundToSwap = teCreatureBox.getDeployedCreatures().get(posToSwap);

        teCreatureBox.setCreatureInPos(posSelected, compoundToSwap);
        teCreatureBox.setCreatureInPos(posToSwap, compoundSelected);
    }

    public void boxPartySwap(int selectedBox, int boxPosSelected, int partyPosToSwap) {
        CreatureNBT selectedBoxCreature = this.boxCreatures.getBoxContents(selectedBox).get(boxPosSelected);
        selectedBoxCreature.setDeploymentType(DeploymentType.PARTY_INACTIVE);
        CreatureNBT partyMemToSwap = this.partyCreatures.get(partyPosToSwap);
        partyMemToSwap.setDeploymentType(DeploymentType.BASE_INACTIVE);

        this.boxCreatures.setBoxCreature(selectedBox, boxPosSelected, partyMemToSwap);
        this.partyCreatures.set(partyPosToSwap, selectedBoxCreature);
    }

    public void boxDeployedPartySwap(World world, BlockPos creatureBoxPos, int boxDepPosSelected, int partyPosToSwap) {
        TileEntity tileEntity = world.getTileEntity(creatureBoxPos);
        if (!(tileEntity instanceof RiftTileEntityCreatureBox)) return;
        RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;

        CreatureNBT selectedBoxDepCreature = teCreatureBox.getDeployedCreatures().get(boxDepPosSelected);
        //reset some deployment exclusive stuff
        this.boxCreatureDeployedModified(selectedBoxDepCreature);

        //change deployment of box deployed creature
        selectedBoxDepCreature.setDeploymentType(DeploymentType.PARTY_INACTIVE);

        CreatureNBT partyMemToSwap = this.partyCreatures.get(partyPosToSwap);
        partyMemToSwap.setDeploymentType(DeploymentType.BASE);

        teCreatureBox.setCreatureInPos(boxDepPosSelected, partyMemToSwap);
        this.partyCreatures.set(partyPosToSwap, selectedBoxDepCreature);
    }

    public void boxDeployedBoxSwap(World world, BlockPos creatureBoxPos, int boxDepPosSelected, int boxToSwapWith, int boxPosToSwap) {
        TileEntity tileEntity = world.getTileEntity(creatureBoxPos);
        if (!(tileEntity instanceof RiftTileEntityCreatureBox)) return;
        RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;

        CreatureNBT selectedBoxDepCreature = teCreatureBox.getDeployedCreatures().get(boxDepPosSelected);
        //reset some deployment exclusive stuff
        this.boxCreatureDeployedModified(selectedBoxDepCreature);

        //change deployment of box deployed creature
        selectedBoxDepCreature.setDeploymentType(DeploymentType.BASE_INACTIVE);

        CreatureNBT boxMemToSwap = this.boxCreatures.getBoxContents(boxToSwapWith).get(boxPosToSwap);
        boxMemToSwap.setDeploymentType(DeploymentType.BASE);

        teCreatureBox.setCreatureInPos(boxDepPosSelected, boxMemToSwap);
        this.boxCreatures.setBoxCreature(boxToSwapWith, boxPosToSwap, selectedBoxDepCreature);
    }

    private void boxCreatureDeployedModified(CreatureNBT compoundBoxDepSelected) {
        compoundBoxDepSelected.resetHomePos();
        compoundBoxDepSelected.setSitting(false);
        //for dimetrodons
        compoundBoxDepSelected.resetTakingCareOfEgg();
        //for creatures with workstations
        compoundBoxDepSelected.resetWorkstation();
        //for creatures with lead based workstations
        compoundBoxDepSelected.resetLeadWorkstation();
        //for turret mode users
        compoundBoxDepSelected.setTurretMode(false);
        //for harvest on wander users
        compoundBoxDepSelected.resetHarvestOnWander();
    }

    @Override
    public void setPartyNBT(FixedSizeList<CreatureNBT> compound) {
        this.partyCreatures = compound;
    }

    @Override
    public FixedSizeList<CreatureNBT> getPartyNBT() {
        return this.partyCreatures;
    }

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

    public enum DeploymentType {
        NONE, //default
        PARTY_INACTIVE,
        PARTY, //with player in party
        BASE, //wandering around box
        BASE_INACTIVE; //sitting in box
    }
}
