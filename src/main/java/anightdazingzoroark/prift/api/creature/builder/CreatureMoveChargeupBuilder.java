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
    private boolean chargeUpThenRelease;
    private boolean chargeUpWhileUse;
    private boolean canRotateWhileReleasing;
    private int maxChargeUp = 100;
    @NotNull
    private TriFunction<ICreature, Integer, Integer, Double> basePowerMultiplier = (creature, basePower, chargeUp) -> 1D;
    @NotNull
    private Function<ICreature, Double> cooldownMultiplier = creature -> 2D;

    private Consumer<ICreature> windupEndEffect;
    private Consumer<ICreature> prereleaseEndEffect;
    private Consumer<ICreature> releaseEndEffect;
    private BiConsumer<ICreature, EntityLivingBase> releaseDuringUseEffect;
    private TriConsumer<ICreature, Integer, Entity> onHitEntityDuringRelease;

    public CreatureMoveChargeupBuilder setChargeUpThenRelease() {
        return this.setChargeUpThenRelease(false);
    }

    public CreatureMoveChargeupBuilder setChargeUpThenRelease(boolean canRotateWhileReleasing) {
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
        return this.setChargeUpWhileUse(false);
    }

    public CreatureMoveChargeupBuilder setChargeUpWhileUse(boolean canRotateWhileReleasing) {
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

    public CreatureMoveChargeupBuilder setMaxChargeUp(int value) {
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
        this.cooldownMultiplier = creature -> value;
        return this;
    }

    public CreatureMoveChargeupBuilder setCooldownMultiplier(@NotNull Function<ICreature, Double> cooldownMultiplier) {
        this.cooldownMultiplier = cooldownMultiplier;
        return this;
    }

    @NotNull
    public Function<ICreature, Double> getCooldownMultiplier() {
        return this.cooldownMultiplier;
    }

    public CreatureMoveChargeupBuilder setWindupEndEffect(@NotNull Consumer<ICreature> windupEndEffect) {
        this.windupEndEffect = windupEndEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getWindupEndEffect() {
        return this.windupEndEffect;
    }

    public CreatureMoveChargeupBuilder setPrereleaseEndEffect(@NotNull Consumer<ICreature> prereleaseEndEffect) {
        this.prereleaseEndEffect = prereleaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getPrereleaseEndEffect() {
        return this.prereleaseEndEffect;
    }

    public CreatureMoveChargeupBuilder setReleaseEndEffect(@NotNull Consumer<ICreature> releaseEndEffect) {
        this.releaseEndEffect = releaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getReleaseEndEffect() {
        return this.releaseEndEffect;
    }

    public CreatureMoveChargeupBuilder setReleaseDuringUseEffect(@NotNull BiConsumer<ICreature, EntityLivingBase> releaseDuringUseEffect) {
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
        this.onHitEntityDuringRelease = onHitEntityDuringRelease;
        return this;
    }

    @Nullable
    public TriConsumer<ICreature, Integer, Entity> getOnHitEntityDuringRelease() {
        return this.onHitEntityDuringRelease;
    }

    @NotNull
    public CreatureMoveChargeupBuilder copy() {
        CreatureMoveChargeupBuilder toReturn = new CreatureMoveChargeupBuilder();
        toReturn.chargeUpThenRelease = this.chargeUpThenRelease;
        toReturn.chargeUpWhileUse = this.chargeUpWhileUse;
        toReturn.canRotateWhileReleasing = this.canRotateWhileReleasing;
        toReturn.maxChargeUp = this.maxChargeUp;
        toReturn.basePowerMultiplier = this.basePowerMultiplier;
        toReturn.cooldownMultiplier = this.cooldownMultiplier;
        toReturn.windupEndEffect = this.windupEndEffect;
        toReturn.prereleaseEndEffect = this.prereleaseEndEffect;
        toReturn.releaseEndEffect = this.releaseEndEffect;
        toReturn.releaseDuringUseEffect = this.releaseDuringUseEffect;
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
