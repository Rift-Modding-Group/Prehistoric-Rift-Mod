package anightdazingzoroark.prift.api.creature.builder;

import org.jetbrains.annotations.NotNull;

/**
 * Defines how a creature navigates in the world.
 * */
public class CreatureNavigationBuilder {
    //extremely important
    protected boolean locked;

    private boolean canWalk;

    private boolean canSwim;
    private String[] swimmingBlockWhitelist;

    private boolean canFly;

    private boolean canLeap;
    private double leapHeight;
    private int leapDelay;
    private double leapDistance;

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
        this.locked = true;
    }

    /**
     * Allow land navigation.
     * */
    public CreatureNavigationBuilder setCanWalk() {
        this.checkIfLocked();
        this.canWalk = true;
        return this;
    }

    public boolean getCanWalk() {
        return this.canWalk;
    }

    /**
     * Allows swimming navigation. By default it's just water, but in practice liquids and
     * even solid blocks can be considered.
     * */
    public CreatureNavigationBuilder setCanSwim() {
        return this.setCanSwim("minecraft:water");
    }

    public CreatureNavigationBuilder setCanSwim(@NotNull String... swimmingBlockWhitelist) {
        this.checkIfLocked();
        for (String blockName : swimmingBlockWhitelist) {
            if (blockName.isBlank()) {
                throw new IllegalArgumentException("A swimming block whitelist cannot contain a null or blank name!");
            }
        }

        this.canSwim = true;
        this.swimmingBlockWhitelist = swimmingBlockWhitelist.clone();
        return this;
    }

    public boolean getCanSwim() {
        return this.canSwim;
    }

    @NotNull
    public String[] getSwimmingBlockWhitelist() {
        return this.swimmingBlockWhitelist.clone();
    }

    /**
     * Allow navigation through flight.
     * */
    public CreatureNavigationBuilder setCanFly() {
        this.checkIfLocked();
        this.canFly = true;
        return this;
    }

    public boolean getCanFly() {
        return this.canFly;
    }

    /**
     * Allow walking pathfinding to cross obstacles and gaps which require a full leap
     * This also allows the player to make the creature jump when riding them.
     * */
    public CreatureNavigationBuilder setCanLeap(double leapHeight, int leapDelay, double leapDistance) {
        this.checkIfLocked();
        if (leapHeight <= 1D) {
            throw new IllegalArgumentException("Leap height must be greater than one block!");
        }
        if (leapDelay < 0) throw new IllegalArgumentException("Leap delay cannot be negative!");
        if (leapDistance <= 0D) {
            throw new IllegalArgumentException("Leap distance must be positive!");
        }

        this.canLeap = true;
        this.leapHeight = leapHeight;
        this.leapDelay = leapDelay;
        this.leapDistance = leapDistance;
        return this;
    }

    public boolean getCanLeap() {
        return this.canLeap;
    }

    public double getLeapHeight() {
        return this.leapHeight;
    }

    public int getLeapDelay() {
        return this.leapDelay;
    }

    public double getLeapDistance() {
        return this.leapDistance;
    }

    /**
     * A creature needs at least one primary way to navigate.
     * */
    public boolean isValid() {
        return (this.canWalk || this.canSwim || this.canFly) && (!this.canLeap || this.canWalk);
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) {
            throw new IllegalCallerException("A setter for a creature navigation builder cannot be called after the navigation is registered!");
        }
    }
}
