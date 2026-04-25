package anightdazingzoroark.prift.server.entity.creaturenew.builder;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;
import net.minecraft.client.resources.I18n;

import java.util.Map;

//this class defines creature information
public abstract class AbstractCreatureBuilder<T extends AbstractCreatureBuilder<T>> {
    //all the following variables are required and must not be null, validated in isValid()
    private final Class<? extends RiftCreatureNew> creatureClass;
    private Map<RiftCreatureEnums.Stats, Double> stats;
    private RiftCreatureEnums.CreatureCategory creatureCategory;
    private RiftCreatureEnums.CreatureDiet creatureDiet;
    private int[] spawnEggColors;
    private float[] scaleRangeForAge;

    //the following can be left alone
    private float[] mainHitboxSize = new float[]{1f, 1f};
    private boolean hostileToHumans;
    private boolean retaliateWhenAttacked, broadcastRetaliation;
    private boolean isNocturnal;
    private boolean canBeKnockedBack;
    private boolean flopOnLand;
    private String[] breathableBlocks = new String[]{"minecraft:air"};
    private RiftCreatureEnums.Movement[] movementOptions;
    private boolean cannotFloatOnWater = true;
    private boolean isHerder;
    private int inventorySize = 27;
    private int daysUntilAdult = 1;
    private int physicalReach = 4;

    public AbstractCreatureBuilder(Class<? extends RiftCreatureNew> creatureClass) {
        this.creatureClass = creatureClass;
    }

    @SuppressWarnings("unchecked")
    protected final T getThis() {
        return (T) this;
    }

    public Class<? extends RiftCreatureNew> getCreatureClass() {
        return this.creatureClass;
    }

    /**
     * Set the stats of the creature.
     * Stats are to be on a scale of 0.5-10 with steps of 0.5
     * and will be represented as stars on most UIs
     * */
    public T setStats(double health, double meleeAttack, double elementalAttack, double stamina, double speed) {
        this.stats = Map.of(
                RiftCreatureEnums.Stats.HEALTH, health,
                RiftCreatureEnums.Stats.MELEE_DAMAGE, meleeAttack,
                RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE, elementalAttack,
                RiftCreatureEnums.Stats.STAMINA, stamina,
                RiftCreatureEnums.Stats.SPEED, speed
        );
        return this.getThis();
    }

    public Map<RiftCreatureEnums.Stats, Double> getStats() {
        return this.stats;
    }

    /**
     * Set category of the species
     * */
    public T setCreatureCategory(RiftCreatureEnums.CreatureCategory creatureCategory) {
        this.creatureCategory = creatureCategory;
        return this.getThis();
    }

    public RiftCreatureEnums.CreatureCategory getCreatureCategory() {
        return this.creatureCategory;
    }

    /**
     * Set diet of the species
     * */
    public T setCreatureDiet(RiftCreatureEnums.CreatureDiet creatureDiet) {
        this.creatureDiet = creatureDiet;
        return this.getThis();
    }

    public RiftCreatureEnums.CreatureDiet getCreatureDiet() {
        return this.creatureDiet;
    }

    /**
     * Set the colors of the spawn egg for the creature
     * */
    public T setSpawnEggColors(int background, int foreground) {
        this.spawnEggColors = new int[]{background, foreground};
        return this.getThis();
    }

    public int[] getSpawnEggColors() {
        return this.spawnEggColors;
    }

    /**
     * Set the range of values for a creature to be scaled by based on their age
     * So babies will be smol, adults will be big
     * */
    public T setScaleRangeForAge(float min, float max) {
        this.scaleRangeForAge = new float[]{min, max};
        return this.getThis();
    }

    public float[] getScaleRangeForAge() {
        return this.scaleRangeForAge;
    }

    /**
     * Set main hitbox size, which for now manage collisions with entity
     * */
    public T setMainHitboxSize(float width, float height) {
        this.mainHitboxSize = new float[]{width, height};
        return this.getThis();
    }

