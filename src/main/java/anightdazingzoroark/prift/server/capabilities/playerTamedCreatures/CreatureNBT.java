package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;

//a helper class where creature nbt is stored and various aspects of said nbt is taken
public class CreatureNBT {
    private final NBTTagCompound creatureNBT;

    public CreatureNBT() {
        this.creatureNBT = new NBTTagCompound();
    }

    public CreatureNBT(NBTTagCompound creatureNBT) {
        this.creatureNBT = creatureNBT;
    }

    public CreatureNBT(RiftCreature creature) {
        this.creatureNBT = NewPlayerTamedCreaturesHelper.createNBTFromCreature(creature);
    }

    public boolean nbtIsEmpty() {
        return this.creatureNBT.isEmpty();
    }

    public RiftCreature getCreatureAsNBT(World world) {
        if (this.creatureNBT.isEmpty()) return null;
        return NewPlayerTamedCreaturesHelper.createCreatureFromNBT(world, this.creatureNBT);
    }

    public NBTTagCompound getCreatureNBT() {
        return this.creatureNBT;
    }

    public RiftCreatureType getCreatureType() {
        if (this.creatureNBT.isEmpty()) return null;
        return RiftCreatureType.values()[this.creatureNBT.getByte("CreatureType")];
    }

    public int getCreatureLevel() {
        if (this.creatureNBT.isEmpty()) return -1;
        return this.creatureNBT.getInteger("Level");
    }

    public String getCreatureLevelWithText() {
        if (this.creatureNBT.isEmpty()) return "";
        return I18n.format("tametrait.level", this.getCreatureLevel());
    }

    public String getCreatureName(boolean includeLevel) {
        if (this.creatureNBT.isEmpty()) return "";
        String partyMemName = (this.creatureNBT.hasKey("CustomName") && !this.creatureNBT.getString("CustomName").isEmpty()) ? this.creatureNBT.getString("CustomName") : this.getCreatureType().getTranslatedName();
        return includeLevel ? I18n.format("journal.party_member.name", partyMemName, this.getCreatureLevel()) : partyMemName;
    }

    //index 0 is current health
    //index 1 is max health
    public float[] getCreatureHealth() {
        if (this.creatureNBT.isEmpty()) return new float[]{0, 0};
        float health = this.creatureNBT.getFloat("Health");
        float maxHealth = health;
        for (NBTBase nbtBase: this.creatureNBT.getTagList("Attributes", 10).tagList) {
            if (nbtBase instanceof NBTTagCompound) {
                NBTTagCompound tagCompound = (NBTTagCompound) nbtBase;

                if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals("generic.maxHealth")) continue;

                maxHealth = (float) tagCompound.getDouble("Base");
            }
        }
        return new float[]{health, maxHealth};
    }

    //index 0 is current energy
    //index 1 is max energy
    public int[] getCreatureEnergy() {
        if (this.creatureNBT.isEmpty()) return new int[]{0, 0};
        int energy = this.creatureNBT.getInteger("Energy");
        int maxEnergy = RiftConfigHandler.getConfig(this.getCreatureType()).stats.maxEnergy;
        return new int[]{energy, maxEnergy};
    }

    //index 0 is current xp
    //index 1 is max xp
    public int[] getCreatureXP() {
        if (this.creatureNBT.isEmpty()) return new int[]{0, 0};
        int xp = this.creatureNBT.getInteger("XP");
        int maxXP = this.getCreatureType().getMaxXP(this.creatureNBT.getInteger("Level"));
        return new int[]{xp, maxXP};
    }

    public int getAgeInTicks() {
        if (this.creatureNBT.isEmpty()) return -1;
        return this.creatureNBT.getInteger("AgeTicks");
    }

    public int getAgeInDays() {
        return this.getAgeInTicks() / 2400;
    }

    public String getAcquisitionInfoString() {
        if (this.creatureNBT.isEmpty()) return new CreatureAcquisitionInfo(null, 0L).acquisitionInfoString();
        CreatureAcquisitionInfo acquisitionInfo = new CreatureAcquisitionInfo(this.creatureNBT.getCompoundTag("AcquisitionInfo"));
        return acquisitionInfo.acquisitionInfoString();
    }
    public UUID getUniqueID() {
        if (this.creatureNBT.isEmpty()) return RiftUtil.nilUUID;
        return this.creatureNBT.getUniqueId("UniqueID");
    }
}
