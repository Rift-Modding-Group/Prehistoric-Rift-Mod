package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.world.World;

import java.util.UUID;

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

    //for hypnosis
    void hypnotize(); //this is meant for use on client side only
    void hypnotize(RiftCreature hypnotizer);
    void setHypnotizerUUID(UUID hypnotizer);
    boolean isHypnotized();
    RiftCreature getHypnotizer(World world);
    UUID hypnotizerUUID();
    void unhypnotize();

    //i have no idea why i put this all here this isnt even a status lmao
    //its just to make sure that dismounting after riding something
    //doesnt kill you from fall damage
    void setRiding(boolean value);
    boolean isRiding();
}
