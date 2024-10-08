package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerTamedCreatures extends EntityProperties<EntityPlayer> {
    private List<NBTTagCompound> partyCreatures = new ArrayList<>();
    private List<NBTTagCompound> boxCreatures = new ArrayList<>();
    private int lastSelected = 0;
    private int maxPartySize = 4; //range from 4 to 16
    public static final int maxBoxSize = 80;

    @Override
    public void init() {}

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setInteger("LastSelected", this.lastSelected);
        compound.setInteger("MaxPartySize", this.maxPartySize);

        //for party creatures
        NBTTagList partyCreaturesList = new NBTTagList();
        if (!this.partyCreatures.isEmpty()) {
            for (NBTTagCompound partyNBT : this.partyCreatures) partyCreaturesList.appendTag(partyNBT);
            compound.setTag("PartyCreatures", partyCreaturesList);
        }
        else compound.setTag("PartyCreatures", partyCreaturesList);

        //for box creatures
        NBTTagList boxCreaturesList = new NBTTagList();
        if (!this.boxCreatures.isEmpty()) {
            for (NBTTagCompound boxNBT : this.boxCreatures) boxCreaturesList.appendTag(boxNBT);
            compound.setTag("BoxCreatures", boxCreaturesList);
        }
        else compound.setTag("BoxCreatures", boxCreaturesList);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        this.lastSelected = compound.getInteger("LastSelected");
        this.maxPartySize = compound.getInteger("MaxPartySize");

        //for party creatures
        this.partyCreatures.clear();
        if (compound.hasKey("PartyCreatures")) {
            NBTTagList partyCreaturesList = compound.getTagList("PartyCreatures", 10);
            for (int i = 0; i < partyCreaturesList.tagCount(); i++) {
                this.partyCreatures.add(partyCreaturesList.getCompoundTagAt(i));
            }
        }

        //for box creatures
        this.boxCreatures.clear();
        if (compound.hasKey("BoxCreatures")) {
            NBTTagList boxCreaturesList = compound.getTagList("BoxCreatures", 10);
            for (int i = 0; i < boxCreaturesList.tagCount(); i++) {
                this.boxCreatures.add(boxCreaturesList.getCompoundTagAt(i));
            }
        }
    }

    public void setLastSelected(int value) {
        this.lastSelected = value;
    }

    public int getLastSelected() {
        return this.lastSelected;
    }

    public void setMaxPartySize(int value) {
        this.maxPartySize = value;
    }

    public int getMaxPartySize() {
        return this.maxPartySize;
    }

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

    public void rearrangePartyCreatures(int posSelected, int posToSwap) {
        if (posSelected == posToSwap) return;
        NBTTagCompound compoundSelected = this.partyCreatures.get(posSelected);
        NBTTagCompound compoundToSwap = this.partyCreatures.get(posToSwap);
        this.partyCreatures.set(posSelected, compoundToSwap);
        this.partyCreatures.set(posToSwap, compoundSelected);
    }

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

    public List<NBTTagCompound> getPartyNBT() {
        return this.partyCreatures;
    }

    public void addToBoxCreatures(RiftCreature creature) {
        if (this.boxCreatures.size() < maxBoxSize) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setUniqueId("UniqueID", creature.getUniqueID());
            compound.setString("CustomName", creature.getCustomNameTag());
            creature.writeEntityToNBT(compound);
            this.boxCreatures.add(compound);
        }
    }

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

    public void updateCreatures(RiftCreature creature) {
        for (RiftCreature partyCreature : this.getPartyCreatures(creature.world)) {
            if (partyCreature != null && partyCreature.getUniqueID().equals(creature.getUniqueID())) {
                NBTTagCompound partyMemCompound = new NBTTagCompound();
                NBTTagCompound partyMemCompoundUpdt = new NBTTagCompound();

                partyMemCompound.setUniqueId("UniqueID", partyCreature.getUniqueID());
                partyMemCompoundUpdt.setUniqueId("UniqueID", creature.getUniqueID());

                partyMemCompound.setString("CustomName", partyCreature.getCustomNameTag());
                partyMemCompoundUpdt.setString("CustomName", creature.getCustomNameTag());

                partyCreature.writeEntityToNBT(partyMemCompound);
                creature.writeEntityToNBT(partyMemCompoundUpdt);

                if (this.partyCreatures.contains(partyMemCompound)) this.partyCreatures.set(this.partyCreatures.indexOf(partyMemCompound), partyMemCompoundUpdt);
            }
        }
    }

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

    @Override
    public String getID() {
        return "Player Tamed Entities";
    }

    @Override
    public int getTrackingTime() {
        return 1;
    }

    @Override
    public Class<EntityPlayer> getEntityClass() {
        return EntityPlayer.class;
    }

    public enum DeploymentType {
        NONE, //default
        PARTY_INACTIVE,
        PARTY, //with player in party
        BASE, //wandering around box
        BASE_INACTIVE //sitting in box
    }
}
