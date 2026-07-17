package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Contains information about a move's windup.
 * */
public class CreatureMoveChargeupBuilder {
    private boolean chargeUpThenRelease;
    private boolean chargeUpWhileUse;
    @NotNull
    private Function<RiftCreature, Integer> buildup = creature -> 1;
    private int maxChargeUp = 100;
    @NotNull
    private Function<RiftCreature, Double> cooldownMultiplier = creature -> 2D;

    private Consumer<RiftCreature> windupEndEffect;
    private Consumer<RiftCreature> prereleaseEndEffect;
    private Consumer<RiftCreature> releaseEndEffect;

    /**
     * Make it so that the creature charges up the move, then releases it
     * */
    public CreatureMoveChargeupBuilder setChargeUpThenRelease() {
        if (this.chargeUpWhileUse) return this; //todo: add an exception
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
        if (this.chargeUpThenRelease) return this; //todo: add an exception here too
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
     * Set how much buildup this move gains per charge tick.
     * */
    public CreatureMoveChargeupBuilder setBuildup(int value) {
        this.buildup = creature -> value;
        return this;
    }

    public CreatureMoveChargeupBuilder setBuildup(@NotNull Function<RiftCreature, Integer> buildup) {
        this.buildup = buildup;
        return this;
    }

    @NotNull
    public Function<RiftCreature, Integer> getBuildup() {
        return this.buildup;
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

    /**
     * Set what will happen when ending the windup of a move
     * */
    public CreatureMoveChargeupBuilder setWindupEndEffect(@NotNull Consumer<RiftCreature> windupEndEffect) {
        this.windupEndEffect = windupEndEffect;
        return this;
    }

    @Nullable
    public Consumer<RiftCreature> getWindupEndEffect() {
        return this.windupEndEffect;
    }

    /**
     * Set what will happen in the end of the transition of the windup and release phases
     * */
    public CreatureMoveChargeupBuilder setPrereleaseEndEffect(@NotNull Consumer<RiftCreature> prereleaseEndEffect) {
        this.prereleaseEndEffect = prereleaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<RiftCreature> getPrereleaseEndEffect() {
        return this.prereleaseEndEffect;
    }

    /**
     * Set what will happen in the end of the release phase
     * */
    public CreatureMoveChargeupBuilder setReleaseEndEffect(@NotNull Consumer<RiftCreature> releaseEndEffect) {
        this.releaseEndEffect = releaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<RiftCreature> getReleaseEndEffect() {
        return this.releaseEndEffect;
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
        toReturn.windupEndEffect = this.windupEndEffect;
        toReturn.prereleaseEndEffect = this.prereleaseEndEffect;
        toReturn.releaseEndEffect = this.releaseEndEffect;

        return toReturn;
    }

    public enum ChargeupPhase {
        PREWINDUP, //before proper windup
        WINDUP, //winding up a chargeup move
        PRERELEASING, //between windup and releasing. the end of this phase is when the move hit starts
        RELEASING, //releasing a chargeup move
        FINISHING //recovering from using a chargeup move
    }
}
