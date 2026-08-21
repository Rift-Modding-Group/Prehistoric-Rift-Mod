package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.config.RiftCreatureConfig;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.ai.RiftGoToLandFromWater;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.ai.RiftFindTarget;
import anightdazingzoroark.prift.server.entity.ai.RiftUnmountedUseMove;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.util.MathUtil;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * le heart and soul of this mod
 * */
public class RiftCreature extends EntityTameable implements IAnimatable<AnimationDataEntity>, IRiftCreature, ICreature, IRayCreator<RiftCreature> {
    @NotNull
    private RiftCreatureBuilder creatureType;
    @NotNull
    private final RiftLibInventoryHandler creatureInventory;
    @NotNull
    private AnimationDataEntity animData;

    public static final IAttribute ELEMENTAL_DAMAGE_ATTRIBUTE = new RangedAttribute(null, "rift.elementalDamage", 2.0, 0.0, 2048.0);
    public static final IAttribute STAMINA_ATTRIBUTE = new RangedAttribute(null, "rift.stamina", 2.0, 0.0, 2048.0);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> NATURE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Float> STAMINA_CURRENT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<CreatureMoveStorage> CREATURE_MOVES = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_MOVE_STORAGE);
    private static final DataParameter<CreatureStatsStorage> CREATURE_STATS = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_STATS_STORAGE);
    private static final DataParameter<String> CREATURE_PHASE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USE_BLOCK_BREAK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    //--custom property values, which can be called and manipulated from a creature builder--
    @NotNull
    private Map<String, AbstractPropertyValue<?>> propertyValueMap = Map.of();

    //--server side primitive params--
    //manages a creature's ability to sprint based on whether or not it attacked before
    public int sprintToAttackCooldown;
    //manages a creature's ability to leap based on whether it attacked before
    public int leapToAttackCooldown;
    //when a creature fails to use a move or takes too long to pathfind for melee move,
    //this counts up, which then makes them use a ranged move or their sprint move
    private int frustration;
    private int attackTargetHitCount;
    //when a creature uses offensive moves on its target and it still stays alive,
    //this counts up, which can then be used in priority predicate
    private int rage;
    private int currentRageThreshold;
    private int rageEndCountdown;

    //target pathing state
    private boolean unableToPathToTarget;

    //fall impact state
    private boolean trackingFallImpact;
    private double highestAirborneY;
    private double lastFallImpactYDelta;

    //ray specific params
    protected Map<String, RiftLibRayBuilder> rayMap;
    protected Map<String, TriConsumer<ICreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;

    public RiftCreature(World worldIn) {
        this(worldIn, RiftCreatureRegistry.DEFAULT_CREATURE);
    }

    public RiftCreature(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = resolveCreatureBuilder(creatureName);
        this.creatureInventory = new RiftLibInventoryHandler(this.creatureType.getInventorySize());
        this.moveHelper = new RiftCreatureMoveHelper(this);
        this.navigator = new RiftCreaturePathNavigate(this, worldIn);
        this.applyCreatureTypeSettings();
        this.animData = new AnimationDataEntity(this, holder -> this.scale());

        if (worldIn != null && !worldIn.isRemote) this.initCreatureAI();
    }

    @NotNull
    private static RiftCreatureBuilder resolveCreatureBuilder(String creatureName) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(creatureName);
        if (builder == null) builder = RiftCreatureRegistry.getCreatureBuilder(RiftCreatureRegistry.DEFAULT_CREATURE);
        if (builder == null) throw new IllegalStateException("Creature type " + creatureName + " is not registered!");
        return builder;
    }

    private void applyCreatureTypeSettings() {
        this.setSize(this.creatureType.getMainHitboxSize()[0], this.creatureType.getMainHitboxSize()[1]);
        if (this.creatureType.getPropertyValueMap() != null) this.propertyValueMap = new HashMap<>(this.creatureType.getPropertyValueMap());
        else this.propertyValueMap = Map.of();

        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(this.creatureType.getCanBeKnockedBack() ? 0D : 1D);
        Map<String, RiftLibRayBuilder> configuredRays = this.creatureType.getRayMap();
        this.rayMap = configuredRays == null ? new HashMap<>() : new HashMap<>(configuredRays);
        Map<String, TriConsumer<ICreature, BlockPos, RiftLibRay.RayHitResult>> configuredRayEffects = this.creatureType.getRayHitEffectMap();
        this.rayHitEffectMap = configuredRayEffects == null ? new HashMap<>() : new HashMap<>(configuredRayEffects);

        this.trackingFallImpact = false;
        this.lastFallImpactYDelta = 0D;
        if (this.creatureType.getFallCreatesImpact()) {
            this.rayMap.put("fallImpactRay", new RiftLibRayBuilder()
                    .setImpactOnly()
                    .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                    .setMaxMotionDistance(Math.max(1D, this.width * 1.5D))
                    .setOnlyOneSegment()
                    .setMotionSpeed(1.5D)
            );
            this.rayHitEffectMap.put("fallImpactRay", (creature, rayOrigin, rayHitResult) -> {
                if (creature.getEntityWorld().isRemote) return;
                for (Entity hitEntity : rayHitResult.hitEntities()) {
                    if (hitEntity instanceof EntityLivingBase) {
                        this.attackEntityFromFallImpact(hitEntity, this.lastFallImpactYDelta);
                    }
                }
            });
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(NATURE, (byte) 0);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(STAMINA_CURRENT, 0f);
        this.dataManager.register(CREATURE_MOVES, new CreatureMoveStorage());
        this.dataManager.register(CREATURE_STATS, new CreatureStatsStorage());
        this.dataManager.register(CREATURE_PHASE, "");
        this.dataManager.register(LEAPING, false);
        this.dataManager.register(USE_BLOCK_BREAK, false);
    }

    //this is gonna be mostly for registering the custom attributes
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        //vanilla ATTACK_DAMAGE is to be used for melee damage attribute
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getAttributeMap().registerAttribute(ELEMENTAL_DAMAGE_ATTRIBUTE);
        this.getAttributeMap().registerAttribute(STAMINA_ATTRIBUTE);
        this.getAttributeMap().getAttributeInstance(EntityLivingBase.SWIM_SPEED).setBaseValue(1.5D);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        //creature is to be an adult
        this.setAgeInTicks(this.creatureType.getDaysUntilAdult() * 24000);

        //set level based on distance from 0, 0
        double distFromCenter = Math.sqrt(this.posX * this.posX + this.posZ * this.posZ);
        double levelSlopeResult = MathUtil.slopeResult(distFromCenter, false, 0, 1024, 1, 2);
        levelSlopeResult = Math.clamp(levelSlopeResult, 1, 10);
        levelSlopeResult = Math.round(levelSlopeResult);
        this.setLevel((int) levelSlopeResult);

        //initialize creature nature
        int randNatureIndex = this.rand.nextInt(RiftCreatureEnums.Nature.values().length);
        this.setNature(RiftCreatureEnums.Nature.values()[randNatureIndex]);

        //initialize creature stats
        CreatureStatsStorage creatureStatsStorage = this.getCreatureStats();
        creatureStatsStorage.initializeIndividualValues();
        creatureStatsStorage.parseStats(this.creatureType, this.getLevel(), this.getNature());
        creatureStatsStorage.applyStatsToCreature(this);
        this.setCreatureStats(creatureStatsStorage);

        //initialize creature moves
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCreatureUser(this.creatureType);

        //return value
        return super.onInitialSpawn(difficulty, livingdata);
    }

    /**
     * better than EntityLiving.initEntityAI() :tm:
     * */
    private void initCreatureAI() {
        //aint makin a new class again for this shit
        if (this.creatureType.getRetaliateWhenAttacked() != null) {
            this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false) {
                @Override
                public boolean shouldExecute() {
                    if (creatureType.getRetaliateWhenAttacked() != null
                            && !creatureType.getRetaliateWhenAttacked().apply((RiftCreature) this.taskOwner, this.taskOwner.getRevengeTarget())
                    ) {
                        return false;
                    }
                    return super.shouldExecute();
                }
            });
        }
        this.targetTasks.addTask(2, new RiftFindTarget(this, true));

        this.tasks.addTask(1, new RiftUnmountedUseMove(this));
        if (!this.creatureType.getNavigation().getCanSwim()) {
            this.tasks.addTask(2, new RiftGoToLandFromWater(this));
        }
        this.tasks.addTask(3, new EntityAIWander(this, 1D));
        this.tasks.addTask(4, new EntityAILookIdle(this) {
            @Override
            public void resetTask() {
                this.idleTime = 0;
            }
        });
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //disable default growth system
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        //server only operations
        if (!this.world.isRemote) {
            //keep the pose active for the full airborne portion even if pathing
            //relinquishes its leap action before the creature reaches the ground
            boolean continueLeapPose = this.dataManager.get(LEAPING) && !this.onGround;
            this.setLeaping(this.getCreatureMoveHelper().isLeaping() || continueLeapPose);

            //tick fall impacts
            if (!this.creatureType.getFallCreatesImpact() || this.bodyTouchingLiquid()) {
                this.trackingFallImpact = false;
            }
            else if (!this.onGround) {
                if (!this.trackingFallImpact) this.highestAirborneY = this.posY;
                else this.highestAirborneY = Math.max(this.highestAirborneY, this.posY);
                this.trackingFallImpact = true;
            }
            else if (this.trackingFallImpact) {
                this.trackingFallImpact = false;
                double landingYDelta = this.highestAirborneY - this.posY;
                if (landingYDelta > 2D) { //falling down more than 2 blocks should create fall impacts
                    this.lastFallImpactYDelta = landingYDelta;
                    RiftLibRayHelper.createRay(this, "fallImpactRay", "centerPoint");
                }
            }

            //set age
            this.setAgeInTicks(this.getAgeInTicks() + 1);

            //tick creature move storage
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.updateUsableMoves(this, this.getAttackTarget());
            creatureMoveStorage.tickCooldowns();
            if (creatureMoveStorage.shouldCancelCurrentMoveForMissingTarget(this)) {
                creatureMoveStorage.finishCurrentMoveUse(this);
            }
            else creatureMoveStorage.tickCurrentMove(this, this.getAttackTarget());

            //tick sprinting related stuff
            if (this.sprintToAttackCooldown > 0) this.sprintToAttackCooldown--;
            if (this.leapToAttackCooldown > 0) this.leapToAttackCooldown--;

            //tick creature rage
            if (this.getAttackTarget() != null) {
                //set rage threshold to between 1 - 2 minutes
                if (this.currentRageThreshold <= 0) this.currentRageThreshold = MathUtil.randomInRange(this.world.rand, 1200, 2400);
                this.rage = Math.min(this.currentRageThreshold, this.rage + 1);

                //set rage end countdown to max, which is 3 minutes
                this.rageEndCountdown = 3600;
            }
            //when target is gone, it will take a while for that rage to subside
            else {
                this.rageEndCountdown = Math.max(0, this.rageEndCountdown - 1);
                if (this.rageEndCountdown == 0) {
                    this.rage = 0;
                    this.currentRageThreshold = 0;
                }
            }

            //tick the creature on tick lambda
            if (this.creatureType.getUpdateEffect() != null) this.creatureType.getUpdateEffect().accept(this);
        }
    }

    /**
     * Used to check if a given BlockPos is within the bounds of an AnimatedBoundingBox
     * */
    public boolean posWithinBoundingBox(@NotNull Vec3d posVec, @NotNull String boundingBoxName) {
        AxisAlignedBB aabb = this.animData.getWorldSpaceAABB(boundingBoxName);
        if (aabb == null) return false;
        return aabb.grow(1e-5D).contains(posVec);
    }

    public boolean aabbIntersectsBoundingBox(@NotNull AxisAlignedBB otherAABB, @NotNull String boundingBoxName) {
        AxisAlignedBB aabb = this.animData.getWorldSpaceAABB(boundingBoxName);
        if (aabb == null) return false;
        return aabb.intersects(otherAABB);
    }

    //---ICreature implementations from the api starts here---
    @Override
    @NotNull
    public RiftCreatureConfig getCreatureConfig() {
        return IRiftCreature.super.getCreatureConfig();
    }

    @Override
    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public int getSprintToAttackCooldown() {
        return this.sprintToAttackCooldown;
    }

    @Override
    public int getLeapToAttackCooldown() {
        return this.leapToAttackCooldown;
    }

    @Override
    public boolean hasStraightWalkingPathTo(@NotNull EntityLivingBase target) {
        return this.getCreaturePathNavigate().hasStraightWalkingPathTo(target);
    }
    //---ICreature implementations from the api ends here---

    //this gets the scale of the model of the entity
    public float scale() {
        return MathUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                this.creatureType.getScaleRangeForAge()[0], this.creatureType.getScaleRangeForAge()[1]
        );
    }

    //the vanilla attack entity method. is now used for damage calculations
    //use this when attacking an entity
    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn == null) return false;

        CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
        if (creatureMoveBuilder == null) return false;

        //get damagesource and modify based on some stuff, like element
        DamageSource damageSource = DamageSource.causeMobDamage(this);
        if (creatureMoveBuilder.getElement() == Element.FIRE) damageSource.setFireDamage();

        //apply damage
        double damage = CreatureMoveHelper.calculateDamage(this);
        boolean flag = entityIn.attackEntityFrom(damageSource, (float) damage);
        if (creatureMoveBuilder.getOnTargetHitEffect() != null && creatureMoveBuilder.getMakesContact()) {
            creatureMoveBuilder.getOnTargetHitEffect().accept(this, entityIn);
        }

        //apply elemental effects
        if (creatureMoveBuilder.getElement() != null) {
            creatureMoveBuilder.getElement().applyElementEffect.accept(entityIn, creatureMoveBuilder.getElementEffectStrength());
        }

        //other stuff
        Entity attackTarget = this.getAttackTarget();
        Entity hitEntity = entityIn instanceof MultiPartEntityPart hitboxPart ? (Entity) hitboxPart.parent : entityIn;
        if (attackTarget != null && hitEntity == attackTarget) this.attackTargetHitCount++;
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    //this method is to be used when attacking from sprinting. sprinting is considered
    //a physical move that makes contact
    public void attackEntityFromSprint(Entity entityIn) {
        this.attackEntityFromMovement(entityIn, 10);
    }

    //this method is to be used when a leap attack makes contact with its target
    public void attackEntityFromLeap(Entity entityIn) {
        this.attackEntityFromMovement(entityIn, 10);
    }

    //this method is to be used by a configured fall impact
    public void attackEntityFromFallImpact(Entity entityIn, double landingYDelta) {
        double basePower = 10D * Math.max(0D, landingYDelta);
        this.attackEntityFromMovement(entityIn, basePower);
    }

    private void attackEntityFromMovement(Entity entityIn, double basePower) {
        if (entityIn == null) return;
        double attackStat = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        double movementDamage = attackStat * basePower * 0.005D;

        entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) movementDamage);
        this.setLastAttackedEntity(entityIn);
    }

    //test if another entity is related to this creature
    //such as if its tamed to its owner
    public boolean isRelatedToEntity(Entity entity) {
        if (entity instanceof MultiPartEntityPart hitboxPart) {
            Entity hitboxParent = (Entity) hitboxPart.parent;
            return this.isRelatedToEntity(hitboxParent);
        }
        else if (entity instanceof EntityTameable entityTameable) {
            return entityTameable.isTamed() && entityTameable.getOwner() != null && entityTameable.getOwner().equals(this.getOwner());
        }
        else if (entity instanceof EntityPlayer entityPlayer) {
            return this.isTamed() && this.getOwner() != null && this.getOwner().equals(entityPlayer);
        }
        return false;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target) {
        if (target != this.getAttackTarget()) this.unableToPathToTarget = false;
        super.setAttackTarget(target);
    }

    @Override
    public int getMaxFallHeight() {
        return this.creatureType.getMaxFallHeight();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        float adjustedDistance = distance - this.getMaxFallHeight() + 3f;
        super.fall(adjustedDistance, damageMultiplier);
    }

    //-----properties management-----
    @SuppressWarnings("unchecked")
    public <I> I getProperty(String key) {
        if (!this.propertyValueMap.containsKey(key)) {
            throw new UnsupportedOperationException("Key " + key + " does not exist in property map for " + this.creatureType.getName() + "!");
        }
        return (I) this.propertyValueMap.get(key);
    }

    public <I> void setProperty(String key, I value) {
        AbstractPropertyValue<I> propertyValue = this.getProperty(key);
        if (propertyValue.getHeldClass() != value.getClass()) {
            throw new UnsupportedOperationException("Key " + key + " does not represent given value " + value + "!");
        }
        propertyValue.setValue(value);
        //todo: make this able to sync to client as well
    }

    //-----frustration management-----
    //todo: move all this to usemoveunmounted?
    public boolean atFrustrationThreshold() {
        return this.frustration >= 100;
    }

    public boolean atPathingFrustrationInterval(int pathingTicks) {
        return pathingTicks >= 80;
    }

    public void resetFrustration() {
        this.frustration = 0;
    }

    public void addFrustration(int frustrationToAdd) {
        this.frustration = Math.min(100, this.frustration + frustrationToAdd);
    }

    public int getAttackTargetHitCount() {
        return this.attackTargetHitCount;
    }

    //-----rage management-----
    public boolean atRageThreshold() {
        return this.rage >= this.currentRageThreshold;
    }

    //-----creature phase management-----
    public String getPhase() {
        return this.dataManager.get(CREATURE_PHASE);
    }

    public void setPhase(String value) {
        if (value == null) return;
        this.dataManager.set(CREATURE_PHASE, value);
    }

    //-----block break management-----
    public boolean getUseBlockBreak() {
        return this.dataManager.get(USE_BLOCK_BREAK);
    }

    public void setUseBlockBreak(boolean value) {
        this.dataManager.set(USE_BLOCK_BREAK, value);
    }

    //-----move use management-----
    public String getCurrentMove() {
        return this.getCreatureMoves().getCurrentMove();
    }

    public void setCurrentMove(@NotNull String name) {
        if (this.isLeaping() && !name.isEmpty()) return;
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCurrentMove(name);
    }

    //-----navigation management-----
    @NotNull
    public CreatureNavigationBuilder getNavigationBuilder() {
        return this.creatureType.getNavigation();
    }

    @NotNull
    public RiftCreaturePathNavigate getCreaturePathNavigate() {
        return (RiftCreaturePathNavigate) this.navigator;
    }

    @NotNull
    public RiftCreatureMoveHelperBase getCreatureMoveHelper() {
        return (RiftCreatureMoveHelperBase) this.moveHelper;
    }

    //mostly here just to sync leaping to client
    //and of course is private
    private void setLeaping(boolean value) {
        this.dataManager.set(LEAPING, value);
    }

    //client friendly query for leaping
    public boolean isLeaping() {
        return this.world.isRemote ? this.dataManager.get(LEAPING) : this.getCreatureMoveHelper().isLeaping();
    }

    public boolean isUnableToPathToTarget() {
        return this.unableToPathToTarget;
    }

    public void setUnableToPathToTarget(boolean unableToPathToTarget) {
        this.unableToPathToTarget = unableToPathToTarget;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        this.stepHeight = 0.5f;
        this.jumpMovementFactor = 0.02f;

        //get out of 2 block or more deep water pits
        if (forward > 0) {
            double highestWaterPos = this.highestWaterPos();
            if (this.bodyTouchingLiquid() && this.posY >= highestWaterPos - 2 && this.posY <= highestWaterPos + 2) {
                double xMove = this.width * Math.sin(-Math.toRadians(this.rotationYaw));
                double zMove = this.width * Math.cos(Math.toRadians(this.rotationYaw));
                BlockPos ahead = new BlockPos(this.posX + xMove, highestWaterPos, this.posZ + zMove);
                BlockPos above = ahead.up();
                if (this.world.getBlockState(ahead).getMaterial().isSolid() && !this.world.getBlockState(above).getMaterial().isSolid() && !this.world.isRemote) {
                    this.setPosition(this.posX + xMove, highestWaterPos + 1D, this.posZ + zMove);
                }
            }
        }

        //float above water
        if (this.bodyTouchingLiquid()) this.motionY += 0.1D;

        super.travel(strafe, vertical, forward);
    }

    private double highestWaterPos() {
        double maxHeight = this.world.getActualHeight() - this.getPosition().getY();
        if (this.bodyTouchingLiquid()) {
            if (!this.world.getBlockState(this.getPosition()).getMaterial().isLiquid()) return 0D;
            for (int i = 0; i <= maxHeight; i++) {
                BlockPos pos = this.getPosition().add(0, i, 0);
                if (!this.world.getBlockState(pos).getMaterial().isLiquid()) return this.getPosition().getY() + i - 1;
            }
        }
        return 0D;
    }

    /**
     * nonhitboxed creatures use their main body
     * */
    public boolean bodyTouchingLiquid() {
        return this.isInWater() || this.isInLava();
    }

    //-----IRiftCreature boilerplate stuff-----
    @Override
    @NotNull
    public RiftCreatureBuilder getCreatureType() {
        return this.creatureType;
    }

    @Override
    public int getLevel() {
        return this.dataManager.get(LEVEL);
    }

    @Override
    public void setLevel(int value) {
        this.dataManager.set(LEVEL, value);
    }

    @Override
    public RiftCreatureEnums.Nature getNature() {
        byte natureOrdinal = this.dataManager.get(NATURE);
        if (natureOrdinal < 0 || natureOrdinal >= RiftCreatureEnums.Nature.values().length) return null;
        return RiftCreatureEnums.Nature.values()[natureOrdinal];
    }

    @Override
    public void setNature(RiftCreatureEnums.Nature value) {
        byte byteToSet = value != null ? (byte) value.ordinal() : (byte) -1;
        this.dataManager.set(NATURE, byteToSet);
    }

    @Override
    public int getAgeInTicks() {
        return this.dataManager.get(AGE_TICKS);
    }

    public int getAgeInDays() {
        return this.getAgeInTicks() / 20;
    }

    @Override
    public void setAgeInTicks(int value) {
        this.dataManager.set(AGE_TICKS, value);
    }

    @Override
    public float getStamina() {
        return this.dataManager.get(STAMINA_CURRENT);
    }

    @Override
    public void setStamina(float value) {
        this.dataManager.set(STAMINA_CURRENT, value);
    }

    @Override
    public float getMaxStamina() {
        return (float) this.getEntityAttribute(STAMINA_ATTRIBUTE).getAttributeValue();
    }

    @Override
    @NotNull
    public RiftLibInventoryHandler getCreatureInventory() {
        return this.creatureInventory;
    }

    @Override
    public CreatureStatsStorage getCreatureStats() {
        return this.dataManager.get(CREATURE_STATS);
    }

    @Override
    public void setCreatureStats(CreatureStatsStorage value) {
        this.dataManager.set(CREATURE_STATS, value);
    }

    @Override
    public CreatureMoveStorage getCreatureMoves() {
        return this.dataManager.get(CREATURE_MOVES);
    }

    @Override
    public void setCreatureMoves(CreatureMoveStorage value) {
        this.dataManager.set(CREATURE_MOVES, value);
    }

    //-----nbt parsing related stuff-----
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeCreatureNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("CreatureType")) {
            RiftCreatureBuilder builder = resolveCreatureBuilder(compound.getString("CreatureType"));
            if (this.creatureType != builder) {
                this.creatureType = builder;
                this.creatureInventory.setSize(this.creatureType.getInventorySize());
                this.applyCreatureTypeSettings();
                this.animData = new AnimationDataEntity(this, holder -> this.scale());
            }
        }
        this.readCreatureNBT(compound);
    }

    //-----dynamic ride pos related methods-----
    public RiftCreature getDynamicRideUser() {
        return this;
    }

    //-----ray related methods-----
    @Override
    public RiftCreature getRayCreator() {
        return this;
    }

    @Override
    public Map<String, RiftLibRayBuilder> getRayBuilders() {
        return this.rayMap;
    }

    @Override
    public void applyRaySegments(String rayName, BlockPos rayOrigin, RiftLibRay.RayHitResult rayHitResult) {
        if (this.rayHitEffectMap == null) return;
        this.rayHitEffectMap.get(rayName).accept(this, rayOrigin, rayHitResult);
    }

    //-----animation related methods-----
    @Override
    @NonNull
    public AnimationDataEntity getAnimationData() {
        return this.animData;
    }

    @Override
    public void initializeAnimationData(AnimationDataEntity animationData) {
        //-----create animation controllers-----
        //---for normal stuff---
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "movement", "default",
                new AnimationControllerState<AnimationDataEntity>("default")
                        .addStateTransition("moving", animData -> this.getCurrentMove().isEmpty() && !this.isLeaping() && animData.isMoving()),
                new AnimationControllerState<AnimationDataEntity>("moving", 0.1)
                        .addAnimation("animation."+this.creatureType.getName()+".walk")
                        .addStateTransition("default", animData -> !this.getCurrentMove().isEmpty() || this.isLeaping() || !animData.isMoving())
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "sprintPosing", "default",
                new AnimationControllerState<AnimationDataEntity>("default", 0.2)
                        .addStateTransition("sprint", animData -> this.getCurrentMove().isEmpty() && !this.isLeaping() && this.isSprinting()),
                new AnimationControllerState<AnimationDataEntity>("sprint", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".sprint_pose")
                        .addStateTransition("default", animData -> !this.getCurrentMove().isEmpty() || this.isLeaping() || !this.isSprinting())
        ));
        if (this.creatureType.getNavigation().getCanLeap()) {
            animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "leaping", "default",
                    new AnimationControllerState<AnimationDataEntity>("default", 0.1)
                            .addStateTransition("leaping", animData -> this.isLeaping()),
                    new AnimationControllerState<AnimationDataEntity>("leaping", 0.1)
                            .addAnimation("animation."+this.creatureType.getName()+".leap")
                            .addStateTransition("default", animData -> !this.isLeaping())
            ));
        }
        //---for moves---
        //start with default
        this.initAnimControllerForPhase(animationData, "");
        //now to the other phases
        for (Map.Entry<String, CreaturePhaseBuilder> phase : this.creatureType.getPhaseBuilderMaps().entrySet()) {
            this.initAnimControllerForPhase(animationData, phase.getKey());
        }

        //-----create animation message effects-----
        animationData.addAnimationMessageEffect("moveHitEffect", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            if (creatureMoveStorage.canRunCurrentMoveHitEffect()) creatureMoveStorage.runCurrentMoveHitEffect(this);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupPrewindupFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.PREWINDUP);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupWindupFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder chargeupBuilder = creatureMoveBuilder == null ? null : creatureMoveBuilder.getMoveChargeupBuilder();
            if (chargeupBuilder != null && chargeupBuilder.getChargeUpWhileUse()) {
                creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.WINDUP);
            }
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupPrereleasingFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.PRERELEASING);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupReleasingFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder chargeupBuilder = creatureMoveBuilder == null ? null : creatureMoveBuilder.getMoveChargeupBuilder();
            if (chargeupBuilder != null && chargeupBuilder.getChargeUpThenRelease()) {
                creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.RELEASING);
            }
        }, Side.SERVER));
    }

    private void initAnimControllerForPhase(AnimationDataEntity animationData, @NotNull String phase) {
        List<AnimationControllerState<AnimationDataEntity>> creatureMovesStates = new ArrayList<>();
        List<ImmutablePair<String, CreatureMoveBuilder>> moveBuilderMap;
        if (phase.isEmpty()) moveBuilderMap = this.creatureType.getMoves();
        else moveBuilderMap = this.creatureType.getPhaseBuilderMaps().get(phase).getMoves();

        //add the initial state
        AnimationControllerState<AnimationDataEntity> initialState = new AnimationControllerState<>("default");
        creatureMovesStates.add(initialState);

        //define anim controller name
        final String controllerName = "moveUse" + phase;

        //iterate over each move
        for (ImmutablePair<String, CreatureMoveBuilder> moveEntry : moveBuilderMap) {
            final String moveName = moveEntry.getKey();
            CreatureMoveBuilder moveBuilder = moveEntry.getValue();
            CreatureMoveChargeupBuilder chargeupBuilder = moveBuilder.getMoveChargeupBuilder();

            //for chargeup moves, create states for each phase
            if (chargeupBuilder != null) {
                for (ChargeupPhase currentChargeupPhase : ChargeupPhase.values()) {
                    String phaseName = currentChargeupPhase.name().toLowerCase();
                    String controllerStateName = moveName + "_" + phaseName;

                    //---add transition in initial state---
                    initialState.addStateTransition(controllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, currentChargeupPhase));

                    //---define corresponding state---
                    AnimationControllerState<AnimationDataEntity> stateToAdd = new AnimationControllerState<AnimationDataEntity>(controllerStateName)
                            .addAnimation("animation."+this.creatureType.getName()+"."+controllerStateName);

                    //and loop again xd
                    for (ChargeupPhase otherChargeupPhase : ChargeupPhase.values()) {
                        if (otherChargeupPhase == currentChargeupPhase) continue;

                        String otherChargeupPhaseName = otherChargeupPhase.name().toLowerCase();
                        String otherControllerStateName = moveName + "_" + otherChargeupPhaseName;

                        stateToAdd.addStateTransition(
                                otherControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, otherChargeupPhase)
                        );
                    }

                    //exclusive for finish, to transition back to default
                    if (currentChargeupPhase == ChargeupPhase.FINISHING) {
                        stateToAdd.addStateTransition("default", animData -> animData.allAnimationsFinished(controllerName))
                                .addExitEffect(animData -> this.onMoveFinish(moveName));
                    }
                    //emergency exit condition for other phases, mostly for client
                    else stateToAdd.addStateTransition("default", animData -> this.getCreatureMoves().getCurrentMove().isEmpty());

                    //add the state
                    creatureMovesStates.add(stateToAdd);
                }
            }
            //for non chargeup moves, add other anim states for each move to a single anim controller
            else {
                //transition from initial state to a state associated with the move
                initialState.addStateTransition(moveName, animData -> this.getCurrentMove().equals(moveName));

                //create state for move
                AnimationControllerState<AnimationDataEntity> moveState = new AnimationControllerState<AnimationDataEntity>(moveName)
                        .addStateTransition("default", animData -> animData.allAnimationsFinished(controllerName))
                        .addExitEffect(animData -> this.onMoveFinish(moveName));

                //if the move state has multiple animation names, make it so that upon entry it
                //generates a random number to then use
                String[] moveAnimNames = moveBuilder.getAnimNames();
                if (moveAnimNames.length > 1) {
                    moveState.addEntryEffect(animData -> animData.setVariable("chosenMove", this.rand.nextInt(moveAnimNames.length)));
                }

                //iterate over each of the anim names and put them in the state for the move
                for (int index = 0; index < moveAnimNames.length; index++) {
                    String moveAnimName = "animation." + this.creatureType.getName() + "." + moveAnimNames[index];

                    //only 1 move, just add the move anim name
                    if (moveAnimNames.length == 1) moveState.addAnimation(moveAnimName);
                    //multiple moves, add the move anim name and a predicate that uses the chosenMove molang variable above
                    else {
                        int finalIndex = index;
                        moveState.addAnimation(moveAnimName, animData -> {
                            return animData.getVariable("chosenMove") == finalIndex;
                        });
                    }
                }

                //now add the final state
                creatureMovesStates.add(moveState);
            }
        }

        //now create animation controller for phase
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, controllerName, "default",
                creatureMovesStates.toArray(new AnimationControllerState[0])
        ));
    }

    //small helper method for defining what happens when a move finishes
    private void onMoveFinish(@NotNull String moveName) {
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        if (!creatureMoveStorage.hasCurrentMoveEndEffectFired()) {
            //use moveName, as it might have been erased on server after being cleared on client
            CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getUsableMoveBuilder(moveName);
            if (!this.world.isRemote && creatureMoveBuilder != null && creatureMoveBuilder.getOnMoveEndEffect() != null) {
                creatureMoveBuilder.getOnMoveEndEffect().accept(this);
            }
            creatureMoveStorage.markCurrentMoveEndEffectFired();
        }
        creatureMoveStorage.resetCurrentMove(this);
    }

    @NotNull
    public Vec3d getLocatorWorldPos(@NotNull String name) {
        AnimatedLocator animatedLocator = this.animData.getAnimatedLocator(name);
        if (animatedLocator == null) return this.getPositionVector();

        Vec3d modelSpacePos = animatedLocator.getModelSpacePosition();
        float parentScale = this.scale();
        float locatorX = -(float) (modelSpacePos.x / 16f);
        float locatorY = (float) (modelSpacePos.y / 16f);
        float locatorZ = -(float) (modelSpacePos.z / 16f);
        Vec3d locatorPos = new Vec3d(locatorX * parentScale, locatorY * parentScale, locatorZ * parentScale);

        double yawHead = -Math.toRadians(this.rotationYawHead);
        double yawBody = -Math.toRadians(this.rotationYaw);
        double yaw = this.isBeingRidden() ? yawBody : yawHead;
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0f, yaw, 0f);
        locatorPos = VectorUtils.rotateVectorWithQuaternion(locatorPos, quaternion);

        return new Vec3d(
                this.posX + locatorPos.x,
                this.posY + locatorPos.y,
                this.posZ + locatorPos.z
        );
    }

    //-----other useless events idk nor care about-----
    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
