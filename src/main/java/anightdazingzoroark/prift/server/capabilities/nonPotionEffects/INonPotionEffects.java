package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

public interface INonPotionEffects {
    void setBleeding(int strength, int ticks);
    void reduceBleedTick();
    boolean isBleeding();
    int getBleedStrength();
    int getBleedTick();

    void setBolaCaptured(int ticks);
    void reduceBolaCapturedTick();
    boolean isBolaCaptured();
    int getBolaCapturedTick();

    void setGrabbed(boolean value);
    boolean isGrabbed();

    //i have no idea why i put this all here this isnt even a status lmao
    //its just to make sure that dismounting after riding something
    //doesnt kill you from fall damage
    void setRiding(boolean value);
    boolean isRiding();
}
