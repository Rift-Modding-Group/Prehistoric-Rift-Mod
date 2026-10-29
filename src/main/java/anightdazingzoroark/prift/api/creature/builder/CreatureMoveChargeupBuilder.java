package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.riftlib.util.TriFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Contains information about a move's windup.
 */
public class CreatureMoveChargeupBuilder {
    private boolean locked;
    private boolean chargeUpThenRelease;
    private boolean chargeUpWhileUse;
    private boolean canRotateWhileReleasing;
    private boolean canRotateWhileCharging;
    private boolean canLookWhileUsing;
    private int maxChargeUp = 100;
    @NotNull
    private TriFunction<ICreature, Integer, Integer, Double> basePowerMultiplier = (creature, basePower, chargeUp) -> 1D;
    @NotNull
    private Function<ICreature, Double> cooldownMultiplier = creature -> 2D;

    @Nullable
    private BiConsumer<ICreature, Integer> windupEndEffect;
    private Consumer<ICreature> prereleaseEndEffect;
    private Consumer<ICreature> releaseEndEffect;
    private BiConsumer<ICreature, Integer> windupEffect;
    private BiConsumer<ICreature, EntityLivingBase> releaseDuringUseEffect;
    private TriConsumer<ICreature, Integer, Entity> onHitEntityDuringRelease;

    public CreatureMoveChargeupBuilder setChargeUpThenRelease() {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        return this.setChargeUpThenRelease(false);
    }

