package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CreatureMoveBuilder {
    //extremely important
    protected boolean locked;

    //all the following variables are required and must not be null, validated in isValid()
    private MoveType moveType;
    private BiConsumer<ICreature, EntityLivingBase> onMoveBeginEffect;

    //the following can be left alone
    private int movePower;
    private int moveCooldown; //will be overriden by chargeup builder cooldown
    private float staminaCost;
    private float staminaDrainPerSecond;
    private CreatureMoveChargeupBuilder moveChargeupBuilder;
    private boolean requireFindTargetToUse;
    private boolean makesContact;
    private boolean useLookBasedFacing;
    @NotNull
    private Element element = Element.NEUTRAL;
    private int elementEffectStrength;
    private Consumer<ICreature> onMoveHitEffect;
    @Nullable
    private BiConsumer<ICreature, Entity> whileMoveUseEffect;
    @Nullable
    private BiConsumer<ICreature, Entity> onTargetHitEffect;
    @Nullable
    private BiConsumer<ICreature, BlockPos> onBlockHitEffect;
    @Nullable
    private Consumer<ICreature> onMoveEndEffect;
    private boolean useCanStopMovement;
    private boolean preventMovement;
    @NotNull
    private RiddenAimingType riddenAimingType = RiddenAimingType.NONE;
    @NotNull
    private RiddenCrosshairType riddenCrosshairType = RiddenCrosshairType.POINT;
    private float @NotNull [] riddenPitchBounds = new float[]{-15f, 15f};

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
        if (this.moveChargeupBuilder != null) this.moveChargeupBuilder.lock();
        this.locked = true;
    }

    /**
     * Set the base power of a move.
     * This is involved in damage calculation in addition to the users attack stats.
     * Is completely ignored if the move is a status move.
     * */
    public CreatureMoveBuilder setBasePower(int value) {
        this.checkIfLocked();

        this.movePower = value;
        return this;
    }

    public int getBasePower() {
        return this.movePower;
    }

    /**
     * Set the cooldown in ticks for a move.
     * */
    public CreatureMoveBuilder setCooldown(int value) {
        this.checkIfLocked();

        this.moveCooldown = value;
        return this;
    }

    public int getMoveCooldown() {
        return this.moveCooldown;
    }

    /**
     * How much stamina (in percentage of the creature's max stamina) this move uses
     * */
    public CreatureMoveBuilder setStaminaCost(float value) {
        this.checkIfLocked();
        if (value < 0f || value > 1f) {
            throw new IllegalArgumentException("Stamina cost must be a percentage between 0 and 1!");
        }

        this.staminaCost = value;
        return this;
    }

    public float getStaminaCost() {
        return this.staminaCost;
    }

    /**
     * How much stamina (in percentage of the creature's max stamina) this move uses
     * when releasing. Mostly for moves with a chargeup feature.
     * */
    public CreatureMoveBuilder setStaminaDrainPerSecond(float value) {
        this.checkIfLocked();
        if (value < 0f || value > 1f) {
            throw new IllegalArgumentException("Stamina drain must be a percentage between 0 and 1!");
        }

        this.staminaDrainPerSecond = value;
        return this;
    }

    public float getStaminaDrainPerSecond() {
        return this.staminaDrainPerSecond;
    }

    /**
     * Set the chargeup information for this move
     * */
    public CreatureMoveBuilder setMoveChargeupBuilder(@NotNull CreatureMoveChargeupBuilder moveChargeupBuilder) {
        this.checkIfLocked();

        this.moveChargeupBuilder = moveChargeupBuilder;
        return this;
    }

    @Nullable
    public CreatureMoveChargeupBuilder getMoveChargeupBuilder() {
        return this.moveChargeupBuilder;
    }

    /**
     * Set that this move will be used only if the creature has encountered a target
     * */
    public CreatureMoveBuilder setRequireFindTargetToUse() {
        this.checkIfLocked();

        this.requireFindTargetToUse = true;
        return this;
    }

    public boolean getRequireFindTargetToUse() {
        return this.requireFindTargetToUse;
    }

    /**
     * Not all physical moves make contact and not all elemental moves are ranged,
     * so this one is to be used to define whether or not a move makes contact with the target
     * */
    public CreatureMoveBuilder setMakesContact() {
        this.checkIfLocked();

        this.makesContact = true;
        return this;
    }

    public boolean getMakesContact() {
        return this.makesContact;
    }

    /**
     * Keep the user stationary throughout this move, including charge-up and recovery.
     * */
    public CreatureMoveBuilder setPreventMovement() {
        this.checkIfLocked();
        this.preventMovement = true;
        return this;
    }

    public boolean getPreventMovement() {
        return this.preventMovement;
    }

    /**
     * Face the target at move start, accounting for the look yaw retained by the animation.
     * */
    @NotNull
    public CreatureMoveBuilder setUseLookBasedFacing() {
        this.checkIfLocked();
        this.useLookBasedFacing = true;
        return this;
    }

    public boolean getUseLookBasedFacing() {
        return this.useLookBasedFacing;
    }

    /**
     * Controls how this move behaves while its rider holds the use-item button.
     * */
    public CreatureMoveBuilder setRiddenAimingType(@NotNull RiddenAimingType riddenAimingType) {
        this.checkIfLocked();

        this.riddenAimingType = riddenAimingType;
        return this;
    }

    @NotNull
    public RiddenAimingType getRiddenAimingType() {
        return this.riddenAimingType;
    }

    /**
     * set move type of move
     * */
    public CreatureMoveBuilder setMoveType(@NotNull MoveType moveType) {
        this.checkIfLocked();
        this.moveType = moveType;
        return this;
    }

    public MoveType getMoveType() {
        return this.moveType;
    }

    public CreatureMoveBuilder setElement(@NotNull Element element) {
        this.checkIfLocked();
        this.element = element;
        return this;
    }

    @NotNull
    public Element getElement() {
        return this.element;
    }

    /**
     * Set what will happen when the creature starts using the move
     * */
    public CreatureMoveBuilder setOnMoveBeginEffect(@NotNull BiConsumer<ICreature, EntityLivingBase> onMoveBeginEffect) {
        this.checkIfLocked();

        this.onMoveBeginEffect = onMoveBeginEffect;
        return this;
    }

    public BiConsumer<ICreature, EntityLivingBase> getOnMoveBeginEffect() {
        return this.onMoveBeginEffect;
    }

    /**
     * Set what will happen when the move's animation reaches the "hit" phase
     * */
    public CreatureMoveBuilder setOnMoveHitEffect(@NotNull Consumer<ICreature> onMoveHitEffect) {
        this.checkIfLocked();

        this.onMoveHitEffect = onMoveHitEffect;
        return this;
    }

    public Consumer<ICreature> getOnMoveHitEffect() {
        return this.onMoveHitEffect;
    }

    /**
     * Add additional effects for what will happen when the move ends.
     * */
    public CreatureMoveBuilder setOnMoveEndEffect(@NotNull Consumer<ICreature> onMoveEnd) {
        this.checkIfLocked();

        this.onMoveEndEffect = onMoveEnd;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getOnMoveEndEffect() {
        return this.onMoveEndEffect;
    }

    /**
     * Set what will happen exclusively while the move is being used, start to end
     * */
    public CreatureMoveBuilder setWhileMoveUseEffect(@NotNull BiConsumer<ICreature, Entity> whileMoveUseEffect) {
        this.checkIfLocked();
        this.whileMoveUseEffect = whileMoveUseEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, Entity> getWhileMoveUseEffect() {
        return this.whileMoveUseEffect;
    }

    /**
     * Set any additional effects that will happen when attacking an entity
     * */
    public CreatureMoveBuilder setOnHitTargetEffect(@NotNull BiConsumer<ICreature, Entity> onTargetHitEffect) {
        this.checkIfLocked();

        this.onTargetHitEffect = onTargetHitEffect;
        return this;
    }

    @Nullable
    public BiConsumer<ICreature, Entity> getOnTargetHitEffect() {
        return this.onTargetHitEffect;
    }

    @NotNull
    public CreatureMoveBuilder setRiddenCrosshairType(@NotNull RiddenCrosshairType type) {
        this.checkIfLocked();
        this.riddenCrosshairType = type;
        return this;
    }

    @NotNull
    public RiddenCrosshairType getRiddenCrosshairType() {
        return this.riddenCrosshairType;
    }

    /**
     * Vertical mouse limits, in degrees; negative pitch aims upward.
     * */
    @NotNull
    public CreatureMoveBuilder setRiddenPitchBounds(float min, float max) {
        this.checkIfLocked();
        if (min < -90f || max > 90f || min > max) {
            throw new IllegalArgumentException("Ridden pitch bounds must be ordered within -90 and 90 degrees!");
        }
        this.riddenPitchBounds = new float[]{min, max};
        return this;
    }

    public float @NotNull [] getRiddenPitchBounds() {
        return this.riddenPitchBounds.clone();
    }

    /**
     * Get validity based on if some params are not null
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        if (this.moveType == null) return false;
        if (this.staminaDrainPerSecond > 0f && this.moveChargeupBuilder == null) return false;
        if (this.moveChargeupBuilder != null) {
            return this.moveChargeupBuilder.getChargeUpThenRelease() || this.moveChargeupBuilder.getChargeUpWhileUse();
        }
        return true;
    }

    /**
     * Create a copy of this builder
     * */
    @NotNull
    public CreatureMoveBuilder copy() {
        CreatureMoveBuilder toReturn = new CreatureMoveBuilder();

        toReturn.moveType = this.moveType;
        toReturn.onMoveBeginEffect = this.onMoveBeginEffect;

        toReturn.movePower = this.movePower;
        toReturn.moveCooldown = this.moveCooldown;
        toReturn.staminaCost = this.staminaCost;
        toReturn.staminaDrainPerSecond = this.staminaDrainPerSecond;
        toReturn.moveChargeupBuilder = this.moveChargeupBuilder == null ? null : this.moveChargeupBuilder.copy();
        toReturn.requireFindTargetToUse = this.requireFindTargetToUse;
        toReturn.makesContact = this.makesContact;
        toReturn.useLookBasedFacing = this.useLookBasedFacing;
        toReturn.element = this.element;
        toReturn.elementEffectStrength = this.elementEffectStrength;
        toReturn.onMoveHitEffect = this.onMoveHitEffect;
        toReturn.whileMoveUseEffect = this.whileMoveUseEffect;
        toReturn.onTargetHitEffect = this.onTargetHitEffect;
        toReturn.onBlockHitEffect = this.onBlockHitEffect;
        toReturn.onMoveEndEffect = this.onMoveEndEffect;
        toReturn.useCanStopMovement = this.useCanStopMovement;
        toReturn.preventMovement = this.preventMovement;
        toReturn.riddenAimingType = this.riddenAimingType;
        toReturn.riddenCrosshairType = this.riddenCrosshairType;
        toReturn.riddenPitchBounds = this.riddenPitchBounds.clone();

        return toReturn;
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a move builder cannot be called after the move is registered!");
    }

    public enum MoveType {
        PHYSICAL,
        SPECIAL,
        STATUS;

        @NotNull
        public String getTranslatedName() {
            return I18n.format("move.creature.type." + this.name().toLowerCase());
        }
    }

    /**
     * This affects what kind of crosshair will be used when the move is aiming.
     * It affects how many entities or blocks will be hit by a move.
     * */
    public enum RiddenCrosshairType {
        POINT,
        WIDE
    }

    public enum RiddenAimingType {
        NONE,
        RANGED,
        TARGETED_MELEE
    }
}
