package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PlayerTamedCreatures extends EntityProperties<EntityPlayer> {
    private final List<NBTTagCompound> partyCreatures = new ArrayList<>();
    private final List<NBTTagCompound> boxCreatures = new ArrayList<>();
    private int maxPartySize = 4; //range from 4 to 16
    public static final int maxBoxSize = 80;

    @Override
    public void init() {}

    @Override
    public void saveNBTData(NBTTagCompound compound) {
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
        this.maxPartySize = compound.getInteger("MaxPartySize");

        //for party creatures
        this.partyCreatures.clear();
        if (compound.hasKey("PartyCreatures")) {
            System.out.println("get party creatures");
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

    public void setMaxPartySize(int value) {
        this.maxPartySize = value;
    }

    public int getMaxPartySize() {
        return this.maxPartySize;
    }

    public void addToPartyCreatures(RiftCreature creature) {
        if (this.partyCreatures.size() < this.maxPartySize) {
            NBTTagCompound compound = new NBTTagCompound();
            creature.writeToNBT(compound);
            this.partyCreatures.add(compound);
        }
    }

    public List<NBTTagCompound> getPartyNBT() {
        return this.partyCreatures;
    }

    public List<RiftCreature> getPartyCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.partyCreatures) {
            RiftCreatureType creatureType = RiftCreatureType.values()[compound.getByte("CreatureType")];
            if (creatureType != null) {
                RiftCreature creature = creatureType.invokeClass(world);
                creature.readEntityFromNBT(compound);
                creatures.add(creature);
            }
        }
        return creatures;
    }

    public void addToBoxCreatures(RiftCreature creature) {
        if (this.boxCreatures.size() < maxBoxSize) {
            NBTTagCompound compound = new NBTTagCompound();
            creature.writeToNBT(compound);
            this.boxCreatures.add(compound);
        }
    }

    public List<RiftCreature> getBoxCreatures(World world) {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.boxCreatures) {
            RiftCreature creature = (RiftCreature)EntityList.createEntityFromNBT(compound, world);
            creatures.add(creature);
        }
        return creatures;
    }

    @Override
    public String getID() {
        return "Player Tamed Entities";
    }

    @Override
    public int getTrackingTime() {
        return 20;
    }

    @Override
    public Class<EntityPlayer> getEntityClass() {
        return EntityPlayer.class;
    }
}
