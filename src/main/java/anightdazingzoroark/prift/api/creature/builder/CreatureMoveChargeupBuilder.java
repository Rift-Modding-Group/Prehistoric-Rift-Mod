package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.ICreature;
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
    private Function<ICreature, Double> cooldownMultiplier = creature -> 2D;

    private Consumer<ICreature> windupEndEffect;
    private Consumer<ICreature> prereleaseEndEffect;
    private Consumer<ICreature> releaseEndEffect;
    private BiConsumer<ICreature, EntityLivingBase> releaseDuringUseEffect;

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

    @NotNull
    public CreatureMoveChargeupBuilder copy() {
        CreatureMoveChargeupBuilder toReturn = new CreatureMoveChargeupBuilder();
        toReturn.chargeUpThenRelease = this.chargeUpThenRelease;
        toReturn.chargeUpWhileUse = this.chargeUpWhileUse;
        toReturn.canRotateWhileReleasing = this.canRotateWhileReleasing;
        toReturn.maxChargeUp = this.maxChargeUp;
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
