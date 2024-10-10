package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.PlayerTamedCreatures.DeploymentType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerTamedCreatures implements IPlayerTamedCreatures {
    private List<NBTTagCompound> partyCreatures = new ArrayList<>();
    private List<NBTTagCompound> boxCreatures = new ArrayList<>();
    private int lastSelected = 0;
    private int maxPartySize = 4; //range from 4 to 16
    public static final int maxBoxSize = 80;

    @Override
    public void setLastSelected(int value) {
        this.lastSelected = value;
    }

    @Override
    public int getLastSelected() {
        return this.lastSelected;
    }

    @Override
    public void setMaxPartySize(int value) {
        this.maxPartySize = value;
    }

    @Override
    public int getMaxPartySize() {
        return this.maxPartySize;
    }

    @Override
    public void addToPartyCreatures(RiftCreature creature) {
        if (this.partyCreatures.size() < this.maxPartySize) {
            boolean canAdd = false;
            if (this.partyCreatures.isEmpty()) canAdd = true;
            else if (this.partyCreatures.stream().noneMatch(nbt -> nbt.getUniqueId("UniqueID").equals(creature.getUniqueID()))) canAdd = true;

            if (canAdd) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setUniqueId("UniqueID", creature.getUniqueID());
                compound.setString("CustomName", creature.getCustomNameTag());
                creature.writeEntityToNBT(compound);
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

    @Override
    public List<RiftCreature> getPartyCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.partyCreatures) {
            RiftCreatureType creatureType = RiftCreatureType.values()[compound.getByte("CreatureType")];
            UUID uniqueID = compound.getUniqueId("UniqueID");
            String customName = compound.getString("CustomName");
            if (creatureType != null) {
                RiftCreature creature = creatureType.invokeClass(world);

                //attributes and creature health dont carry over on client side, this should be a workaround
                if (world.isRemote) {
                    creature.setHealth(compound.getFloat("Health"));
                    SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), compound.getTagList("Attributes", 10));
                }

                creature.readEntityFromNBT(compound);
                creature.setUniqueId(uniqueID);
                creature.setCustomNameTag(customName);
                creatures.add(creature);
            }
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
    public void addToPartyNBT(NBTTagCompound compound) {
        this.partyCreatures.add(compound);
    }

    @Override
    public void removeFromPartyNBT(NBTTagCompound compound) {
        this.partyCreatures.remove(compound);
    }

    @Override
    public void addToBoxCreatures(RiftCreature creature) {
        if (this.boxCreatures.size() < maxBoxSize) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setUniqueId("UniqueID", creature.getUniqueID());
            compound.setString("CustomName", creature.getCustomNameTag());
            creature.writeEntityToNBT(compound);
            this.boxCreatures.add(compound);
        }
    }

    @Override
    public List<RiftCreature> getBoxCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.boxCreatures) {
            RiftCreatureType creatureType = RiftCreatureType.values()[compound.getByte("CreatureType")];
            UUID uniqueID = compound.getUniqueId("UniqueID");
            String customName = compound.getString("CustomName");
            if (creatureType != null) {
                RiftCreature creature = creatureType.invokeClass(world);

                //attributes and creature health dont carry over on client side, this should be a workaround
                if (world.isRemote) {
                    creature.setHealth(compound.getFloat("Health"));
                    SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), compound.getTagList("Attributes", 10));
                }

                creature.readEntityFromNBT(compound);
                creature.setUniqueId(uniqueID);
                creature.setCustomNameTag(customName);
                creatures.add(creature);
            }
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
    public void updateCreatures(RiftCreature creature) {
        for (NBTTagCompound partyMemCompound : this.partyCreatures) {
            if (partyMemCompound.getUniqueId("UniqueID") != null && partyMemCompound.getUniqueId("UniqueID").equals(creature.getUniqueID())) {
                NBTTagCompound partyMemCompoundUpdt = new NBTTagCompound();
                partyMemCompoundUpdt.setUniqueId("UniqueID", creature.getUniqueID());
                partyMemCompoundUpdt.setString("CustomName", creature.getCustomNameTag());
                creature.writeEntityToNBT(partyMemCompoundUpdt);

                if (this.partyCreatures.contains(partyMemCompound)) this.partyCreatures.set(this.partyCreatures.indexOf(partyMemCompound), partyMemCompoundUpdt);
            }
        }
    }

    @Override
    public void modifyCreature(UUID uuid, NBTTagCompound compound) {
        //find in party first
        for (NBTTagCompound partyMemCompound : this.partyCreatures) {
            if (partyMemCompound.getUniqueId("UniqueID").equals(uuid)) {
                for (String key : compound.getKeySet()) {
                    NBTBase value = compound.getTag(key);
                    if (partyMemCompound.hasKey(key)) partyMemCompound.setTag(key, value);
                }
                return;
            }
        }
        //find in creature box
        for (NBTTagCompound partyMemCompound : this.boxCreatures) {
            if (partyMemCompound.getUniqueId("UniqueID").equals(uuid)) {
                for (String key : compound.getKeySet()) {
                    NBTBase value = compound.getTag(key);
                    if (partyMemCompound.hasKey(key)) partyMemCompound.setTag(key, value);
                }
                return;
            }
        }
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
}
