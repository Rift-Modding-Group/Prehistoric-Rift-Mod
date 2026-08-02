package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.ai.RiftUnmountedUseMove;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate;
import anightdazingzoroark.prift.server.entity.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.Element;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.prift.util.MathUtil;
import anightdazingzoroark.prift.util.TriConsumer;
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
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityCow;
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
import org.jspecify.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiftCreature extends EntityTameable implements IAnimatable<AnimationDataEntity>, IRiftCreature, IRayCreator<RiftCreature> {
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

    //--custom property values, which can be called and manipulated from a creature builder--
    @NotNull
    private Map<String, AbstractPropertyValue<?>> propertyValueMap = Map.of();

    //--server side primitive params--
    //manages a creature's ability to sprint based on whether or not it attacked before
    public int sprintToAttackCooldown;
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

    //ray specific params
    protected Map<String, RiftLibRayBuilder> rayMap;
    protected Map<String, TriConsumer<RiftCreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;

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
        this.rayMap = this.creatureType.getRayMap();
        this.rayHitEffectMap = this.creatureType.getRayHitEffectMap();
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
    }

    //this is gonna be mostly for registering the custom attributes
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        //vanilla ATTACK_DAMAGE is to be used for melee damage attribute
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getAttributeMap().registerAttribute(ELEMENTAL_DAMAGE_ATTRIBUTE);
        this.getAttributeMap().registerAttribute(STAMINA_ATTRIBUTE);
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
        this.setCreatureMoves(creatureMoveStorage);

        //return value
        return super.onInitialSpawn(difficulty, livingdata);
    }

    //this is temporary for testing purposes, will be replaced w something more dynamic
    //after being developed further
    @Override
    protected void initEntityAI() {
        //temporary, will use the configs soon
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityCow.class, true));
        //this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));

        this.tasks.addTask(1, new RiftUnmountedUseMove(this));
        this.tasks.addTask(2, new EntityAIWander(this, 1D/*, 15*/));
        this.tasks.addTask(3, new EntityAILookIdle(this) {
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
            this.setLeaping(this.getCreatureMoveHelper().isLeaping());

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
            this.setCreatureMoves(creatureMoveStorage);

            //tick sprinting related stuff
            if (this.sprintToAttackCooldown > 0) this.sprintToAttackCooldown--;

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
        if (entityIn == null) return;

        double attackStat = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int sprintBasePower = 10; //is like this so that i can adjust whenever i need to
        double sprintDamage = attackStat * sprintBasePower * 0.005D;

        entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) sprintDamage);
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

    //-----move use management-----
    public String getCurrentMove() {
        return this.getCreatureMoves().getCurrentMove();
    }

    public void setCurrentMove(String name) {
        if (this.isLeaping() && name != null && !name.isEmpty()) return;
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCurrentMove(name);
        this.setCreatureMoves(creatureMoveStorage);
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
        return this.dataManager.get(LEAPING);
    }

    public boolean isUnableToPathToTarget() {
        EntityLivingBase target = this.getAttackTarget();
        return target != null && target.isEntityAlive() && this.unableToPathToTarget;
    }

    public void setUnableToPathToTarget(boolean unableToPathToTarget) {
        EntityLivingBase target = this.getAttackTarget();
        this.unableToPathToTarget = unableToPathToTarget && target != null && target.isEntityAlive();
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
            this.setCreatureMoves(creatureMoveStorage);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupPrewindupFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.PREWINDUP);
            this.setCreatureMoves(creatureMoveStorage);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupWindupFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder chargeupBuilder = creatureMoveBuilder == null ? null : creatureMoveBuilder.getMoveChargeupBuilder();
            if (chargeupBuilder != null && chargeupBuilder.getChargeUpWhileUse()) {
                creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.WINDUP);
            }
            this.setCreatureMoves(creatureMoveStorage);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupPrereleasingFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.PRERELEASING);
            this.setCreatureMoves(creatureMoveStorage);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveChargeupReleasingFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder chargeupBuilder = creatureMoveBuilder == null ? null : creatureMoveBuilder.getMoveChargeupBuilder();
            if (chargeupBuilder != null && chargeupBuilder.getChargeUpThenRelease()) {
                creatureMoveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.RELEASING);
            }
            this.setCreatureMoves(creatureMoveStorage);
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveFinished", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            if (!creatureMoveStorage.hasCurrentMoveEndEffectFired()) {
                CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
                if (!this.world.isRemote && creatureMoveBuilder != null && creatureMoveBuilder.getOnMoveEndEffect() != null) {
                    creatureMoveBuilder.getOnMoveEndEffect().accept(this);
                }
                creatureMoveStorage.markCurrentMoveEndEffectFired();
            }

            creatureMoveStorage.resetCurrentMove(this);
            this.setCreatureMoves(creatureMoveStorage);
        }, Side.CLIENT, Side.SERVER));
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
                String prewindupStateName = ChargeupPhase.PREWINDUP.name().toLowerCase();
                String windupStateName = ChargeupPhase.WINDUP.name().toLowerCase();
                String prereleasingStateName = ChargeupPhase.PRERELEASING.name().toLowerCase();
                String releasingStateName = ChargeupPhase.RELEASING.name().toLowerCase();
                String finishingStateName = ChargeupPhase.FINISHING.name().toLowerCase();

                String prewindupControllerStateName = moveName + "_" + prewindupStateName;
                String windupControllerStateName = moveName + "_" + windupStateName;
                String prereleasingControllerStateName = moveName + "_" + prereleasingStateName;
                String releasingControllerStateName = moveName + "_" + releasingStateName;
                String finishingControllerStateName = moveName + "_" + finishingStateName;

                initialState.addStateTransition(prewindupControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.PREWINDUP))
                        .addStateTransition(windupControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.WINDUP))
                        .addStateTransition(prereleasingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.PRERELEASING))
                        .addStateTransition(releasingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.RELEASING))
                        .addStateTransition(finishingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.FINISHING));

                AnimationControllerState<AnimationDataEntity> prewindupState = new AnimationControllerState<AnimationDataEntity>(prewindupControllerStateName)
                        .addAnimation("animation."+this.creatureType.getName()+"."+moveName+"_"+prewindupStateName)
                        .addStateTransition(finishingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.FINISHING))
                        .addStateTransition(windupControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.WINDUP));

                AnimationControllerState<AnimationDataEntity> windupState = new AnimationControllerState<AnimationDataEntity>(windupControllerStateName)
                        .addAnimation("animation."+this.creatureType.getName()+"."+moveName+"_"+windupStateName)
                        .addStateTransition(finishingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.FINISHING))
                        .addStateTransition(prereleasingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.PRERELEASING));

                AnimationControllerState<AnimationDataEntity> prereleasingState = new AnimationControllerState<AnimationDataEntity>(prereleasingControllerStateName)
                        .addAnimation("animation."+this.creatureType.getName()+"."+moveName+"_"+prereleasingStateName)
                        .addStateTransition(finishingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.FINISHING))
                        .addStateTransition(releasingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.RELEASING));

                AnimationControllerState<AnimationDataEntity> releasingState = new AnimationControllerState<AnimationDataEntity>(releasingControllerStateName)
                        .addAnimation("animation."+this.creatureType.getName()+"."+moveName+"_"+releasingStateName)
                        .addStateTransition(finishingControllerStateName, animData -> this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.FINISHING));

                AnimationControllerState<AnimationDataEntity> finishingState = new AnimationControllerState<AnimationDataEntity>(finishingControllerStateName)
                        .addAnimation("animation."+this.creatureType.getName()+"."+moveName+"_"+finishingStateName)
                        .addStateTransition("default", animData -> !this.getCreatureMoves().currentMoveMatches(moveName, ChargeupPhase.FINISHING));

                creatureMovesStates.add(prewindupState);
                creatureMovesStates.add(windupState);
                creatureMovesStates.add(prereleasingState);
                creatureMovesStates.add(releasingState);
                creatureMovesStates.add(finishingState);
            }
            //for non chargeup moves, add other anim states for each move to a single anim controller
            else {
                //transition from initial state to a state associated with the move
                initialState.addStateTransition(moveName, animData -> this.getCurrentMove().equals(moveName));

                //create state for move
                AnimationControllerState<AnimationDataEntity> moveState = new AnimationControllerState<AnimationDataEntity>(moveName)
                        .addStateTransition("default", animData -> animData.allAnimationsFinished(controllerName))
                        .addExitEffect(animData -> animData.sendMessage("moveFinished"));

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
