package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

public interface INonPotionEffects {
    //for bleeding
    void setBleeding(int strength, int ticks);
    void stopBleeding();
    void reduceBleedTick();
    boolean isBleeding();
    int getBleedStrength();
    int getBleedTick();

    //for bola captured
    void setBolaCaptured(int ticks);
    void resetBolaCaptured();
    void reduceBolaCapturedTick();
    boolean isBolaCaptured();
    int getBolaCapturedTick();

    //for grabbed
    void setGrabbed(boolean value);
    boolean isGrabbed();

    //coming soon: for hypnosis

    //i have no idea why i put this all here this isnt even a status lmao
    //its just to make sure that dismounting after riding something
    //doesnt kill you from fall damage
    void setRiding(boolean value);
    boolean isRiding();
}
