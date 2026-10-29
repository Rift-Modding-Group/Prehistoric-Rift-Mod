package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry;
import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry.MoveDefinition;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureFood;
import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.util.CreatureMoveUtil;
import net.minecraft.entity.EntityLivingBase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

//this class defines creature information
public abstract class AbstractCreatureBuilder<T extends AbstractCreatureBuilder<T>> {
    //extremely important
    protected boolean locked;

    //all the following variables are required and must not be null, validated in isValid()
    private Map<RiftCreatureEnums.Stats, Double> stats;
    private Element[] element;
    private RiftCreatureEnums.CreatureCategory creatureCategory;
    private RiftCreatureEnums.CreatureDiet creatureDiet;
    private int[] spawnEggColors;
    private float[] scaleRangeForAge;
    private String tributeItemPartName;
    @NotNull
    private Map<String, MoveDefinition> moves = Map.of();
    @NotNull
    private Map<DetectionRule, MoveDefinition> detectionOwners = Map.of();
    @NotNull
    private List<ImmutablePair<String, CreatureMoveBuilder>> orderedMoves = List.of();
    @NotNull
    private List<CreatureMoveSelectorBuilder.MoveRule> moveRules = List.of();

    private Map<String, MoveDefinition> resolvedMoves = Map.of();
    private Map<DetectionRule, MoveDefinition> resolvedDetectionOwners = Map.of();
    private List<CreatureMoveSelectorBuilder.MoveRule> resolvedMoveRules = List.of();
    private int maximumMoveSlots = -1;
    @Nullable
    private List<String> spawnMoves;
    @NotNull
    private Map<Integer, List<String>> levelupMoves = Map.of();
    private final Map<String, List<String>> moveAnimations = new LinkedHashMap<>();
    private final Map<String, Map<ChargeupPhase, List<String>>> chargeupMoveAnimations = new LinkedHashMap<>();

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
    private int daysUntilAdult = 1;
    private boolean fallCreatesImpact;
    private boolean fallSlowly;
    private int sprintBasePower = 10;
    private int sprintCooldown = 300;
    @Nullable
    private CreatureDomesticationBuilder domestication;
    @Nullable
    private CreatureNavigationBuilder navigation;
    private CreatureMoveSelectorBuilder moveSelector = new CreatureMoveSelectorBuilder();
    @Nullable
    private Map<String, Integer> blockBreakLevelMap;
    private List<String> defaultTargetWhitelist;
    private List<String> defaultTargetBlacklist;
    private List<Object> defaultFoodItemWhitelist;
    private List<String> defaultFoodItemBlacklist;

