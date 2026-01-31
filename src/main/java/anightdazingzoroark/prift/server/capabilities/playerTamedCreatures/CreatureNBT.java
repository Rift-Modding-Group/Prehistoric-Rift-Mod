package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//a helper class where creature nbt is stored and various aspects of said nbt is taken
public class CreatureNBT {
    private final NBTTagCompound creatureNBT;
    private int countdownCarry;
    private boolean canSendUpdateUndeployed; //this will be relevant for when updating undeployed creatures only, gets set to true when the undeployed creature gets changed, and set to false when its value is being get

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

    public RiftCreature recreateCreatureAsNBT(World world) {
        if (this.creatureNBT == null || this.creatureNBT.isEmpty()) return null;
        RiftCreature creature = this.getCreatureType().invokeClass(world);

        //attributes and creature health dont carry over on client side, this should be a workaround
        if (world.isRemote) {
            creature.setHealth(this.getCreatureHealth()[0]);
            SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), this.creatureNBT.getTagList("Attributes", 10));
        }

        creature.readEntityFromNBT(this.creatureNBT);
        creature.setUniqueId(UUID.randomUUID());
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

    public void setCreatureHealth(float value) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setFloat("Health", value);
    }

    //index 0 is current health
    //index 1 is max health
    public float[] getCreatureHealth() {
        if (this.creatureNBT.isEmpty()) return new float[]{0, 0};
        float health = this.creatureNBT.getFloat("Health");
        float maxHealth = health;
        NBTTagList attributeList = this.creatureNBT.getTagList("Attributes", 10);
        for (int x = 0; x < attributeList.tagCount(); x++) {
            NBTTagCompound tagCompound = attributeList.getCompoundTagAt(x);

            if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals("generic.maxHealth")) continue;

            maxHealth = (float) tagCompound.getDouble("Base");
        }
        return new float[]{health, maxHealth};
    }

    public void setCreatureEnergy(int value) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setInteger("Energy", value);
    }

    //index 0 is current energy
    //index 1 is max energy
    public int[] getCreatureEnergy() {
        if (this.creatureNBT.isEmpty()) return new int[]{0, 0};
        int energy = this.creatureNBT.getInteger("Energy");
        int maxEnergy = RiftConfigHandler.getConfig(this.getCreatureType()).stats.maxEnergy;
        return new int[]{energy, maxEnergy};
    }

    public void setCreatureXP(int xp) {
        if (this.creatureNBT.isEmpty()) return;
        this.creatureNBT.setInteger("XP", xp);
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
        NBTTagList inventoryTagList = this.getItemListNBT();
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
        NBTTagList inventoryTagList = this.getItemListNBT();
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
        if (this.nbtIsEmpty() || playerToTest == null) return false;
        UUID ownerUUID = UUID.fromString(this.creatureNBT.getString("OwnerUUID"));
        return playerToTest.getUniqueID().equals(ownerUUID);
    }

    public void resetTakingCareOfEgg() {
        if (this.nbtIsEmpty() || this.getCreatureType() != RiftCreatureType.DIMETRODON) return;
        this.creatureNBT.setBoolean("TakingCareOfEgg", false);
    }

    public void resetHomePos() {
        if (this.nbtIsEmpty()) return;
        this.creatureNBT.setBoolean("HasHomePos", false);
        this.creatureNBT.removeTag("HomePosX");
        this.creatureNBT.removeTag("HomePosY");
        this.creatureNBT.removeTag("HomePosZ");
    }

    public boolean hasWorkstationData() {
        return !this.nbtIsEmpty() && this.creatureNBT.hasKey("HasWorkstation");
    }

    public void resetWorkstation() {
        if (this.nbtIsEmpty() || !this.hasWorkstationData()) return;
        this.creatureNBT.setBoolean("HasWorkstation", false);
        this.creatureNBT.removeTag("WorkstationX");
        this.creatureNBT.removeTag("WorkstationY");
        this.creatureNBT.removeTag("WorkstationZ");
    }

    public boolean hasLeadWorkstationData() {
        return !this.nbtIsEmpty() && this.creatureNBT.hasKey("UsingLeadForWork");
    }

    public void resetLeadWorkstation() {
        if (this.nbtIsEmpty() || !this.hasLeadWorkstationData()) return;
        this.creatureNBT.setBoolean("UsingLeadForWork", false);
        this.creatureNBT.removeTag("LeadWorkPosX");
        this.creatureNBT.removeTag("LeadWorkPosY");
        this.creatureNBT.removeTag("LeadWorkPosZ");
    }

    public void setTurretMode(boolean value) {
        if (this.nbtIsEmpty()) return;
        this.creatureNBT.setBoolean("TurretMode", value);
    }

    public boolean hasHarvestOnWanderData() {
        return !this.nbtIsEmpty() && this.creatureNBT.hasKey("CanHarvest");
    }

    public void resetHarvestOnWander() {
        if (this.nbtIsEmpty() || !this.hasHarvestOnWanderData()) return;
        this.creatureNBT.setBoolean("CanHarvest", false);
    }

    public void setSitting(boolean value) {
        if (this.nbtIsEmpty()) return;
        this.creatureNBT.setBoolean("Sitting", value);
    }

    public boolean isBaby() {
        if (this.nbtIsEmpty()) return false;
        return this.getAgeInDays() < 1;
    }

    //THIS IS TO BE RUN SERVER SIDE ONLY
    public void regenNotDeployed() {
        if (this.nbtIsEmpty()) return;

        //update health
        if ((GeneralConfig.naturalCreatureRegen || GeneralConfig.creatureEatFromInventory) && this.getCreatureHealth()[0] < this.getCreatureHealth()[1]) {
            this.setCreatureHealth(this.healthToRegenerate());
            this.canSendUpdateUndeployed = true;
        }

        //update energy
        if (this.getCreatureEnergy()[0] < this.getCreatureEnergy()[1]) {
            this.setCreatureEnergy(this.energyToRegenerate());
            this.canSendUpdateUndeployed = true;
        }
    }

    public boolean getCanSendUpdateUndeployed() {
        boolean toReturn = this.canSendUpdateUndeployed;
        if (this.canSendUpdateUndeployed) this.canSendUpdateUndeployed = false;
        return toReturn;
    }

    private int healthToRegenerate() {
        int toReturn = (int) this.getCreatureHealth()[0];

        for (int i = 1; i <= 1200; i++) {
            //corresponds to every 5 seconds, regen 2 health points
            if (GeneralConfig.naturalCreatureRegen && i % 100 == 0) {
                toReturn += 2;
                if (toReturn > this.getCreatureHealth()[1]) return (int) this.getCreatureHealth()[1];
            }

            //corresponds to every 3 seconds, regen a variable no of health points
            //based on last food item slot
            if (GeneralConfig.creatureEatFromInventory && i % 60 == 0) {
                for (int j = this.getItemListNBT().tagCount() - 1; j >= 0; j--) {
                    NBTTagCompound itemNBT = (NBTTagCompound) this.getItemListNBT().get(j);

                    //gear is exempted from the check
                    if (j == this.getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE)
                            || j == this.getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON)) continue;

                    ItemStack itemStack = new ItemStack(itemNBT);
                    if (this.isFavoriteFood(itemStack) && !this.isEnergyRegenItem(itemStack)) {
                        toReturn += this.getFavoriteFoodHeal(itemStack);
                        itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                        break;
                    }
                }
                if (toReturn > this.getCreatureHealth()[1]) return (int) this.getCreatureHealth()[1];
            }
        }

        return toReturn;
    }

    private int energyToRegenerate() {
        int toReturn = this.getCreatureEnergy()[0];

        for (int i = 1; i <= 1200; i++) {
            if (i % this.getCreatureType().energyRechargeSpeed() == 0) {
                toReturn += 1;
                if (toReturn > this.getCreatureEnergy()[1]) return this.getCreatureEnergy()[1];
            }

            if (GeneralConfig.creatureEatFromInventory && i % 60 == 0) {
                for (int j = this.getItemListNBT().tagCount() - 1; j >= 0; j--) {
                    NBTTagCompound itemNBT = (NBTTagCompound) this.getItemListNBT().get(j);

                    //gear is exempted from the check
                    if (j == this.getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE)
                            || j == this.getCreatureType().slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON)) continue;

                    ItemStack itemStack = new ItemStack(itemNBT);
                    if (this.isEnergyRegenItem(itemStack)) {
                        toReturn += this.getEnergyRegenItemValue(itemStack);
                        itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                        break;
                    }
                }
                if (toReturn > this.getCreatureEnergy()[1]) return this.getCreatureEnergy()[1];
            }
        }

        return toReturn;
    }

    private boolean isFavoriteFood(ItemStack stack) {
        boolean flag = false;
        for (RiftCreatureConfig.Food food : RiftConfigHandler.getConfig(this.getCreatureType()).general.favoriteFood) {
            if (!flag) flag = RiftUtil.itemStackEqualToString(stack, food.itemId);
        }
        return flag;
    }

    public int getFavoriteFoodHeal(ItemStack stack) {
        RiftCreatureConfig.Food foodToHeal = new RiftCreatureConfig.Food("", 0);
        boolean flag = false;
        for (RiftCreatureConfig.Food food : RiftConfigHandler.getConfig(this.getCreatureType()).general.favoriteFood) {
            if (!flag) {
                flag = RiftUtil.itemStackEqualToString(stack, food.itemId);
                foodToHeal = food;
            }
        }
        if (flag) return (int) Math.ceil(this.getCreatureHealth()[1] * foodToHeal.percentageHeal);
        return 0;
    }

    private boolean isEnergyRegenItem(ItemStack stack) {
        RiftCreatureType.CreatureDiet diet = this.getCreatureType().getCreatureDiet();
        List<String> itemList = new ArrayList<>();
        if (diet == RiftCreatureType.CreatureDiet.HERBIVORE || diet == RiftCreatureType.CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.CARNIVORE || diet == RiftCreatureType.CreatureDiet.PISCIVORE || diet == RiftCreatureType.CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.OMNIVORE) {
            itemList = new ArrayList<>(Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods));
            itemList.addAll(Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods));
        }

        for (String foodItem : itemList) {
            int first = foodItem.indexOf(":");
            int second = foodItem.indexOf(":", first + 1);
            int third = foodItem.indexOf(":", second + 1);
            String itemId = foodItem.substring(0, second);
            int itemData = Integer.parseInt(foodItem.substring(second + 1, third));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId)) && (itemData == -1 || itemData == stack.getMetadata())) return true;
        }

        return false;
    }

    private int getEnergyRegenItemValue(ItemStack stack) {
        RiftCreatureType.CreatureDiet diet = this.getCreatureType().getCreatureDiet();
        List<String> itemList = new ArrayList<>();
        if (diet == RiftCreatureType.CreatureDiet.HERBIVORE || diet == RiftCreatureType.CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.CARNIVORE || diet == RiftCreatureType.CreatureDiet.PISCIVORE || diet == RiftCreatureType.CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.OMNIVORE) {
            itemList = new ArrayList<>(Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods));
            itemList.addAll(Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods));
        }

        for (String itemEntry : itemList) {
            int first = itemEntry.indexOf(":");
            int second = itemEntry.indexOf(":", first + 1);
            int third = itemEntry.indexOf(":", second + 1);
            String itemId = itemEntry.substring(0, second);
            int itemData = Integer.parseInt(itemEntry.substring(second + 1, third));
            if (stack.getItem().equals(Item.getByNameOrId(itemId)) && (itemData == -1 || itemData == stack.getMetadata())) {
                return Integer.parseInt(itemEntry.substring(third + 1));
            }
        }
        return 0;
    }

    public int[] getBirthTimeMinutes() {
        if (this.nbtIsEmpty()) return new int[]{};
        int pregnancyTime = this.creatureNBT.getInteger("PregnancyTime");
        int minutes = (int)(pregnancyTime / 1200f);
        int seconds = (int)(pregnancyTime / 20f);
        seconds = seconds - (minutes * 60);
        return new int[]{minutes, seconds};
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
