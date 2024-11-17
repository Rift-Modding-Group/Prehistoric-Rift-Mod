package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class PlayerTamedCreaturesHelper {
    //player party and creature box
    private static IPlayerTamedCreatures getPlayerTamedCreatures(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    public static List<RiftCreature> getPlayerParty(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartyCreatures(player.world);
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
        return getPlayerTamedCreatures(player).getLastOpenedTime();
    }

    public static void setCreatureBoxLastOpenedTime(EntityPlayer player, int lastOpenedTime) {
        if (player.world.isRemote) {
            int timeToSubtract = lastOpenedTime - getCreatureBoxLastOpenedTime(player);
            for (RiftCreature creature : getPlayerTamedCreatures(player).getBoxCreatures(player.world)) {
                if (creature.getHealth() / creature.getMaxHealth() <= 0) {
                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setInteger("BoxReviveTime", creature.getBoxReviveTime() - timeToSubtract);
                    getPlayerTamedCreatures(player).modifyCreature(creature.getUniqueID(), tagCompound);
                    RiftMessages.WRAPPER.sendToServer(new RiftModifyPlayerCreature(player, creature.getUniqueID(), tagCompound));
                }
            }

            getPlayerTamedCreatures(player).setLastOpenedTime(lastOpenedTime);
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

            getPlayerTamedCreatures(player).setLastOpenedTime(lastOpenedTime);
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

            //regenerate health
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
}
