package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        NBTTagCompound compoundSelected = this.boxCreatures.getBoxContents(selectedBox).get(posSelected);
        NBTTagCompound compoundToSwap = this.boxCreatures.getBoxContents(boxToSwapWith).get(posToSwap);
        this.boxCreatures.setBoxCreature(boxToSwapWith, posToSwap, compoundSelected);
        this.boxCreatures.setBoxCreature(selectedBox, posSelected, compoundToSwap);
    }

    public void rearrangeDeployedBoxCreatures(World world, BlockPos pos, int posSelected, int posToSwap) {
        if (posSelected == posToSwap) return;
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundSelected = creatureBox.getCreatureList().get(posSelected);
                NBTTagCompound compoundToSwap = creatureBox.getCreatureList().get(posToSwap);

                creatureBox.replaceInCreatureList(posSelected, compoundToSwap);
                creatureBox.replaceInCreatureList(posToSwap, compoundSelected);
            }
        }
    }

    public void boxPartySwap(int selectedBox, int boxPosSelected, int partyPosToSwap) {
        NBTTagCompound selectedBoxCreature = this.boxCreatures.getBoxContents(selectedBox).get(boxPosSelected);
        CreatureNBT partyMemToSwap = this.partyCreatures.get(partyPosToSwap);
        this.boxCreatures.setBoxCreature(selectedBox, boxPosSelected, partyMemToSwap.getCreatureNBT());
        this.partyCreatures.set(partyPosToSwap, new CreatureNBT(selectedBoxCreature));
    }

    public void boxDeployedPartySwap(World world, BlockPos creatureBoxPos, int boxDepPosSelected, int partyPosToSwap) {}

    public void boxDeployedBoxSwap(World world, BlockPos creatureBoxPos, int boxDepPosSelected, int boxToSwapWith, int boxPosToSwap) {}

    @Deprecated
    public void partyCreatureToBoxCreature(int partyPosSelected, int boxPosSelected) {
        /*
        NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
        NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
        compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE_INACTIVE.ordinal());
        compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.PARTY_INACTIVE.ordinal());
        this.partyCreatures.set(partyPosSelected, compoundBoxSelected);
        this.boxCreatures.set(boxPosSelected, compoundPartySelected);
         */
    }

    @Deprecated
    public void partyCreatureToBox(int partyPosSelected) {
        /*
        NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
        compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE_INACTIVE.ordinal());
        this.partyCreatures.remove(partyPosSelected);
        this.boxCreatures.add(compoundPartySelected);
         */
    }

    @Deprecated
    public void partyCreatureToBoxCreatureDeployed(World world, BlockPos pos, int partyPosSelected, int boxDepPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), true);
                compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                this.partyCreatures.set(partyPosSelected, compoundBoxDepSelected);
                creatureBox.replaceInCreatureList(boxDepPosSelected, compoundPartySelected);
            }
        }
         */
    }

    @Deprecated
    public void partyCreatureToBoxDeployed(World world, BlockPos pos, int partyPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
                compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                this.partyCreatures.remove(partyPosSelected);
                creatureBox.addToCreatureList(compoundPartySelected);
            }
        }
         */
    }

    @Deprecated
    public void boxCreatureToPartyCreature(int boxPosSelected, int partyPosSelected) {
        /*
        NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
        NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
        compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.PARTY_INACTIVE.ordinal());
        compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE_INACTIVE.ordinal());
        this.boxCreatures.set(boxPosSelected, compoundPartySelected);
        this.partyCreatures.set(partyPosSelected, compoundBoxSelected);
         */
    }

    @Deprecated
    public void boxCreatureToParty(int boxPosSelected) {
        /*
        NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
        compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.PARTY_INACTIVE.ordinal());
        this.boxCreatures.remove(boxPosSelected);
        this.partyCreatures.add(compoundBoxSelected);
         */
    }

    @Deprecated
    public void boxCreatureToBoxCreatureDeployed(World world, BlockPos pos, int boxPosSelected, int boxDepPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), false);
                compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                this.boxCreatures.set(boxPosSelected, compoundBoxDepSelected);
                creatureBox.replaceInCreatureList(boxDepPosSelected, compoundBoxSelected);
            }
        }
         */
    }

    @Deprecated
    public void boxCreatureToBoxDeployed(World world, BlockPos pos, int boxPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
                compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                this.boxCreatures.remove(boxPosSelected);
                creatureBox.addToCreatureList(compoundBoxSelected);
            }
        }
         */
    }

    @Deprecated
    public void boxCreatureDeployedToPartyCreature(World world, BlockPos pos, int boxDepPosSelected, int partyPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), true);
                NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
                compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                creatureBox.replaceInCreatureList(boxDepPosSelected, compoundPartySelected);
                this.partyCreatures.set(partyPosSelected, compoundBoxDepSelected);
            }
        }
         */
    }

    @Deprecated
    public void boxCreatureDeployedToParty(World world, BlockPos pos, int boxDepPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), true);
                this.partyCreatures.add(compoundBoxDepSelected);
                creatureBox.removeFromCreatureList(boxDepPosSelected);
            }
        }
         */
    }

    @Deprecated
    public void boxCreatureDeployedToBoxCreature(World world, BlockPos pos, int boxDepPosSelected, int boxPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), false);
                NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
                compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                creatureBox.replaceInCreatureList(boxDepPosSelected, compoundBoxSelected);
                this.boxCreatures.set(boxPosSelected, compoundBoxDepSelected);
            }
        }
         */
    }

    @Deprecated
    public void boxCreatureDeployedToBox(World world, BlockPos pos, int boxDepPosSelected) {
        /*
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), false);
                this.boxCreatures.add(compoundBoxDepSelected);
                creatureBox.removeFromCreatureList(boxDepPosSelected);
            }
        }
         */
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
    public void addToBoxNBT(NBTTagCompound compound) {
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

    @Override
    public void removeBoxCreatureDeployedInventory(World world, BlockPos pos, int partyPos) {
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound creature = creatureBox.getCreatureList().get(partyPos);
                NBTTagList nbtItemList = creature.getTagList("Items", 10);
                RiftCreatureType creatureType = RiftCreatureType.values()[creature.getByte("CreatureType")];
                for (int x = 0; x < nbtItemList.tagCount(); x++) {
                    NBTTagCompound nbttagcompound = nbtItemList.getCompoundTagAt(x);
                    int j = nbttagcompound.getByte("Slot") & 255;
                    boolean unremovableSlot = (creatureType.canBeSaddled && j == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE))
                            || (creatureType.canHoldLargeWeapon && j == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON));
                    if (!unremovableSlot) nbtItemList.removeTag(x);
                }
            }
        }
    }

    @Override
    public void removeCreature(UUID uuid) {
        /*
        this.partyCreatures = this.partyCreatures..stream()
                .filter(compound -> {
                    return compound.getUniqueId("UniqueID") != null && !compound.getUniqueId("UniqueID").equals(uuid);
                }).collect(Collectors.toList());
         */
        /*
        this.boxCreatures = this.boxCreatures.stream()
                .filter(compound -> {
                    return compound.getUniqueId("UniqueID") != null && !compound.getUniqueId("UniqueID").equals(uuid);
                }).collect(Collectors.toList());
         */
    }

    public void removeCreatureFromBoxDeployed(World world, BlockPos pos, UUID uuid) {
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                List<NBTTagCompound> newCreatureBoxDeployedList = creatureBox.getCreatureList().stream()
                                .filter(compound -> {
                                    return compound.getUniqueId("UniqueID") != null && !compound.getUniqueId("UniqueID").equals(uuid);
                                }).collect(Collectors.toList());
                creatureBox.setCreatureList(newCreatureBoxDeployedList);
            }
        }
    }

    public enum DeploymentType {
        NONE, //default
        PARTY_INACTIVE,
        PARTY, //with player in party
        BASE, //wandering around box
        BASE_INACTIVE //sitting in box
    }
}
