package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
todo: make it so only server side gets truly edited
this means that all getter functions update client to sync w server info first before returning value
while setter functions update only server side
*/
public class PlayerTamedCreaturesHelper {
    //player party and creature box
    public static IPlayerTamedCreatures getPlayerTamedCreatures(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    public static List<RiftCreature> getPlayerParty(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartyCreatures(player.world);
    }

    public static List<RiftCreature> getPlayerBox(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getBoxCreatures(player.world);
    }

    @Deprecated
    public static void addToPlayerBox(EntityPlayer player, RiftCreature creature) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).addToBoxCreatures(creature);
            RiftMessages.WRAPPER.sendToServer(new RiftAddToBox(player, creature));
        }
        else {
            getPlayerTamedCreatures(player).addToBoxCreatures(creature);
            RiftMessages.WRAPPER.sendToAll(new RiftAddToBox(player, creature));
        }
    }

    @Deprecated
    public static List<NBTTagCompound> getPlayerBoxNBT(EntityPlayer player) {
        return new ArrayList<>();
    }

    //force sync client to data and vice versa
    public static void forceSyncParty(EntityPlayer player) {
        if (player.world.isRemote) {
            //if (getPlayerPartyNBT(player).isEmpty()) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
        }
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyNBT(player));
    }

    public static void forceSyncBox(EntityPlayer player) {
        /*
        if (player.world.isRemote) {
            if (getPlayerBoxNBT(player).isEmpty()) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxNBT(player));
        }
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxNBT(player));
         */
    }

    public static void forceSyncPartySizeLevel(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartySizeLevel(player));
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartySizeLevel(player));
    }

    public static void forceSyncBoxSizeLevel(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxSizeLevel(player));
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxSizeLevel(player));
    }

    public static void forceSyncLastSelected(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncLastSelected(player));
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncLastSelected(player));
    }

    public static void forceSyncPartyLastOpenedTime(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyLastOpenedTime(player));
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyLastOpenedTime(player));
    }

    public static void forceSyncBoxLastOpenedTime(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxLastOpenedTime(player));
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxLastOpenedTime(player));
    }

    //getting and changing other variables
    public static int getMaxPartySize(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getMaxPartySize();
    }

    public static int getMaxBoxSize(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getMaxBoxSize();
    }

    public static int getBoxSizeLevel(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getBoxSizeLevel();
    }

    public static void upgradePlayerBox(EntityPlayer player, int value) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).setBoxSizeLevel(value);
            RiftMessages.WRAPPER.sendToServer(new RiftUpgradePlayerBox(player, value));
        }
        else {
            getPlayerTamedCreatures(player).setBoxSizeLevel(value);
            RiftMessages.WRAPPER.sendToAll(new RiftUpgradePlayerBox(player, value));
        }
    }

    public static int getCreatureBoxLastOpenedTime(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getBoxLastOpenedTime();
    }

    public static void setCreatureBoxLastOpenedTime(EntityPlayer player, int lastOpenedTime) {
        if (player.world.isRemote) {
            //forceSyncBoxLastOpenedTime(player); //force sync server side to client side
            int timeToSubtract = lastOpenedTime - getCreatureBoxLastOpenedTime(player);
            for (RiftCreature creature : getPlayerTamedCreatures(player).getBoxCreatures(player.world)) {
                if (creature.getHealth() / creature.getMaxHealth() <= 0) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setInteger("BoxReviveTime", creature.getBoxReviveTime() - timeToSubtract);
                    getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }

            getPlayerTamedCreatures(player).setBoxLastOpenedTime(lastOpenedTime);
            RiftMessages.WRAPPER.sendToServer(new RiftCreatureBoxSetLastOpenedTime(player, lastOpenedTime));
        }
        else {
            int timeToSubtract = lastOpenedTime - getCreatureBoxLastOpenedTime(player);
            for (RiftCreature creature : getPlayerTamedCreatures(player).getBoxCreatures(player.world)) {
                if (creature.getHealth() / creature.getMaxHealth() <= 0) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setInteger("BoxReviveTime", creature.getBoxReviveTime() - timeToSubtract);
                    getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                    RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }

            getPlayerTamedCreatures(player).setBoxLastOpenedTime(lastOpenedTime);
            RiftMessages.WRAPPER.sendToAll(new RiftCreatureBoxSetLastOpenedTime(player, lastOpenedTime));
        }
    }

    public static void openToRegenPlayerBoxCreatures(EntityPlayer player) {
        for (RiftCreature creature : getPlayerTamedCreatures(player).getBoxCreatures(player.world)) {
            if (creature.getHealth()/creature.getMaxHealth() <= 0) {
                NBTTagCompound tagCompound = new NBTTagCompound();

                if (creature.getBoxReviveTime() > 0) {
                    tagCompound.setInteger("BoxReviveTime", creature.getBoxReviveTime() - 1);

                    if (player.world.isRemote) {
                        getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                        RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    }
                    else {
                        getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                        RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    }
                }
                else {
                    tagCompound.setFloat("Health", creature.getMaxHealth());
                    tagCompound.setShort("HurtTime", (short)0);
                    tagCompound.setInteger("HurtByTimestamp", 0);
                    tagCompound.setShort("DeathTime", (short)0);
                    RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }
        }
    }

    public static int getPartyLastOpenedTime(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartyLastOpenedTime();
    }

    public static void setPartyLastOpenedTime(EntityPlayer player, int lastOpenedTime) {
        if (player.world.isRemote) {
            int timeToSubtract = lastOpenedTime - getPartyLastOpenedTime(player);
            for (RiftCreature creature : getPlayerTamedCreatures(player).getPartyCreatures(player.world)) {
                if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                    NBTTagCompound tagCompound = new NBTTagCompound();

                    float newHealthLevel = creature.getHealth();
                    int newEnergyLevel = creature.getEnergy();

                    //create tag list from creature inventory
                    NBTTagList nbtItemList = new NBTTagList();
                    if (creature.creatureInventory != null) {
                        for (int i = 0; i < creature.creatureInventory.getSizeInventory(); ++i) {
                            ItemStack itemstack = creature.creatureInventory.getStackInSlot(i);
                            if (!itemstack.isEmpty()) {
                                NBTTagCompound nbttagcompound = new NBTTagCompound();
                                nbttagcompound.setByte("Slot", (byte) i);
                                itemstack.writeToNBT(nbttagcompound);
                                nbtItemList.appendTag(nbttagcompound);
                            }
                        }
                    }

                    //health regen
                    if (creature.getHealth() < creature.getMaxHealth() && creature.getHealth() > 0) {
                        for (int j = 0; j < timeToSubtract; j++) {
                            //natural regen
                            if (GeneralConfig.naturalCreatureRegen && j % 100 == 0) {
                                newHealthLevel += 2f;
                                if (newHealthLevel >= creature.getMaxHealth()) break;
                            }

                            //food based regen
                            if (GeneralConfig.creatureEatFromInventory && j % 60 == 0) {
                                //manipulate based on item edibility
                                for (int i = nbtItemList.tagCount() - 1; i >= 0; i--) {
                                    NBTTagCompound itemNBT = (NBTTagCompound) nbtItemList.get(i);

                                    //skip if slot is at 0, which is reserved for saddles only
                                    if (creature.creatureType.canBeSaddled && itemNBT.getByte("Slot") == 0) continue;

                                    ItemStack itemStack = new ItemStack(itemNBT);
                                    if (creature.isFavoriteFood(itemStack) && !creature.isEnergyRegenItem(itemStack)) {
                                        newHealthLevel += creature.getFavoriteFoodHeal(new ItemStack(itemNBT));
                                        itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                        break;
                                    }
                                }
                                if (newHealthLevel >= creature.getMaxHealth()) break;
                            }
                        }
                    }

                    //energy regen
                    if (creature.getEnergy() < creature.getMaxEnergy()) {
                        for (int j = 0; j < timeToSubtract; j++) {
                            //natural regen
                            if (j % creature.creatureType.energyRechargeSpeed() == 0) {
                                newEnergyLevel += 1;
                                if (creature.getEnergy() >= creature.getMaxEnergy()) break;
                            }

                            //food based regen
                            if (GeneralConfig.creatureEatFromInventory && j % 60 == 0) {
                                //manipulate based on item edibility
                                for (int i = nbtItemList.tagCount() - 1; i >= 0; i--) {
                                    NBTTagCompound itemNBT = (NBTTagCompound) nbtItemList.get(i);

                                    //skip if slot is at 0, which is reserved for saddles only
                                    if (creature.creatureType.canBeSaddled && itemNBT.getByte("Slot") == 0) continue;

                                    ItemStack itemStack = new ItemStack(itemNBT);
                                    if (creature.isEnergyRegenItem(itemStack)) {
                                        newEnergyLevel += creature.getEnergyRegenItemValue(new ItemStack(itemNBT));
                                        itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                        break;
                                    }
                                }
                                if (newEnergyLevel >= creature.getMaxEnergy()) break;
                            }
                        }
                    }

                    tagCompound.setTag("Items", nbtItemList);
                    tagCompound.setFloat("Health", Math.min(newHealthLevel, creature.getMaxHealth()));
                    tagCompound.setInteger("Energy", newEnergyLevel);

                    RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }
            getPlayerTamedCreatures(player).setPartyLastOpenedTime(lastOpenedTime);
            RiftMessages.WRAPPER.sendToServer(new RiftPartySetLastOpenedTime(player, lastOpenedTime));
        }
        else {
            int timeToSubtract = lastOpenedTime - getPartyLastOpenedTime(player);
            for (RiftCreature creature : getPlayerTamedCreatures(player).getPartyCreatures(player.world)) {
                if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                    NBTTagCompound tagCompound = new NBTTagCompound();

                    //natural energy regen
                    if (creature.getEnergy() < creature.getMaxEnergy()) {
                        int newEnergyLevel = creature.getEnergy();
                        int energyDivideTimes = timeToSubtract / creature.creatureType.energyRechargeSpeed();
                        for (int x = 0; x < energyDivideTimes; x++) {
                            newEnergyLevel += 1;
                        }

                        tagCompound.setInteger("Energy", newEnergyLevel);
                    }

                    //natural health regen
                    if (creature.getHealth() < creature.getMaxHealth() && creature.getHealth() > 0) {
                        float newHealthLevel = creature.getHealth();
                        int healthDivideTimes = timeToSubtract / 100;
                        for (int x = 0; x < healthDivideTimes; x++) {
                            newHealthLevel += 2;
                        }

                        tagCompound.setFloat("Health", newHealthLevel);
                    }

                    RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }
            getPlayerTamedCreatures(player).setPartyLastOpenedTime(lastOpenedTime);
            RiftMessages.WRAPPER.sendToAll(new RiftPartySetLastOpenedTime(player, lastOpenedTime));
        }
    }

    public static void regeneratePlayerBoxCreatures(EntityPlayer player) {
        for (RiftCreature creature : getPlayerTamedCreatures(player).getBoxCreatures(player.world)) {
            //regenerate energy
            if (creature.getEnergy() < creature.getMaxEnergy()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setInteger("Energy", creature.getMaxEnergy());

                if (player.world.isRemote) {
                    getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
                else {
                    getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                    RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }

            //reviving creature
            if (creature.getHealth()/creature.getMaxHealth() <= 0) {
                NBTTagCompound tagCompound = new NBTTagCompound();

                if (creature.getBoxReviveTime() > 0) {
                    tagCompound.setInteger("BoxReviveTime", creature.getBoxReviveTime() - 1);

                    if (player.world.isRemote) {
                        getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                        RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    }
                    else {
                        getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                        RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    }
                }
                else {
                    tagCompound.setFloat("Health", creature.getMaxHealth());
                    tagCompound.setShort("HurtTime", (short)0);
                    tagCompound.setInteger("HurtByTimestamp", 0);
                    tagCompound.setShort("DeathTime", (short)0);
                    RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }
        }
    }

    public static void reenergizePartyUndeployedCreatures(EntityPlayer player, int time) {
        if (time <= 0) return;

        for (RiftCreature creature : getPlayerTamedCreatures(player).getPartyCreatures(player.world)) {
            if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                NBTTagCompound tagCompound = new NBTTagCompound();

                int newEnergyValue = creature.getEnergy();
                float newHealthValue = creature.getHealth();

                //create tag list from creature inventory
                NBTTagList nbtItemList = new NBTTagList();
                if (creature.creatureInventory != null) {
                    for (int i = 0; i < creature.creatureInventory.getSizeInventory(); ++i) {
                        ItemStack itemstack = creature.creatureInventory.getStackInSlot(i);
                        if (!itemstack.isEmpty()) {
                            NBTTagCompound nbttagcompound = new NBTTagCompound();
                            nbttagcompound.setByte("Slot", (byte) i);
                            itemstack.writeToNBT(nbttagcompound);
                            nbtItemList.appendTag(nbttagcompound);
                        }
                    }
                }

                if (creature.getHealth() < creature.getMaxHealth() && creature.getHealth() > 0) {
                    //natural health regen
                    if (GeneralConfig.naturalCreatureRegen && time % 100 == 0) newHealthValue += 2f;

                    //health regen from food
                    if (GeneralConfig.creatureEatFromInventory && time % 60 == 0) {
                        //manipulate based on item edibility
                        for (int i = nbtItemList.tagCount() - 1; i >= 0; i--) {
                            NBTTagCompound itemNBT = (NBTTagCompound) nbtItemList.get(i);

                            //skip if slot is at 0, which is reserved for saddles only
                            if (creature.creatureType.canBeSaddled && itemNBT.getByte("Slot") == 0) continue;

                            //create item, use it to heal up, then reduce count
                            ItemStack itemStack = new ItemStack(itemNBT);
                            if (creature.isFavoriteFood(itemStack) && !creature.isEnergyRegenItem(itemStack)) {
                                newHealthValue += creature.getFavoriteFoodHeal(new ItemStack(itemNBT));
                                itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                break;
                            }
                        }
                    }
                }

                if (creature.getEnergy() < creature.getMaxEnergy()) {
                    //natural energy regen
                    if (time % creature.creatureType.energyRechargeSpeed() == 0) newEnergyValue++;

                    //energy regen from food
                    if (GeneralConfig.creatureEatFromInventory && time % 60 == 0) {
                        //manipulate based on item edibility
                        for (int i = nbtItemList.tagCount() - 1; i >= 0; i--) {
                            NBTTagCompound itemNBT = (NBTTagCompound) nbtItemList.get(i);

                            //skip if slot is at 0, which is reserved for saddles only
                            if (creature.creatureType.canBeSaddled && itemNBT.getByte("Slot") == 0) continue;

                            ItemStack itemStack = new ItemStack(itemNBT);
                            if (creature.isEnergyRegenItem(itemStack)) {
                                newEnergyValue += creature.getEnergyRegenItemValue(new ItemStack(itemNBT));
                                itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                break;
                            }
                        }
                    }
                }

                tagCompound.setInteger("Energy", newEnergyValue);
                tagCompound.setFloat("Health", newHealthValue);
                tagCompound.setTag("Items", nbtItemList);

                RiftMessages.WRAPPER.sendToAll(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
            }
        }
    }

    //other helper functions
    public static RiftCreature createCreatureFromNBT(World world, NBTTagCompound compound) {
        if (!compound.hasKey("CreatureType")) return null;
        RiftCreatureType creatureType = RiftCreatureType.values()[compound.getByte("CreatureType")];
        UUID uniqueID = compound.getUniqueId("UniqueID");
        String customName = compound.getString("CustomName");

        RiftCreature creature = creatureType.invokeClass(world);

        //attributes and creature health dont carry over on client side, this should be a workaround
        if (world.isRemote) {
            creature.setHealth(compound.getFloat("Health"));
            SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), compound.getTagList("Attributes", 10));
        }

        creature.readEntityFromNBT(compound);
        creature.setUniqueId(uniqueID);
        creature.setCustomNameTag(customName);
        return creature;
    }

    public static NBTTagCompound createNBTFromCreature(RiftCreature creature) {
        NBTTagCompound compound = new NBTTagCompound();
        //get data that doesnt get saved into nbt properly for some reason
        compound.setUniqueId("UniqueID", creature.getUniqueID());
        compound.setString("CustomName", creature.getCustomNameTag());
        creature.writeEntityToNBT(compound);
        return compound;
    }

    public static void deployCreatureFromParty(EntityPlayer player, int position, boolean deploy) {
        RiftMessages.WRAPPER.sendToAll(new RiftDeployPartyMem(player, position, deploy));
    }

    public static boolean canBeDeployed(EntityPlayer player, int position) {
        RiftCreature creature = getPlayerParty(player).get(position);
        if (player == null || creature == null) return false;
        if (creature instanceof RiftWaterCreature) {
            if (player.world.getBlockState(player.getPosition()).getMaterial() == Material.WATER) return true;
            else if (player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.AIR
                    && ((RiftWaterCreature)creature).isAmphibious()) return true;
        }
        else {
            if (player.world.getBlockState(player.getPosition().down()).getMaterial() == Material.AIR
                && player.isRiding()) return true;
            else if (player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.AIR) return true;
        }
        return false;
    }
}
