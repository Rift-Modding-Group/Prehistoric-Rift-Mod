package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;

import java.util.UUID;

public class NonPotionEffects implements INonPotionEffects {
    private boolean isBleeding;
    private int bleedStrength;
    private int bleedTick;

    private boolean isBolaCaptured;
    private int bolaCapturedTick;

    private boolean isGrabbed;

    private UUID hypnotizerUUID = RiftUtil.nilUUID;

    //todo: deprecate, replace in favor of dismounting causing rider to tp instead of fall down from ride pos
    private boolean isRiding;

    @Override
    public void setBleeding(int strength, int ticks) {
        this.isBleeding = true;
        this.bleedStrength = strength;
        this.bleedTick = ticks;
    }

    @Override
    public void stopBleeding() {
        this.isBleeding = false;
        this.bleedStrength = 0;
        this.bleedTick = 0;
    }

    @Override
    public void reduceBleedTick() {
        if (this.bleedTick-- <= 0) this.isBleeding = false;
    }

    @Override
    public boolean isBleeding() {
        return this.isBleeding;
    }

    @Override
    public int getBleedStrength() {
        return this.bleedStrength;
    }

    @Override
    public int getBleedTick() {
        return this.bleedTick;
    }

    @Override
    public void setBolaCaptured(int ticks) {
        this.isBolaCaptured = true;
        this.bolaCapturedTick = ticks;
    }

    @Override
    public void resetBolaCaptured() {
        this.isBolaCaptured = false;
        this.bolaCapturedTick = 0;
    }

    @Override
    public void reduceBolaCapturedTick() {
        if (this.bolaCapturedTick-- <= 0) this.isBolaCaptured = false;
    }

    @Override
    public boolean isBolaCaptured() {
        return this.isBolaCaptured;
    }

    @Override
    public int getBolaCapturedTick() {
        return this.bolaCapturedTick;
    }

    @Override
    public void setGrabbed(boolean value) {
        this.isGrabbed = value;
    }

    @Override
    public boolean isGrabbed() {
        return this.isGrabbed;
    }

    @Override
    public void setRiding(boolean value) {
        this.isRiding = value;
    }

    @Override
    public boolean isRiding() {
        return this.isRiding;
    }

    @Override
    public void hypnotize(RiftCreature hypnotizer) {
        this.hypnotizerUUID = hypnotizer.getUniqueID();
    }

    @Override
    public void setHypnotizerUUID(UUID uuid) {
        this.hypnotizerUUID = uuid;
    }

    @Override
    public UUID hypnotizerUUID() {
        return this.hypnotizerUUID;
    }

    @Override
    public void unhypnotize() {
        this.hypnotizerUUID = RiftUtil.nilUUID;
    }
}
