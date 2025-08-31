package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//a helper class where creature nbt is stored and various aspects of said nbt is taken
public class CreatureNBT {
    private final NBTTagCompound creatureNBT;
    private int countdownCarry;

    public CreatureNBT() {
        this.creatureNBT = new NBTTagCompound();
    }

    public CreatureNBT(NBTTagCompound creatureNBT) {
        this.creatureNBT = creatureNBT;
    }

    public CreatureNBT(RiftCreature creature) {
        this.creatureNBT = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
    }

    public boolean nbtIsEmpty() {
        return this.creatureNBT == null || this.creatureNBT.isEmpty();
    }

    public RiftCreature getCreatureAsNBT(World world) {
        if (this.creatureNBT == null || this.creatureNBT.isEmpty()) return null;
        RiftCreature creature = this.getCreatureType().invokeClass(world);

        //attributes and creature health dont carry over on client side, this should be a workaround
        if (world.isRemote) {
            creature.setHealth(this.getCreatureHealth()[0]);
            SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), this.creatureNBT.getTagList("Attributes", 10));
        }

        creature.readEntityFromNBT(this.creatureNBT);
        creature.setUniqueId(this.getUniqueID());
        creature.setCustomNameTag(this.getCustomName());
        return creature;
    }

    public NBTTagCompound getCreatureNBT() {
        return this.creatureNBT;
    }

    public RiftCreature findCorrespondingCreature(World world) {
        if (this.creatureNBT.isEmpty()) return null;
        return (RiftCreature) RiftUtil.getEntityFromUUID(world, this.getUniqueID());
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

    public String getCustomName() {
        if (this.creatureNBT.isEmpty()) return "";
        return this.creatureNBT.getString("CustomName");
    }

    public void setCustomName(String name) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setString("CustomName", name);
    }

    public String getCreatureName(boolean includeLevel) {
        if (this.creatureNBT.isEmpty()) return "";
        String partyMemName = (!this.getCustomName().isEmpty()) ? this.getCustomName() : this.getCreatureType().getTranslatedName();
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
        return this.getAgeInTicks() / 24000;
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

    public PlayerTamedCreatures.DeploymentType getDeploymentType() {
        if (this.creatureNBT.isEmpty()) return null;
        return PlayerTamedCreatures.DeploymentType.values()[this.creatureNBT.getInteger("DeploymentType")];
    }

    public void setDeploymentType(PlayerTamedCreatures.DeploymentType deploymentType) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setInteger("DeploymentType", deploymentType.ordinal());
    }

    public NBTTagList getMovesListNBT() {
        if (this.creatureNBT.isEmpty()) return new NBTTagList();
        return this.creatureNBT.getTagList("LearnedMoves", 10);
    }

    public void setMovesListNBT(NBTTagList newMovesList) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setTag("LearnedMoves", newMovesList);
    }

    public List<CreatureMove> getMovesList() {
        if (this.creatureNBT.isEmpty()) return new ArrayList<>();
        List<CreatureMove> toReturn = new ArrayList<>();
        for (int i = 0; i < this.getMovesListNBT().tagCount(); i++) {
            NBTTagCompound moveNBT = this.getMovesListNBT().getCompoundTagAt(i);
            CreatureMove moveToAdd = CreatureMove.values()[moveNBT.getInteger("Move")];
            toReturn.add(moveToAdd);
        }
        return toReturn;
    }

    public void setMove(int pos, CreatureMove creatureMove) {
        if (this.creatureNBT.isEmpty()) return;
        NBTTagList toReplace = this.getMovesListNBT();
        NBTTagCompound newMoveNBT = new NBTTagCompound();
        newMoveNBT.setInteger("Move", creatureMove.ordinal());
        toReplace.set(pos, newMoveNBT);
        this.setMovesListNBT(toReplace);
    }

    public NBTTagList getLearnableMovesListNBT() {
        if (this.creatureNBT.isEmpty()) return new NBTTagList();
        return this.creatureNBT.getTagList("LearnableMoves", 10);
    }

    public void setLearnableMovesListNBT(NBTTagList newMovesList) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setTag("LearnableMoves", newMovesList);
    }

    public List<CreatureMove> getLearnableMovesList() {
        if (this.creatureNBT.isEmpty()) return new ArrayList<>();
        List<CreatureMove> toReturn = new ArrayList<>();
        for (int i = 0; i < this.getLearnableMovesListNBT().tagCount(); i++) {
            NBTTagCompound moveNBT = this.getLearnableMovesListNBT().getCompoundTagAt(i);
            CreatureMove moveToAdd = CreatureMove.values()[moveNBT.getInteger("Move")];
            toReturn.add(moveToAdd);
        }
        return toReturn;
    }

    public void setLearnableMove(int pos, CreatureMove creatureMove) {
        if (this.creatureNBT.isEmpty()) return;
        NBTTagList toReplace = this.getLearnableMovesListNBT();
        NBTTagCompound newMoveNBT = new NBTTagCompound();
        newMoveNBT.setInteger("Move", creatureMove.ordinal());
        toReplace.set(pos, newMoveNBT);
        this.setLearnableMovesListNBT(toReplace);
    }

    public int getMoveCooldown(int moveIndex) {
        switch (moveIndex) {
            case 0:
                return this.creatureNBT.getInteger("CooldownMoveOne");
            case 1:
                return this.creatureNBT.getInteger("CooldownMoveTwo");
            case 2:
                return this.creatureNBT.getInteger("CooldownMoveThree");
        }
        return 0;
    }

    public void setMoveCooldown(int moveIndex, int cooldown) {
        switch (moveIndex) {
            case 0:
                this.creatureNBT.setInteger("CooldownMoveOne", cooldown);
                break;
            case 1:
                this.creatureNBT.setInteger("CooldownMoveTwo", cooldown);
                break;
            case 2:
                this.creatureNBT.setInteger("CooldownMoveThree", cooldown);
                break;
        }
    }

    public NBTTagList getItemListNBT() {
        if (this.creatureNBT.isEmpty()) return new NBTTagList();
        return this.creatureNBT.getTagList("Items", 10);
    }

    public boolean inventoryIsEmpty() {
        if (this.creatureNBT.isEmpty()) return true;
        NBTTagList inventoryTagList = this.creatureNBT.getTagList("Items", 10);
        for (int i = 0; i < inventoryTagList.tagCount(); i++) {
            NBTTagCompound itemNBT = inventoryTagList.getCompoundTagAt(i);
            int j = itemNBT.getByte("Slot") & 255;
            //gear is exempted from the check
            if (j == getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE)
                    || j == getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON)) continue;
            ItemStack stackToTest = new ItemStack(itemNBT);
            if (!stackToTest.equals(ItemStack.EMPTY)) return false;
        }
        return true;
    }

    public void dropInventory(World world) {
        if (this.creatureNBT.isEmpty()) return;

        //step 1: get all the items
        NBTTagList inventoryTagList = this.creatureNBT.getTagList("Items", 10);
        List<ItemStack> itemsToDrop = new ArrayList<>();
        List<Integer> positionsToRemove = new ArrayList<>();
        for (int i = 0; i < inventoryTagList.tagCount(); i++) {
            NBTTagCompound itemNBT = inventoryTagList.getCompoundTagAt(i);
            int j = itemNBT.getByte("Slot") & 255;
            //gear is exempted from the check
            if (j == getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE)
                    || j == getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON)) continue;
            ItemStack stackToTest = new ItemStack(itemNBT);
            if (!stackToTest.equals(ItemStack.EMPTY)) {
                itemsToDrop.add(stackToTest);
                positionsToRemove.add(i);
            }
        }
        if (itemsToDrop.isEmpty()) return;

        //step 2: remove items
        for (Integer posToRemove : positionsToRemove) inventoryTagList.removeTag(posToRemove);
        this.creatureNBT.setTag("Items", inventoryTagList);

        //step 3: drop all the items at the location of the owner
        UUID ownerUUID = UUID.fromString(this.creatureNBT.getString("OwnerUUID"));
        EntityPlayer player = (EntityPlayer) RiftUtil.getEntityFromUUID(world, ownerUUID);
        if (player == null) return;
        for (ItemStack item : itemsToDrop) {
            EntityItem droppedItem = new EntityItem(world, player.posX, player.posY + 0.5, player.posZ);
            droppedItem.setItem(item);
            world.spawnEntity(droppedItem);
        }
    }

    public int getReviveTimeTicks() {
        if (this.creatureNBT.isEmpty()) return 0;
        return this.creatureNBT.getInteger("BoxReviveTime");
    }

    public void countDownReviveTime(int countdownTime) {
        if (this.creatureNBT.isEmpty()) return;

        int trueCountdownTime = 0;
        if (this.countdownCarry == 1) {
            trueCountdownTime = countdownTime / 2 + this.countdownCarry;
            this.countdownCarry = 0;
        }
        else {
            this.countdownCarry = countdownTime % 2;
            trueCountdownTime = countdownTime / 2;
        }

        int oldReviveTime = this.getReviveTimeTicks();
        if (oldReviveTime > 0) this.creatureNBT.setInteger("BoxReviveTime", oldReviveTime - trueCountdownTime);

        if (this.getReviveTimeTicks() <= 0) {
            this.creatureNBT.setFloat("Health", this.getCreatureHealth()[1]);
            this.creatureNBT.setInteger("BoxReviveTime", 0);
        }
    }

    public int[] getReviveTime() {
        if (this.creatureNBT.isEmpty()) return new int[]{0, 0};
        int minutesInt = (int)((float) this.getReviveTimeTicks() / 1200F);
        int secondsInt = (int)((float) this.getReviveTimeTicks() / 20F);
        secondsInt = secondsInt - (minutesInt * 60);

        return new int[]{minutesInt, secondsInt};
    }

    public String getReviveTimeString() {
        if (this.creatureNBT.isEmpty()) return "00:00";

        int minutes = this.getReviveTime()[0];
        int seconds = this.getReviveTime()[1];

        String minutesString = (minutes < 10 ? "0" : "")+minutes;
        String secondsString = (seconds < 10 ? "0" : "")+seconds;

        return minutesString+":"+secondsString;
    }

    public boolean isOwner(EntityPlayer playerToTest) {
        if (playerToTest == null) return false;
        UUID ownerUUID = UUID.fromString(this.creatureNBT.getString("OwnerUUID"));
        return playerToTest.getUniqueID().equals(ownerUUID);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || this.creatureNBT == null) return false;
        if (!(object instanceof CreatureNBT)) return false;
        CreatureNBT nbtToTest = (CreatureNBT) object;
        if (nbtToTest.creatureNBT == null) return false;
        return this.creatureNBT.equals(nbtToTest.creatureNBT);
    }

    @Override
    public String toString() {
        return this.creatureNBT.toString();
    }
}