    public CreatureMoveChargeupBuilder setChargeUpThenRelease(boolean canRotateWhileReleasing) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        if (this.chargeUpWhileUse) {
            throw new IllegalStateException("A chargeup builder can only have one of chargeUpThenRelease and chargeUpWhileUse and not both!");
        }
        this.chargeUpThenRelease = true;
        this.canRotateWhileReleasing = canRotateWhileReleasing;
        return this;
    }

    public boolean getChargeUpThenRelease() {
        return this.chargeUpThenRelease;
    }

    public CreatureMoveChargeupBuilder setChargeUpWhileUse() {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        return this.setChargeUpWhileUse(false);
    }

    public CreatureMoveChargeupBuilder setChargeUpWhileUse(boolean canRotateWhileReleasing) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        if (this.chargeUpThenRelease) {
            throw new IllegalStateException("A chargeup builder can only have one of chargeUpThenRelease and chargeUpWhileUse and not both!");
        }
        this.chargeUpWhileUse = true;
        this.canRotateWhileReleasing = canRotateWhileReleasing;
        return this;
    }

    public boolean getChargeUpWhileUse() {
        return this.chargeUpWhileUse;
    }

    public boolean getCanRotateWhileReleasing() {
        return this.canRotateWhileReleasing;
    }

    /** Allow aiming during prewindup and windup, independently of movement. */
    public CreatureMoveChargeupBuilder setCanRotateWhileCharging(boolean value) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.canRotateWhileCharging = value;
        return this;
    }

    public boolean getCanRotateWhileCharging() {
        return this.canRotateWhileCharging;
    }

    /**
     * Keep normal head tracking active throughout this move's charge-up, release, and recovery.
     * */
    @NotNull
    public CreatureMoveChargeupBuilder setCanLookWhileUsing(boolean value) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.canLookWhileUsing = value;
        return this;
    }

    public boolean getCanLookWhileUsing() {
        return this.canLookWhileUsing;
    }

    public CreatureMoveChargeupBuilder setMaxChargeUp(int value) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.maxChargeUp = value;
        return this;
    }

    public int getMaxChargeUp() {
        return this.maxChargeUp;
    }

    /**
     * Set a base power move multiplier based on the charge
     * */
    public CreatureMoveChargeupBuilder setBasePowerMultiplier(@NotNull TriFunction<ICreature, Integer, Integer, Double> basePowerMultiplier) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.basePowerMultiplier = basePowerMultiplier;
        return this;
    }

    @NotNull
    public TriFunction<ICreature, Integer, Integer, Double> getBasePowerMultiplier() {
        return this.basePowerMultiplier;
    }

    /**
     * Set a cooldown multiplier based on the charge
     * */
    public CreatureMoveChargeupBuilder setCooldownMultiplier(double value) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.cooldownMultiplier = creature -> value;
        return this;
    }

    public CreatureMoveChargeupBuilder setCooldownMultiplier(@NotNull Function<ICreature, Double> cooldownMultiplier) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.cooldownMultiplier = cooldownMultiplier;
        return this;
    }

    @NotNull
    public Function<ICreature, Double> getCooldownMultiplier() {
        return this.cooldownMultiplier;
    }

    /**
     * Sets the effect called when windup ends, receiving the creature and its accumulated charge ticks.
     */
    @NotNull
    public CreatureMoveChargeupBuilder setWindupEndEffect(@NotNull BiConsumer<ICreature, Integer> windupEndEffect) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.windupEndEffect = windupEndEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, Integer> getWindupEndEffect() {
        return this.windupEndEffect;
    }

    public CreatureMoveChargeupBuilder setPrereleaseEndEffect(@NotNull Consumer<ICreature> prereleaseEndEffect) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.prereleaseEndEffect = prereleaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getPrereleaseEndEffect() {
        return this.prereleaseEndEffect;
    }

    public CreatureMoveChargeupBuilder setReleaseEndEffect(@NotNull Consumer<ICreature> releaseEndEffect) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.releaseEndEffect = releaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getReleaseEndEffect() {
        return this.releaseEndEffect;
    }

    /**
     * What happens while a creature is winding up
     * */
    public CreatureMoveChargeupBuilder setWindupEffect(@NotNull BiConsumer<ICreature, Integer> windupEffect) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.windupEffect = windupEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, Integer> getWindupEffect() {
        return this.windupEffect;
    }

    public CreatureMoveChargeupBuilder setReleaseDuringUseEffect(@NotNull BiConsumer<ICreature, EntityLivingBase> releaseDuringUseEffect) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.releaseDuringUseEffect = releaseDuringUseEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, EntityLivingBase> getReleaseDuringUseEffect() {
        return this.releaseDuringUseEffect;
    }

    /**
     * What happens when entities are hit while in the release state
     * */
    public CreatureMoveChargeupBuilder setOnHitEntityDuringRelease(@NotNull TriConsumer<ICreature, Integer, Entity> onHitEntityDuringRelease) {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
        this.onHitEntityDuringRelease = onHitEntityDuringRelease;
        return this;
    }

    @Nullable
    public TriConsumer<ICreature, Integer, Entity> getOnHitEntityDuringRelease() {
        return this.onHitEntityDuringRelease;
    }

    public void lock() {
        this.locked = true;
    }

    @NotNull
    public CreatureMoveChargeupBuilder copy() {
        CreatureMoveChargeupBuilder toReturn = new CreatureMoveChargeupBuilder();
        toReturn.chargeUpThenRelease = this.chargeUpThenRelease;
        toReturn.chargeUpWhileUse = this.chargeUpWhileUse;
        toReturn.canRotateWhileReleasing = this.canRotateWhileReleasing;
        toReturn.canRotateWhileCharging = this.canRotateWhileCharging;
        toReturn.canLookWhileUsing = this.canLookWhileUsing;
        toReturn.maxChargeUp = this.maxChargeUp;
        toReturn.basePowerMultiplier = this.basePowerMultiplier;
        toReturn.cooldownMultiplier = this.cooldownMultiplier;
        toReturn.windupEndEffect = this.windupEndEffect;
        toReturn.prereleaseEndEffect = this.prereleaseEndEffect;
        toReturn.releaseEndEffect = this.releaseEndEffect;
        toReturn.windupEffect = this.windupEffect;
        toReturn.releaseDuringUseEffect = this.releaseDuringUseEffect;
        toReturn.onHitEntityDuringRelease = this.onHitEntityDuringRelease;
        return toReturn;
    }

    public enum ChargeupPhase {
        PREWINDUP,
        WINDUP,
        PRERELEASING,
        RELEASING,
        FINISHING
    }
}
