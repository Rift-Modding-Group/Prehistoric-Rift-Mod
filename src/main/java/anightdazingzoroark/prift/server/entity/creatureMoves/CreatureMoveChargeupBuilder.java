package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Contains information about a move's windup.
 * */
public class CreatureMoveChargeupBuilder {
    private boolean chargeUpThenRelease;
    private boolean chargeUpWhileUse;
    private Function<RiftCreature, Integer> buildup;
    private int maxChargeUp = 100;
    @NotNull
    private Function<RiftCreature, Double> cooldownMultiplier = creature -> 2D;

    /**
     * Make it so that the creature charges up the move, then releases it
     * */
    public CreatureMoveChargeupBuilder setChargeUpThenRelease() {
        if (this.chargeUpWhileUse) return this;
        this.chargeUpThenRelease = true;
        return this;
    }

    public boolean getChargeUpThenRelease() {
        return this.chargeUpThenRelease;
    }

    /**
     * Make it so that the creature charges up and uses the move at the same time
     * */
    public CreatureMoveChargeupBuilder setChargeUpWhileUse() {
        if (this.chargeUpThenRelease) return this;
        this.chargeUpWhileUse = true;
        return this;
    }

    public boolean getChargeUpWhileUse() {
        return this.chargeUpWhileUse;
    }

    /**
     * Set max charge up in ticks
     * */
    public CreatureMoveChargeupBuilder setMaxChargeUp(int value) {
        this.maxChargeUp = value;
        return this;
    }

    public int getMaxChargeUp() {
        return this.maxChargeUp;
    }

    /**
     * Set cooldown multiplier. This refers to how long in ticks a move cools down
     * after it gets used.
     * */
    public CreatureMoveChargeupBuilder setCooldownMultiplier(double value) {
        this.cooldownMultiplier = creature -> value;
        return this;
    }

    public CreatureMoveChargeupBuilder setCooldownMultiplier(@NotNull Function<RiftCreature, Double> cooldownMultiplier) {
        this.cooldownMultiplier = cooldownMultiplier;
        return this;
    }

    @NotNull
    public Function<RiftCreature, Double> getCooldownMultiplier() {
        return this.cooldownMultiplier;
    }

    public CreatureMoveChargeupBuilder copy() {
        CreatureMoveChargeupBuilder toReturn = new CreatureMoveChargeupBuilder();

        toReturn.chargeUpThenRelease = this.chargeUpThenRelease;
        toReturn.chargeUpWhileUse = this.chargeUpWhileUse;
        toReturn.buildup = this.buildup;
        toReturn.maxChargeUp = this.maxChargeUp;
        toReturn.cooldownMultiplier = this.cooldownMultiplier;

        return toReturn;
    }
}
