package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.server.entity.Element;

public class CreatureMoveBuilder {
    //all the following variables are required and must not be null, validated in isValid()
    private String moveName;
    private CreatureMoveEnums.MoveType moveType;

    //the following can be left alone
    private int movePower;
    private boolean makesContact;
    private Element element;
    private double elementEffectChance;
    private int elementEffectStrength;

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
     * Get validity based on if some params are not null
     * */
    public boolean isValid() {
        return this.moveName != null && this.moveType != null;
    }
}
