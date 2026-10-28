package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.util.CreatureLookUtil;

import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.client.ui.RiftCreatureUI;
import anightdazingzoroark.prift.client.rendering.block.CreatureBoxRangeRenderer;
import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxTileEntity;
import anightdazingzoroark.prift.server.entity.RiftDamage;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosList;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import io.netty.buffer.ByteBuf;
import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.builder.CreatureDomesticationBuilder;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureFood;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureConfig;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.ai.RiftFollowHerdLeader;
import anightdazingzoroark.prift.server.entity.ai.RiftFollowBoxOwner;
import anightdazingzoroark.prift.server.entity.ai.RiftCreatureLookHelper;
import anightdazingzoroark.prift.server.entity.ai.RiftGoToLandFromWater;
import anightdazingzoroark.prift.server.entity.ai.RiftHurtByTarget;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.ai.RiftFindTarget;
import anightdazingzoroark.prift.server.entity.ai.RiftUnmountedUseMove;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureLeapHelper;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate.BlockBreakPlanEntry;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder.RiddenAimingType;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder.RiddenCrosshairType;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.config.RiftListsConfig;
import anightdazingzoroark.prift.server.item.RiftItems;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.ProjectileAimUtil;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimArea;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

/**
 * le heart and soul of this mod
 * */
public class RiftCreature extends EntityTameable implements IAnimatable<AnimationDataEntityLiving>, IDynamicRideUser<RiftCreature>, IRiftCreature, ICreature, IRayCreator<RiftCreature>, IEntityAdditionalSpawnData, IGuiHolder<RiftCreatureGuiData> {
    @Nullable
    private BlockPos creatureBoxHome;
    @Nullable
    private AxisAlignedBB creatureBoxBounds;
    @NotNull
    private RiftCreatureBuilder creatureType;
    @NotNull
    private final CreatureGearInventoryHandler creatureGear;
    @NotNull
    private final RiftLibInventoryHandler creatureInventory;
    @NotNull
    private AnimationDataEntityLiving animData;
    @NotNull
    private DynamicRidePosList dynamicRidePosList;

