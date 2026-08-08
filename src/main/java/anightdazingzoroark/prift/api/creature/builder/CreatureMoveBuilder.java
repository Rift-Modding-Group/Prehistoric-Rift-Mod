package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CreatureMoveBuilder {
    //extremely important
    protected boolean locked;

    //all the following variables are required and must not be null, validated in isValid()
    private MoveType moveType;
    private BiConsumer<ICreature, EntityLivingBase> onMoveBeginEffect;
    private Consumer<ICreature> onMoveHitEffect;
    private String[] animNames;

    //the following can be left alone
    private int movePower;
    private int moveCooldown; //will be overriden by chargeup builder cooldown
    private CreatureMoveChargeupBuilder moveChargeupBuilder;
    private boolean requireFindTargetToUse;
    private boolean makesContact;
    private Element element;
    private int elementEffectStrength;
    @Nullable
    private BiConsumer<ICreature, Entity> whileMoveUseEffect;
    @Nullable
    private BiConsumer<ICreature, Entity> onTargetHitEffect;
    @Nullable
    private BiConsumer<ICreature, BlockPos> onBlockHitEffect;
    @Nullable
    private Consumer<ICreature> onMoveEndEffect;
    private boolean useCanStopMovement;

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
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
     *set move to be physical
     * */
    public CreatureMoveBuilder setPhysical() {
        this.checkIfLocked();
        if (this.moveType != null) throw new IllegalCallerException("This move builder already has a move type!");

        this.moveType = MoveType.PHYSICAL;
        return this;
    }

    /**
     * set move to be elemental and also add the chance its effect gets added
     * as well as the strength of that effect
     * if not defined, physical damage will be used instead
     * */
    public CreatureMoveBuilder setElemental(Element element, int elementEffectStrength) {
        this.checkIfLocked();
        if (this.moveType != null) throw new IllegalCallerException("This move builder already has a move type!");

        this.moveType = MoveType.ELEMENTAL;
        this.element = element;
        this.elementEffectStrength = elementEffectStrength;
        return this;
    }

    public Element getElement() {
        return this.element;
    }

    public int getElementEffectStrength() {
        return this.elementEffectStrength;
    }

    /**
     * Set move to be status
     * */
    public CreatureMoveBuilder setStatus() {
        this.checkIfLocked();
        if (this.moveType != null) throw new IllegalCallerException("This move builder already has a move type!");

        this.moveType = MoveType.STATUS;
        return this;
    }

    /**
     * general getter for move type
     * */
    public MoveType getMoveType() {
        return this.moveType;
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
     * Sets the name of the animations to use when using this move.
     * If multiple names are provided, it will randomly switch between the animations.s
     * */
    public CreatureMoveBuilder setAnimNames(@NotNull String... animNames) {
        this.checkIfLocked();

        this.animNames = animNames;
        return this;
    }

    public String[] getAnimNames() {
        return this.animNames;
    }

    /**
     * Set what will happen exclusively while the move is being used, start to end
     * */
    public CreatureMoveBuilder setWhileMoveUseEffect(@NotNull BiConsumer<ICreature, Entity> whileMoveUseEffect) {
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

    /**
     * Get validity based on if some params are not null
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        if (this.moveType == null) return false;
        if (this.moveChargeupBuilder != null) {
            return this.moveChargeupBuilder.getChargeUpThenRelease() || this.moveChargeupBuilder.getChargeUpWhileUse();
        }
        return this.onMoveHitEffect != null && this.animNames != null && this.animNames.length > 0;
    }

    /**
     * Create a copy of this builder
     * */
    @NotNull
    public CreatureMoveBuilder copy() {
        CreatureMoveBuilder toReturn = new CreatureMoveBuilder();

        toReturn.moveType = this.moveType;
        toReturn.onMoveHitEffect = this.onMoveHitEffect;
        toReturn.animNames = this.animNames;

        toReturn.movePower = this.movePower;
        toReturn.moveCooldown = this.moveCooldown;
        toReturn.moveChargeupBuilder = this.moveChargeupBuilder == null ? null : this.moveChargeupBuilder.copy();
        toReturn.requireFindTargetToUse = this.requireFindTargetToUse;
        toReturn.makesContact = this.makesContact;
        toReturn.element = this.element;
        toReturn.elementEffectStrength = this.elementEffectStrength;
        toReturn.whileMoveUseEffect = this.whileMoveUseEffect;
        toReturn.onTargetHitEffect = this.onTargetHitEffect;
        toReturn.onBlockHitEffect = this.onBlockHitEffect;
        toReturn.onMoveEndEffect = this.onMoveEndEffect;
        toReturn.useCanStopMovement = this.useCanStopMovement;

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
        ELEMENTAL,
        STATUS
    }
}
