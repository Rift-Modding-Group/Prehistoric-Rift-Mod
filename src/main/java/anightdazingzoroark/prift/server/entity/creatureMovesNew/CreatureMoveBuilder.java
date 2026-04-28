package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.server.entity.Element;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CreatureMoveBuilder {
    //all the following variables are required and must not be null, validated in isValid()
    private String moveName;
    private CreatureMoveEnums.MoveType moveType;
    private BiFunction<RiftCreatureNew, Entity, Integer> canUsePredicate;
    private Consumer<RiftCreatureNew> onMoveHitEffect;

    //the following can be left alone
    private int movePower;
    private boolean requireFindTargetToUse;
    private boolean makesContact;
    private Element element;
    private double elementEffectChance;
    private int elementEffectStrength;
    private BiConsumer<RiftCreatureNew, Entity> onTargetHitEffect;
    private BiConsumer<RiftCreatureNew, BlockPos> onBlockHitEffect;
    private boolean useCanStopMovement;

    /**
     * Set the name of the move, is required
     * */
    public CreatureMoveBuilder setName(String name) {
        this.moveName = name;
        return this;
    }

    public String getName() {
        return this.moveName;
    }

    /**
     * Set the base power of a move.
     * This is involved in damage calculation in addition to the users attack stats.
     * Is completely ignored if the move is a status move.
     * */
    public CreatureMoveBuilder setBasePower(int value) {
        this.movePower = value;
        return this;
    }

    public int getBasePower() {
        return this.movePower;
    }

    /**
     * Set that this move will be used only if the creature has encountered a target
     * */
    public CreatureMoveBuilder setRequireFindTargetToUse() {
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
        this.moveType = CreatureMoveEnums.MoveType.PHYSICAL;
        return this;
    }

    /**
     * set move to be elemental and also add the chance its effect gets added
     * as well as the strength of that effect
     * if not defined, physical damage will be used instead
     * */
    public CreatureMoveBuilder setElemental(Element element, double elementEffectChance, int elementEffectStrength) {
        this.moveType = CreatureMoveEnums.MoveType.ELEMENTAL;
        this.element = element;
        this.elementEffectChance = elementEffectChance;
        this.elementEffectStrength = elementEffectStrength;
        return this;
    }

    public Element getElement() {
        return this.element;
    }

    public double getElementEffectChance() {
        return this.elementEffectChance;
    }

    public int getElementEffectStrength() {
        return this.elementEffectStrength;
    }

    /**
     * Set move to be status
     * */
    public CreatureMoveBuilder setStatus() {
        this.moveType = CreatureMoveEnums.MoveType.STATUS;
        return this;
    }

    /**
     * general getter for move type
     * */
    public CreatureMoveEnums.MoveType getMoveType() {
        return this.moveType;
    }

    /**
     * Set the conditions in which the creature can use this move. This affects when on its own
     * and when being ridden. The function parameters are the creature that will use it and the
     * potential target it can attack. The return value is an integer that represents the priority in
     * which the move will be used. Higher priority values means the move will be more likely
     * to be used. A negative priority means it will never be used.
     * */
    public CreatureMoveBuilder setCanUsePredicate(BiFunction<RiftCreatureNew, Entity, Integer> canUsePredicate) {
        this.canUsePredicate = canUsePredicate;
        return this;
    }

    public BiFunction<RiftCreatureNew, Entity, Integer> getCanUsePredicate() {
        return this.canUsePredicate;
    }

    /**
     * Set what will happen when the move's animation reaches the "hit" phase
     * */
    public CreatureMoveBuilder setOnMoveHitEffect(Consumer<RiftCreatureNew> onMoveHitEffect) {
        this.onMoveHitEffect = onMoveHitEffect;
        return this;
    }

    public Consumer<RiftCreatureNew> getOnMoveHitEffect() {
        return this.onMoveHitEffect;
    }

    /**
     * Set any additional effects that will happen when attacking an entity
     * */
    public CreatureMoveBuilder setOnHitTargetEffect(BiConsumer<RiftCreatureNew, Entity> onTargetHitEffect) {
        this.onTargetHitEffect = onTargetHitEffect;
        return this;
    }

    public BiConsumer<RiftCreatureNew, Entity> getOnTargetHitEffect() {
        return this.onTargetHitEffect;
    }

    /**
     * Make it so that when the move is being used, the user cannot move
     * */
    public CreatureMoveBuilder setUseCanStopMovement() {
        this.useCanStopMovement = true;
        return this;
    }

    public boolean getUseCanStopMovement() {
        return this.useCanStopMovement;
    }

    /**
     * Get validity based on if some params are not null
     * */
    public boolean isValid() {
        return this.moveType != null && this.canUsePredicate != null && this.onMoveHitEffect != null;
    }
}
