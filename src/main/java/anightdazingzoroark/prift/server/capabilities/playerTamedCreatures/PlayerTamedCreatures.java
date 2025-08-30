package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerTamedCreatures implements IPlayerTamedCreatures {
    private FixedSizeList<CreatureNBT> partyCreatures = new FixedSizeList(6, new CreatureNBT());
    private CreatureBoxStorage boxCreatures = new CreatureBoxStorage();
    private int lastSelected = 0;
    private int boxSizeLevel = 0;
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
    public int getMaxBoxSize() {
        return 80 + this.boxSizeLevel * 20;
    }

    @Override
    public void setBoxSizeLevel(int value) {
        if (value <= 4) this.boxSizeLevel = value;
    }

    @Override
    public int getBoxSizeLevel() {
        return this.boxSizeLevel;
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
        selectedBoxDepCreature.setDeploymentType(DeploymentType.BASE_INACTIVE);
        CreatureNBT boxMemToSwap = this.boxCreatures.getBoxContents(boxToSwapWith).get(boxPosToSwap);
        boxMemToSwap.setDeploymentType(DeploymentType.BASE);

        teCreatureBox.setCreatureInPos(boxDepPosSelected, boxMemToSwap);
        this.boxCreatures.setBoxCreature(boxToSwapWith, boxPosToSwap, selectedBoxDepCreature);
    }

    @Override
    public List<RiftCreature> getPartyCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (CreatureNBT compound : this.partyCreatures.getList()) {
            RiftCreature creature = compound.getCreatureAsNBT(world);
            if (creature != null) creatures.add(creature);
        }
        return creatures;
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

    @Deprecated
    @Override
    public void addToBoxCreatures(RiftCreature creature) {
        /*
        if (this.boxCreatures.size() < this.getMaxBoxSize()) {
            NBTTagCompound compound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
            this.boxCreatures.add(compound);
        }
         */
    }

    @Deprecated
    @Override
    public List<RiftCreature> getBoxCreatures(World world) {
        /*
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.boxCreatures) {
            RiftCreature creature = PlayerTamedCreaturesHelper.createCreatureFromNBT(world, compound);
            if (creature != null) creatures.add(creature);
        }
        return creatures;
         */
        return new ArrayList<>();
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

    @Deprecated
    @Override
    public void modifyCreature(UUID uuid, NBTTagCompound compound) {
        //find in party first
        /*
        for (CreatureNBT partyMemCompound : this.partyCreatures.getList()) {
            if (partyMemCompound.getUniqueID().equals(uuid)) {
                //replace each nbt tag
                for (String key : compound.getKeySet()) {
                    NBTBase value = compound.getTag(key);
                    partyMemCompound.setTag(key, value);
                }
                return;
            }
        }
        //find in creature box
        for (NBTTagCompound partyMemCompound : this.boxCreatures) {
            if (partyMemCompound.getUniqueId("UniqueID").equals(uuid)) {
                //replace each nbt tag
                for (String key : compound.getKeySet()) {
                    NBTBase value = compound.getTag(key);
                    partyMemCompound.setTag(key, value);
                }
                return;
            }
        }
         */
    }

    @Override
    public void removePartyCreatureInventory(int partyPos) {
        CreatureNBT partyMember = this.partyCreatures.get(partyPos);
        NBTTagList nbtItemList = partyMember.getCreatureNBT().getTagList("Items", 10);
        RiftCreatureType creatureType = partyMember.getCreatureType();
        for (int x = 0; x < nbtItemList.tagCount(); x++) {
            NBTTagCompound nbttagcompound = nbtItemList.getCompoundTagAt(x);
            int j = nbttagcompound.getByte("Slot") & 255;
            boolean unremovableSlot = (creatureType.canBeSaddled && j == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE))
                    || (creatureType.canHoldLargeWeapon && j == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON));
            if (!unremovableSlot) nbtItemList.removeTag(x);
        }
    }

    public enum DeploymentType {
        NONE, //default
        PARTY_INACTIVE,
        PARTY, //with player in party
        BASE, //wandering around box
        BASE_INACTIVE; //sitting in box

        public String getDeploymentInfo(EntityPlayer player) {
            if (this == PARTY)
                return I18n.format("deployment_info.party", player.getName());
            else if (this == BASE)
                return I18n.format("deployment_info.box", player.getName());
            return "";
        }
    }
}
