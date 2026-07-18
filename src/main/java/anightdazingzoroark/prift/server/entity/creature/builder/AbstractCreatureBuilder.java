package anightdazingzoroark.prift.server.entity.creature.builder;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.CreatureMoveSelectorBuilder;
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
    private CreatureMoveSelectorBuilder moveSelector = new CreatureMoveSelectorBuilder();
    private Map<String, RiftLibRayBuilder> rayMap;
    private Map<String, TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;

    @SuppressWarnings("unchecked")
    protected final T getThis() {
        return (T) this;
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
        this.checkIfLocked();

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
    public T setCreatureCategory(@NotNull RiftCreatureEnums.CreatureCategory creatureCategory) {
        this.checkIfLocked();

        this.creatureCategory = creatureCategory;
        return this.getThis();
    }

    public RiftCreatureEnums.CreatureCategory getCreatureCategory() {
        return this.creatureCategory;
    }

    /**
     * Set diet of the species
     * */
    public T setCreatureDiet(@NotNull RiftCreatureEnums.CreatureDiet creatureDiet) {
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

        moveBuilder.lock();
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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

        return this.setRetaliateWhenAttacked(false);
    }

    /**
     * Similar to above, but has additional option where, if the creature is a herder and is in a herd,
     * the herdmates will help it
     * */
    public T setRetaliateWhenAttacked(boolean broadcastRetaliation) {
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

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
        this.checkIfLocked();

        this.daysUntilAdult = value;
        return this.getThis();
    }

    public int getDaysUntilAdult() {
        return this.daysUntilAdult;
    }

    /**
     * Set how this creature chooses moves when it is not being controlled by a rider.
     * */
    public T setMoveSelector(@NotNull CreatureMoveSelectorBuilder moveSelector) {
        this.checkIfLocked();

        moveSelector.lock();
        this.moveSelector = moveSelector;
        return this.getThis();
    }

    @NotNull
    public CreatureMoveSelectorBuilder getMoveSelector() {
        return this.moveSelector;
    }

    /**
     * Create a map of RiftLibrary rays that this creature will use.
     * */
    public T addUsableRay(@NotNull String name, @NotNull RiftLibRayBuilder builder, @NotNull TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult> rayHitEffect) {
        this.checkIfLocked();

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
        return this.stats != null
                && this.creatureCategory != null
                && this.creatureDiet != null
                && this.spawnEggColors != null
                && this.scaleRangeForAge != null
                && this.moveSelector != null
                && !this.moveList.isEmpty();
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a creature builder cannot be called after the creature is created!");
    }
}
