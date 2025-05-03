package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

public class NonPotionEffects implements INonPotionEffects {
    private boolean isBleeding;
    private int bleedStrength;
    private int bleedTick;

    private boolean isBolaCaptured;
    private int bolaCapturedTick;

    private boolean isGrabbed;

    private boolean isRiding;

    @Override
    public void setBleeding(int strength, int ticks) {
        this.isBleeding = true;
        this.bleedStrength = strength;
        this.bleedTick = ticks;
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
}
