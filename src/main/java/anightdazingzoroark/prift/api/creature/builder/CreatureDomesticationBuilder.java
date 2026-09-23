package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import org.jetbrains.annotations.NotNull;

//builder to describe domestication for this creature
public class CreatureDomesticationBuilder {
    //extremely important
    protected boolean locked;

    //all the following variables are required and must not be null, validated in isValid()
    private RiftCreatureEnums.TamingMethod tamingMethod;

    //can be left alone
    private int inventorySize = 27;
    private boolean isRideable;

    /**
     * set le taming method
     * */
    public CreatureDomesticationBuilder setTamingMethod(@NotNull RiftCreatureEnums.TamingMethod tamingMethod) {
        this.checkIfLocked();

        this.tamingMethod = tamingMethod;
        return this;
    }

    public RiftCreatureEnums.TamingMethod getTamingMethod() {
        return this.tamingMethod;
    }

    /**
     * Set the creature's inventory size
     * */
    public CreatureDomesticationBuilder setInventorySize(int value) {
        this.checkIfLocked();
        if (value < 1) throw new IllegalArgumentException("Creature inventory size must be at least 1!");

        this.inventorySize = value;
        return this;
    }

    public int getInventorySize() {
        return this.inventorySize;
    }

    /**
     * Make sure the creature can be saddled for riding
     * */
    public CreatureDomesticationBuilder setIsRideable() {
        this.checkIfLocked();

        this.isRideable = true;
        return this;
    }

    public boolean getIsRideable() {
        return this.isRideable;
    }

    /**
     * Get validity based on if all params are not null
     * */
    public boolean isValid() {
        return this.tamingMethod != null;
    }

    /**
     * Lock this builder after its creature type is registered.
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a creature domestication builder cannot be called after the creature is created!");
    }
}
