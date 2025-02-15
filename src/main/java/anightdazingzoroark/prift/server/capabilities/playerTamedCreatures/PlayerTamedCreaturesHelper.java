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

import java.util.List;
import java.util.UUID;

public class PlayerTamedCreaturesHelper {
    //player party and creature box
    public static IPlayerTamedCreatures getPlayerTamedCreatures(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    public static List<RiftCreature> getPlayerParty(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartyCreatures(player.world);
    }

    public static void addToPlayerParty(EntityPlayer player, RiftCreature creature) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).addToPartyCreatures(creature);
            RiftMessages.WRAPPER.sendToServer(new RiftAddToParty(player, creature));
        }
        else {
            getPlayerTamedCreatures(player).addToPartyCreatures(creature);
            RiftMessages.WRAPPER.sendToAll(new RiftAddToParty(player, creature));
        }
    }

    public static List<NBTTagCompound> getPlayerPartyNBT(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartyNBT();
    }

    public static void setPlayerPartyNBT(EntityPlayer player, List<NBTTagCompound> tagCompounds) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).setPartyNBT(tagCompounds);
            RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player, tagCompounds));
        }
        else {
            getPlayerTamedCreatures(player).setPartyNBT(tagCompounds);
            RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyNBT(player, tagCompounds));
        }
    }

    public static List<RiftCreature> getPlayerBox(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getBoxCreatures(player.world);
    }

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

    public static void addToPlayerBoxViaNBT(EntityPlayer player, RiftCreature creature) {
        if (player.world.isRemote) {
            NBTTagCompound tagCompound = createNBTFromCreature(creature);
            getPlayerTamedCreatures(player).addToBoxNBT(tagCompound);
            RiftMessages.WRAPPER.sendToServer(new RiftAddToBoxNBT(player, tagCompound));
        }
        else {
            NBTTagCompound tagCompound = createNBTFromCreature(creature);
            getPlayerTamedCreatures(player).addToBoxNBT(tagCompound);
            RiftMessages.WRAPPER.sendToAll(new RiftAddToBoxNBT(player, tagCompound));
        }
    }

    public static List<NBTTagCompound> getPlayerBoxNBT(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getBoxNBT();
    }

    //force sync client to data and vice versa
    public static void forceSyncParty(EntityPlayer player) {
        if (player.world.isRemote) {
            if (getPlayerPartyNBT(player).isEmpty()) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player));
        }
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyNBT(player));
    }

    public static void forceSyncBox(EntityPlayer player) {
        if (player.world.isRemote) {
            if (getPlayerBoxNBT(player).isEmpty()) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxNBT(player));
        }
        else RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxNBT(player));
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

    //update creatures
    public static void updateAllPartyMems(EntityPlayer player) {
        if (player.world.isRemote) {
            for (RiftCreature creature : getPlayerParty(player)) updatePartyMem(creature);
        }
        else {
            for (RiftCreature creature : getPlayerParty(player)) updatePartyMem(creature);
        }
    }

    public static void updatePartyMem(RiftCreature creature) {
        if (creature.getOwner() == null) return;
        if (creature.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftUpdatePartyDeployed((EntityPlayer) creature.getOwner(), creature));
        else RiftMessages.WRAPPER.sendToAll(new RiftUpdatePartyDeployed((EntityPlayer) creature.getOwner(), creature));
    }

    //getting and changing other variables
    public static int getMaxPartySize(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getMaxPartySize();
    }

    public static int getMaxBoxSize(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getMaxBoxSize();
    }

    public static int getLastSelected(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getLastSelected();
    }

    public static void setLastSelected(EntityPlayer player, int value) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).setLastSelected(value);
            RiftMessages.WRAPPER.sendToServer(new RiftSetPartyLastSelected(player, value));
        }
        else {
            getPlayerTamedCreatures(player).setLastSelected(value);
            RiftMessages.WRAPPER.sendToAll(new RiftSetPartyLastSelected(player, value));
        }
    }

    public static int getPartySizeLevel(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartySizeLevel();
    }

    public static void upgradePlayerParty(EntityPlayer player, int value) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).setPartySizeLevel(value);
            RiftMessages.WRAPPER.sendToServer(new RiftUpgradePlayerParty(player, value));
        }
        else {
            getPlayerTamedCreatures(player).setPartySizeLevel(value);
            RiftMessages.WRAPPER.sendToAll(new RiftUpgradePlayerParty(player, value));
        }
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
                                    if (creature.canBeSaddled() && itemNBT.getByte("Slot") == 0) continue;

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
                    if (creature.getEnergy() < 20) {
                        for (int j = 0; j < timeToSubtract; j++) {
                            //natural regen
                            if (j % creature.creatureType.getMaxEnergyRegenMod(creature.getLevel()) == 0) {
                                newEnergyLevel += 1;
                                if (newEnergyLevel >= 20) break;
                            }

                            //food based regen
                            if (GeneralConfig.creatureEatFromInventory && j % 60 == 0) {
                                //manipulate based on item edibility
                                for (int i = nbtItemList.tagCount() - 1; i >= 0; i--) {
                                    NBTTagCompound itemNBT = (NBTTagCompound) nbtItemList.get(i);

                                    //skip if slot is at 0, which is reserved for saddles only
                                    if (creature.canBeSaddled() && itemNBT.getByte("Slot") == 0) continue;

                                    ItemStack itemStack = new ItemStack(itemNBT);
                                    if (creature.isEnergyRegenItem(itemStack)) {
                                        newEnergyLevel += creature.getEnergyRegenItemValue(new ItemStack(itemNBT));
                                        itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                        break;
                                    }
                                }
                                if (newEnergyLevel >= 20) break;
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
                    if (creature.getEnergy() < 20) {
                        int newEnergyLevel = creature.getEnergy();
                        int energyDivideTimes = timeToSubtract / creature.creatureType.getMaxEnergyRegenMod(creature.getLevel());
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
            if (creature.getEnergy() < 20) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setInteger("Energy", 20);

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
                            if (creature.canBeSaddled() && itemNBT.getByte("Slot") == 0) continue;

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

                if (creature.getEnergy() < 20) {
                    //natural energy regen
                    if (time % creature.creatureType.getMaxEnergyRegenMod(creature.getLevel()) == 0) newEnergyValue++;

                    //energy regen from food
                    if (GeneralConfig.creatureEatFromInventory && time % 60 == 0) {
                        //manipulate based on item edibility
                        for (int i = nbtItemList.tagCount() - 1; i >= 0; i--) {
                            NBTTagCompound itemNBT = (NBTTagCompound) nbtItemList.get(i);

                            //skip if slot is at 0, which is reserved for saddles only
                            if (creature.canBeSaddled() && itemNBT.getByte("Slot") == 0) continue;

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

    //swapping related stuff (good lord im going insane)
    public static void rearrangePartyCreatures(EntityPlayer player, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).rearrangePartyCreatures(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_PARTY, player, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).rearrangePartyCreatures(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_PARTY, player, posSelected, posToSwap));
        }
    }

    public static void partyBoxSwap(EntityPlayer player, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).partyCreatureToBoxCreature(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_SWAP, player, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).partyCreatureToBoxCreature(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_SWAP, player, posSelected, posToSwap));
        }
    }

    public static void partyToBox(EntityPlayer player, int posSelected) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).partyCreatureToBox(posSelected);
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX, player, posSelected));
        }
        else {
            getPlayerTamedCreatures(player).partyCreatureToBox(posSelected);
            RiftMessages.WRAPPER.sendToAll(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX, player, posSelected));
        }
    }

    public static void partyBoxDeployedSwap(EntityPlayer player, BlockPos pos, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).partyCreatureToBoxCreatureDeployed(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_DEPLOYED_SWAP, player, pos, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).partyCreatureToBoxCreatureDeployed(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_DEPLOYED_SWAP, player, pos, posSelected, posToSwap));
        }
    }

    public static void partyToBoxDeployed(EntityPlayer player, BlockPos pos, int posSelected) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).partyCreatureToBoxDeployed(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX_DEPLOYED, player, pos, posSelected));
        }
        else {
            getPlayerTamedCreatures(player).partyCreatureToBoxDeployed(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX_DEPLOYED, player, pos, posSelected));
        }
    }

    public static void rearrangeBoxCreatures(EntityPlayer player, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).rearrangeBoxCreatures(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_BOX, player, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).rearrangeBoxCreatures(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_BOX, player, posSelected, posToSwap));
        }
    }

    public static void boxPartySwap(EntityPlayer player, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureToPartyCreature(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_PARTY_SWAP, player, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureToPartyCreature(posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_PARTY_SWAP, player, posSelected, posToSwap));
        }
    }

    public static void boxToParty(EntityPlayer player, int posSelected) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureToParty(posSelected);
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_TO_PARTY, player, posSelected));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureToParty(posSelected);
            RiftMessages.WRAPPER.sendToAll(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_TO_PARTY, player, posSelected));
        }
    }

    public static void boxBoxDeployedSwap(EntityPlayer player, BlockPos pos, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureToBoxCreatureDeployed(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_BOX_DEPLOYED_SWAP, player, pos, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureToBoxCreatureDeployed(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_BOX_DEPLOYED_SWAP, player, pos, posSelected, posToSwap));
        }
    }

    public static void boxToBoxDeployed(EntityPlayer player, BlockPos pos, int posSelected) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureToBoxDeployed(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_TO_BOX_DEPLOYED, player, pos, posSelected));
        }
        else {
            getPlayerTamedCreatures(player).partyCreatureToBoxDeployed(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_TO_BOX_DEPLOYED, player, pos, posSelected));
        }
    }

    public static void rearrangeBoxDeployedCreatures(EntityPlayer player, BlockPos pos, int posSelected, int posToSwap) {}

    public static void boxDeployedPartySwap(EntityPlayer player, BlockPos pos, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureDeployedToPartyCreature(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_PARTY_SWAP, player, pos, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureDeployedToPartyCreature(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_PARTY_SWAP, player, pos, posSelected, posToSwap));
        }
    }

    public static void boxDeployedToParty(EntityPlayer player, BlockPos pos, int posSelected) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureDeployedToParty(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_PARTY, player, pos, posSelected));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureDeployedToParty(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_PARTY, player, pos, posSelected));
        }
    }

    public static void boxDeployedBoxSwap(EntityPlayer player, BlockPos pos, int posSelected, int posToSwap) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureDeployedToBoxCreature(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_BOX_SWAP, player, pos, posSelected, posToSwap));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureDeployedToBoxCreature(player.world, pos, posSelected, posToSwap);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_BOX_SWAP, player, pos, posSelected, posToSwap));
        }
    }

    public static void boxDeployedToBox(EntityPlayer player, BlockPos pos, int posSelected) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).boxCreatureDeployedToBox(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_BOX, player, pos, posSelected));
        }
        else {
            getPlayerTamedCreatures(player).boxCreatureDeployedToBox(player.world, pos, posSelected);
            RiftMessages.WRAPPER.sendToAll(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_BOX, player, pos, posSelected));
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
        compound.setUniqueId("UniqueID", creature.getUniqueID());
        compound.setString("CustomName", creature.getCustomNameTag());
        creature.writeEntityToNBT(compound);
        return compound;
    }

    public static void deployCreatureFromParty(EntityPlayer player, int position, boolean deploy) {
        if (player.world.isRemote) {
            RiftMessages.WRAPPER.sendToServer(new RiftDeployPartyMem(player, position, deploy));
            RiftMessages.WRAPPER.sendToAll(new RiftDeployPartyMem(player, position, deploy));
        }
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
