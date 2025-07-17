package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

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
    private List<NBTTagCompound> partyCreatures = new ArrayList<>();
    private List<NBTTagCompound> boxCreatures = new ArrayList<>();
    private int lastSelected = 0;
    private int boxSizeLevel = 0;
    private int partyLastOpenedTime = 0;
    private int boxLastOpenedTime = 0;

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
    public NBTTagCompound getPartyMemberTag(UUID uuid) {
        //find in party first
        for (NBTTagCompound partyMemCompound : this.partyCreatures) {
            if (partyMemCompound.getUniqueId("UniqueID").equals(uuid)) {
                return partyMemCompound;
            }
        }
        return new NBTTagCompound();
    }

    @Override
    public void addToPartyCreatures(RiftCreature creature) {
        if (this.partyCreatures.size() < this.getMaxPartySize()) {
            boolean canAdd = false;
            if (this.partyCreatures.isEmpty()) canAdd = true;
            else if (this.partyCreatures.stream().noneMatch(nbt -> nbt.getUniqueId("UniqueID").equals(creature.getUniqueID()))) canAdd = true;

            if (canAdd) {
                NBTTagCompound compound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                this.partyCreatures.add(compound);
            }
        }
    }

    @Override
    public void rearrangePartyCreatures(int posSelected, int posToSwap) {
        if (posSelected == posToSwap) return;
        NBTTagCompound compoundSelected = this.partyCreatures.get(posSelected);
        NBTTagCompound compoundToSwap = this.partyCreatures.get(posToSwap);
        this.partyCreatures.set(posSelected, compoundToSwap);
        this.partyCreatures.set(posToSwap, compoundSelected);
    }

    @Override
    public void rearrangeBoxCreatures(int posSelected, int posToSwap) {
        if (posSelected == posToSwap) return;
        NBTTagCompound compoundSelected = this.boxCreatures.get(posSelected);
        NBTTagCompound compoundToSwap = this.boxCreatures.get(posToSwap);
        this.boxCreatures.set(posSelected, compoundToSwap);
        this.boxCreatures.set(posToSwap, compoundSelected);
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

    public void partyCreatureToBoxCreature(int partyPosSelected, int boxPosSelected) {
        NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
        NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
        compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE_INACTIVE.ordinal());
        compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.PARTY_INACTIVE.ordinal());
        this.partyCreatures.set(partyPosSelected, compoundBoxSelected);
        this.boxCreatures.set(boxPosSelected, compoundPartySelected);
    }

    public void partyCreatureToBox(int partyPosSelected) {
        NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
        compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE_INACTIVE.ordinal());
        this.partyCreatures.remove(partyPosSelected);
        this.boxCreatures.add(compoundPartySelected);
    }

    public void partyCreatureToBoxCreatureDeployed(World world, BlockPos pos, int partyPosSelected, int boxDepPosSelected) {
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
    }

    public void partyCreatureToBoxDeployed(World world, BlockPos pos, int partyPosSelected) {
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
                compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                this.partyCreatures.remove(partyPosSelected);
                creatureBox.addToCreatureList(compoundPartySelected);
            }
        }
    }

    public void boxCreatureToPartyCreature(int boxPosSelected, int partyPosSelected) {
        NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
        NBTTagCompound compoundPartySelected = this.partyCreatures.get(partyPosSelected);
        compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.PARTY_INACTIVE.ordinal());
        compoundPartySelected.setByte("DeploymentType", (byte) DeploymentType.BASE_INACTIVE.ordinal());
        this.boxCreatures.set(boxPosSelected, compoundPartySelected);
        this.partyCreatures.set(partyPosSelected, compoundBoxSelected);
    }

    public void boxCreatureToParty(int boxPosSelected) {
        NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
        compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.PARTY_INACTIVE.ordinal());
        this.boxCreatures.remove(boxPosSelected);
        this.partyCreatures.add(compoundBoxSelected);
    }

    public void boxCreatureToBoxCreatureDeployed(World world, BlockPos pos, int boxPosSelected, int boxDepPosSelected) {
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
    }

    public void boxCreatureToBoxDeployed(World world, BlockPos pos, int boxPosSelected) {
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxSelected = this.boxCreatures.get(boxPosSelected);
                compoundBoxSelected.setByte("DeploymentType", (byte) DeploymentType.BASE.ordinal());
                this.boxCreatures.remove(boxPosSelected);
                creatureBox.addToCreatureList(compoundBoxSelected);
            }
        }
    }

    public void boxCreatureDeployedToPartyCreature(World world, BlockPos pos, int boxDepPosSelected, int partyPosSelected) {
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
    }

    public void boxCreatureDeployedToParty(World world, BlockPos pos, int boxDepPosSelected) {
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), true);
                this.partyCreatures.add(compoundBoxDepSelected);
                creatureBox.removeFromCreatureList(boxDepPosSelected);
            }
        }
    }

    public void boxCreatureDeployedToBoxCreature(World world, BlockPos pos, int boxDepPosSelected, int boxPosSelected) {
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
    }

    public void boxCreatureDeployedToBox(World world, BlockPos pos, int boxDepPosSelected) {
        if (world.getTileEntity(pos) instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) world.getTileEntity(pos);
            if (creatureBox != null) {
                NBTTagCompound compoundBoxDepSelected = this.boxCreatureDeployedModified(creatureBox.getCreatureList().get(boxDepPosSelected), false);
                this.boxCreatures.add(compoundBoxDepSelected);
                creatureBox.removeFromCreatureList(boxDepPosSelected);
            }
        }
    }

    @Override
    public List<RiftCreature> getPartyCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.partyCreatures) {
            RiftCreature creature = PlayerTamedCreaturesHelper.createCreatureFromNBT(world, compound);
            if (creature != null) creatures.add(creature);
        }
        return creatures;
    }

    @Override
    public void setPartyNBT(List<NBTTagCompound> compound) {
        this.partyCreatures = compound;
    }

    @Override
    public List<NBTTagCompound> getPartyNBT() {
        return this.partyCreatures;
    }

    @Override
    public void setPartyMemNBT(int index, NBTTagCompound compound) {
        this.partyCreatures.set(index, compound);
    }

    @Override
    public void addToPartyNBT(NBTTagCompound compound) {
        this.partyCreatures.add(compound);
    }

    @Override
    public void removeFromPartyNBT(NBTTagCompound compound) {
        this.partyCreatures.remove(compound);
    }

    @Override
    public void addToBoxCreatures(RiftCreature creature) {
        if (this.boxCreatures.size() < this.getMaxBoxSize()) {
            NBTTagCompound compound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
            this.boxCreatures.add(compound);
        }
    }

    @Override
    public List<RiftCreature> getBoxCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.boxCreatures) {
            RiftCreature creature = PlayerTamedCreaturesHelper.createCreatureFromNBT(world, compound);
            if (creature != null) creatures.add(creature);
        }
        return creatures;
    }

    @Override
    public void setBoxNBT(List<NBTTagCompound> compound) {
        this.boxCreatures = compound;
    }

    @Override
    public List<NBTTagCompound> getBoxNBT() {
        return this.boxCreatures;
    }

    @Override
    public void addToBoxNBT(NBTTagCompound compound) {
        this.boxCreatures.add(compound);
    }

    @Override
    public void removeFromBoxNBT(NBTTagCompound compound) {
        this.boxCreatures.remove(compound);
    }

    @Override
    public void modifyCreature(UUID uuid, NBTTagCompound compound) {
        //find in party first
        for (NBTTagCompound partyMemCompound : this.partyCreatures) {
            if (partyMemCompound.getUniqueId("UniqueID").equals(uuid)) {
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
    }

    @Override
    public void replaceCreature(UUID uuid, NBTTagCompound compound) {
        //find in party first
        for (int x = 0; x < this.partyCreatures.size(); x++) {
            if (this.partyCreatures.get(x).getUniqueId("UniqueID").equals(uuid)) {
                this.partyCreatures.set(x, compound);
                return;
            }
        }

        //find in creature box
        for (int x = 0; x < this.boxCreatures.size(); x++) {
            if (this.boxCreatures.get(x).getUniqueId("UniqueID").equals(uuid)) {
                this.boxCreatures.set(x, compound);
                return;
            }
        }
    }

    @Override
    public void removePartyCreatureInventory(int partyPos) {
        NBTTagCompound partyMember = this.partyCreatures.get(partyPos);
        NBTTagList nbtItemList = partyMember.getTagList("Items", 10);
        RiftCreatureType creatureType = RiftCreatureType.values()[partyMember.getByte("CreatureType")];
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

    private NBTTagCompound boxCreatureDeployedModified(NBTTagCompound compoundBoxDepSelected, boolean toParty) {
        byte deploymentTypeByte = toParty ? (byte) DeploymentType.PARTY_INACTIVE.ordinal() : (byte) DeploymentType.BASE_INACTIVE.ordinal();
        compoundBoxDepSelected.setByte("DeploymentType", deploymentTypeByte);
        compoundBoxDepSelected.setBoolean("HasHomePos", false);
        compoundBoxDepSelected.removeTag("HomePosX");
        compoundBoxDepSelected.removeTag("HomePosY");
        compoundBoxDepSelected.removeTag("HomePosZ");
        compoundBoxDepSelected.setBoolean("Sitting", false);
        //for dimetrodons
        if (RiftCreatureType.values()[compoundBoxDepSelected.getByte("CreatureType")] == RiftCreatureType.DIMETRODON) {
            compoundBoxDepSelected.setBoolean("TakingCareOfEgg", false);
        }
        //for speed
        double oldSpeed = RiftCreatureType.values()[compoundBoxDepSelected.getByte("CreatureType")].invokeClass(Minecraft.getMinecraft().world).getSpeed();
        double oldWaterSpeed = RiftCreatureType.values()[compoundBoxDepSelected.getByte("CreatureType")].invokeClass(Minecraft.getMinecraft().world).getWaterSpeed();
        NBTTagList attributeTagList = compoundBoxDepSelected.getTagList("Attributes", 10);
        for (int x = 0; x < attributeTagList.tagList.size(); x++) {
            NBTTagCompound attributeTagCompound = (NBTTagCompound) attributeTagList.get(x);
            if (attributeTagCompound.getString("Name").equals("generic.movementSpeed")) {
                attributeTagCompound.setDouble("Base", oldSpeed);
                attributeTagList.set(x, attributeTagCompound);
            }
            else if (attributeTagCompound.getString("Name").equals("forge.swimSpeed")) {
                attributeTagCompound.setDouble("Base", oldWaterSpeed);
                attributeTagList.set(x, attributeTagCompound);
            }
        }
        //for creatures with workstations
        if (compoundBoxDepSelected.hasKey("HasWorkstation")) compoundBoxDepSelected.setBoolean("HasWorkstation", false);
        //for creatures with lead based workstations
        if (compoundBoxDepSelected.hasKey("UsingLeadForWork")) compoundBoxDepSelected.setBoolean("UsingLeadForWork", false);
        //for turret mode users
        if (compoundBoxDepSelected.hasKey("TurretMode")) compoundBoxDepSelected.setBoolean("TurretMode", false);
        //for harvest on wander users
        if (compoundBoxDepSelected.hasKey("CanHarvest")) compoundBoxDepSelected.setBoolean("CanHarvest", false);
        return compoundBoxDepSelected;
    }

    @Override
    public void removeCreature(UUID uuid) {
        this.partyCreatures = this.partyCreatures.stream()
                .filter(compound -> {
                    return compound.getUniqueId("UniqueID") != null && !compound.getUniqueId("UniqueID").equals(uuid);
                }).collect(Collectors.toList());
        this.boxCreatures = this.boxCreatures.stream()
                .filter(compound -> {
                    return compound.getUniqueId("UniqueID") != null && !compound.getUniqueId("UniqueID").equals(uuid);
                }).collect(Collectors.toList());
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