    public float[] getMainHitboxSize() {
        return this.mainHitboxSize;
    }

    /***
     * Make creature attack humans. Humans include players, villagers, pillagers, and witches
     * */
    public T setHostileToHumans() {
        this.hostileToHumans = true;
        return this.getThis();
    }

    public boolean getHostileToHumans() {
        return this.hostileToHumans;
    }

    /**
     * Make creature fight back if attacked
     * */
    public T setRetaliateWhenAttacked() {
        return this.setRetaliateWhenAttacked(false);
    }

    /**
     * Similar to above, but has additional option where, if the creature is a herder and is in a herd,
     * the herdmates will help it
     * */
    public T setRetaliateWhenAttacked(boolean broadcastRetaliation) {
        this.retaliateWhenAttacked = true;
        this.broadcastRetaliation = broadcastRetaliation;
        return this.getThis();
    }

    public boolean[] getRetaliateWhenAttacked() {
        return new boolean[]{this.retaliateWhenAttacked, this.broadcastRetaliation};
    }

    /**
     * Make creature nocturnal
     * */
    public T setIsNocturnal() {
        this.isNocturnal = true;
        return this.getThis();
    }

    public boolean getIsNocturnal() {
        return this.isNocturnal;
    }

    /**
     * Make it so the creature can be knocked back
     * */
    public T setCanBeKnockedBack() {
        this.canBeKnockedBack = true;
        return this.getThis();
    }

    public boolean getCanBeKnockedBack() {
        return this.canBeKnockedBack;
    }

    /**
     * Make creature flop on land, effectively making them waterbound
     * */
    public T setFlopOnLand() {
        this.flopOnLand = true;
        return this.getThis();
    }

    public boolean getFlopOnLand() {
        return this.flopOnLand;
    }

    /**
     * Set which blocks a creature can breathe in. If left alone, just air is considered
     * */
    public T setBreathableBlocks(String... breathableBlocks) {
        this.breathableBlocks = breathableBlocks;
        return this.getThis();
    }

    public String[] getBreathableBlocks() {
        return this.breathableBlocks;
    }

    /**
     * Set movement options for creature. Note that walking isn't here because all creatures
     * must have some kind of land movement no matter what
     * */
    public T setMovementOptions(RiftCreatureEnums.Movement... options) {
        this.movementOptions = options;
        return this.getThis();
    }

    public RiftCreatureEnums.Movement[] getMovementOptions() {
        return this.movementOptions;
    }

    /**
     * Most land creatures can float on water, this disables that
     * Creatures that swim ignore this
     * */
    public T setCannotFloatOnWater() {
        this.cannotFloatOnWater = false;
        return this.getThis();
    }

    public boolean getCannotFloatOnWater() {
        return this.cannotFloatOnWater;
    }

    /**
     * Allows creature to perform herding
     * */
    public T setIsHerder() {
        this.isHerder = true;
        return this.getThis();
    }

    public boolean isHerder() {
        return this.isHerder;
    }

    /**
     * Set the creature's inventory size
     * */
    public T setInventorySize(int value) {
        this.inventorySize = value;
        return this.getThis();
    }

    public int getInventorySize() {
        return this.inventorySize;
    }

    /**
     * Set how long the creature spends as a baby
     * */
    public T setDaysUntilAdult(int value) {
        this.daysUntilAdult = value;
        return this.getThis();
    }

    public int getDaysUntilAdult() {
        return this.daysUntilAdult;
    }

    /**
     * Get the distance in blocks from which this creature can use its contact moves
     * */
    public T setPhysicalReach(int value) {
        this.physicalReach = value;
        return this.getThis();
    }

    public int getPhysicalReach() {
        return this.physicalReach;
    }

    /**
     * Get validity based on if all params are not null
     * */
    public boolean isValid() {
        return this.creatureClass != null
                && this.stats != null
                && this.creatureCategory != null
                && this.creatureDiet != null
                && this.spawnEggColors != null
                && this.scaleRangeForAge != null;
    }
}