    public static final IAttribute ELEMENTAL_DAMAGE_ATTRIBUTE = new RangedAttribute(null, "rift.elementalDamage", 2.0, 0.0, 2048.0).setShouldWatch(true);
    public static final IAttribute STAMINA_ATTRIBUTE = new RangedAttribute(null, "rift.stamina", 2.0, 0.0, 2048.0).setShouldWatch(true);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> XP = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> NATURE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Float> STAMINA_CURRENT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> STAGGERED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<CreatureMoveStorage> CREATURE_MOVES = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_MOVE_STORAGE);
    private static final DataParameter<CreatureStatsStorage> CREATURE_STATS = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_STATS_STORAGE);
    private static final DataParameter<String> CREATURE_PHASE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USE_BLOCK_BREAK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> TAMING_PROGRESS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte> TAME_TARGETING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> DEPLOYMENT_TYPE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BASE_DEPLOYMENT_BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> EAT_FROM_INVENTORY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEAP_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPRINT_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> RIDDEN_AIMING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    //--custom property values, which can be called and manipulated from a creature builder--
    @NotNull
    private Map<String, AbstractPropertyValue<?>> propertyValueMap = Map.of();

    //---remembered player targets (server only)---
    @NotNull
    private final List<UUID> rememberedPlayerTargetUUIDs = new ArrayList<>();

    //--server side primitive params and objects--
    @NotNull
    private final RiftCreatureSprintHelper sprintHelper;
    private int staminaDrainTicks;
    private float pendingStaminaDrain;
    @Nullable
    private ChargeupPhase riddenMoveChargeupPhase;
    private int riddenMoveChargeupPhaseTicks;
    @NotNull
    private String riddenAimingMoveName = "";
    @Nullable
    private RiddenAimTarget riddenAimTarget;
    private double riddenMeleeAimReach;
    @Nullable
    private RiddenAimArea riddenAimArea;
    private boolean riddenAreaAttackActive;
    @NotNull
    private final Set<Integer> riddenAreaHitEntities = new HashSet<>();
    //when a creature fails to use a move or takes too long to pathfind for melee move,
    //this counts up, which then makes them use a ranged move or their sprint move
    private int frustration;
    private int attackTargetHitCount;
    //when a creature is targeting this counts up, which can then be used in priority predicate
    private int rage;
    private int currentRageThreshold;
    private int rageEndCountdown;
    //tiredness of creature from tranq bombs. counts down every 0.5 seconds
    private int tiredness;
    private int tirednessCountdown;
    private int staminaRegenCountdown;
    private int inactiveStaminaRegenTicks;
    private int baseFollowOutOfRangeTicks;
    @NotNull
    private CreatureAcquisitionInfo acquisitionInfo = CreatureAcquisitionInfo.NONE;
    private RiftCreatureEnums.@Nullable SleepCause sleepCause;
    //herd helper
    @Nullable
    private RiftCreatureHerdHelper herdHelper;
    private int recentlyHitTimer;

    //target pathing state
    private boolean unableToPathToTarget;
    private int blockBreakEffectAttemptCount;
    private int moveFinishCount;
    private final Map<BlockPos, BlockBreakPlanEntry> activeBlockBreakPlan = new HashMap<>();

    //fall impact state
    private boolean trackingFallImpact;
    private double highestAirborneY;
    private double lastFallImpactYDelta;

    //ray specific params
    protected Map<String, RiftLibRayBuilder> rayMap;
    protected Map<String, TriConsumer<ICreature, BlockPos, RiftLibRay.RayHitResult>> rayHitEffectMap;

    public RiftCreature(World worldIn) {
        this(worldIn, RiftCreatureRegistry.PLACEHOLDER_CREATURE);
    }

    public RiftCreature(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = resolveCreatureBuilder(creatureName);
        this.sprintHelper = new RiftCreatureSprintHelper(this);
        this.creatureGear = new CreatureGearInventoryHandler(this);
        CreatureDomesticationBuilder domestication = this.creatureType.getDomestication();
        this.creatureInventory = new RiftLibInventoryHandler(domestication == null ? 1 : domestication.getInventorySize());
        this.lookHelper = new RiftCreatureLookHelper(this);
        this.moveHelper = new RiftCreatureMoveHelper(this);
        this.navigator = new RiftCreaturePathNavigate(this, worldIn);
        this.applyCreatureTypeSettings();
        this.animData = new CreatureAnimationData(this);
        this.dynamicRidePosList = new DynamicRidePosList(this, this.animData);
        this.combatTracker = new CombatTracker(this) {
            @Override
            @NotNull
            public ITextComponent getDeathMessage() {
                ITextComponent message = super.getDeathMessage();
                if (!RiftCreature.this.isTamed()) return message;
                return new TextComponentTranslation("party.warning.creature_died", message);
            }
        };

        if (worldIn != null && !worldIn.isRemote) {
            this.herdHelper = this.canDoHerding() ? new RiftCreatureHerdHelper(this) : null;
            this.initCreatureAI();
        }
    }

    @NotNull
    private static RiftCreatureBuilder resolveCreatureBuilder(String creatureName) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(creatureName);
        if (builder == null) builder = RiftCreatureRegistry.getCreatureBuilder(RiftCreatureRegistry.PLACEHOLDER_CREATURE);
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

    private void changeCreatureType(RiftCreatureBuilder builder) {
        if (this.creatureType == builder) return;

        this.leaveHerd();
        this.creatureType = builder;
        CreatureDomesticationBuilder domestication = this.creatureType.getDomestication();
        this.creatureInventory.setSize(domestication == null ? 1 : domestication.getInventorySize());
        this.applyCreatureTypeSettings();
        this.animData = new CreatureAnimationData(this);
        this.dynamicRidePosList = new DynamicRidePosList(this, this.animData);
        this.onCreatureTypeChanged();

        if (this.world != null && !this.world.isRemote) {
            this.tasks.taskEntries.clear();
            this.targetTasks.taskEntries.clear();
            this.herdHelper = this.canDoHerding() ? new RiftCreatureHerdHelper(this) : null;
            this.initCreatureAI();
        }
    }

    protected void onCreatureTypeChanged() {}

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(XP, 0);
        this.dataManager.register(NATURE, (byte) 0);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(STAMINA_CURRENT, 0f);
        this.dataManager.register(STAGGERED, false);
        this.dataManager.register(CreatureGearInventoryHandler.SADDLED, false);
        this.dataManager.register(CREATURE_MOVES, new CreatureMoveStorage());
        this.dataManager.register(CREATURE_STATS, new CreatureStatsStorage());
        this.dataManager.register(CREATURE_PHASE, "");
        this.dataManager.register(LEAPING, false);
        this.dataManager.register(USE_BLOCK_BREAK, false);
        this.dataManager.register(SLEEPING, false);
        this.dataManager.register(TAMING_PROGRESS, 0f);
        this.dataManager.register(TAME_TARGETING, (byte) 0);
        this.dataManager.register(DEPLOYMENT_TYPE, (byte) -1);
        this.dataManager.register(BASE_DEPLOYMENT_BEHAVIOR, (byte) RiftCreatureEnums.BaseDeploymentBehavior.WANDER.ordinal());
        this.dataManager.register(EAT_FROM_INVENTORY, false);
        this.dataManager.register(LEAP_COOLDOWN, 0);
        this.dataManager.register(SPRINT_COOLDOWN, 0);
        this.dataManager.register(RIDDEN_AIMING, false);
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
        this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32D);
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
        creatureStatsStorage.initializeIndividualValues(this.world.rand);
        creatureStatsStorage.parseStats(this.creatureType.getStats());
        creatureStatsStorage.applyStatsToCreature(this);
        this.setCreatureStats(creatureStatsStorage);

        //initialize creature moves
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCreatureUser(this.creatureType);

        //play the first idle sound immediately
        this.playLivingSound();

        //return value
        return super.onInitialSpawn(difficulty, livingdata);
    }

    /**
     * better than EntityLiving.initEntityAI() :tm:
     * */
    private void initCreatureAI() {
        if (this.creatureType.getRetaliateWhenAttacked() != null) {
            this.targetTasks.addTask(1, new RiftHurtByTarget(this));
        }
        if (this.creatureType.getDomestication() != null) {
            this.targetTasks.addTask(1, new EntityAIOwnerHurtTarget(this) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.getIsSleeping()
                            && !RiftCreature.this.isBeingRidden()
                            && RiftCreature.this.getTameTargeting() == RiftCreatureEnums.TameTargeting.ASSIST
                            && super.shouldExecute();
                }
            });
            this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.getIsSleeping()
                            && !RiftCreature.this.isBeingRidden()
                            && RiftCreature.this.getTameTargeting() == RiftCreatureEnums.TameTargeting.DEFENSIVE
                            && super.shouldExecute();
                }
            });
        }
        this.targetTasks.addTask(2, new RiftFindTarget(this, true));

        if (this.getNavigationBuilder() != null && this.creatureType.getPanicOnHit()) {
            this.tasks.addTask(0, new EntityAIPanic(this, 1.25D) {
                @Override
                public boolean shouldExecute() {
                    if (RiftCreature.this.isTamed() || RiftCreature.this.isStaggered()) return false;

                    //special case for herders
                    if (RiftCreature.this.herdHelper != null) {
                        return RiftCreature.this.herdHelper.getMembers().stream().anyMatch(member -> {
                            return member != RiftCreature.this && member.canDoHerding() && member.getRevengeTarget() != null;
                        }) && this.findRandomPosition();
                    }

                    //other
                    return super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return !RiftCreature.this.isTamed() && !RiftCreature.this.isStaggered() && super.shouldContinueExecuting();
                }
            });
        }
        if (this.getNavigationBuilder() != null && this.creatureType.getFleePredicate() != null) {
            this.tasks.addTask(0, new EntityAIAvoidEntity<EntityLivingBase>(
                    this, EntityLivingBase.class,
                    this::shouldFleeFrom, this.creatureType.getFleeSearchDistance(),
                    1D, 1D
            ) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.isTamed() && !RiftCreature.this.isStaggered() && super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return this.closestLivingEntity != null
                            && RiftCreature.this.shouldFleeFrom(this.closestLivingEntity)
                            && !RiftCreature.this.isTamed()
                            && !RiftCreature.this.isStaggered()
                            && super.shouldContinueExecuting();
                }

                @Override
                public void startExecuting() {
                    RiftCreature.this.setAttackTarget(null);
                    super.startExecuting();
                }
            });
        }
        this.tasks.addTask(1, new RiftUnmountedUseMove(this));
        if (this.getNavigationBuilder() != null && !this.getNavigationBuilder().getCanSwim()) {
            this.tasks.addTask(2, new RiftGoToLandFromWater(this));
        }
        if (this.getNavigationBuilder() != null && this.creatureType.isHerder()) {
            this.tasks.addTask(3, new RiftFollowHerdLeader(this));
        }
        if (this.getNavigationBuilder() != null && this.creatureType.getDomestication() != null) {
            this.tasks.addTask(3, new RiftFollowBoxOwner(this));
            this.tasks.addTask(3, new EntityAIFollowOwner(this, 1D, 10f, 4f) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.getIsSleeping() && !RiftCreature.this.isBeingRidden()
                            && RiftCreature.this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE
                            && RiftCreature.this.getAttackTarget() == null && !RiftCreature.this.isStaggered()
                            && super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return !RiftCreature.this.getIsSleeping() && !RiftCreature.this.isBeingRidden()
                            && RiftCreature.this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE
                            && RiftCreature.this.getAttackTarget() == null && !RiftCreature.this.isStaggered()
                            && super.shouldContinueExecuting();
                }
            });
        }
        if (this.getNavigationBuilder() != null) {
            this.tasks.addTask(4, new EntityAIWander(this, 1D) {
                @Override
                public boolean shouldExecute() {
                    return (!RiftCreature.this.isTamed() || RiftCreature.this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE
                            && RiftCreature.this.getBaseDeploymentBehavior() == RiftCreatureEnums.BaseDeploymentBehavior.WANDER)
                            && !RiftCreature.this.getIsSleeping() && !RiftCreature.this.isStaggered() && RiftCreature.this.canLeadHerdBehavior() && super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return (!RiftCreature.this.isTamed() || RiftCreature.this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE
                            && RiftCreature.this.getBaseDeploymentBehavior() == RiftCreatureEnums.BaseDeploymentBehavior.WANDER)
                            && !RiftCreature.this.getIsSleeping() && !RiftCreature.this.isStaggered() && RiftCreature.this.canLeadHerdBehavior() && super.shouldContinueExecuting();
                }

                @Override
                public void resetTask() {
                    if (RiftCreature.this.isTamed() && RiftCreature.this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE
                            || RiftCreature.this.getBaseDeploymentBehavior() != RiftCreatureEnums.BaseDeploymentBehavior.WANDER
                            || RiftCreature.this.isStaggered() || !RiftCreature.this.canLeadHerdBehavior()) RiftCreature.this.getNavigator().clearPath();
                    super.resetTask();
                }
            });
            this.tasks.addTask(5, new EntityAILookIdle(this) {
                @Override
                public boolean shouldExecute() {
                    return RiftCreature.this.getAttackTarget() == null && !RiftCreature.this.getIsSleeping()
                            && !RiftCreature.this.isBeingRidden() && !RiftCreature.this.isStaggered()
                            && RiftCreature.this.getRNG().nextFloat() < 0.15f;
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return RiftCreature.this.getAttackTarget() == null && !RiftCreature.this.getIsSleeping()
                            && !RiftCreature.this.isBeingRidden() && !RiftCreature.this.isStaggered()
                            && super.shouldContinueExecuting();
                }

                @Override
                public void resetTask() {
                    this.idleTime = 0;
                }
            });
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //disable default growth system
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        //for slow fall entities, reduce fall speed
        if (this.creatureType.getFallSlowly() && !this.onGround && this.motionY < 0D) {
            this.motionY *= 0.6D;
        }

        //server only operations
        if (!this.world.isRemote) {
            if (this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE) {
                this.setDead();
                return;
            }

            //go back to wandering when owner is out of range after some time
            if (this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE
                    && this.getBaseDeploymentBehavior() == RiftCreatureEnums.BaseDeploymentBehavior.FOLLOW
            ) {
                EntityLivingBase owner = this.getOwner();
                AxisAlignedBB bounds = this.getDeploymentBounds();
                if (owner != null && owner.isEntityAlive() && owner.world == this.world
                        && bounds != null && bounds.contains(owner.getPositionVector())
                ) {
                    this.baseFollowOutOfRangeTicks = 0;
                }
                else if (++this.baseFollowOutOfRangeTicks >= 10) {
                    this.setBaseDeploymentBehavior(RiftCreatureEnums.BaseDeploymentBehavior.WANDER);
                }
            }
            else this.baseFollowOutOfRangeTicks = 0;
            this.enforceCreatureBoxRange();

            this.getCreatureMoveHelper().getLeapHelper().updateLeap(RiftCreatureLeapHelper.UpdatePhase.TICK);

            if (this.isBeingRidden()) {
                if (!this.isSaddled()) this.removePassengers();
                else {
                    if (this.getCurrentMove().isEmpty()) this.setAttackTarget(null);
                    this.getNavigator().clearPath();
                }
            }
            this.sprintHelper.updateSprint(RiftCreatureSprintHelper.UpdatePhase.TICK);

            //sleep and tiredness from tranq bombs
            if (!this.getIsSleeping() && this.tiredness >= 100) {
                this.sleepCause = RiftCreatureEnums.SleepCause.TRANQ_BOMB;
                this.setIsSleeping(true);
            }
            if (this.tiredness > 0) {
                this.tirednessCountdown++;
                //as good as 0.5 seconds
                if (this.tirednessCountdown >= 10) {
                    this.tiredness--;
                    this.tirednessCountdown = 0;
                }
            }
            //wake up
            if (this.tiredness <= 0 && this.sleepCause == RiftCreatureEnums.SleepCause.TRANQ_BOMB) {
                this.sleepCause = null;
                this.setIsSleeping(false);
            }

            //tick herding
            if (this.herdHelper != null) this.herdHelper.onUpdate();

            //tick recently hit timer
            if (this.recentlyHitTimer >= 0) this.recentlyHitTimer--;

            //creatures must drop threats that they are configured to flee
            if (this.shouldFleeFrom(this.getAttackTarget())) this.setAttackTarget(null);

            //tick fall impacts
            if (this.getIsSleeping() || !this.creatureType.getFallCreatesImpact() || this.bodyTouchingLiquid()) {
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

            //-----tick creature move storage-----
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            creatureMoveStorage.updateUsableMoves(this, this.getAttackTarget());
            creatureMoveStorage.tickCooldowns();
            boolean cancelCurrentMoveForMissingTarget = creatureMoveStorage.shouldCancelCurrentMoveForMissingTarget(this);
            if (cancelCurrentMoveForMissingTarget) creatureMoveStorage.finishCurrentMoveUse(this);

            CreatureMoveBuilder currentMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder currentMoveChargeupBuilder = currentMoveBuilder == null
                    ? null : currentMoveBuilder.getMoveChargeupBuilder();

            ChargeupPhase currentRiddenChargeupPhase;
            if (creatureMoveStorage.currentMoveMatches(this.getCurrentMove(), ChargeupPhase.WINDUP)) {
                currentRiddenChargeupPhase = ChargeupPhase.WINDUP;
            }
            else if (creatureMoveStorage.currentMoveMatches(this.getCurrentMove(), ChargeupPhase.PRERELEASING)) {
                currentRiddenChargeupPhase = ChargeupPhase.PRERELEASING;
            }
            else currentRiddenChargeupPhase = null;

            if (this.isBeingRidden()
                    && currentMoveChargeupBuilder != null
                    && currentMoveChargeupBuilder.getChargeUpWhileUse()
                    && currentRiddenChargeupPhase != null
            ) {
                if (this.riddenMoveChargeupPhase != currentRiddenChargeupPhase) {
                    this.riddenMoveChargeupPhase = currentRiddenChargeupPhase;
                    this.riddenMoveChargeupPhaseTicks = 0;
                }
                this.riddenMoveChargeupPhaseTicks++;
                if (this.riddenMoveChargeupPhaseTicks >= 7) {
                    creatureMoveStorage.finishCurrentMoveChargeupPhase(this, currentRiddenChargeupPhase);
                    this.riddenMoveChargeupPhase = null;
                    this.riddenMoveChargeupPhaseTicks = 0;
                }
            }
            else {
                this.riddenMoveChargeupPhase = null;
                this.riddenMoveChargeupPhaseTicks = 0;
            }

            //-----stamina consumption-----
            float staminaDrainPerSecond = 0f;
            int staminaConsumptionInterval = 0;

            //stamina consumption when moving
            if (this.animData.isMoving()) {
                if (this.isSprinting()) {
                    staminaDrainPerSecond = MoveResult.SPRINT.staminaConsumption();
                    staminaConsumptionInterval = MoveResult.SPRINT.staminaConsumptionInterval();
                }
                //stamina consumption when moving normally only applies on tamed creatures
                //that are being ridden controlled by a player
                else if (this.isTamed() && this.getControllingPassenger() != null) {
                    staminaDrainPerSecond = 0.01f;
                    staminaConsumptionInterval = 60;
                }
            }
            boolean currentMoveDrainsStamina = currentMoveBuilder != null
                    && (!this.getUseBlockBreak() || this.isBeingRidden())
                    && currentMoveBuilder.getStaminaDrainPerSecond() > 0f
                    && creatureMoveStorage.currentMoveMatches(this.getCurrentMove(), ChargeupPhase.RELEASING);
            if (currentMoveDrainsStamina) {
                staminaDrainPerSecond += currentMoveBuilder.getStaminaDrainPerSecond();
                int moveStaminaConsumptionInterval = MoveResult.USE_MOVE.staminaConsumptionInterval();
                staminaConsumptionInterval = staminaConsumptionInterval == 0
                        ? moveStaminaConsumptionInterval
                        : Math.min(staminaConsumptionInterval, moveStaminaConsumptionInterval);
            }

            boolean staminaStoppedCurrentMove = false;
            if (staminaDrainPerSecond > 0f) {
                float staminaDrainThisTick = staminaDrainPerSecond / 20f;
                if (this.getStamina() > 0) {
                    this.pendingStaminaDrain += staminaDrainThisTick;
                    this.staminaDrainTicks++;
                    if (this.staminaDrainTicks >= staminaConsumptionInterval) {
                        if (!this.useStamina(this.pendingStaminaDrain)) {
                            this.sprintHelper.endSprint();
                            if (currentMoveDrainsStamina) {
                                creatureMoveStorage.finishCurrentMoveUse(this);
                                staminaStoppedCurrentMove = true;
                            }
                        }
                        this.pendingStaminaDrain = 0f;
                        this.staminaDrainTicks = 0;
                    }
                }
                else {
                    if (this.pendingStaminaDrain > 0f) this.useStamina(this.pendingStaminaDrain);
                    this.pendingStaminaDrain = 0f;
                    this.staminaDrainTicks = 0;
                    this.sprintHelper.endSprint();
                    if (currentMoveDrainsStamina) {
                        creatureMoveStorage.finishCurrentMoveUse(this);
                        staminaStoppedCurrentMove = true;
                    }
                }
            }
            else {
                if (this.pendingStaminaDrain > 0f) this.useStamina(this.pendingStaminaDrain);
                this.pendingStaminaDrain = 0f;
                this.staminaDrainTicks = 0;
            }

            if (!cancelCurrentMoveForMissingTarget && !staminaStoppedCurrentMove) {
                creatureMoveStorage.tickCurrentMove(this, this.getAttackTarget());
            }
            if (this.isBeingRidden()) this.dataManager.setDirty(CREATURE_MOVES);

            //when out of stamina, creature will go into a staggered pose and recover after 10-20 seconds
            if (this.getStamina() <= 0f && !this.isStaggered()) {
                this.sprintHelper.endSprint();
                if (this.getCurrentMove().isEmpty() && !this.isLeaping()) {
                    this.setStaggered(true);
                    this.staminaRegenCountdown = this.rand.nextInt(200, 401); //as good as 10-20 seconds
                    this.getNavigator().clearPath();
                    this.getCreatureMoveHelper().stopMovement();
                    if (!this.isBeingRidden()) this.setUseBlockBreak(false);
                    this.pendingStaminaDrain = 0f;
                    this.staminaDrainTicks = 0;
                }
            }

            //-----stamina regen-----
            if (this.isStaggered()) {
                this.inactiveStaminaRegenTicks = 0;
                this.staminaRegenCountdown--;
                if (this.staminaRegenCountdown <= 0) {
                    this.setStamina(this.getMaxStamina());
                    this.setStaggered(false);
                }
            }
            else if (this.getStamina() < this.getMaxStamina()) {
                boolean inactive = !this.animData.isMoving() && this.getAttackTarget() == null
                        && this.getCurrentMove().isEmpty() && !this.isSprinting() && !this.isLeaping();
                if (inactive) {
                    this.inactiveStaminaRegenTicks++;
                    if (this.inactiveStaminaRegenTicks >= 900) {
                        this.setStamina(this.getMaxStamina());
                        this.inactiveStaminaRegenTicks = 0;
                    }
                }
                else this.inactiveStaminaRegenTicks = 0;
            }
            else this.inactiveStaminaRegenTicks = 0;

            //-----eat one useful food item from the inventory every 3 seconds when enabled-----
            if (this.isTamed() && this.getEatFromInventory() && this.ticksExisted % 60 == 0
                    && (this.getHealth() < this.getMaxHealth() || this.getStamina() < this.getMaxStamina())
            ) {
                RiftLibInventoryHandler.ItemSearchResult foodSearchResult = this.creatureInventory.findItem(
                        RiftLibInventoryHandler.ItemSearchDirection.LAST_TO_FIRST,
                        foodStack -> {
                            RiftCreatureFood creatureFood = this.getCreatureFood(foodStack);
                            if (creatureFood == null) return false;

                            boolean restoresHealth = creatureFood.percentHealed != null && creatureFood.percentHealed > 0f
                                    && this.getHealth() < this.getMaxHealth();
                            boolean restoresStamina = creatureFood.percentReenergized != null && creatureFood.percentReenergized > 0f
                                    && this.getStamina() < this.getMaxStamina();
                            return restoresHealth || restoresStamina;
                        }
                );
                if (foodSearchResult.successful()) {
                    ItemStack foodStack = foodSearchResult.foundStack();
                    RiftCreatureFood creatureFood = this.getCreatureFood(foodStack);
                    if (creatureFood != null) {
                        if (creatureFood.percentHealed != null && creatureFood.percentHealed > 0f) {
                            this.heal(this.getMaxHealth() * creatureFood.percentHealed);
                        }
                        if (creatureFood.percentReenergized != null && creatureFood.percentReenergized > 0f) {
                            this.setStamina(this.getStamina() + this.getMaxStamina() * creatureFood.percentReenergized);
                        }
                        if (creatureFood.foodEffects != null) {
                            for (RiftCreatureFood.FoodEffect foodEffect : creatureFood.foodEffects) {
                                if (foodEffect.effectId == null || foodEffect.effectDuration == null || foodEffect.effectStrength == null) continue;

                                Potion potion = Potion.getPotionFromResourceLocation(foodEffect.effectId);
                                if (potion != null && foodEffect.effectDuration > 0) {
                                    this.addPotionEffect(new PotionEffect(
                                            potion, foodEffect.effectDuration * 20, Math.max(0, foodEffect.effectStrength)
                                    ));
                                }
                            }
                        }

                        this.creatureInventory.extractItem(foodSearchResult.slot(), 1, false);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    }
                }
            }

            //tick creature rage
            if (this.getAttackTarget() != null) {
                //set rage threshold to between 1 - 1.5 minutes
                if (this.currentRageThreshold <= 0) this.currentRageThreshold = this.world.rand.nextInt(1200, 1801);
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

    @Override
    public boolean processInteract(EntityPlayer player, @NotNull EnumHand hand) {
        CreatureDomesticationBuilder domestication = this.creatureType.getDomestication();
        ItemStack heldItem = player.getHeldItem(hand);

        //tamed only effects
        if (this.isTamed()) {
            if (this.isOwner(player)) {
                if (heldItem.isEmpty() && !this.world.isRemote) {
                    //mount a saddled creature
                    if (this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE
                            && this.canBeRidden() && this.isSaddled() && !player.isSneaking() && !this.getIsSleeping()
                    ) {
                        player.startRiding(this);
                    }
                    //switch between creature base behavior when in base
                    else if (this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE && !player.isSneaking()) {
                        RiftCreatureEnums.BaseDeploymentBehavior current = this.getBaseDeploymentBehavior();
                        int nextEnum;
                        if (current.ordinal() + 1 < RiftCreatureEnums.BaseDeploymentBehavior.values().length) {
                            nextEnum = current.ordinal() + 1;
                        }
                        else nextEnum = 0;

                        RiftCreatureEnums.BaseDeploymentBehavior next = RiftCreatureEnums.BaseDeploymentBehavior.values()[nextEnum];
                        this.setBaseDeploymentBehavior(next);

                        //send message
                        player.sendStatusMessage(
                                new TextComponentTranslation(
                                        "party.warning.base_creature_behavior", this.getName(), next.getTranslatedName()
                                ),
                                false
                        );
                    }
                    //open creature ui
                    else RiftCreatureGuiFactory.INSTANCE.open(player, this);
                }
                //feed tamed creatures for healing
                else if (!heldItem.isEmpty()) {
                    RiftCreatureFood creatureFood = this.getCreatureFood(heldItem);
                    if (creatureFood != null && creatureFood.percentHealed != null && creatureFood.percentHealed > 0f
                            && this.getHealth() < this.getMaxHealth()
                    ) {
                        this.consumeItemFromStack(player, heldItem);
                        this.heal(this.getMaxHealth() * creatureFood.percentHealed);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    }
                }
                return true;
            }
            else return false;
        }
        //stuff for wild creatures
        else if (domestication != null) {
            //creative meal automatically tames creature
            if (heldItem.getItem() == RiftItems.CREATIVE_MEAL) {
                this.consumeItemFromStack(player, heldItem);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.tameCreature(player);
                return true;
            }
            //normal feed taming
            else if (!heldItem.isEmpty()) {
                RiftCreatureFood creatureFood = this.getCreatureFood(heldItem);

                boolean hasTamingEffectiveness;
                if (creatureFood == null) hasTamingEffectiveness = false;
                else hasTamingEffectiveness = creatureFood.tameEffectiveness != null && creatureFood.tameEffectiveness.length > 0;

                //feed creature taming food to eventually tame it
                if (creatureFood != null && ((hasTamingEffectiveness && domestication.getTamingMethod() == RiftCreatureEnums.TamingMethod.FEED)
                        || (this.getIsSleeping() && this.sleepCause == RiftCreatureEnums.SleepCause.TRANQ_BOMB))
                ) {
                    float effectiveness = this.rand.nextFloat(creatureFood.tameEffectiveness[0], creatureFood.tameEffectiveness[1]);
                    if (this.getIsSleeping() && this.sleepCause == RiftCreatureEnums.SleepCause.TRANQ_BOMB) {
                        effectiveness *= this.getTamingEffectivenessForLevel();
                    }

                    float updatedProgress = Math.clamp(this.getTamingProgress() + effectiveness, 0f, 1f);
                    boolean completesTaming = updatedProgress >= 1f;
                    if (!completesTaming || !ForgeEventFactory.onAnimalTame(this, player)) {
                        this.consumeItemFromStack(player, heldItem);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());

                        if (completesTaming) this.tameCreature(player);
                        else this.setTamingProgress(updatedProgress);
                    }
                    return true;
                }
                //to prevent idiots from attempting to feed anything then complaining,
                //give them a warning them tell them the right way to tame the creature
                else if (creatureFood != null && domestication.getTamingMethod() == RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED
                        && !(this.getIsSleeping() && this.sleepCause == RiftCreatureEnums.SleepCause.TRANQ_BOMB) && !this.world.isRemote
                ) {
                    player.sendStatusMessage(new TextComponentTranslation("reminder.incorrect_taming_method"), false);
                    return false;
                }
                else return false;
            }
            else return false;
        }
        else return super.processInteract(player, hand);
    }

    /**
     * Used to check if a given BlockPos is within the bounds of an AnimatedBoundingBox
     * */
    public boolean posWithinBoundingBox(@NotNull Vec3d posVec, @NotNull String boundingBoxName) {
        AxisAlignedBB aabb = this.animData.getWorldSpaceAABB(boundingBoxName);
        if (aabb == null) return false;
        return aabb.grow(1e-5D).contains(posVec);
    }

    @Override
    public boolean aabbIntersectsBoundingBox(@NotNull AxisAlignedBB otherAABB, @NotNull String boundingBoxName) {
        AxisAlignedBB aabb = this.animData.getWorldSpaceAABB(boundingBoxName);
        if (aabb == null) return false;
        return aabb.intersects(otherAABB);
    }

    @Override
    public boolean aabbIntersectsBoundingBoxTag(@NotNull AxisAlignedBB otherAABB, @NotNull String tagName) {
        List<AnimatedBoundingBox> boundingBoxesInTag = this.animData.getAnimatedBoundingBoxesByTag().get(tagName);
        if (boundingBoxesInTag == null) return false;
        for (AnimatedBoundingBox animatedBoundingBox : boundingBoxesInTag) {
            AxisAlignedBB aabb = this.animData.getWorldSpaceAABB(animatedBoundingBox.getName());
            if (aabb != null && aabb.intersects(otherAABB)) return true;
        }
        return false;
    }

    //---misc ICreature implementations from the api starts here---
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
    public boolean hasStraightWalkingPathTo(@NotNull EntityLivingBase target) {
        return this.getCreaturePathNavigate().hasStraightWalkingPathTo(target);
    }

    @Override
    public double horizontalDistanceFromEntity(@NotNull Entity entity) {
        double dx = Math.pow(this.posX - entity.posX, 2);
        double dz = Math.pow(this.posZ - entity.posZ, 2);
        return this.width / 2D + Math.sqrt(dx + dz);
    }

    @Override
    public double verticalDistanceFromEntity(@NotNull Entity entity) {
        double thisMidY = this.posY + this.height / 2D;
        double entityMidY = entity.posY + entity.height / 2D;
        return this.height / 2D + Math.abs(thisMidY - entityMidY);
    }
    //---misc ICreature implementations from the api ends here---

    //this gets the scale of the model of the entity
    public float scale() {
        return MathUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                this.creatureType.getScaleRangeForAge()[0], this.creatureType.getScaleRangeForAge()[1]
        );
    }

    /**
     * Blocks friendly damage and applies the attack element against each distinct creature element.
     * */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source != null && (this.isRelatedToEntity(source.getTrueSource()) || this.isRelatedToEntity(source.getImmediateSource()))) {
            return false;
        }

        if (source != null) {
            Element attackElement;
            if (source instanceof RiftDamage.RiftElementDamageSource elementalSource) attackElement = elementalSource.element;
            else if (source.isFireDamage()) attackElement = Element.FIRE;
            else if (source == DamageSource.LIGHTNING_BOLT) attackElement = Element.ELECTRIC;
            else if (source.getTrueSource() != null || source.getImmediateSource() != null) attackElement = Element.NEUTRAL;
            else {
                if (!this.world.isRemote) this.recentlyHitTimer = 200;
                return super.attackEntityFrom(source, amount);
            }

            double multiplier = Arrays.stream(this.getCreatureType().getElement()).distinct()
                    .mapToDouble(element -> element.damageMultiplierFromAttackElement(attackElement))
                    .reduce(1D, (combined, effectiveness) -> combined * effectiveness);
            if (multiplier == 0D) return false;
            amount *= (float) multiplier;
        }

        if (!this.world.isRemote) this.recentlyHitTimer = 200;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (!this.world.isRemote && this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE && this.creatureBoxHome != null) {
            this.setDeploymentType(RiftCreatureEnums.CreatureDeployment.BASE_INACTIVE);
            if (this.world.getTileEntity(this.creatureBoxHome) instanceof CreatureBoxTileEntity tile) {
                tile.updateCreature(this);
            }
        }
        if (!this.world.isRemote && this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) {
            this.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE);
            if (this.getOwner() instanceof EntityPlayer player) {
                PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
                if (playerParty != null) playerParty.updatePartyMember(this);
            }
        }
        super.onDeath(cause);
    }

    /**
     * the vanilla attack entity method. is now used for damage calculations
     * use this when attacking an entity
     * */
    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        Entity hitEntity = entityIn instanceof MultiPartEntityPart hitboxPart ? (Entity) hitboxPart.parent : entityIn;

        //protection
        if (this.isRelatedToEntity(entityIn)) return false;

        //get move used
        CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
        if (creatureMoveBuilder == null) return false;

        //get damagesource and modify based on some stuff, like element
        DamageSource damageSource = RiftDamage.getElementDamageSource("mob", this, this, creatureMoveBuilder.getElement());

        //apply damage
        double damage = CreatureMoveHelper.calculateDamage(this);
        boolean flag = entityIn.attackEntityFrom(damageSource, (float) damage);

        //hit effects that require making contact
        if (creatureMoveBuilder.getMakesContact()) {
            if (creatureMoveBuilder.getOnTargetHitEffect() != null) creatureMoveBuilder.getOnTargetHitEffect().accept(this, entityIn);

            if (creatureMoveBuilder.getMoveChargeupBuilder() != null && creatureMoveBuilder.getMoveChargeupBuilder().getOnHitEntityDuringRelease() != null) {
                creatureMoveBuilder.getMoveChargeupBuilder().getOnHitEntityDuringRelease().accept(this, this.getCreatureMoves().getCurrentMoveChargeUpTicks(), entityIn);
            }
        }

        //other stuff
        Entity attackTarget = this.getAttackTarget();
        if (attackTarget != null && hitEntity == attackTarget) this.attackTargetHitCount++;
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    //this method is to be used when attacking from sprinting. sprinting is considered
    //a physical move that makes contact
    public void attackEntityFromSprint(Entity entityIn) {
        this.attackEntityFromMovement(entityIn, this.creatureType.getSprintBasePower());
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

    /**
     * this is for testing if another entity is related to this creature
     * such as if its tamed to its owner or if it is a herdmate or teammate
     * */
    public boolean isRelatedToEntity(@Nullable Entity entity) {
        if (entity instanceof MultiPartEntityPart hitboxPart) {
            Entity hitboxParent = (Entity) hitboxPart.parent;
            return this.isRelatedToEntity(hitboxParent);
        }
        else if (entity != null && this.isOnSameTeam(entity)) {
            return true;
        }
        else if (entity instanceof RiftCreature otherCreature && this.isHerdmate(otherCreature)) {
            return true;
        }
        else if (entity instanceof EntityTameable entityTameable) {
            return entityTameable.isTamed() && this.isTamed() && entityTameable.getOwner() != null && entityTameable.getOwner().equals(this.getOwner());
        }
        else if (entity instanceof EntityPlayer entityPlayer) {
            return this.isTamed() && this.getOwner() != null && this.getOwner().equals(entityPlayer);
        }
        return false;
    }

    @Override
    public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {
        return target != null && !this.isRelatedToEntity(target);
    }

    public boolean shouldFleeFrom(@Nullable EntityLivingBase entity) {
        return !this.isTamed() && entity != null && this.creatureType.getFleePredicate() != null
                && this.creatureType.getFleePredicate().test(this, entity);
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target) {
        if (target != null && (this.getIsSleeping() || this.isBeingRidden() || this.shouldFleeFrom(target))) return;
        boolean targetChanged = target != this.getAttackTarget();
        if (targetChanged) this.unableToPathToTarget = false;
        super.setAttackTarget(target);
        if (targetChanged && this.herdHelper != null && this.herdHelper.getLeader() == this) {
            this.herdHelper.updateLeaderTarget(this, target);
        }
    }

    @Override
    public int getMaxFallHeight() {
        return this.creatureType.getMaxFallHeight();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (this.creatureType.getFallSlowly()) return;
        float adjustedDistance = distance - this.getMaxFallHeight() + 3f;
        super.fall(adjustedDistance, damageMultiplier);
    }

    @Override
    @NotNull
    protected ResourceLocation getLootTable() {
        return new ResourceLocation(RiftInitialize.MODID, "entities/" + this.creatureType.getName());
    }

    @Override
    public String getName() {
        return this.getName(true);
    }

    public String getName(boolean showLevel) {
        String toReturn = this.hasCustomName() ? this.getCustomNameTag() : this.creatureType.getLocalizedName();
        if (showLevel) toReturn = toReturn + " (" + I18n.format("info.level", this.getLevel()) + ")";
        return toReturn;
    }

    @Override
    public boolean getAlwaysRenderNameTag() {
        return this.hasCustomName(); //teehee
    }

    @Nullable
    private RiftCreatureFood getCreatureFood(@NotNull ItemStack itemStack) {
        RiftCreatureConfig creatureConfig = this.getCreatureConfig();
        RiftListsConfig listsConfig = ServerProxy.jsonConfigParser.getListsConfig();

        //---check blacklists first---
        //reorganize first to have items first, anything w colons is presumed to be an item
        List<String> blacklist = new ArrayList<>(creatureConfig.foodItemBlacklist);
        blacklist.sort(Comparator.comparing(s -> !s.contains(":")));

        //now check
        for (String blacklistEntry : blacklist) {
            //w colon, presumed to be item entry
            if (blacklistEntry.contains(":")) {
                if (RiftUtil.itemStackMatchesString(itemStack, blacklistEntry)) return null;
            }
            //no colon, presumed to be list
            else {
                List<RiftCreatureFood> innerBlacklist = listsConfig.foodGroups.get(blacklistEntry);
                if (innerBlacklist == null) continue;

                if (innerBlacklist.stream().anyMatch(creatureFood -> RiftUtil.itemStackMatchesString(itemStack, creatureFood.itemId))) {
                    return null;
                }
            }
        }

        //---check whitelists---
        //reorganize first to have individual RiftCreatureFood entries first
        List<Object> whitelist = new ArrayList<>(creatureConfig.foodItemWhitelist);
        whitelist.sort(Comparator.comparing(object -> !(object instanceof RiftCreatureFood)));

        //now check
        for (Object object : whitelist) {
            //check creature food
            if (object instanceof RiftCreatureFood creatureFood) {
                if (RiftUtil.itemStackMatchesString(itemStack, creatureFood.itemId)) return creatureFood;
            }
            //check in list
            else if (object instanceof String string) {
                List<RiftCreatureFood> innerWhitelist = listsConfig.foodGroups.get(string);
                if (innerWhitelist == null) continue;

                Optional<RiftCreatureFood> match = innerWhitelist.stream()
                        .filter(creatureFood -> RiftUtil.itemStackMatchesString(itemStack, creatureFood.itemId))
                        .findFirst();
                if (match.isPresent()) return match.get();
            }
        }

        //---e---
        return null;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public void onRemovedFromWorld() {
        this.leaveHerd();
        super.onRemovedFromWorld();
    }

    /**
     * push other entities
     * */
    @Override
    public void collideWithEntity(Entity entityIn) {
        if (!this.isLargerThanEntity(entityIn)) {
            super.collideWithEntity(entityIn);
            return;
        }

        //special cases where given entity cannot be pushed back
        if (entityIn instanceof IProjectile || entityIn instanceof EntityFireball || entityIn.equals(this)
                || this.isRidingSameEntity(entityIn) || entityIn.noClip || (this.getCreatureMoveHelper().isLeaping() && entityIn.onGround)
        ) {
            return;
        }

        double dispX = entityIn.posX - this.posX;
        double dispZ = entityIn.posZ - this.posZ;
        double maxDisp = MathHelper.absMax(dispX, dispZ);

        maxDisp = MathHelper.sqrt(Math.max(maxDisp, 0.01D));
        dispX /= maxDisp;
        dispZ /= maxDisp;
        double d3 = Math.min(1D / maxDisp, 1D);

        dispX *= d3;
        dispZ *= d3;
        dispX *= 0.05f;
        dispZ *= 0.05f;
        dispX *= 1f - this.entityCollisionReduction;
        dispZ *= 1f - this.entityCollisionReduction;
        dispX = MathHelper.clamp(dispX, -0.05D, 0.05D);
        dispZ = MathHelper.clamp(dispZ, -0.05D, 0.05D);

        entityIn.addVelocity(dispX, 0D, dispZ);

        //mark dirty to force push on players
        if (entityIn instanceof EntityPlayer) entityIn.velocityChanged = true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isLargerThanEntity(@Nullable Entity entity) {
        if (entity == null) return false;
        float thisVolume = this.width * this.width * this.height;
        float entityVolume = entity.width * entity.width * entity.height;
        return thisVolume >= entityVolume;
    }

    //-----sound management-----
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.getIsSleeping()) return null;
        return RiftSounds.getCreatureSound(this.creatureType.getName(), "idle");
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return RiftSounds.getCreatureSound(this.creatureType.getName(), "hurt");
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return RiftSounds.getCreatureSound(this.creatureType.getName(), "death");
    }

    @Override
    public float getSoundVolume() {
        return MathUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                0.5f, 2f
        );
    }

    @Override
    public float getSoundPitch() {
        float maturity = MathUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                0f, 1f
        );
        return 1.25f - maturity * 0.25f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05f;
    }

    //-----herding management-----
    public boolean canDoHerding() {
        return !this.isTamed() && this.creatureType.isHerder() && this.creatureType.getMaxHerdSize() >= 2;
    }

    public boolean isInHerd() {
        return this.herdHelper != null && this.herdHelper.getSize() > 1;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isHerdLeader() {
        return this.herdHelper != null && this.herdHelper.getLeader() == this;
    }

    /**
     * solitary creatures retain normal behavior until they form a herd.
     */
    public boolean canLeadHerdBehavior() {
        return this.herdHelper == null || this.herdHelper.getLeader() == this;
    }

    @Nullable
    public RiftCreature getHerdLeader() {
        return this.herdHelper == null ? null : this.herdHelper.getLeader();
    }

    public boolean isHerdmate(@Nullable RiftCreature otherCreature) {
        if (otherCreature == null || otherCreature == this || this.herdHelper == null) {
            return false;
        }
        return this.herdHelper.contains(otherCreature);
    }

    public void leaveHerd() {
        if (this.herdHelper != null) this.herdHelper.removeMember(this);
    }

    @Nullable
    public RiftCreatureHerdHelper getHerd() {
        return this.herdHelper;
    }

    //-----properties management-----
    @SuppressWarnings("unchecked")
    public <I extends AbstractPropertyValue<?>> I getProperty(String key) {
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

    //-----projectile management-----
    @Override
    public void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, float velocity, float inaccuracy) {
        CreatureMoveBuilder moveBuilder = this.getCreatureMoves().getUsableMoveBuilder(this.getCurrentMove());
        if (moveBuilder == null) return;

        RiftProjectile projectile = new RiftProjectile(this, projectileBuilder, moveBuilder);
        Vec3d shootVector;
        Vec3d aimPosition = this.getRiddenAimPosition();
        if (aimPosition != null && moveBuilder.getRiddenAimingType() == RiddenAimingType.RANGED) {
            shootVector = ProjectileAimUtil.getLaunchVelocity(projectile.getPositionVector(), aimPosition, velocity);
        }
        else {
            shootVector = new Vec3d(this.getLookVec().x, 0D, this.getLookVec().z).scale(16D);
        }

        projectile.shoot(shootVector.x, shootVector.y, shootVector.z, velocity, inaccuracy);
        this.world.spawnEntity(projectile);
    }

    @Override
    public void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, @NotNull EntityLivingBase target, float velocity, float inaccuracy) {
        CreatureMoveBuilder moveBuilder = this.getCreatureMoves().getUsableMoveBuilder(this.getCurrentMove());
        if (moveBuilder == null) return;

        RiftProjectile projectile = new RiftProjectile(this, projectileBuilder, moveBuilder);
        double directionX = target.posX - projectile.posX;
        double directionY = (target.posY + target.height / 2D) - projectile.posY;
        double directionZ = target.posZ - projectile.posZ;
        projectile.shoot(directionX, directionY, directionZ, velocity, inaccuracy);
        this.world.spawnEntity(projectile);
    }

    //-----remembered player target management-----
    public void rememberPlayerTarget(@NotNull EntityPlayer player) {
        if (player.world.isRemote || this.rememberedPlayerTargetUUIDs.contains(player.getUniqueID())) return;
        this.rememberedPlayerTargetUUIDs.add(player.getUniqueID());
    }

    @NotNull
    public List<UUID> getRememberedPlayerTargetUUIDs() {
        return List.copyOf(this.rememberedPlayerTargetUUIDs);
    }

    public boolean isRememberedPlayerTarget(@NotNull EntityPlayer player) {
        return this.rememberedPlayerTargetUUIDs.stream().anyMatch(uuid -> player.getUniqueID().equals(uuid));
    }

    //-----sprint management-----
    @Override
    public int getSprintCooldown() {
        return this.dataManager.get(SPRINT_COOLDOWN);
    }

    @Override
    public void setSprintCooldown(int value) {
        this.dataManager.set(SPRINT_COOLDOWN, Math.max(0, value));
    }

    @Override
    public boolean canSprintToAttack() {
        return this.sprintHelper.canSprint();
    }

    @NotNull
    public RiftCreatureSprintHelper getSprintHelper() {
        return this.sprintHelper;
    }

    //-----leap management-----
    @Override
    public int getLeapCooldown() {
        return this.dataManager.get(LEAP_COOLDOWN);
    }

    @Override
    public void setLeapCooldown(int value) {
        this.dataManager.set(LEAP_COOLDOWN, Math.max(0, value));
    }

    @Override
    public boolean canLeapToAttack() {
        return this.getLeapCooldown() == 0;
    }

    public void removeLeapToAttackCooldown() {
        this.setLeapCooldown(0);
    }

    public void resetLeapToAttackCooldown() {
        this.setLeapCooldown(this.world.rand.nextInt(5, 11) * 20);
    }

    //-----frustration management-----
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
        return this.rage > this.currentRageThreshold;
    }

    //-----tiredness management-----
    public void addTiredness(int value) {
        if (this.world.isRemote || value <= 0) return; //server only
        if (this.getHealth() > this.getMaxHealth() * 0.15f) return; //must be at 15% of max health or less to be tired
        this.tiredness = Math.max(0, this.tiredness + value);
    }

    //-----sleep management-----
    public boolean getIsSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setIsSleeping(boolean value) {
        boolean enteringSleep = value && !this.getIsSleeping();
        this.dataManager.set(SLEEPING, value);
        if (!enteringSleep || this.world.isRemote) return;

        this.getNavigator().clearPath();
        this.getCreatureMoveHelper().stopMovement();
        this.setCurrentMove("");
        this.setUseBlockBreak(false);
        this.sprintHelper.endSprint();
        this.setAttackTarget(null);
        this.pendingStaminaDrain = 0f;
        this.staminaDrainTicks = 0;
    }

    @Override
    protected boolean isMovementBlocked() {
        return this.getIsSleeping() || this.isStaggered() || super.isMovementBlocked();
    }

    //-----taming management-----
    public void tameCreature(@NotNull EntityPlayer player) {
        if (this.world.isRemote) return;

        this.setTamingProgress(0f);
        this.setTamedBy(player);
        this.setAcquisitionInfo(new CreatureAcquisitionInfo(
                CreatureAcquisitionInfo.AcquisitionMethod.TAMED_FROM_WILD,
                System.currentTimeMillis() / 1000L
        ));
        this.setTameTargeting(RiftCreatureEnums.TameTargeting.ASSIST);
        this.setAttackTarget(null);
        this.setRevengeTarget(null);
        this.rememberedPlayerTargetUUIDs.clear();
        this.leaveHerd();
        this.herdHelper = null;
        this.tiredness = 0;
        this.enablePersistence();
        this.world.setEntityState(this, (byte) 7);
        this.setHealth(this.getMaxHealth());
        player.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished", this.getDisplayName()), false);

        PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
        if (playerParty != null && !playerParty.addPartyMember(this)) {
            if (PlayerCreatureBoxProperties.addTamedCreature(player, this)) {
                player.sendStatusMessage(new TextComponentTranslation("gui.prift.box.tamed_to_box"), false);
            }
            else {
                player.sendStatusMessage(new TextComponentTranslation("gui.prift.box.all_full"), false);
            }
        }
    }

    public float getTamingProgress() {
        return this.dataManager.get(TAMING_PROGRESS);
    }

    public void setTamingProgress(float value) {
        this.dataManager.set(TAMING_PROGRESS, Math.clamp(value, 0f, 1f));
    }

    //exponentially reduce taming effectiveness from food
    //and tiredness from tranq bombs based on level
    public float getTamingEffectivenessForLevel() {
        return Math.max(0.25f, 1f / (1f + Math.max(0, this.getLevel() - 1) * 0.1f));
    }

    @NotNull
    public RiftCreatureEnums.TameTargeting getTameTargeting() {
        return RiftCreatureEnums.TameTargeting.values()[this.dataManager.get(TAME_TARGETING)];
    }

    public void setTameTargeting(@NotNull RiftCreatureEnums.TameTargeting value) {
        if (this.getTameTargeting() != value) this.setAttackTarget(null);
        this.dataManager.set(TAME_TARGETING, (byte) value.ordinal());
    }

    public boolean getEatFromInventory() {
        return this.dataManager.get(EAT_FROM_INVENTORY);
    }

    public void setEatFromInventory(boolean value) {
        this.dataManager.set(EAT_FROM_INVENTORY, value);
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

        //clear block break plans when set false
        if (!value) this.activeBlockBreakPlan.clear();
    }

    /**
     * check block-breaking rules against this creature's configured tool levels.
     */
    public boolean canBreakBlock(@NotNull BlockPos blockPos) {
        IBlockState blockState = this.world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (!block.canEntityDestroy(blockState, this.world, blockPos, this)) return false;

        //negative harvest level assumes anything can break it
        //so make sure that it got low hardness when breakin
        float hardness = blockState.getBlockHardness(this.world, blockPos);
        int harvestLevel = block.getHarvestLevel(blockState);
        if (harvestLevel < 0 && hardness >= 0f && hardness <= 1f) return true;

        //search in block break level map
        Map<String, Integer> blockBreakLevels = this.creatureType.getBlockBreakLevelMap();
        if (blockBreakLevels == null) return false;

        for (Map.Entry<String, Integer> blockBreakEntry : blockBreakLevels.entrySet()) {
            //fences r strange
            boolean woodenFenceCheck = hardness >= 0f && blockBreakEntry.getKey().equals("axe")
                    && blockState.getMaterial() == Material.WOOD && blockState.getBlock() instanceof BlockFence;
            if ((block.isToolEffective(blockBreakEntry.getKey(), blockState) || woodenFenceCheck)
                    && blockBreakEntry.getValue() >= Math.max(0, harvestLevel)
            ) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private Set<BlockPos> getBreakableBlocksInFront() {
        Set<BlockPos> blocks = new HashSet<>();
        boolean riderMoveBreaking = this.getCreatureMoves().canCurrentMoveBreakBlocksFromRider(this);

        //---aiming!!!---
        if (riderMoveBreaking && this.isRiddenAiming() && this.getCurrentMove().equals(this.riddenAimingMoveName)) {
            RiddenAimTarget aimTarget = this.riddenAimTarget;
            if (aimTarget == null || aimTarget.blockPosition() == null || aimTarget.blockFace() == null) return blocks;

            int holeWidth = (int) Math.ceil(this.width);
            int holeHeight = (int) Math.ceil(this.height);
            EnumFacing.Axis hitAxis = aimTarget.blockFace().getAxis();

            //if crosshair hits bottom or top of block, break 1 block deep and use creature width for holesize
            if (hitAxis == EnumFacing.Axis.Y) {
                int minimumX = this.getCenteredHoleMinimum(aimTarget.position().x, holeWidth);
                int minimumZ = this.getCenteredHoleMinimum(aimTarget.position().z, holeWidth);
                for (int xOffset = 0; xOffset < holeWidth; xOffset++) {
                    for (int zOffset = 0; zOffset < holeWidth; zOffset++) {
                        this.addRiddenAimBreakableBlock(
                                blocks,
                                new BlockPos(
                                        minimumX + xOffset,
                                        aimTarget.blockPosition().getY(),
                                        minimumZ + zOffset
                                )
                        );
                    }
                }
                return blocks;
            }
            //otherwise, make hole based on height and width of user, block
            //hit by crosshair is the bottom and the center
            else {
                int minimumHorizontal;
                if (hitAxis == EnumFacing.Axis.X) {
                    minimumHorizontal = this.getCenteredHoleMinimum(aimTarget.position().z, holeWidth);
                }
                else {
                    minimumHorizontal = this.getCenteredHoleMinimum(aimTarget.position().x, holeWidth);
                }
                int minimumY = MathHelper.floor(aimTarget.position().y);
                for (int horizontalOffset = 0; horizontalOffset < holeWidth; horizontalOffset++) {
                    for (int verticalOffset = 0; verticalOffset < holeHeight; verticalOffset++) {
                        BlockPos blockPosition;
                        if (hitAxis == EnumFacing.Axis.X) {
                            blockPosition = new BlockPos(
                                    aimTarget.blockPosition().getX(),
                                    minimumY + verticalOffset,
                                    minimumHorizontal + horizontalOffset
                            );
                        }
                        else {
                            blockPosition = new BlockPos(
                                    minimumHorizontal + horizontalOffset,
                                    minimumY + verticalOffset,
                                    aimTarget.blockPosition().getZ()
                            );
                        }
                        this.addRiddenAimBreakableBlock(blocks, blockPosition);
                    }
                }
                return blocks;
            }
        }
        //---not aiming, use front zone hitboxes---
        else {
            List<AnimatedBoundingBox> frontZones = this.animData.getAnimatedBoundingBoxesByTag().get("frontZone");
            if (frontZones == null) return blocks;

            for (AnimatedBoundingBox frontZone : frontZones) {
                AxisAlignedBB frontBounds = this.animData.getWorldSpaceAABB(frontZone.getName());
                if (frontBounds == null) continue;

                BlockPos minimum = new BlockPos(Math.floor(frontBounds.minX), Math.floor(frontBounds.minY), Math.floor(frontBounds.minZ));
                BlockPos maximum = new BlockPos(
                        Math.ceil(frontBounds.maxX) - 1D,
                        Math.ceil(frontBounds.maxY) - 1D,
                        Math.ceil(frontBounds.maxZ) - 1D
                );
                for (BlockPos blockPos : BlockPos.getAllInBoxMutable(minimum, maximum)) {
                    BlockPos immutablePos = blockPos.toImmutable();
                    IBlockState blockState = this.world.getBlockState(immutablePos);
                    AxisAlignedBB collisionBounds = blockState.getCollisionBoundingBox(this.world, immutablePos);
                    if (collisionBounds == null) continue;

                    AxisAlignedBB worldCollisionBounds = collisionBounds.offset(immutablePos);
                    BlockBreakPlanEntry planEntry = this.getBlockBreakPlan(immutablePos);
                    boolean riderSprintBreaking = this.isBeingRidden() && this.isSprinting();
                    if (planEntry == null && !riderSprintBreaking && !riderMoveBreaking) continue;

                    boolean ordinaryJumpable = planEntry != null
                            && this.getCreaturePathNavigate().isStandardJumpable(planEntry, worldCollisionBounds);
                    if ((riderSprintBreaking || riderMoveBreaking || !ordinaryJumpable)
                            && worldCollisionBounds.intersects(frontBounds)
                            && this.canBreakBlock(immutablePos)) {
                        blocks.add(immutablePos);
                    }
                }
            }
            return blocks;
        }
    }

    private int getCenteredHoleMinimum(double center, int size) {
        return (int) Math.round(center - size * 0.5D);
    }

    private void addRiddenAimBreakableBlock(@NotNull Set<BlockPos> blocks, @NotNull BlockPos blockPosition) {
        if (this.canBreakBlock(blockPosition)) blocks.add(blockPosition);
    }

    //---block break methods for ai use---
    public int getBlockBreakEffectAttemptCount() {
        return this.blockBreakEffectAttemptCount;
    }

    public void recordBlockBreakEffectAttempt() {
        this.blockBreakEffectAttemptCount++;
    }

    public int getMoveFinishCount() {
        return this.moveFinishCount;
    }

    public void snapshotBlockBreakPlan() {
        this.activeBlockBreakPlan.clear();
        this.activeBlockBreakPlan.putAll(this.getCreaturePathNavigate().copyPlannedBlockBreaks());
    }

    @Nullable
    private BlockBreakPlanEntry getBlockBreakPlan(@NotNull BlockPos blockPos) {
        return !this.activeBlockBreakPlan.isEmpty() ? this.activeBlockBreakPlan.get(blockPos) : this.getCreaturePathNavigate().getPlannedBlockBreak(blockPos);
    }

    public boolean hasBreakableBlocksInFront() {
        return !this.getBreakableBlocksInFront().isEmpty();
    }

    public boolean hasBlockBreakZone() {
        List<AnimatedBoundingBox> frontZones = this.animData.getAnimatedBoundingBoxesByTag().get("frontZone");
        return frontZones != null && !frontZones.isEmpty();
    }

    /**
     * breaks blocks in collision boxes with "frontZone" tag, meant for use while pathing
     */
    public void breakBlocksInFrontInPathing() {
        if (this.world.isRemote) return;

        CreatureNavigationBuilder navigation = this.getNavigationBuilder();
        if ((this.bodyTouchingLiquid() && (navigation == null || !navigation.getCanSwim()))
                || !ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
            return;
        }

        Set<BlockPos> breakableBlocks = this.getBreakableBlocksInFront();
        if (breakableBlocks.isEmpty()) return;

        for (BlockPos blockPos : breakableBlocks) {
            IBlockState blockState = this.world.getBlockState(blockPos);

            //hmmm
            if (this.getCreaturePathNavigate().isBlockBreakTemporarilyDenied(blockPos) || !this.canBreakBlock(blockPos)) continue;

            //event block lol
            if (!ForgeEventFactory.onEntityDestroyBlock(this, blockPos, blockState)) {
                this.getCreaturePathNavigate().markBlockBreakDenied(blockPos);
                continue;
            }

            this.world.destroyBlock(blockPos, true);
        }
        this.getCreaturePathNavigate().invalidateBlockBreakPathCache();
    }

    //-----move use management-----
    @Override
    @NotNull
    public String getCurrentMove() {
        return this.getCreatureMoves().getCurrentMove();
    }

    public void setCurrentMove(@NotNull String name) {
        if (this.isLeaping() && !name.isEmpty()) return;
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        creatureMoveStorage.setCurrentMove(name);
    }

    public void useMoveFromRider(@NotNull EntityPlayer rider, int moveIndex) {
        if (this.world.isRemote || this.getControllingPassenger() != rider || !this.riderCanControl() || this.isLeaping()) {
            return;
        }

        CreatureMoveStorage moveStorage = this.getCreatureMoves();
        List<ImmutablePair<String, CreatureMoveBuilder>> moves = moveStorage.getUsableMoves();
        if (moveIndex < 0 || moveIndex >= moves.size() || !moveStorage.getCurrentMove().isEmpty()) return;

        ImmutablePair<String, CreatureMoveBuilder> selectedMove = moves.get(moveIndex);
        String moveName = selectedMove.getKey();
        CreatureMoveBuilder moveBuilder = selectedMove.getValue();
        if (moveStorage.moveCurrentCooldown(moveName) > 0) return;

        float attackYaw = rider.rotationYaw;
        this.rotationYaw = attackYaw;
        this.prevRotationYaw = attackYaw;
        this.renderYawOffset = attackYaw;
        this.prevRenderYawOffset = attackYaw;
        this.rotationYawHead = attackYaw;
        this.prevRotationYawHead = attackYaw;
        this.riddenAreaHitEntities.clear();
        this.riddenAreaAttackActive = false;
        if (this.isRiddenAiming() && moveName.equals(this.riddenAimingMoveName) && moveBuilder.getRiddenCrosshairType() == RiddenCrosshairType.WIDE) {
            this.riddenMeleeAimReach = this.calculateRiddenMeleeAimReach(moveIndex);
            this.riddenAreaAttackActive = this.riddenMeleeAimReach > 0D;
        }
        else if (!this.isRiddenAiming() || !moveName.equals(this.riddenAimingMoveName)) {
            this.riddenAimingMoveName = "";
            this.riddenAimArea = null;
            this.riddenMeleeAimReach = 0D;
        }

        super.setAttackTarget(null);
        this.sprintHelper.endSprint();
        this.setCurrentMove(moveName);
        if (!moveStorage.getCurrentMove().equals(moveName) || !this.useStamina(moveBuilder.getStaminaCost())) {
            this.riddenAreaAttackActive = false;
            this.setCurrentMove("");
            super.setAttackTarget(null);
            return;
        }
        CreatureMoveChargeupBuilder chargeupBuilder = moveBuilder.getMoveChargeupBuilder();
        if (chargeupBuilder != null && chargeupBuilder.getChargeUpWhileUse()) {
            moveStorage.finishCurrentMoveChargeupPhase(this, ChargeupPhase.PREWINDUP);
        }
        if (moveBuilder.getOnMoveBeginEffect() != null) moveBuilder.getOnMoveBeginEffect().accept(this, null);
        this.dataManager.setDirty(CREATURE_MOVES);
    }

    public void releaseMoveFromRider(@NotNull EntityPlayer rider) {
        if (this.world.isRemote || this.getControllingPassenger() != rider) return;
        this.getCreatureMoves().requestCurrentMoveRelease();
        this.dataManager.setDirty(CREATURE_MOVES);
    }

    //-----ridden aiming management-----
    @Override
    @Nullable
    public Vec3d getRiddenAimPosition() {
        if (this.riddenAimTarget != null && this.isRiddenAiming() &&
                (this.getCurrentMove().isEmpty() || this.getCurrentMove().equals(this.riddenAimingMoveName))
        ) {
            return this.riddenAimTarget.position();
        }
        return null;
    }

    /**
     * Select move targets before applying damage. Shared by ridden strike effects and
     * RiftLibrary offense hitboxes so both paths enforce the same aim and hit tracking.
     * */
    public boolean attackEntityWithMove(@NotNull Entity entityIn) {
        if (this.isRelatedToEntity(entityIn)) return false;

        CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
        if (creatureMoveBuilder == null) return false;

        Entity hitEntity = entityIn instanceof MultiPartEntityPart hitboxPart ? (Entity) hitboxPart.parent : entityIn;
        if (this.getControllingPassenger() instanceof EntityPlayer
                && this.riddenAimArea != null && this.riddenAreaAttackActive
                && this.getCurrentMove().equals(this.riddenAimingMoveName)
        ) {
            if (hitEntity.getLowestRidingEntity() == this.getLowestRidingEntity()) return false;
            if (this.riddenAreaHitEntities.contains(hitEntity.getEntityId())) return false;
            Vec3d hitPosition = this.riddenAimArea.getHitPosition(entityIn.getEntityBoundingBox());
            Vec3d eyes = this.getPositionEyes(1f);

            if (hitPosition == null || hitPosition.squareDistanceTo(eyes) > this.riddenMeleeAimReach * this.riddenMeleeAimReach) {
                return false;
            }
            //both the camera and creature must have an unobstructed path to the selected surface.
            for (Vec3d origin : List.of(eyes, this.riddenAimArea.origin())) {
                RayTraceResult wall = this.world.rayTraceBlocks(
                        origin, hitPosition, false, true, false
                );
                if (wall != null && origin.squareDistanceTo(wall.hitVec) + 1E-6D < origin.squareDistanceTo(hitPosition)) return false;
            }
            this.riddenAreaHitEntities.add(hitEntity.getEntityId());
        }
        else if (this.isRiddenAiming()
                && creatureMoveBuilder.getRiddenAimingType() == RiddenAimingType.TARGETED_MELEE
                && this.getCurrentMove().equals(this.riddenAimingMoveName)
        ) {
            if (this.riddenAimTarget == null) return false;
            Entity aimedEntity = this.riddenAimTarget.getEntity(this.world);
            if (aimedEntity == null || hitEntity != aimedEntity || !this.riddenAreaHitEntities.add(hitEntity.getEntityId())) return false;
        }

        return this.attackEntityAsMob(entityIn);
    }

    public boolean isTargetedMeleeRiddenMove(int moveIndex) {
        List<ImmutablePair<String, CreatureMoveBuilder>> moves = this.getCreatureMoves().getUsableMoves();
        return moveIndex >= 0 && moveIndex < moves.size()
                && moves.get(moveIndex).getValue().getRiddenAimingType() == RiddenAimingType.TARGETED_MELEE;
    }

    public double calculateRiddenMeleeAimReach(int moveIndex) {
        List<ImmutablePair<String, CreatureMoveBuilder>> moves = this.getCreatureMoves().getUsableMoves();
        if (moveIndex < 0 || moveIndex >= moves.size()) return 0D;
        AbstractCreatureBuilder<?> configuration = this.creatureType;
        if (!this.getPhase().isEmpty()) configuration = this.creatureType.getPhaseBuilderMaps().get(this.getPhase());
        if (configuration == null) return 0D;
        AbstractCreatureBuilder.MoveDefinition definition = configuration.getMoveDefinitions().get(moves.get(moveIndex).getKey());
        if (definition == null) return 0D;

        //for attacks with anim detection
        Vec3d origin = new Vec3d(0D, this.getEyeHeight(), 0D);
        double reach = 0D;
        for (MoveRuleBuilder rule : definition.rules()) {
            for (DetectionRule detection : rule.getDetectionRules()) {
                if (!(detection instanceof AnimatedHitboxDetectionRule animated)) continue;
                AnimatedHitboxDetectionRule.AttackPreview preview = animated.createPreview(this, null);
                if (preview == null) continue;
                for (AxisAlignedBB bounds : preview.bounds(Vec3d.ZERO, 0f)) {
                    double x = Math.max(Math.abs(bounds.minX - origin.x), Math.abs(bounds.maxX - origin.x));
                    double y = Math.max(Math.abs(bounds.minY - origin.y), Math.abs(bounds.maxY - origin.y));
                    double z = Math.max(Math.abs(bounds.minZ - origin.z), Math.abs(bounds.maxZ - origin.z));
                    reach = Math.max(reach, Math.sqrt(x * x + y * y + z * z));
                }
            }
        }
        if (reach > 0D) return reach;

        //moves without animated detection retain their declared front-zone reach
        List<AnimatedBoundingBox> frontHitZones = this.animData.getAnimatedBoundingBoxesByTag().get("frontZone");
        if (frontHitZones == null) return 0D;
        origin = this.getPositionEyes(1f);
        for (AnimatedBoundingBox hitbox : frontHitZones) {
            AxisAlignedBB bounds = this.animData.getWorldSpaceAABB(hitbox.getName());
            if (bounds != null) reach = Math.max(reach, origin.distanceTo(bounds.getCenter()) + bounds.getAverageEdgeLength() * 0.5D);
        }
        return reach;
    }

    public boolean isRiddenAimLocked() {
        CreatureMoveBuilder move = this.getCreatureMoves().getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
        return move != null && move.getUseLookBasedFacing() && !this.canRotateWhileCharging() && (charge == null || !charge.getCanLookWhileUsing());
    }

    /**
     * Validates and applies server aim.
     * */
    public void setRiddenAimFromRider(
            @NotNull EntityPlayer rider, int moveIndex, boolean targetlessRanged,
            @NotNull RiddenAimTarget aimTarget, @Nullable RiddenAimArea aimArea
    ) {
        if (this.world.isRemote || this.getControllingPassenger() != rider) return;

        boolean areaAttackActive = this.riddenAreaAttackActive && !this.riddenAimingMoveName.isEmpty()
                && this.getCurrentMove().equals(this.riddenAimingMoveName);
        this.riddenAreaAttackActive = areaAttackActive;
        List<ImmutablePair<String, CreatureMoveBuilder>> moves = this.getCreatureMoves().getUsableMoves();
        if (moveIndex < 0 || moveIndex >= moves.size()) return;
        ImmutablePair<String, CreatureMoveBuilder> selectedMove = moves.get(moveIndex);
        RiddenAimingType aimingType = selectedMove.getValue().getRiddenAimingType();
        if (aimingType == RiddenAimingType.NONE) return;

        //keep the executing attack associated with its aim until it finishes.
        if (areaAttackActive && !selectedMove.getKey().equals(this.riddenAimingMoveName)) return;

        //capture melee reach when aiming starts or changes moves, retain it during an attack
        if (!areaAttackActive && (!this.isRiddenAiming() || !selectedMove.getKey().equals(this.riddenAimingMoveName))) {
            this.riddenMeleeAimReach = aimingType == RiddenAimingType.TARGETED_MELEE ? this.calculateRiddenMeleeAimReach(moveIndex) : 0D;
        }

        //remove target components unsupported by the aiming type and limit the endpoint distance
        Vec3d riderEyes = rider.getPositionEyes(1f);
        if (targetlessRanged && aimingType != RiddenAimingType.RANGED) return;
        if (targetlessRanged) {
            aimTarget = new RiddenAimTarget(-1, aimTarget.position(), null, null);
        }
        else if (aimingType != RiddenAimingType.TARGETED_MELEE) {
            aimTarget = new RiddenAimTarget(aimTarget.entityId(), aimTarget.position(), null, null);
        }
        if (aimTarget.position().squareDistanceTo(riderEyes) > 6400D) return;

        //validate the wide crosshair's origin, direction, proportions, and alignment with the endpoint
        if (aimArea != null) {
            if (selectedMove.getValue().getRiddenCrosshairType() != RiddenCrosshairType.WIDE
                    || aimArea.origin().squareDistanceTo(this.getPositionEyes(1f)) > 256D
                    || aimArea.forward().lengthSquared() <= 0.99D || aimArea.forward().lengthSquared() >= 1.01D
                    || aimArea.horizontalSlope() <= 0D || aimArea.horizontalSlope() > 1D
                    || aimArea.verticalSlope() <= 0D || aimArea.verticalSlope() > 1D
                    || Math.abs(aimArea.horizontalSlope() / aimArea.verticalSlope()
                    - RiddenAimArea.HORIZONTAL_SCREEN_RADIUS / RiddenAimArea.VERTICAL_SCREEN_RADIUS) >= 1E-6D
                    || aimArea.forward().dotProduct(aimTarget.position().subtract(aimArea.origin()).normalize()) <= 0.98D
            ) {
                return;
            }
        }
        //a locked pose retains its captured target, while an executing area's selection follows the camera
        if (this.isRiddenAimLocked()) {
            if (aimArea != null && areaAttackActive) this.riddenAimArea = aimArea;
            return;
        }

        //clip targeted aim to intervening blocks, targetless ranged aim keeps its direction endpoint
        Vec3d creatureAimOrigin = this.getPositionEyes(1f);
        RayTraceResult creatureBlockHit = null;
        if (!targetlessRanged) {
            creatureBlockHit = this.world.rayTraceBlocks(
                    creatureAimOrigin, aimTarget.position(), false, true, false
            );
        }
        if (creatureBlockHit != null
                && creatureAimOrigin.squareDistanceTo(creatureBlockHit.hitVec) + 1E-6D < creatureAimOrigin.squareDistanceTo(aimTarget.position())
        ) {
            if (aimingType == RiddenAimingType.TARGETED_MELEE) {
                aimTarget = new RiddenAimTarget(-1, creatureBlockHit.hitVec, creatureBlockHit.getBlockPos(), creatureBlockHit.sideHit);
            }
            else {
                aimTarget = new RiddenAimTarget(-1, creatureBlockHit.hitVec, null, null);
            }
        }

        //enforce melee reach, reject invalid entity targets, and give valid entities priority over blocks
        Entity aimedEntity = aimTarget.getEntity(this.world);
        double reachWithNetworkTolerance = this.riddenMeleeAimReach + 0.25D;
        boolean meleeAimOutOfRange = aimingType == RiddenAimingType.TARGETED_MELEE
                && aimTarget.position().squareDistanceTo(creatureAimOrigin) > reachWithNetworkTolerance * reachWithNetworkTolerance;
        if (meleeAimOutOfRange) {
            aimTarget = new RiddenAimTarget(-1, aimTarget.position(), null, null);
        }
        else if (aimedEntity == null || aimedEntity == this || aimedEntity == rider || aimedEntity.getDistanceSq(this) > 6400D) {
            aimTarget = new RiddenAimTarget(-1, aimTarget.position(), aimTarget.blockPosition(), aimTarget.blockFace());
        }
        else {
            aimTarget = new RiddenAimTarget(aimTarget.entityId(), aimTarget.position(), null, null);
        }

        //keep a block face only when its block is in range and contains the aim endpoint
        if (aimTarget.blockPosition() == null) {
            aimTarget = new RiddenAimTarget(aimTarget.entityId(), aimTarget.position(), null, null);
        }
        else {
            IBlockState targetBlockState = this.world.getBlockState(aimTarget.blockPosition());
            AxisAlignedBB targetBlockBounds = targetBlockState.getCollisionBoundingBox(this.world, aimTarget.blockPosition());
            boolean targetBlockOutOfRange = aimTarget.blockPosition().distanceSq(riderEyes.x, riderEyes.y, riderEyes.z) > 6400D;
            boolean aimMissesTargetBlock = targetBlockBounds == null
                    || !targetBlockBounds.offset(aimTarget.blockPosition()).grow(0.01D).contains(aimTarget.position());
            if (targetBlockOutOfRange || aimMissesTargetBlock) {
                aimTarget = new RiddenAimTarget(aimTarget.entityId(), aimTarget.position(), null, null);
            }
        }

        //apply the validated aim
        this.riddenAimingMoveName = selectedMove.getKey();
        this.riddenAimTarget = aimTarget;
        this.riddenAimArea = aimArea;
        this.setRiddenAimActive(true);
    }

    /**
     * Applies local aim immediately.
     * */
    public void setRiddenAimingFromClient(@NotNull RiddenAimTarget aimTarget, int moveIndex, boolean leapAim) {
        if (!this.world.isRemote || this.isRiddenAimLocked()) return;
        //leaping
        if (leapAim) {
            if (!(this.getControllingPassenger() instanceof EntityPlayer rider) || !this.getCreatureMoveHelper().getLeapHelper().canChargeRiddenLeap(rider)) return;
            this.riddenAimTarget = aimTarget;
            this.riddenAimingMoveName = "";
            this.setRiddenAimActive(true);
        }
        //using a move
        else {
            List<ImmutablePair<String, CreatureMoveBuilder>> moves = this.getCreatureMoves().getUsableMoves();
            if (moveIndex < 0 || moveIndex >= moves.size()) return;
            this.riddenAimTarget = aimTarget;
            this.riddenAimingMoveName = moves.get(moveIndex).getKey();
            this.setRiddenAimActive(true);
        }
    }

    /**
     * Ends aiming logic, works on both sides
     * */
    public void stopRiddenAim() {
        this.setRiddenAimActive(false);
        this.riddenAimTarget = null;
        boolean matchingMove = !this.riddenAimingMoveName.isEmpty() && this.getCurrentMove().equals(this.riddenAimingMoveName);
        this.riddenAreaAttackActive = this.riddenAreaAttackActive && matchingMove;
        //Keep an executing area's selection and reach when its rider releases aim.
        //The client retains the move name until its matching animation finishes.
        if (!matchingMove || !this.world.isRemote && !this.riddenAreaAttackActive) {
            this.riddenAimingMoveName = "";
            this.riddenAimArea = null;
            this.riddenMeleeAimReach = 0D;
        }
    }

    private boolean isRiddenAiming() {
        return this.dataManager.get(RIDDEN_AIMING);
    }

    private void setRiddenAimActive(boolean active) {
        this.dataManager.set(RIDDEN_AIMING, active);
    }

    //-----stamina use management-----
    //getting stamina cost excempts special modifiers: only base stamina stat matters
    private float getStaminaCost(float nominalMaximumFraction) {
        if (nominalMaximumFraction <= 0f) return 0f;
        return nominalMaximumFraction * 100f;
    }

    public boolean useStamina(float nominalMaximumFraction) {
        if (nominalMaximumFraction < 0f) return false;
        float staminaCost = this.getStaminaCost(nominalMaximumFraction);
        if (staminaCost <= 0f) return true;
        if (this.getStamina() <= 0f) return false;

        this.setStamina(this.getStamina() - staminaCost);
        this.inactiveStaminaRegenTicks = 0;
        return true;
    }

    public boolean isStaggered() {
        return this.dataManager.get(STAGGERED);
    }

    private void setStaggered(boolean value) {
        this.dataManager.set(STAGGERED, value);
    }

    //-----navigation management-----
    @Nullable
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

    //just indicate that it is indeed leaping, meant to sync w client of course
    public void setLeaping(boolean value) {
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

    @Nullable
    public BlockPos findNearestLandBlock(double detectRange, boolean onlyInFront) {
        double bodyYPos;
        if (this instanceof RiftCreatureHitboxed hitboxedCreature) {
            bodyYPos = hitboxedCreature.getMultiHitboxList().getCollisionHitboxByName("body").posY;
        }
        else bodyYPos = this.posY;

        int horizontalDetectBound = (int) Math.ceil(detectRange / 2D);
        BlockPos closest = null;
        double closestDistanceSq = Double.MAX_VALUE;
        BlockPos.MutableBlockPos posToTest = new BlockPos.MutableBlockPos();
        for (int x = -horizontalDetectBound; x <= horizontalDetectBound; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -horizontalDetectBound; z <= horizontalDetectBound; z++) {
                    posToTest.setPos(this.posX + x, bodyYPos + y, this.posZ + z);
                    if (this.world.getBlockState(posToTest).getMaterial() != Material.AIR
                            || !this.world.getBlockState(posToTest.down()).getMaterial().isSolid()) {
                        continue;
                    }

                    if (onlyInFront) {
                        double displacementX = posToTest.getX() - this.posX;
                        double displacementZ = posToTest.getZ() - this.posZ;
                        double forwardX = Math.sin(-Math.toRadians(this.rotationYaw));
                        double forwardZ = Math.cos(Math.toRadians(this.rotationYaw));
                        if (displacementX * forwardX + displacementZ * forwardZ <= 0D) continue;
                    }

                    double distanceSq = posToTest.distanceSq(this.posX, this.posY, this.posZ);
                    if (distanceSq >= closestDistanceSq) continue;

                    AxisAlignedBB shoreBounds = this.getEntityBoundingBox().offset(
                            posToTest.getX() - this.posX,
                            posToTest.getY() - this.getEntityBoundingBox().minY,
                            posToTest.getZ() - this.posZ
                    );
                    if (this.world.collidesWithAnyBlock(shoreBounds)) continue;

                    closest = posToTest.toImmutable();
                    closestDistanceSq = distanceSq;
                }
            }
        }
        return closest;
    }

    public boolean canRotateWhileCharging() {
        CreatureMoveStorage moves = this.getCreatureMoves();
        CreatureMoveBuilder move = moves.getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
        return charge != null && charge.getCanRotateWhileCharging()
                && (moves.currentMoveMatches(this.getCurrentMove(), ChargeupPhase.PREWINDUP)
                || moves.currentMoveMatches(this.getCurrentMove(), ChargeupPhase.WINDUP));
    }

    public boolean isMovementLockedByMove() {
        CreatureMoveBuilder move = this.getCreatureMoves().getMoveBuilderCurrentMove();
        return move != null && move.getPreventMovement();
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (this.isBoxSitting()) {
            x = 0D;
            z = 0D;
            this.motionX = 0D;
            this.motionZ = 0D;
        }
        if (this.isMovementLockedByMove()) {
            this.motionX = 0D;
            this.motionY = 0D;
            this.motionZ = 0D;
            return;
        }
        super.move(type, x, y, z);
        this.enforceCreatureBoxRange();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isBoxSitting()) {
            this.getNavigator().clearPath();
            this.getCreatureMoveHelper().stopMovement();
            this.motionX = 0D;
            this.motionZ = 0D;
            super.travel(0f, 0f, 0f);
            return;
        }
        if (this.isMovementLockedByMove()) {
            this.getNavigator().clearPath();
            this.getCreatureMoveHelper().stopMovement();
            this.motionX = 0D;
            this.motionY = 0D;
            this.motionZ = 0D;
            return;
        }
        boolean riddenMoveActive = this.isBeingRidden() && !this.getCurrentMove().isEmpty();

        //make sure staggering and ridden moves cannot retain walking input
        if (this.isStaggered() || riddenMoveActive) {
            strafe = 0;
            forward = 0;
            this.setAIMoveSpeed(0f);
            this.setMoveForward(0f);
            this.setMoveStrafing(0f);
        }

        boolean useStandardTravel = true;
        this.sprintHelper.updateSprint(RiftCreatureSprintHelper.UpdatePhase.TRAVEL);

        //move when riding and saddled
        if (this.isBeingRidden() && this.riderCanControl() && this.getControllingPassenger() instanceof EntityPlayer playerController) {
            strafe = playerController.moveStrafing * 0.5f;
            forward = playerController.moveForward;
            //slow down when going backwards
            if (forward < 0) forward *= 0.5f;
            this.stepHeight = 1f;
            this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1f;
            float moveSpeed = (float) Math.max(0, this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            if (this.isSprinting()) moveSpeed *= 1.75f;
            this.setAIMoveSpeed(moveSpeed);

        }
        else if (!this.isBeingRidden()) {
            this.stepHeight = 0.5f;
            this.jumpMovementFactor = 0.02f;
        }

        if (this.getCreatureMoveHelper().getLeapHelper().updateLeap(RiftCreatureLeapHelper.UpdatePhase.TRAVEL)) {
            useStandardTravel = false;
        }

        boolean riddenGroundMovement = this.isBeingRidden() && this.riderCanControl() && (forward != 0f || strafe != 0f);
        if (useStandardTravel) {
            if (riddenGroundMovement && forward > 0f && this.bodyTouchingLiquid()) {
                BlockPos shorePos = this.findNearestLandBlock(this.width * 2D, true);
                if (shorePos != null) {
                    double displacementX = shorePos.getX() - this.posX;
                    double displacementZ = shorePos.getZ() - this.posZ;
                    double shoreTransferDistanceSq = this.width * this.width;
                    if (displacementX * displacementX + displacementZ * displacementZ <= shoreTransferDistanceSq) {
                        this.setPosition(shorePos.getX(), shorePos.getY(), shorePos.getZ());
                    }
                }
            }

            //float above water
            if (this.bodyTouchingLiquid()) this.motionY += 0.1D;

            super.travel(strafe, vertical, forward);
        }
    }

    public boolean riderCanControl() {
        return this.getControllingPassenger() != null && this.isSaddled() && !this.getIsSleeping() && !this.isStaggered()
                && this.getCreatureMoves().getCurrentMove().isEmpty();
    }

    @Nullable
    public BlockPos getCreatureBoxHome() {
        return this.creatureBoxHome;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isGlowing() {
        return super.isGlowing() || this.world.isRemote && CreatureBoxRangeRenderer.isHighlighted(this);
    }

    public void setCreatureBoxHome(@Nullable BlockPos position) {
        this.creatureBoxHome = position == null ? null : position.toImmutable();
        this.creatureBoxBounds = position == null ? null : CreatureBoxTileEntity.getRange(position);
    }

    @Nullable
    public AxisAlignedBB getDeploymentBounds() {
        return this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE ? this.creatureBoxBounds : null;
    }

    public boolean isWithinDeploymentRange(double x, double y, double z) {
        AxisAlignedBB bounds = this.getDeploymentBounds();
        if (bounds == null) return true;
        double radius = this.width * 0.5D;
        return x - radius >= bounds.minX && x + radius <= bounds.maxX
                && y >= bounds.minY && y + this.height <= bounds.maxY
                && z - radius >= bounds.minZ && z + radius <= bounds.maxZ;
    }

    public void enforceCreatureBoxRange() {
        AxisAlignedBB bounds = this.getDeploymentBounds();
        if (this.world.isRemote || bounds == null || this.creatureBoxHome == null) return;
        if (this.isBeingRidden()) this.removePassengers();
        if (this.isWithinDeploymentRange(this.posX, this.posY, this.posZ)) return;
        this.getNavigator().clearPath();
        this.getCreatureMoveHelper().getLeapHelper().cancelLeap();
        this.getCreatureMoveHelper().stopMovement();
        this.motionX = 0D;
        this.motionY = 0D;
        this.motionZ = 0D;
        this.fallDistance = 0f;
        Vec3d destination = CreatureBoxTileEntity.findDeploymentPosition(this, this.creatureBoxHome);
        if (destination == null) {
            double radius = this.width * 0.5D + 0.01D;
            destination = new Vec3d(Math.clamp(this.posX, bounds.minX + radius, bounds.maxX - radius),
                    Math.clamp(this.posY, Math.max(0D, bounds.minY), Math.min(256D, bounds.maxY) - this.height),
                    Math.clamp(this.posZ, bounds.minZ + radius, bounds.maxZ - radius));
        }
        this.setPositionAndUpdate(destination.x, destination.y, destination.z);
    }

    //nonhitboxed creatures use their main body
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
        this.dataManager.set(LEVEL, Math.clamp(value, 1, RiftGeneralConfig.creatures.maxLevel));
    }

    @Override
    public int getXP() {
        return this.dataManager.get(XP);
    }

    @Override
    public void setXP(int value) {
        this.dataManager.set(XP, this.getLevel() >= RiftGeneralConfig.creatures.maxLevel ? 0 : Math.clamp(value, 0, this.getMaxXP()));
    }

    public void addXP(int value) {
        if (this.world.isRemote || !this.isTamed() || value <= 0 || this.getLevel() >= RiftGeneralConfig.creatures.maxLevel) return;

        //add xp and see if it results in levelup
        long accumulatedXP = (long) this.getXP() + value;
        boolean leveledUp = false;
        EntityLivingBase owner = this.getOwner();
        while (this.getLevel() < RiftGeneralConfig.creatures.maxLevel && accumulatedXP >= this.getMaxXP()) {
            accumulatedXP -= this.getMaxXP();
            this.setLevel(this.getLevel() + 1);
            leveledUp = true;
            if (owner instanceof EntityPlayer player) {
                player.sendStatusMessage(new TextComponentTranslation("reminder.level_up", this.getDisplayName(), this.getLevel()), false);
            }
        }
        this.setXP((int) accumulatedXP);

        //update stats, scale old health and stamina to new, and update party after levelup
        if (leveledUp) {
            float healthFraction = this.getHealth() / this.getMaxHealth();
            float staminaFraction = this.getStamina() / this.getMaxStamina();
            this.getCreatureStats().applyStatsToCreature(this);
            this.setHealth(this.getMaxHealth() * healthFraction);
            this.setStamina(this.getMaxStamina() * staminaFraction);

            if (owner instanceof EntityPlayer player) {
                PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
                if (playerParty != null) playerParty.updatePartyMember(this);
            }
        }
    }

    @Override
    public int getExperiencePoints(EntityPlayer player) {
        double averageStats = this.creatureType.getStats().values().stream().mapToDouble(value -> value).average().orElse(0D);
        return Math.max(1, (int) Math.round(averageStats * Math.max(1, this.getLevel())));
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
        this.dataManager.set(STAMINA_CURRENT, Math.clamp(value, 0f, this.getMaxStamina()));
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
    public void setCreatureInventory(RiftLibInventoryHandler value) {
        if (value != null) this.creatureInventory.deserializeNBT(value.serializeNBT());
    }

    @Override
    @NotNull
    public RiftLibInventoryHandler getCreatureGear() {
        return this.creatureGear;
    }

    @Override
    public void setCreatureGear(RiftLibInventoryHandler value) {
        if (value != null) this.creatureGear.deserializeNBT(value.serializeNBT());
        this.setSaddled(this.canBeRidden() && this.creatureGear.getStackInSlot(0).getItem() == Items.SADDLE);
    }

    public boolean isSaddled() {
        return this.dataManager.get(CreatureGearInventoryHandler.SADDLED);
    }

    public void setSaddled(boolean value) {
        this.dataManager.set(CreatureGearInventoryHandler.SADDLED, value);
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

    @Override
    @NonNull
    public CreatureAcquisitionInfo getAcquisitionInfo() {
        return this.acquisitionInfo;
    }

    @Override
    public void setAcquisitionInfo(@NotNull CreatureAcquisitionInfo value) {
        this.acquisitionInfo = value;
    }

    @Override
    public RiftCreatureEnums.CreatureDeployment getDeploymentType() {
        byte deploymentTypeOrdinal = this.dataManager.get(DEPLOYMENT_TYPE);
        if (deploymentTypeOrdinal < 0 || deploymentTypeOrdinal >= RiftCreatureEnums.CreatureDeployment.values().length) return null;
        return RiftCreatureEnums.CreatureDeployment.values()[deploymentTypeOrdinal];
    }

    @Override
    public void setDeploymentType(RiftCreatureEnums.CreatureDeployment value) {
        RiftCreatureEnums.CreatureDeployment previous = this.getDeploymentType();
        this.dataManager.set(DEPLOYMENT_TYPE, value != null ? (byte) value.ordinal() : (byte) -1);
        if (value != RiftCreatureEnums.CreatureDeployment.BASE || previous != RiftCreatureEnums.CreatureDeployment.BASE) {
            this.setBaseDeploymentBehavior(RiftCreatureEnums.BaseDeploymentBehavior.WANDER);
        }
    }

    @Override
    @NotNull
    public RiftCreatureEnums.BaseDeploymentBehavior getBaseDeploymentBehavior() {
        if (this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE) return RiftCreatureEnums.BaseDeploymentBehavior.WANDER;
        byte ordinal = this.dataManager.get(BASE_DEPLOYMENT_BEHAVIOR);
        RiftCreatureEnums.BaseDeploymentBehavior[] behaviors = RiftCreatureEnums.BaseDeploymentBehavior.values();
        if (ordinal < 0 || ordinal >= behaviors.length) return RiftCreatureEnums.BaseDeploymentBehavior.WANDER;
        return behaviors[ordinal];
    }

    @Override
    public void setBaseDeploymentBehavior(RiftCreatureEnums.@Nullable BaseDeploymentBehavior value) {
        if (value == null || this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE) {
            value = RiftCreatureEnums.BaseDeploymentBehavior.WANDER;
        }
        byte previous = this.dataManager.get(BASE_DEPLOYMENT_BEHAVIOR);
        this.dataManager.set(BASE_DEPLOYMENT_BEHAVIOR, (byte) value.ordinal());
        if (previous != value.ordinal()) {
            this.baseFollowOutOfRangeTicks = 0;
            if (!this.world.isRemote) {
                this.getNavigator().clearPath();
                if (this.moveHelper instanceof RiftCreatureMoveHelperBase helper) helper.stopMovement();
                this.motionX = 0D;
                this.motionZ = 0D;
            }
        }
    }

    public boolean isBoxSitting() {
        return this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.BASE
                && this.getBaseDeploymentBehavior() == RiftCreatureEnums.BaseDeploymentBehavior.SIT;
    }

    @Override
    public int getInactiveStaminaRegen() {
        return this.inactiveStaminaRegenTicks;
    }

    @Override
    public void setInactiveStaminaRegen(int value) {
        this.inactiveStaminaRegenTicks = value;
    }

    @Override
    public void regenerateStaminaInactive() {
        if (this.getStamina() >= this.getMaxStamina()) return;

        if (this.inactiveStaminaRegenTicks++ >= MAX_INACTIVITY_STAMINA_REGEN) {
            this.setStamina(this.getMaxStamina());
            this.inactiveStaminaRegenTicks = 0;
        }
    }

    @Override
    public boolean getWasRecentlyHit() {
        return this.recentlyHitTimer > 0;
    }

    //-----nbt parsing related stuff-----
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.creatureBoxHome != null) compound.setLong(CreatureBoxTileEntity.HOME_TAG, this.creatureBoxHome.toLong());

        //remembered player targets
        NBTTagList rememberedPlayerTargetsNBT = new NBTTagList();
        for (UUID rememberedUUID : this.rememberedPlayerTargetUUIDs) {
            NBTTagCompound nbtToAdd = new NBTTagCompound();
            nbtToAdd.setUniqueId("UUID", rememberedUUID);
            rememberedPlayerTargetsNBT.appendTag(nbtToAdd);
        }
        compound.setTag("RememberedPlayerTargets", rememberedPlayerTargetsNBT);

        //sleep state
        compound.setInteger("Tiredness", this.tiredness);
        compound.setBoolean("Sleeping", this.getIsSleeping());
        compound.setByte("SleepCause", this.sleepCause == null ? (byte) -1 : (byte) this.sleepCause.ordinal());

        //domestication state
        compound.setFloat("TamingProgress", this.getTamingProgress());

        //other nbt tags
        this.writeCreatureNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setCreatureBoxHome(compound.hasKey(CreatureBoxTileEntity.HOME_TAG)
                ? BlockPos.fromLong(compound.getLong(CreatureBoxTileEntity.HOME_TAG)) : null);

        //remembered player targets
        NBTTagList rememberedPlayerTargetsNBT = compound.getTagList("RememberedPlayerTargets", 10);
        for (int i = 0; i < rememberedPlayerTargetsNBT.tagCount(); i++) {
            NBTTagCompound nbtFromList = rememberedPlayerTargetsNBT.getCompoundTagAt(i);
            UUID playerUUID = nbtFromList.getUniqueId("UUID");
            if (!this.rememberedPlayerTargetUUIDs.contains(playerUUID)) this.rememberedPlayerTargetUUIDs.add(playerUUID);
        }

        //creature types
        if (compound.hasKey("CreatureType")) {
            RiftCreatureBuilder builder = resolveCreatureBuilder(compound.getString("CreatureType"));
            this.changeCreatureType(builder);
        }

        //other nbt tags
        this.readCreatureNBT(compound);

        //sleep state is restored last so entering sleep can cancel a saved move
        this.tiredness = Math.max(0, compound.getInteger("Tiredness"));
        int savedSleepCauseOrdinal = compound.hasKey("SleepCause", 1) ? compound.getByte("SleepCause") : -1;
        this.sleepCause = savedSleepCauseOrdinal >= 0 ? RiftCreatureEnums.SleepCause.values()[savedSleepCauseOrdinal] : null;

        boolean sleeping = compound.getBoolean("Sleeping") && this.sleepCause != null;
        if (!sleeping) this.sleepCause = null;
        this.setIsSleeping(sleeping);

        //domestication state
        this.setTamingProgress(compound.getFloat("TamingProgress"));
        int tameTargetingOrdinal = compound.hasKey("TameTargeting") ? compound.getByte("TameTargeting") : 0;
        this.setTameTargeting(RiftCreatureEnums.TameTargeting.values()[tameTargetingOrdinal]);
        if (this.isTamed()) {
            this.leaveHerd();
            this.herdHelper = null;
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.creatureType.getName());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.changeCreatureType(resolveCreatureBuilder(ByteBufUtils.readUTF8String(additionalData)));
    }

    //-----dynamic ride pos related methods-----
    @NotNull
    public RiftCreature getDynamicRideUser() {
        return this;
    }

    @Override
    @NotNull
    public DynamicRidePosList ridePosList() {
        return this.dynamicRidePosList;
    }

    @Override
    @NotNull
    public List<String> locatorRidePositions() {
        return List.of();
    }

    @Override
    @Nullable
    public String locatorControllerPosition() {
        if (this.creatureType.getDomestication() == null) return null;
        return this.creatureType.getDomestication().getControllerRideLocator();
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer player && this.isTamed() && this.isOwner(player)) return passenger;
        }
        return null;
    }

    @Override
    public boolean canBeSteered() {
        return this.isSaddled() && this.getControllingPassenger() != null;
    }

    @Override
    public boolean canRotateMounted() {
        CreatureMoveBuilder move = this.getCreatureMoves().getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
        if (move != null && move.getUseLookBasedFacing() && !this.canRotateWhileCharging()) return false;
        if (charge != null && charge.getCanRotateWhileCharging() && !this.canRotateWhileCharging()) return false;
        return !this.getCreatureMoveHelper().getLeapHelper().isRiddenLeap()
                && !this.isSprinting() && !this.isStaggered();
    }

    @Override
    public float getRenderYaw(float partialTicks) {
        CreatureMoveBuilder move = CreatureLookUtil.getRiddenAimMove(this, this.getCurrentMove());
        CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
        if (charge != null && charge.getCanLookWhileUsing() && this.canRotateMounted()
                && this.getControllingPassenger() instanceof EntityPlayer rider
        ) {
            float turn = MathHelper.wrapDegrees(rider.rotationYaw - rider.prevRotationYaw);
            return (float) MathUtil.slopeResult(
                    partialTicks, true, 0D, 1D,
                    rider.prevRotationYaw, rider.prevRotationYaw + turn
            );
        }
        return IDynamicRideUser.super.getRenderYaw(partialTicks);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        IDynamicRideUser.super.updatePassenger(passenger);
        if (passenger == this.getControllingPassenger() && this.canRotateMounted() && this.isRiddenAiming()) {
            float[] bounds = CreatureLookUtil.getRiddenAimPitchBounds(this, this.riddenAimingMoveName);
            this.rotationPitch = Math.clamp(passenger.rotationPitch, bounds[0], bounds[1]);
        }
    }

    @Override
    public void applyRidePosition(Entity passenger, Vec3d ridePos) {
        if (passenger == this.getControllingPassenger()) super.updatePassenger(passenger);
        else IDynamicRideUser.super.applyRidePosition(passenger, ridePos);
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return rider != this.getControllingPassenger() && super.shouldDismountInWater(rider);
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getDeploymentType() != RiftCreatureEnums.CreatureDeployment.BASE
                && this.canBeRidden() && this.isSaddled() && passenger instanceof EntityPlayer player
                && this.isTamed() && this.isOwner(player) && super.canFitPassenger(passenger);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (!this.world.isRemote && passenger == this.getControllingPassenger()) {
            this.setAttackTarget(null);
            this.getNavigator().clearPath();
            this.getCreatureMoveHelper().stopMovement();
            this.setUseBlockBreak(false);
            this.getCreatureMoves().resetCurrentMove(this);
            this.setRiddenAimActive(false);
            this.riddenAimingMoveName = "";
            this.riddenAimTarget = null;
            this.riddenMeleeAimReach = 0D;
            this.riddenAreaAttackActive = false;
            this.riddenAimArea = null;
            this.riddenAreaHitEntities.clear();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        boolean removedController = passenger == this.getControllingPassenger();
        super.removePassenger(passenger);
        if (removedController) {
            this.getCreatureMoveHelper().getLeapHelper().cancelLeap();
            this.setRiddenAimActive(false);
            this.riddenAimingMoveName = "";
            this.riddenAimTarget = null;
            this.riddenMeleeAimReach = 0D;
            this.riddenAreaAttackActive = false;
            this.riddenAimArea = null;
            this.riddenAreaHitEntities.clear();
        }
        if (!this.world.isRemote && removedController) {
            this.getCreatureMoves().resetCurrentMove(this);
            this.setAttackTarget(null);
            this.setUseBlockBreak(false);
            this.sprintHelper.endSprint();
            this.dataManager.setDirty(CREATURE_MOVES);
        }
    }

    //-----ray related methods-----
    @Override
    @NotNull
    public RiftCreature getRayCreator() {
        return this;
    }

    @Override
    @NotNull
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
    @NotNull
    public AnimationDataEntityLiving getAnimationData() {
        return this.animData;
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataEntityLiving animationData) {
        //-----set scale-----
        animationData.setScale(holder -> this.scale());

        //-----create animation controllers-----
        //---for normal stuff---
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntityLiving>(this, "movement", "default",
                new AnimationControllerState<AnimationDataEntityLiving>("default")
                        .addStateTransition("moving", animData -> {
                            return !this.getIsSleeping() && this.getCurrentMove().isEmpty() && !this.isLeaping() && animData.isMoving();
                        })
                        .addStateTransition("falling", data -> {
                            return !this.getIsSleeping() && this.creatureType.getFallSlowly() && !this.onGround;
                        }),
                new AnimationControllerState<AnimationDataEntityLiving>("moving", 0.1)
                        .addAnimation("animation."+this.creatureType.getName()+".walk")
                        .addStateTransition("default", animData -> {
                            return this.getIsSleeping() || !this.getCurrentMove().isEmpty() || this.isLeaping() || !animData.isMoving();
                        })
                        .addStateTransition("falling", data -> {
                            return !this.getIsSleeping() && this.creatureType.getFallSlowly() && !this.onGround;
                        }),
                new AnimationControllerState<AnimationDataEntityLiving>("falling")
                        .addAnimation("animation." + this.creatureType.getName() + ".fall")
                        .addStateTransition("default", data -> this.onGround)
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntityLiving>(this, "sprintPosing", "default",
                new AnimationControllerState<AnimationDataEntityLiving>("default", 0.2)
                        .addStateTransition("sprint", animData -> !this.getIsSleeping() && this.getCurrentMove().isEmpty()
                                && !this.isLeaping() && this.isSprinting()),
                new AnimationControllerState<AnimationDataEntityLiving>("sprint", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".sprint_pose")
                        .addStateTransition("default", animData -> this.getIsSleeping() || !this.getCurrentMove().isEmpty()
                                || this.isLeaping() || !this.isSprinting())
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntityLiving>(this, "sleeping", "default",
                new AnimationControllerState<AnimationDataEntityLiving>("default", 0.2)
                        .addStateTransition("sleeping", animData -> this.getIsSleeping()),
                new AnimationControllerState<AnimationDataEntityLiving>("sleeping", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".sleep")
                        .addStateTransition("default", animData -> !this.getIsSleeping())
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntityLiving>(this, "staggered", "default",
                new AnimationControllerState<AnimationDataEntityLiving>("default", 0.2)
                        .addStateTransition("staggered", animData -> this.isStaggered()),
                new AnimationControllerState<AnimationDataEntityLiving>("staggered", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".staggered")
                        .addStateTransition("default", animData -> !this.isStaggered())
        ));
        if (this.getNavigationBuilder() != null && this.getNavigationBuilder().getCanLeap()) {
            animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntityLiving>(this, "leaping", "default",
                    new AnimationControllerState<AnimationDataEntityLiving>("default", 0.1)
                            .addStateTransition("leaping", animData -> !this.getIsSleeping() && this.isLeaping()),
                    new AnimationControllerState<AnimationDataEntityLiving>("leaping", 0.1)
                            .addAnimation("animation."+this.creatureType.getName()+".leap")
                            .addStateTransition("default", animData -> this.getIsSleeping() || !this.isLeaping())
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
        animationData.addAnimationMessageEffect("riddenTargetedMeleeHitEffect", new AnimatableRunValue(() -> {
            if (this.riddenAimArea != null && this.riddenAreaAttackActive
                    && this.getCurrentMove().equals(this.riddenAimingMoveName)) {
                AxisAlignedBB areaBounds = new AxisAlignedBB(this.getPositionEyes(1f), this.getPositionEyes(1f))
                        .grow(this.riddenMeleeAimReach);
                for (Entity entity : this.world.getEntitiesInAABBexcluding(this, areaBounds,
                        entity -> entity != null && entity.isEntityAlive() && entity.canBeCollidedWith())) {
                    this.attackEntityWithMove(entity);
                }
                return;
            }
            CreatureMoveBuilder creatureMoveBuilder = this.getCreatureMoves().getMoveBuilderCurrentMove();
            if (!this.isRiddenAiming() || this.riddenAimTarget == null || creatureMoveBuilder == null
                    || creatureMoveBuilder.getUseLookBasedFacing()
                    || creatureMoveBuilder.getRiddenAimingType() != RiddenAimingType.TARGETED_MELEE
                    || !this.getCurrentMove().equals(this.riddenAimingMoveName)
            ) {
                return;
            }

            //for attacking entity
            Entity aimedEntity = this.riddenAimTarget.getEntity(this.world);
            if (aimedEntity != null) this.attackEntityWithMove(aimedEntity);
            //for block breaking
            else if (this.getUseBlockBreak() && this.riddenAimTarget.blockPosition() != null) {
                CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
                if (creatureMoveStorage.canRunCurrentMoveHitEffect()) {
                    creatureMoveStorage.runCurrentMoveHitEffect(this);
                }
            }
        }, Side.SERVER));
        animationData.addAnimationMessageEffect("moveBlockBreakEffect", new AnimatableRunValue(() -> {
            CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
            if (this.getUseBlockBreak() && creatureMoveStorage.canRunCurrentMoveHitEffect()) {
                creatureMoveStorage.runCurrentMoveHitEffect(this);
            }
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

    @SuppressWarnings("unchecked")
    private void initAnimControllerForPhase(AnimationDataEntityLiving animationData, @NotNull String phase) {
        List<AnimationControllerState<AnimationDataEntityLiving>> creatureMovesStates = new ArrayList<>();
        List<ImmutablePair<String, CreatureMoveBuilder>> moveBuilderMap;
        if (phase.isEmpty()) moveBuilderMap = this.creatureType.getMoves();
        else moveBuilderMap = this.creatureType.getPhaseBuilderMaps().get(phase).getMoves();

        //add the initial state
        AnimationControllerState<AnimationDataEntityLiving> initialState = new AnimationControllerState<AnimationDataEntityLiving>("default")
                .addAnimation("animation." + this.creatureType.getName() + ".look", animData -> this.getPhase().equals(phase));
        creatureMovesStates.add(initialState);

        //define anim controller name
        String controllerName = "moveUse" + phase;

        //iterate over each move
        for (ImmutablePair<String, CreatureMoveBuilder> moveEntry : moveBuilderMap) {
            String moveName = moveEntry.getKey();
            CreatureMoveBuilder moveBuilder = moveEntry.getValue();
            CreatureMoveChargeupBuilder chargeupBuilder = moveBuilder.getMoveChargeupBuilder();

            //for chargeup moves, create states for each phase
            if (chargeupBuilder != null) {
                for (ChargeupPhase currentChargeupPhase : ChargeupPhase.values()) {
                    String chargeupPhaseName = currentChargeupPhase.name().toLowerCase();
                    String controllerStateName = moveName + "_" + chargeupPhaseName;

                    //---add transition in initial state---
                    initialState.addStateTransition(controllerStateName, animData -> !this.getIsSleeping()
                            && this.getCreatureMoves().currentMoveMatches(moveName, currentChargeupPhase));

                    //---define corresponding state---
                    double transitionLength = currentChargeupPhase == ChargeupPhase.FINISHING ? 0.15D : 0D;
                    AnimationControllerState<AnimationDataEntityLiving> stateToAdd = new AnimationControllerState<AnimationDataEntityLiving>(controllerStateName, transitionLength, EasingType.EaseInOutSine)
                            .addAnimation("animation."+this.creatureType.getName()+"."+controllerStateName);

                    //and loop again xd
                    for (ChargeupPhase otherChargeupPhase : ChargeupPhase.values()) {
                        if (otherChargeupPhase == currentChargeupPhase) continue;

                        String otherChargeupPhaseName = otherChargeupPhase.name().toLowerCase();
                        String otherControllerStateName = moveName + "_" + otherChargeupPhaseName;

                        stateToAdd.addStateTransition(
                                otherControllerStateName, animData -> !this.getIsSleeping()
                                        && this.getCreatureMoves().currentMoveMatches(moveName, otherChargeupPhase)
                        );
                    }

                    //exclusive for finish, to transition back to default
                    if (currentChargeupPhase == ChargeupPhase.FINISHING) {
                        stateToAdd.addStateTransition("default", animData -> this.getIsSleeping()
                                        || animData.allAnimationsFinished(controllerName))
                                .addExitEffect(animData -> this.onMoveFinish(moveName));
                    }
                    //emergency exit condition for other phases, mostly for client
                    else stateToAdd.addStateTransition("default", animData -> this.getIsSleeping()
                            || this.getCreatureMoves().getCurrentMove().isEmpty());

                    //add the state
                    creatureMovesStates.add(stateToAdd);
                }
            }
            //for non chargeup moves, add other anim states for each move to a single anim controller
            else {
                //transition from initial state to a state associated with the move
                initialState.addStateTransition(moveName, animData -> !this.getIsSleeping() && this.getCurrentMove().equals(moveName));

                //create state for move
                AnimationControllerState<AnimationDataEntityLiving> moveState = new AnimationControllerState<AnimationDataEntityLiving>(moveName, 0.15D, EasingType.EaseInOutSine)
                        .addStateTransition("default", animData -> this.getIsSleeping()
                                || animData.allAnimationsFinished(controllerName))
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
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntityLiving>(this, controllerName, "default",
                creatureMovesStates.toArray(new AnimationControllerState[0])
        ));
    }

    //small helper method for defining what happens when a move finishes
    private void onMoveFinish(@NotNull String moveName) {
        CreatureMoveStorage creatureMoveStorage = this.getCreatureMoves();
        if (!this.world.isRemote) this.moveFinishCount++;
        if (!creatureMoveStorage.hasCurrentMoveEndEffectFired()) {
            //use moveName, as it might have been erased on server after being cleared on client
            CreatureMoveBuilder creatureMoveBuilder = creatureMoveStorage.getUsableMoveBuilder(moveName);
            if (!this.world.isRemote && creatureMoveBuilder != null && creatureMoveBuilder.getOnMoveEndEffect() != null) {
                creatureMoveBuilder.getOnMoveEndEffect().accept(this);
            }
            creatureMoveStorage.markCurrentMoveEndEffectFired();
        }
        creatureMoveStorage.resetCurrentMove(this);
        if (moveName.equals(this.riddenAimingMoveName)) {
            if (!this.isRiddenAiming()) {
                this.riddenAimingMoveName = "";
                this.riddenAimArea = null;
                this.riddenMeleeAimReach = 0D;
            }
            this.riddenAreaAttackActive = false;
            this.riddenAreaHitEntities.clear();
        }
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

    //-----ui stuff-----
    @Override
    public ModularPanel buildUI(RiftCreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureUI.show(data, syncManager, settings);
    }

    //-----other useless events idk nor care about-----
    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
