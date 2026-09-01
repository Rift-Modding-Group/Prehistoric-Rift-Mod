package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.config.RiftCreatureFood;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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
    private String tributeItemPartName;
    private final List<ImmutablePair<String, CreatureMoveBuilder>> moveList = new ArrayList<>();

    //the following can be left alone
    private float[] mainHitboxSize = new float[]{1f, 1f};
    private int maxFallHeight = 3;
    private BiFunction<ICreature, EntityLivingBase, Boolean> retaliateWhenAttacked;
    private boolean rememberPlayerAttacker;
    private boolean isNocturnal;
    private boolean canBeKnockedBack;
    private boolean flopOnLand;
    private String[] breathableBlocks = new String[]{"minecraft:air"};
    private boolean isHerder;
    private int maxHerdSize;
    private boolean canRetreat;
    private int inventorySize = 27;
    private int daysUntilAdult = 1;
    private boolean fallCreatesImpact;
    private boolean cannotBePushed;
    @NotNull
    private CreatureNavigationBuilder navigation = new CreatureNavigationBuilder().setCanWalk();
    private CreatureMoveSelectorBuilder moveSelector = new CreatureMoveSelectorBuilder();
    @Nullable
    private Map<String, Integer> blockBreakLevelMap;
    private Map<String, RiftLibRayBuilder> rayMap;
    private Map<String, TriConsumer<ICreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;
    private List<String> defaultTargetWhitelist;
    private List<String> defaultTargetBlacklist;
    private List<RiftCreatureFood> defaultFoodItemWhitelist;
    private List<String> defaultFoodItemBlacklist;

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
        return this.spawnEggColors.clone();
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
        return this.scaleRangeForAge.clone();
    }

    /**
     * "Tribute" items are simply body parts of a creature
     * They're mostly trophies as of now, but will be useful soon...
     * */
    public T setTributeItemPartName(@NotNull String name) {
        this.checkIfLocked();

        this.tributeItemPartName = name;
        return this.getThis();
    }

    public String getTributeItemPartName() {
        return this.tributeItemPartName;
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
        return List.copyOf(this.moveList);
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
        return this.mainHitboxSize.clone();
    }

    /**
     * Set how many blocks this creature can safely fall. Any further and
     * it will take standard minecraft fall damage
     * */
    public T setMaxFallHeight(int maxFallHeight) {
        this.checkIfLocked();
        if (maxFallHeight < 0) throw new IllegalArgumentException("Maximum fall height cannot be negative!");

        this.maxFallHeight = maxFallHeight;
        return this.getThis();
    }

    public int getMaxFallHeight() {
        return this.maxFallHeight;
    }

    /**
     * Makes this creature create a damaging impact ray when it lands after falling.
     * */
    public T setFallCreatesImpact() {
        this.checkIfLocked();

        this.fallCreatesImpact = true;
        return this.getThis();
    }

    public boolean getFallCreatesImpact() {
        return this.fallCreatesImpact;
    }

    /**
     * Make it so this creature cannot be pushed
     * */
    public T setCannotBePushed() {
        this.checkIfLocked();

        this.cannotBePushed = true;
        return this.getThis();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getCannotBePushed() {
        return this.cannotBePushed;
    }

    /**
     * Make creature fight back if attacked
     * */
    public T setRetaliateWhenAttacked() {
        this.checkIfLocked();

        return this.setRetaliateWhenAttacked((creature, target) -> true);
    }

    public T setRetaliateWhenAttacked(@NotNull BiFunction<ICreature, EntityLivingBase, Boolean> retaliateWhenAttacked) {
        this.checkIfLocked();
        this.retaliateWhenAttacked = retaliateWhenAttacked;
        return this.getThis();
    }

    @Nullable
    public BiFunction<ICreature, EntityLivingBase, Boolean> getRetaliateWhenAttacked() {
        return this.retaliateWhenAttacked;
    }

    /**
     * If this creature retaliates when attacked, setting this will make it so that when a player
     * attacks this, they will remember that player and always be hostile to them.
     * */
    public T setRememberPlayerAttacker() {
        this.rememberPlayerAttacker = true;
        return this.getThis();
    }

    public boolean getRememberPlayerAttacker() {
        return this.rememberPlayerAttacker;
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
        return this.breathableBlocks.clone();
    }

    /**
     * Allows creature to perform herding
     * */
    public T setIsHerder() {
        return this.setIsHerder(4);
    }

    /**
     * Similar to above, but maximum total herd size, including the leader, is defined.
     * */
    public T setIsHerder(int herdSize) {
        this.checkIfLocked();
        if (herdSize < 2) throw new IllegalArgumentException("Maximum herd size must be at least 2!");

        this.isHerder = true;
        this.maxHerdSize = herdSize;
        return this.getThis();
    }

    public boolean isHerder() {
        return this.isHerder;
    }

    public int getMaxHerdSize() {
        return this.maxHerdSize;
    }

    /**
     * Make it so that the creature retreats during combat when its stamina reaches the configured threshold.
     */
    public T setCanRetreat() {
        this.checkIfLocked();

        this.canRetreat = true;
        return this.getThis();
    }

    public boolean getCanRetreat() {
        return this.canRetreat;
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
     * Set how the creature can navigate in the world. By default its just walking.
     * */
    public T setNavigation(@NotNull CreatureNavigationBuilder navigation) {
        this.checkIfLocked();

        navigation.lock();
        this.navigation = navigation;
        return this.getThis();
    }

    @NotNull
    public CreatureNavigationBuilder getNavigation() {
        return this.navigation;
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

    public T addBlockBreakLevel(@NotNull String tool, int level) {
        this.checkIfLocked();

        if (level < 0) throw new IllegalArgumentException("Block Harvest level cannot be negative!");

        if (this.blockBreakLevelMap == null) this.blockBreakLevelMap = new HashMap<>();
        this.blockBreakLevelMap.put(tool, level);
        return this.getThis();
    }

    @Nullable
    public Map<String, Integer> getBlockBreakLevelMap() {
        if (this.blockBreakLevelMap == null) return null;
        return Map.copyOf(this.blockBreakLevelMap);
    }

    /**
     * Create a map of RiftLibrary rays that this creature will use.
     * */
    public T addUsableRay(@NotNull String name, @NotNull RiftLibRayBuilder builder, @NotNull TriConsumer<ICreature, BlockPos, RiftLibRay.RayHitResult> rayHitEffect) {
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
        if (this.rayMap == null) return null;
        return Map.copyOf(this.rayMap);
    }

    @Nullable
    public Map<String, TriConsumer<ICreature, BlockPos, RiftLibRay.RayHitResult>> getRayHitEffectMap() {
        if (this.rayHitEffectMap == null) return null;
        return Map.copyOf(this.rayHitEffectMap);
    }

    /**
     * Put a new entry in the whitelist for entities to attack or
     * a list defined in lists.json. This will be the default
     * value in the associated config.
     * */
    public T addDefaultTargetWhitelistEntry(@NotNull String entry) {
        this.checkIfLocked();

        if (this.defaultTargetWhitelist == null) this.defaultTargetWhitelist = new ArrayList<>();
        this.defaultTargetWhitelist.add(entry);

        return this.getThis();
    }

    public boolean hasDefaultTargetWhitelist() {
        return this.defaultTargetWhitelist != null;
    }

    @Nullable
    public List<String> getDefaultTargetWhitelist() {
        if (this.defaultTargetWhitelist == null) return null;
        return List.copyOf(this.defaultTargetWhitelist);
    }

    /**
     * Put a new entry in the blacklist for entities to attack or
     * a list defined in lists.json. This will be the default
     * value in the associated config.
     * */
    public T addDefaultTargetBlacklistEntry(@NotNull String entry) {
        this.checkIfLocked();

        if (this.defaultTargetBlacklist == null) this.defaultTargetBlacklist = new ArrayList<>();
        this.defaultTargetBlacklist.add(entry);

        return this.getThis();
    }

    public boolean hasDefaultTargetBlacklist() {
        return this.defaultTargetBlacklist != null;
    }

    @Nullable
    public List<String> getDefaultTargetBlacklist() {
        if (this.defaultTargetBlacklist == null) return null;
        return List.copyOf(this.defaultTargetBlacklist);
    }

    /**
     * Put a new entry in the whitelist for food items to consume or
     * a list defined in lists.json. This will be the default value
     * in the associated config.
     * */
    public T addDefaultFoodItemWhitelistEntry(@NotNull RiftCreatureFood entry) {
        this.checkIfLocked();

        if (this.defaultFoodItemWhitelist == null) this.defaultFoodItemWhitelist = new ArrayList<>();
        this.defaultFoodItemWhitelist.add(entry);

        return this.getThis();
    }

    public boolean hasDefaultFoodItemWhitelist() {
        return this.defaultFoodItemWhitelist != null;
    }

    @Nullable
    public List<RiftCreatureFood> getDefaultFoodItemWhitelist() {
        if (this.defaultFoodItemWhitelist == null) return null;
        return List.copyOf(this.defaultFoodItemWhitelist);
    }

    /**
     * Put a new entry in the blacklist for food items to consume or
     * a list defined in lists.json. This will be the default value
     * in the associated config.
     * */
    public T addDefaultFoodItemBlacklistEntry(@NotNull String entry) {
        this.checkIfLocked();

        if (this.defaultFoodItemBlacklist == null) this.defaultFoodItemBlacklist = new ArrayList<>();
        this.defaultFoodItemBlacklist.add(entry);

        return this.getThis();
    }

    public boolean hasDefaultFoodItemBlacklist() {
        return this.defaultFoodItemBlacklist != null;
    }

    @Nullable
    public List<String> getDefaultFoodItemBlacklist() {
        if (this.defaultFoodItemBlacklist == null) return null;
        return List.copyOf(this.defaultFoodItemBlacklist);
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
                && this.navigation.isValid()
                && this.tributeItemPartName != null
                && !this.moveList.isEmpty();
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a creature builder cannot be called after the creature is created!");
    }
}
