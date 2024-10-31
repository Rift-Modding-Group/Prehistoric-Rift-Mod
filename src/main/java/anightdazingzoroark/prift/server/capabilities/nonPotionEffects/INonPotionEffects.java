package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;

public interface INonPotionEffects {
    void setBleeding(int strength, int ticks);
    void reduceBleedTick();
    boolean isBleeding();
    int getBleedStrength();
    int getBleedTick();

    void setCapturedByCreature(RiftCreature creature);
    void undoCapture();
    RiftCreature getCaptor();

    void setBolaCaptured(int ticks);
    void reduceBolaCapturedTick();
    boolean isBolaCaptured();
    int getBolaCapturedTick();
}
