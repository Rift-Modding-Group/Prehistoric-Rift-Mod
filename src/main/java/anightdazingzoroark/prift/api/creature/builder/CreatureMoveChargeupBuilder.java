package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.riftlib.util.TriFunction;
import net.minecraft.client.resources.I18n;
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

    //must be defined
    @Nullable
    private ChargeType chargeType;
    private boolean canRotate;
    private boolean canRotateWhileCharging;
    private boolean canLookWhileUsing;
    private int maxChargeUp = 100;
    private int releaseDuration;
    @NotNull
    private TriFunction<ICreature, Integer, Integer, Double> basePowerMultiplier = (creature, basePower, chargeUp) -> 1D;
    @NotNull
    private Function<ICreature, Double> cooldownMultiplier = creature -> 2D;
    private boolean releaseOnEndWindup;

    //can be left alone
    @Nullable
    private BiConsumer<ICreature, Integer> windupEndEffect;
    @Nullable
    private Consumer<ICreature> windupStartEffect;
    private Consumer<ICreature> prereleaseEndEffect;
    private Consumer<ICreature> releaseEndEffect;
    private BiConsumer<ICreature, Integer> windupEffect;
    private TriConsumer<ICreature, Integer, EntityLivingBase> releaseDuringUseEffect;
    private TriConsumer<ICreature, Integer, Entity> onHitEntityDuringRelease;

    @NotNull
    public CreatureMoveChargeupBuilder setChargeType(@Nullable ChargeType chargeType) {
        this.checkIfLocked();
        this.chargeType = chargeType;
        return this;
    }

    @NotNull
    public ChargeType getChargeType() {
        if (this.chargeType == null) throw new IllegalStateException("That's not meant to happen...");
        return this.chargeType;
    }

    @NotNull
    public CreatureMoveChargeupBuilder setCanRotate() {
        this.checkIfLocked();
        this.canRotate = true;
        return this;
    }

    public boolean getCanRotate() {
        return this.canRotate;
    }

    /**
     * Allow aiming during prewindup and windup, independently of movement.
     * */
    public CreatureMoveChargeupBuilder setCanRotateWhileCharging() {
        this.checkIfLocked();
        this.canRotateWhileCharging = true;
        return this;
    }

    public boolean getCanRotateWhileCharging() {
        return this.canRotateWhileCharging;
    }

    /**
     * Keep normal head tracking active throughout this move's charge-up, release, and recovery.
     * */
    @NotNull
    public CreatureMoveChargeupBuilder setCanLookWhileUsing() {
        this.checkIfLocked();
        this.canLookWhileUsing = true;
        return this;
    }

    public boolean getCanLookWhileUsing() {
        return this.canLookWhileUsing;
    }

    public CreatureMoveChargeupBuilder setMaxChargeUp(int value) {
        this.checkIfLocked();
        this.maxChargeUp = value;
        return this;
    }

    public int getMaxChargeUp() {
        return this.maxChargeUp;
    }

    /**
     * Set a fixed release duration in ticks; zero leaves release timing to the animation.
     */
    @NotNull
    public CreatureMoveChargeupBuilder setReleaseDuration(int ticks) {
        this.checkIfLocked();
        this.releaseDuration = Math.max(0, ticks);
        return this;
    }

    public int getReleaseDuration() {
        return this.releaseDuration;
    }

    /**
     * Set a base power move multiplier based on the charge
     * */
    public CreatureMoveChargeupBuilder setBasePowerMultiplier(@NotNull TriFunction<ICreature, Integer, Integer, Double> basePowerMultiplier) {
        this.checkIfLocked();
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
        this.checkIfLocked();
        this.cooldownMultiplier = creature -> value;
        return this;
    }

    public CreatureMoveChargeupBuilder setCooldownMultiplier(@NotNull Function<ICreature, Double> cooldownMultiplier) {
        this.checkIfLocked();
        this.cooldownMultiplier = cooldownMultiplier;
        return this;
    }

    @NotNull
    public Function<ICreature, Double> getCooldownMultiplier() {
        return this.cooldownMultiplier;
    }

    /**
     * Make it so that, the moment that the move windup ends, it instantly releases.
     * Meant for use on moves with
     * */
    public CreatureMoveChargeupBuilder setReleaseOnEndWindup() {
        this.checkIfLocked();
        if (this.chargeType != ChargeType.CHARGE_UP_THEN_RELEASE) throw new IllegalCallerException("This can only be defined on moves that charge up to then release!");
        this.releaseOnEndWindup = true;
        return this;
    }

    public boolean getReleaseOnEndWindup() {
        return this.releaseOnEndWindup;
    }

    /**
     * Sets the effect called when windup starts.
     */
    @NotNull
    public CreatureMoveChargeupBuilder setWindupStartEffect(@NotNull Consumer<ICreature> windupStartEffect) {
        this.checkIfLocked();
        this.windupStartEffect = windupStartEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getWindupStartEffect() {
        return this.windupStartEffect;
    }

    /**
     * Sets the effect called when windup ends, receiving the creature and its accumulated charge ticks.
     */
    @NotNull
    public CreatureMoveChargeupBuilder setWindupEndEffect(@NotNull BiConsumer<ICreature, Integer> windupEndEffect) {
        this.checkIfLocked();
        this.windupEndEffect = windupEndEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, Integer> getWindupEndEffect() {
        return this.windupEndEffect;
    }

    public CreatureMoveChargeupBuilder setPrereleaseEndEffect(@NotNull Consumer<ICreature> prereleaseEndEffect) {
        this.checkIfLocked();
        this.prereleaseEndEffect = prereleaseEndEffect;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getPrereleaseEndEffect() {
        return this.prereleaseEndEffect;
    }

    public CreatureMoveChargeupBuilder setReleaseEndEffect(@NotNull Consumer<ICreature> releaseEndEffect) {
        this.checkIfLocked();
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
        this.checkIfLocked();
        this.windupEffect = windupEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, Integer> getWindupEffect() {
        return this.windupEffect;
    }

    public CreatureMoveChargeupBuilder setReleaseDuringUseEffect(@NotNull TriConsumer<ICreature, Integer, EntityLivingBase> releaseDuringUseEffect) {
        this.checkIfLocked();
        this.releaseDuringUseEffect = releaseDuringUseEffect;
        return this;
    }

    @Nullable
    public TriConsumer<ICreature, Integer, EntityLivingBase> getReleaseDuringUseEffect() {
        return this.releaseDuringUseEffect;
    }

    /**
     * What happens when entities are hit while in the release state
     * */
    public CreatureMoveChargeupBuilder setOnHitEntityDuringRelease(@NotNull TriConsumer<ICreature, Integer, Entity> onHitEntityDuringRelease) {
        this.checkIfLocked();
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

    /**
     * Whether the required charge type has been defined.
     */
    public boolean isValid() {
        return this.chargeType != null;
    }

    @NotNull
    public CreatureMoveChargeupBuilder copy() {
        CreatureMoveChargeupBuilder toReturn = new CreatureMoveChargeupBuilder();
        toReturn.chargeType = this.chargeType;
        toReturn.canRotate = this.canRotate;
        toReturn.canRotateWhileCharging = this.canRotateWhileCharging;
        toReturn.canLookWhileUsing = this.canLookWhileUsing;
        toReturn.maxChargeUp = this.maxChargeUp;
        toReturn.releaseDuration = this.releaseDuration;
        toReturn.basePowerMultiplier = this.basePowerMultiplier;
        toReturn.cooldownMultiplier = this.cooldownMultiplier;
        toReturn.windupStartEffect = this.windupStartEffect;
        toReturn.windupEndEffect = this.windupEndEffect;
        toReturn.prereleaseEndEffect = this.prereleaseEndEffect;
        toReturn.releaseEndEffect = this.releaseEndEffect;
        toReturn.windupEffect = this.windupEffect;
        toReturn.releaseDuringUseEffect = this.releaseDuringUseEffect;
        toReturn.onHitEntityDuringRelease = this.onHitEntityDuringRelease;
        return toReturn;
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A registered move chargeup builder cannot be changed");
    }

    public enum ChargeType {
        CHARGE_UP_THEN_RELEASE,
        CHARGE_UP_WHILE_USE;

        @NotNull
        public String getTranslatedDescription() {
            return I18n.format("prift.chargeup."+this.name().toLowerCase()+".description");
        }
    }

    public enum ChargeupPhase {
        PREWINDUP,
        WINDUP,
        PRERELEASING,
        RELEASING,
        FINISHING
    }
}
