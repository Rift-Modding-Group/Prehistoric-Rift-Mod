package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TameStatusType;

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
                baby.setTameStatus(TameStatusType.SIT);
                baby.setTameBehavior(TameBehaviorType.PASSIVE);
                baby.setLocationAndAngles(parent.posX, parent.posY, parent.posZ, 0.0F, 0.0F);
                parent.world.spawnEntity(baby);
                this.setPregnant(false, 0);
            }
        }
    }
}