    @SuppressWarnings("unchecked")
    protected T getThis() {
        return (T) this;
    }

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
        if (this.locked) return;
        this.setMoveSelector(this.moveSelector);
        if (this.getSpawnMoves().size() > this.getMaximumMoveSlots()) {
            throw new IllegalArgumentException("Spawn moves exceed the maximum move slots");
        }
        for (String name : this.moveAnimations.keySet()) {
            if (CreatureMoveRegistry.getMove(name) == null) {
                throw new IllegalArgumentException("Unknown registered move: " + name);
            }
        }
        for (String name : this.getSpawnMoves()) {
            if (!this.getMoveDefinitions().containsKey(name) || this.getMoveAnimations(name).isEmpty()) {
                throw new IllegalArgumentException("Spawn move has no definition or animation mapping: " + name);
            }
        }
        for (Map.Entry<Integer, List<String>> entry : this.levelupMoves.entrySet()) {
            for (String name : entry.getValue()) {
                if (!this.getMoveDefinitions().containsKey(name) || this.getMoveAnimations(name).isEmpty()) {
                    throw new IllegalArgumentException("Levelup move at level " + entry.getKey()
                            + " has no definition or animation mapping: " + name);
                }
            }
        }
        for (CreatureMoveSelectorBuilder.MoveRule rule : this.moveRules) {
            if (rule.moveResult() == CreatureMoveResult.USE_MOVE && !this.getMoveDefinitions().containsKey(rule.moveRuleBuilder().getMoveName())) {
                throw new IllegalArgumentException("No move is registered for rule " + rule.moveRuleBuilder().getMoveName());
            }
        }
        if (this instanceof RiftCreatureBuilder creature) {
            for (CreaturePhaseBuilder phase : creature.getPhaseBuilderMaps().values()) phase.lock();
        }
        if (this.domestication != null) this.domestication.lock();
        for (MoveDefinition move : this.moves.values()) {
            if (!move.moveBuilder().isValid()) throw new IllegalArgumentException("Invalid move: " + move.name());
        }
        for (String name : this.chargeupMoveAnimations.keySet()) {
            MoveDefinition move = this.getMoveDefinitions().get(name);
            if (move == null || move.moveBuilder().getMoveChargeupBuilder() == null) {
                throw new IllegalArgumentException("Charge phase animations require a mapped chargeup move: " + name);
            }
        }
        this.resolvedMoves = this.getMoveDefinitions();
        this.resolvedDetectionOwners = this.getDetectionOwners();
        this.resolvedMoveRules = this.getMoveRules();
        this.moves.values().forEach(move -> move.moveBuilder().lock());
        this.moveSelector = new CreatureMoveSelectorBuilder().setMoveRules(this.resolvedMoveRules);
        this.moveSelector.lock();
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
                RiftCreatureEnums.Stats.SPECIAL_DAMAGE, elementalAttack,
                RiftCreatureEnums.Stats.STAMINA, stamina,
                RiftCreatureEnums.Stats.SPEED, speed
        );
        return this.getThis();
    }

    public Map<RiftCreatureEnums.Stats, Double> getStats() {
        return this.stats;
    }

    /**
     * Set the element of this creature, make it mono element
     * */
    public T setCreatureElement(@NotNull Element element) {
        this.checkIfLocked();

        this.element = new Element[]{element};
        return this.getThis();
    }

    /**
     * Same as above, but for dual element
     * */
    public T setCreatureElement(@NotNull Element primaryElement, @NotNull Element secondaryElement) {
        this.checkIfLocked();

        this.element = new Element[]{primaryElement, secondaryElement};
        return this.getThis();
    }

    public Element[] getElement() {
        return this.element;
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
     * Legacy creature-local declaration. Prefer CreatureMoveRegistry.registerMove,
     * setSpawnMoves and setMoveAnimations for shared moves.
     * */
    @Deprecated
    public T addMove(@NotNull String name, @NotNull CreatureMoveBuilder moveBuilder) {
        this.checkIfLocked();
        if (this.moves.containsKey(name)) throw new IllegalArgumentException("Move " + name + " is already registered!");

        Map<String, MoveDefinition> declarations = new LinkedHashMap<>(this.moves);
        declarations.put(name, new MoveDefinition(name, moveBuilder, List.of()));
        List<CreatureMoveSelectorBuilder.MoveRule> policies = this.moveSelector.getMoveRules();
        Map<String, MoveDefinition> moves = CreatureMoveUtil.groupMoveRules(declarations, policies);
        Map<DetectionRule, MoveDefinition> owners = CreatureMoveUtil.indexDetectionOwners(moves);
        List<ImmutablePair<String, CreatureMoveBuilder>> orderedMoves = new ArrayList<>(this.orderedMoves);
        orderedMoves.add(new ImmutablePair<>(name, moveBuilder));

        this.moves = moves;
        this.detectionOwners = owners;
        this.orderedMoves = List.copyOf(orderedMoves);
        this.moveRules = List.copyOf(policies);

        return this.getThis();
    }

    /**
     * Ordered spawn moves. An individual's current moves are stored separately.
     * */
    @NotNull
    public List<ImmutablePair<String, CreatureMoveBuilder>> getMoves() {
        return this.getSpawnMoves().stream()
                .map(name -> new ImmutablePair<>(name, this.getMoveDefinitions().get(name).moveBuilder())).toList();
    }

    @NotNull
    public Map<String, MoveDefinition> getMoveDefinitions() {
        if (this.locked) return this.resolvedMoves;
        Map<String, MoveDefinition> definitions = new LinkedHashMap<>();
        for (String name : this.moveAnimations.keySet()) {
            MoveDefinition definition = CreatureMoveRegistry.getMove(name);
            if (definition != null) definitions.put(name, definition);
        }
        definitions.putAll(this.moves);
        return java.util.Collections.unmodifiableMap(definitions);
    }

    /**
     * Keys identify detector instances, rather than comparing detector values.
     * */
    @NotNull
    public Map<DetectionRule, MoveDefinition> getDetectionOwners() {
        if (this.locked) return this.resolvedDetectionOwners;
        return CreatureMoveUtil.indexDetectionOwners(this.getMoveDefinitions());
    }

    /**
     * Ordered AI policies, including sprint and leap policies without a named move.
     * */
    @NotNull
    public List<CreatureMoveSelectorBuilder.MoveRule> getMoveRules() {
        if (this.locked) return this.resolvedMoveRules;
        List<CreatureMoveSelectorBuilder.MoveRule> rules = new ArrayList<>();
        for (MoveDefinition definition : this.getMoveDefinitions().values()) {
            for (MoveRuleBuilder rule : definition.rules()) {
                rules.add(new CreatureMoveSelectorBuilder.MoveRule(CreatureMoveResult.USE_MOVE, rule));
            }
        }
        for (CreatureMoveSelectorBuilder.MoveRule rule : this.moveRules) {
            if (rule.moveResult() != CreatureMoveResult.USE_MOVE) rules.add(rule);
        }
        return List.copyOf(rules);
    }

    /**
     * The number of moves an individual may hold, independent of its starting moves.
     * */
    public T setMaximumMoveSlots(int maximumMoveSlots) {
        this.checkIfLocked();
        if (maximumMoveSlots < 0) throw new IllegalArgumentException("Maximum move slots cannot be negative");
        this.maximumMoveSlots = maximumMoveSlots;
        return this.getThis();
    }

    public int getMaximumMoveSlots() {
        return this.maximumMoveSlots < 0 ? this.orderedMoves.size() : this.maximumMoveSlots;
    }

    /**
     * Assign moves to the creature that it will always spawn with
     * */
    public T setSpawnMoves(@NotNull String... moves) {
        this.checkIfLocked();
        List<String> names = List.of(moves);
        if (names.stream().distinct().count() != names.size()) {
            throw new IllegalArgumentException("Spawn moves cannot contain duplicates");
        }
        this.spawnMoves = names;
        return this.getThis();
    }

    @NotNull
    public List<String> getSpawnMoves() {
        if (this.spawnMoves != null) return this.spawnMoves;
        return this.orderedMoves.stream().map(ImmutablePair::getKey).toList();
    }

    /**
     * Replace the level-to-moves schedule for future levelup learning.
     * Multiple moves may be assigned to one level, in the supplied list order.
     * The schedule is independent of spawn moves and maximum equipped move slots;
     * setting it does not teach or equip any moves.
     * */
    public T setLevelupMoves(@NotNull Map<Integer, List<String>> levelupMoves) {
        this.checkIfLocked();

        Map<Integer, List<String>> movesByLevel = new TreeMap<>();
        for (Map.Entry<Integer, List<String>> entry : levelupMoves.entrySet()) {
            if (entry.getKey() < 1) throw new IllegalArgumentException("Levelup move levels must be at least 1");
            List<String> names = List.copyOf(entry.getValue());
            if (names.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("Levelup move names cannot be blank");
            }
            if (names.stream().distinct().count() != names.size()) {
                throw new IllegalArgumentException("Levelup moves cannot contain duplicates at level " + entry.getKey());
            }
            movesByLevel.put(entry.getKey(), names);
        }
        this.levelupMoves = Collections.unmodifiableMap(movesByLevel);
        return this.getThis();
    }

    /**
     * Immutable schedule in ascending level order, with immutable move lists.
     * */
    @NotNull
    public Map<Integer, List<String>> getLevelupMoves() {
        return this.levelupMoves;
    }

    /**
     * Map a shared move to this creature's animation suffixes (or complete animation names).
     * */
    public T setMoveAnimations(@NotNull String moveName, @NotNull String... animations) {
        this.checkIfLocked();
        if (animations.length == 0 || java.util.Arrays.stream(animations).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("A move requires at least one animation");
        }
        this.moveAnimations.put(moveName, List.of(animations));
        return this.getThis();
    }

    @NotNull
    public List<String> getMoveAnimations(@NotNull String moveName) {
        return this.moveAnimations.getOrDefault(moveName, List.of());
    }

    @NotNull
    public List<String> getMoveAnimations(@NotNull String moveName, @NotNull ChargeupPhase phase) {
        Map<ChargeupPhase, List<String>> phases = this.chargeupMoveAnimations.get(moveName);
        if (phases != null && phases.containsKey(phase)) return phases.get(phase);
        return this.getMoveAnimations(moveName).stream().map(name -> name + "_" + phase.name().toLowerCase()).toList();
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
        if (this.fallSlowly) {
            //todo: replace w logger
            System.out.println("This creature is set to fall slowly! This will be ignored");
            return this.getThis();
        }
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
        if (this.fallSlowly) {
            //todo: replace w logger
            System.out.println("This creature is set to fall slowly! This will be ignored");
            return this.getThis();
        }

        this.fallCreatesImpact = true;
        return this.getThis();
    }

    public boolean getFallCreatesImpact() {
        return this.fallCreatesImpact;
    }

    /**
     * Make it so that this creature falls down slowly similar to vanilla chickens
     * and be immune to fall damage
     * */
    public T setFallSlowly() {
        this.checkIfLocked();

        this.fallSlowly = true;
        this.fallCreatesImpact = false;
        return this.getThis();
    }

    public boolean getFallSlowly() {
        return this.fallSlowly;
    }

    /**
     * Set the base power of this creature's sprint attack. By default it's 10.
     * */
    public T setSprintBasePower(int sprintBasePower) {
        this.checkIfLocked();

        this.sprintBasePower = sprintBasePower;
        return this.getThis();
    }

    public int getSprintBasePower() {
        return this.sprintBasePower;
    }

    /**
     * Set the cooldown for when this creature sprints, by default its 300 ticks or 15 seconds
     * */
    public T setSprintCooldown(int sprintCooldown) {
        this.checkIfLocked();

        this.sprintCooldown = sprintCooldown;
        return this.getThis();
    }

    public int getSprintCooldown() {
        return this.sprintCooldown;
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
     * Set how this creature is domesticated and which features become available after taming.
     * Leaving this unset makes the creature untameable.
     */
    public T setDomestication(@NotNull CreatureDomesticationBuilder domestication) {
        this.checkIfLocked();
        if (!domestication.isValid()) throw new IllegalArgumentException("Creature domestication builder is invalid!");

        this.domestication = domestication;
        return this.getThis();
    }

    @Nullable
    public CreatureDomesticationBuilder getDomestication() {
        return this.domestication;
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
     * Set how the creature can navigate in the world. No navigation means it just does nothing.
     * */
    public T setNavigation(@NotNull CreatureNavigationBuilder navigation) {
        this.checkIfLocked();

        navigation.lock();
        this.navigation = navigation;
        return this.getThis();
    }

    @Nullable
    public CreatureNavigationBuilder getNavigation() {
        return this.navigation;
    }

    /**
     * Set how this creature chooses moves when it is not being controlled by a rider.
     * */
    public T setMoveSelector(@NotNull CreatureMoveSelectorBuilder moveSelector) {
        this.checkIfLocked();

        List<CreatureMoveSelectorBuilder.MoveRule> policies = moveSelector.getMoveRules();
        Map<String, MoveDefinition> moves = CreatureMoveUtil.groupMoveRules(this.moves, policies);
        Map<DetectionRule, MoveDefinition> owners = CreatureMoveUtil.indexDetectionOwners(moves);
        moveSelector.lock();
        this.moveSelector = moveSelector;
        this.moves = moves;
        this.detectionOwners = owners;
        this.moveRules = List.copyOf(policies);
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

    public T addDefaultFoodItemWhitelistEntry(@NotNull String listName) {
        this.checkIfLocked();

        if (this.defaultFoodItemWhitelist == null) this.defaultFoodItemWhitelist = new ArrayList<>();
        this.defaultFoodItemWhitelist.add(listName);

        return this.getThis();
    }

    public boolean hasDefaultFoodItemWhitelist() {
        return this.defaultFoodItemWhitelist != null;
    }

    @Nullable
    public List<Object> getDefaultFoodItemWhitelist() {
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
                && this.element != null
                && this.creatureCategory != null
                && this.creatureDiet != null
                && this.spawnEggColors != null
                && this.scaleRangeForAge != null
                && (this.navigation == null || this.navigation.isValid())
                && (this.domestication == null || this.domestication.isValid())
                && this.tributeItemPartName != null;
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a creature builder cannot be called after the creature is created!");
    }
}
