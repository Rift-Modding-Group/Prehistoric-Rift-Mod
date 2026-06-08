package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.info.Element;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CreatureMoveBuilder {
    //all the following variables are required and must not be null, validated in isValid()
    private CreatureMoveHelper.MoveType moveType;
    private Consumer<RiftCreature> onMoveHitEffect;
    private String[] animNames;

    //the following can be left alone
    private int movePower;
    private boolean requireFindTargetToUse;
    private boolean makesContact;
    private Element element;
    private double elementEffectChance;
    private int elementEffectStrength;
    private BiConsumer<RiftCreature, Entity> onTargetHitEffect;
    private BiConsumer<RiftCreature, BlockPos> onBlockHitEffect;
    private Consumer<RiftCreature> onMoveEndEffect;
    private boolean useCanStopMovement;

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
        this.moveType = CreatureMoveHelper.MoveType.PHYSICAL;
        return this;
    }

    /**
     * set move to be elemental and also add the chance its effect gets added
     * as well as the strength of that effect
     * if not defined, physical damage will be used instead
     * */
    public CreatureMoveBuilder setElemental(Element element, double elementEffectChance, int elementEffectStrength) {
        this.moveType = CreatureMoveHelper.MoveType.ELEMENTAL;
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
        this.moveType = CreatureMoveHelper.MoveType.STATUS;
        return this;
    }

    /**
     * general getter for move type
     * */
    public CreatureMoveHelper.MoveType getMoveType() {
        return this.moveType;
    }

    /**
     * Set what will happen when the move's animation reaches the "hit" phase
     * */
    public CreatureMoveBuilder setOnMoveHitEffect(Consumer<RiftCreature> onMoveHitEffect) {
        this.onMoveHitEffect = onMoveHitEffect;
        return this;
    }

    public Consumer<RiftCreature> getOnMoveHitEffect() {
        return this.onMoveHitEffect;
    }

    /**
     * Add additional effects for what will happen when the move ends.
     * */
    public CreatureMoveBuilder setOnMoveEndEffect(Consumer<RiftCreature> onMoveEnd) {
        this.onMoveEndEffect = onMoveEnd;
        return this;
    }

    public Consumer<RiftCreature> getOnMoveEndEffect() {
        return this.onMoveEndEffect;
    }

    /**
     * Sets the name of the animations to use when using this move.
     * If multiple names are provided, it will randomly switch between the animations.s
     * */
    public CreatureMoveBuilder setAnimNames(String... animNames) {
        if (this.animNames != null) return this;
        this.animNames = animNames;
        return this;
    }

    public String[] getAnimNames() {
        return this.animNames;
    }

    /**
     * Set any additional effects that will happen when attacking an entity
     * */
    public CreatureMoveBuilder setOnHitTargetEffect(BiConsumer<RiftCreature, Entity> onTargetHitEffect) {
        this.onTargetHitEffect = onTargetHitEffect;
        return this;
    }

    public BiConsumer<RiftCreature, Entity> getOnTargetHitEffect() {
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
        return this.moveType != null && this.onMoveHitEffect != null && this.animNames != null && this.animNames.length > 0;
    }

    /**
     * Create a copy of this builder
     * */
    public CreatureMoveBuilder copy() {
        CreatureMoveBuilder toReturn = new CreatureMoveBuilder();

        toReturn.moveType = this.moveType;
        toReturn.onMoveHitEffect = this.onMoveHitEffect;
        toReturn.animNames = this.animNames;

        toReturn.movePower = this.movePower;
        toReturn.requireFindTargetToUse = this.requireFindTargetToUse;
        toReturn.makesContact = this.makesContact;
        toReturn.element = this.element;
        toReturn.elementEffectChance = this.elementEffectChance;
        toReturn.elementEffectStrength = this.elementEffectStrength;
        toReturn.onTargetHitEffect = this.onTargetHitEffect;
        toReturn.onBlockHitEffect = this.onBlockHitEffect;
        toReturn.useCanStopMovement = this.useCanStopMovement;

        return toReturn;
    }
}
