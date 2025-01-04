package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public interface IImpregnable {
    void setPregnant(boolean value, int timer);
    boolean isPregnant();
    void setPregnancyTimer(int value);
    int getPregnancyTimer();
    default <T extends RiftCreature> void createBaby(T parent) {
        if (this.getPregnancyTimer() > 0) {
            this.setPregnancyTimer(this.getPregnancyTimer() - 1);
            if (this.getPregnancyTimer() == 0) {
                RiftCreature baby = parent.creatureType.invokeClass(parent.world);
                baby.setHealth((float) (parent.minCreatureHealth + (0.1) * (parent.getLevel()) * (parent.minCreatureHealth)));
                baby.setAgeInDays(0);
                baby.setTamed(true);
                baby.setOwnerId(parent.getOwnerId());
                baby.setTameBehavior(TameBehaviorType.PASSIVE);
                baby.setLocationAndAngles(parent.posX, parent.posY, parent.posZ, 0.0F, 0.0F);
                this.setPregnant(false, 0);
                parent.setSitting(false);

                EntityPlayer owner = (EntityPlayer) parent.getOwner();

                //update journal
                if (PlayerJournalProgressHelper.getUnlockedCreatures(owner).containsKey(baby.creatureType) && !PlayerJournalProgressHelper.getUnlockedCreatures(owner).get(baby.creatureType)) {
                    PlayerJournalProgressHelper.unlockCreature(owner, baby.creatureType);
                    owner.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", baby.creatureType.getTranslatedName(), RiftControls.openJournal.getDisplayName()), false);
                }

                //update player tamed creatures
                if (PlayerTamedCreaturesHelper.getPlayerParty(owner).size() < PlayerTamedCreaturesHelper.getMaxPartySize(owner)) {
                    baby.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    parent.world.spawnEntity(baby);
                    PlayerTamedCreaturesHelper.addToPlayerParty(owner, baby);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.baby_birthed_to_party", parent.getName()), false);
                }
                else if (PlayerTamedCreaturesHelper.getPlayerBox(owner).size() < PlayerTamedCreaturesHelper.getMaxBoxSize(owner)) {
                    baby.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                    PlayerTamedCreaturesHelper.addToPlayerBoxViaNBT(owner, baby);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.baby_birthed_to_box", parent.getName()), false);
                }
            }
        }
    }

    default int[] getBirthTimeMinutes() {
        int minutes = (int)((float)this.getPregnancyTimer() / 1200F);
        int seconds = (int)((float)this.getPregnancyTimer() / 20F);
        seconds = seconds - (minutes * 60);
        return new int[]{minutes, seconds};
    }

    default void writePregnancyDataToNBT(NBTTagCompound compound) {
        compound.setInteger("PregnancyTime", this.getPregnancyTimer());
        compound.setBoolean("IsPregnancy", this.isPregnant());
    }

    default void readPregnancyDataFromNBT(NBTTagCompound compound) {
        this.setPregnant(compound.getBoolean("IsPregnancy"), compound.getInteger("PregnancyTime"));
    }
}