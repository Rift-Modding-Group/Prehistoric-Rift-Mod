package anightdazingzoroark.prift.server.entity.creature.builder;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.util.FixedSizeList;
import anightdazingzoroark.prift.util.TriConsumer;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this class defines creature information
public abstract class AbstractCreatureBuilder<T extends AbstractCreatureBuilder<T>> {
    //extremely important
    protected boolean locked;

    //all the following variables are required and must not be null, validated in isValid()
    private final Class<? extends RiftCreature> creatureClass;
    private Map<RiftCreatureEnums.Stats, Double> stats;
    private RiftCreatureEnums.CreatureCategory creatureCategory;
    private RiftCreatureEnums.CreatureDiet creatureDiet;
    private int[] spawnEggColors;
    private float[] scaleRangeForAge;
    private final List<ImmutablePair<String, CreatureMoveBuilder>> moveList = new ArrayList<>();

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
    private boolean canSprintToAttack;
    private Map<String, RiftLibRayBuilder> rayMap;
    private Map<String, TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;

    public AbstractCreatureBuilder(Class<? extends RiftCreature> creatureClass) {
        this.creatureClass = creatureClass;
    }

    @SuppressWarnings("unchecked")
    protected final T getThis() {
        return (T) this;
    }

    public Class<? extends RiftCreature> getCreatureClass() {
        return this.creatureClass;
    }

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
        this.locked = true;
    }

    /**
     * Set the stats of the creature.
     * Stats are to be on a scale of 0.5-10 with steps of 0.5
     * and will be represented as stars on most UIs
     * */
    public T setStats(double health, double meleeAttack, double elementalAttack, double stamina, double speed) {
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

        this.scaleRangeForAge = new float[]{min, max};
        return this.getThis();
    }

    public float[] getScaleRangeForAge() {
        return this.scaleRangeForAge;
    }

    /**
     * Set a map containing the moves that this creature can use. Key is the name,
     * value is the move builder for that move.
     * */
    public T addMove(String name, CreatureMoveBuilder moveBuilder) {
        if (this.locked) return this.getThis();
        this.moveList.add(new ImmutablePair<>(name, moveBuilder));
        return this.getThis();
    }

    public List<ImmutablePair<String, CreatureMoveBuilder>> getMoves() {
        return this.moveList;
    }

    /**
     * Set main hitbox size, which for now manage collisions with entity
     * */
    public T setMainHitboxSize(float width, float height) {
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

        return this.setRetaliateWhenAttacked(false);
    }

    /**
     * Similar to above, but has additional option where, if the creature is a herder and is in a herd,
     * the herdmates will help it
     * */
    public T setRetaliateWhenAttacked(boolean broadcastRetaliation) {
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

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
        if (this.locked) return this.getThis();

        this.physicalReach = value;
        return this.getThis();
    }

    public int getPhysicalReach() {
        return this.physicalReach;
    }

    /**
     * Make it so that sprinting can be used as an attack by this creature.
     * Note that its only for when its on its own, when controlled by a rider
     * it can spring to attack when commanded to (by simply sprinting lol)
     * */
    public T setCanSprintToAttack() {
        if (this.locked) return this.getThis();

        this.canSprintToAttack = true;
        return this.getThis();
    }

    public boolean getCanSprintToAttack() {
        return this.canSprintToAttack;
    }

    /**
     * Create a map of RiftLibrary rays that this creature will use.
     * */
    public T addUsableRay(@NotNull String name, @NotNull RiftLibRayBuilder builder, @NotNull TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult> rayHitEffect) {
        if (this.locked) return this.getThis();

        if (this.rayMap == null || this.rayHitEffectMap == null) {
            this.rayMap = new HashMap<>();
            this.rayHitEffectMap = new HashMap<>();
        }
        this.rayMap.put(name, builder);
        this.rayHitEffectMap.put(name, rayHitEffect);
        return this.getThis();
    }

    @Nullable
    public Map<String, RiftLibRayBuilder> getRayMap() {
        return this.rayMap;
    }

    @Nullable
    public Map<String, TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult>> getRayHitEffectMap() {
        return this.rayHitEffectMap;
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
                && this.scaleRangeForAge != null
                && !this.moveList.isEmpty();
    }
}
