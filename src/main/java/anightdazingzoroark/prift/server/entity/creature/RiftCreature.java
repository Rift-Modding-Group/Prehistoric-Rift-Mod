package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.client.ui.RiftCreatureUI;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
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
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate.BlockBreakPlanEntry;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.config.RiftListsConfig;
import anightdazingzoroark.prift.server.item.RiftItems;
import anightdazingzoroark.prift.server.player.PlayerPartyProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.api.util.TriConsumer;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

/**
 * le heart and soul of this mod
 * */
public class RiftCreature extends EntityTameable implements IAnimatable<AnimationDataEntity>, IRiftCreature, ICreature, IRayCreator<RiftCreature>, IEntityAdditionalSpawnData, IGuiHolder<RiftCreatureGuiData> {
    @NotNull
    private RiftCreatureBuilder creatureType;
    @NotNull
    private final RiftLibInventoryHandler creatureInventory;
    @NotNull
    private AnimationDataEntity animData;

    public static final IAttribute ELEMENTAL_DAMAGE_ATTRIBUTE = new RangedAttribute(null, "rift.elementalDamage", 2.0, 0.0, 2048.0).setShouldWatch(true);
    public static final IAttribute STAMINA_ATTRIBUTE = new RangedAttribute(null, "rift.stamina", 2.0, 0.0, 2048.0).setShouldWatch(true);

    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> NATURE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Float> STAMINA_CURRENT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<CreatureMoveStorage> CREATURE_MOVES = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_MOVE_STORAGE);
    private static final DataParameter<CreatureStatsStorage> CREATURE_STATS = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.CREATURE_STATS_STORAGE);
    private static final DataParameter<String> CREATURE_PHASE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USE_BLOCK_BREAK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> TAMING_PROGRESS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte> TAME_TARGETING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> DEPLOYMENT_TYPE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);

    //--custom property values, which can be called and manipulated from a creature builder--
    @NotNull
    private Map<String, AbstractPropertyValue<?>> propertyValueMap = Map.of();

    //---remembered player targets (server only)---
    @NotNull
    private final List<UUID> rememberedPlayerTargetUUIDs = new ArrayList<>();

    //--server side primitive params and objects--
    //manages a creature's ability to sprint based on whether or not it attacked before
    private int sprintToAttackCooldown;
    //manages a creature's ability to leap based on whether it attacked before
    private int leapToAttackCooldown;
    private int staminaDrainTicks;
    private float pendingStaminaDrain;
    private int staminaRegenerationDelay;
    private int staminaRegenerationTicks;
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
    @NotNull
    private CreatureAcquisitionInfo acquisitionInfo = new CreatureAcquisitionInfo(null, 0L);
    private RiftCreatureEnums.@Nullable SleepCause sleepCause;
    //herd helper
    @Nullable
    private RiftCreatureHerdHelper herdHelper;

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
        this(worldIn, RiftCreatureRegistry.DEFAULT_CREATURE);
    }

    public RiftCreature(World worldIn, String creatureName) {
        super(worldIn);
        this.creatureType = resolveCreatureBuilder(creatureName);
        CreatureDomesticationBuilder domestication = this.creatureType.getDomestication();
        this.creatureInventory = new RiftLibInventoryHandler(domestication == null ? 1 : domestication.getInventorySize());
        this.moveHelper = new RiftCreatureMoveHelper(this);
        this.navigator = new RiftCreaturePathNavigate(this, worldIn);
        this.applyCreatureTypeSettings();
        this.animData = new AnimationDataEntity(this);

        if (worldIn != null && !worldIn.isRemote) {
            this.herdHelper = this.canDoHerding() ? new RiftCreatureHerdHelper(this) : null;
            this.initCreatureAI();
        }
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

    private void changeCreatureType(RiftCreatureBuilder builder) {
        if (this.creatureType == builder) return;

        this.leaveHerd();
        this.creatureType = builder;
        CreatureDomesticationBuilder domestication = this.creatureType.getDomestication();
        this.creatureInventory.setSize(domestication == null ? 1 : domestication.getInventorySize());
        this.applyCreatureTypeSettings();
        this.animData = new AnimationDataEntity(this);
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
        this.dataManager.register(NATURE, (byte) 0);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(STAMINA_CURRENT, 0f);
        this.dataManager.register(CREATURE_MOVES, new CreatureMoveStorage());
        this.dataManager.register(CREATURE_STATS, new CreatureStatsStorage());
        this.dataManager.register(CREATURE_PHASE, "");
        this.dataManager.register(LEAPING, false);
        this.dataManager.register(USE_BLOCK_BREAK, false);
        this.dataManager.register(SLEEPING, false);
        this.dataManager.register(TAMING_PROGRESS, 0f);
        this.dataManager.register(TAME_TARGETING, (byte) 0);
        this.dataManager.register(DEPLOYMENT_TYPE, (byte) -1);
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
                            && RiftCreature.this.getTameTargeting() == RiftCreatureEnums.TameTargeting.ASSIST
                            && super.shouldExecute();
                }
            });
            this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.getIsSleeping()
                            && RiftCreature.this.getTameTargeting() == RiftCreatureEnums.TameTargeting.DEFENSIVE
                            && super.shouldExecute();
                }
            });
        }
        this.targetTasks.addTask(2, new RiftFindTarget(this, true));

        if (this.creatureType.getFleePredicate() != null) {
            this.tasks.addTask(0, new EntityAIAvoidEntity<EntityLivingBase>(
                    this, EntityLivingBase.class,
                    this::shouldFleeFrom, this.creatureType.getFleeSearchDistance(),
                    1D, 1D
            ) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.isTamed() && super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return this.closestLivingEntity != null
                            && RiftCreature.this.shouldFleeFrom(this.closestLivingEntity)
                            && !RiftCreature.this.isTamed()
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
        if (!this.creatureType.getNavigation().getCanSwim()) {
            this.tasks.addTask(2, new RiftGoToLandFromWater(this));
        }
        if (this.creatureType.isHerder()) {
            this.tasks.addTask(3, new RiftFollowHerdLeader(this));
        }
        if (this.creatureType.getDomestication() != null) {
            this.tasks.addTask(3, new EntityAIFollowOwner(this, 1D, 10f, 4f) {
                @Override
                public boolean shouldExecute() {
                    return !RiftCreature.this.getIsSleeping() && !RiftCreature.this.isBeingRidden()
                            && RiftCreature.this.getAttackTarget() == null && super.shouldExecute();
                }

                @Override
                public boolean shouldContinueExecuting() {
                    return !RiftCreature.this.getIsSleeping() && !RiftCreature.this.isBeingRidden()
                            && RiftCreature.this.getAttackTarget() == null && super.shouldContinueExecuting();
                }
            });
        }
        this.tasks.addTask(4, new EntityAIWander(this, 1D) {
            @Override
            public boolean shouldExecute() {
                return !RiftCreature.this.isTamed() && RiftCreature.this.canLeadHerdBehavior() && super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                return !RiftCreature.this.isTamed() && RiftCreature.this.canLeadHerdBehavior() && super.shouldContinueExecuting();
            }

            @Override
            public void resetTask() {
                if (RiftCreature.this.isTamed() || !RiftCreature.this.canLeadHerdBehavior()) RiftCreature.this.getNavigator().clearPath();
                super.resetTask();
            }
        });
        this.tasks.addTask(5, new EntityAILookIdle(this) {
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
            if (this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE) {
                this.setDead();
                return;
            }

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

            //creatures must drop threats that they are configured to flee
            if (this.shouldFleeFrom(this.getAttackTarget())) this.setAttackTarget(null);

            //keep the pose active for the full airborne portion even if pathing
            //relinquishes its leap action before the creature reaches the ground
            boolean continueLeapPose = this.dataManager.get(LEAPING) && !this.onGround;
            this.setLeaping(this.getCreatureMoveHelper().isLeaping() || continueLeapPose);

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

            //-----stamina consumption-----
            float staminaDrainPerSecond = 0f;
            int staminaConsumptionInterval = 0;
            if (this.isSprinting()) {
                staminaDrainPerSecond = MoveResult.SPRINT.staminaConsumption();
                staminaConsumptionInterval = MoveResult.SPRINT.staminaConsumptionInterval();
            }
            CreatureMoveBuilder currentMoveBuilder = creatureMoveStorage.getMoveBuilderCurrentMove();
            boolean currentMoveDrainsStamina = currentMoveBuilder != null && !this.getUseBlockBreak()
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
                if (this.canUseStamina(staminaDrainThisTick)) {
                    this.pendingStaminaDrain += staminaDrainThisTick;
                    this.staminaDrainTicks++;
                    if (this.staminaDrainTicks >= staminaConsumptionInterval) {
                        if (!this.useStamina(this.pendingStaminaDrain)) {
                            this.setSprinting(false);
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
                    this.setSprinting(false);
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

            //-----stamina regen-----
            if (this.staminaRegenerationDelay > 0) {
                this.staminaRegenerationDelay--;
                this.staminaRegenerationTicks = 0;
            }
            //fighting and exertion slow down regen
            else if (this.getStamina() < this.getMaxStamina()) {
                this.staminaRegenerationTicks++;
                if (this.staminaRegenerationTicks >= MoveResult.USE_MOVE.staminaConsumptionInterval()) {
                    boolean rapidlyRegenerating = this.getAttackTarget() == null && this.getCurrentMove().isEmpty()
                            && !this.isSprinting() && !this.isLeaping();
                    float staminaRegenerationPerSecond = rapidlyRegenerating ? 0.02f : 0.005f;
                    float staminaToAdd = this.getStaminaCost(staminaRegenerationPerSecond * MoveResult.USE_MOVE.staminaConsumptionInterval() / 20f);
                    this.setStamina(this.getStamina() + staminaToAdd);
                    this.staminaRegenerationTicks = 0;
                }
            }
            else this.staminaRegenerationTicks = 0;

            //tick sprinting related stuff
            if (this.sprintToAttackCooldown > 0) this.sprintToAttackCooldown--;
            if (this.leapToAttackCooldown > 0) this.leapToAttackCooldown--;

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
        //everythin we wanna do here is server only
        if (this.world.isRemote) return super.processInteract(player, hand);

        CreatureDomesticationBuilder domestication = this.creatureType.getDomestication();
        ItemStack heldItem = player.getHeldItem(hand);
        //tamed only effects
        if (this.isTamed()) {
            if (this.isOwner(player)) {
                //open ui
                if (heldItem.isEmpty()) {
                    RiftCreatureGuiFactory.INSTANCE.open(player, this);
                    return true;
                }
                //feed tamed creatures for healing
                else {
                    RiftCreatureFood creatureFood = this.getCreatureFood(heldItem);
                    if (creatureFood == null) return false;

                    if (creatureFood.percentHealed != null && creatureFood.percentHealed > 0f && this.getHealth() < this.getMaxHealth()) {
                        this.consumeItemFromStack(player, heldItem);
                        this.heal(this.getMaxHealth() * creatureFood.percentHealed);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        return true;
                    }
                    else return false;
                }
            }
            else return false;
        }
        //stuff for wild creatures
        else if (domestication != null) {
            //creative meal automatically tames creature
            if (heldItem.getItem() == RiftItems.CREATIVE_MEAL) {
                this.tameCreature(player);
                return true;
            }
            //normal feed taming
            else if (!heldItem.isEmpty()) {
                RiftCreatureFood creatureFood = this.getCreatureFood(heldItem);
                if (creatureFood == null) return false;

                boolean hasTamingEffectiveness = creatureFood.tameEffectiveness != null && creatureFood.tameEffectiveness.length > 0;
                if (hasTamingEffectiveness) {
                    if (domestication.getTamingMethod() == RiftCreatureEnums.TamingMethod.FEED || this.getIsSleeping() && this.sleepCause == RiftCreatureEnums.SleepCause.TRANQ_BOMB) {
                        float firstValue = Math.clamp(creatureFood.tameEffectiveness[0], 0f, 1f);
                        float secondValue = creatureFood.tameEffectiveness.length > 1
                                ? Math.clamp(creatureFood.tameEffectiveness[1], 0f, 1f) : firstValue;
                        float minimum = Math.min(firstValue, secondValue);
                        float maximum = Math.max(firstValue, secondValue);
                        float effectiveness = minimum == maximum ? minimum : this.rand.nextFloat(minimum, maximum);

                        float updatedProgress = Math.clamp(this.getTamingProgress() + effectiveness, 0f, 1f);
                        boolean completesTaming = updatedProgress >= 1f;
                        if (!completesTaming || !ForgeEventFactory.onAnimalTame(this, player)) {
                            this.consumeItemFromStack(player, heldItem);
                            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());

                            if (completesTaming) this.tameCreature(player);
                            else this.setTamingProgress(updatedProgress);
                        }
                    }
                    return true;
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
    //---ICreature implementations from the api ends here---

    //this gets the scale of the model of the entity
    public float scale() {
        return MathUtil.slopeResult(
                this.getAgeInTicks(), true,
                0, this.creatureType.getDaysUntilAdult() * 24000,
                this.creatureType.getScaleRangeForAge()[0], this.creatureType.getScaleRangeForAge()[1]
        );
    }

    /**
     * the vanilla receive damage method. for blocking damage from related entities.
     * */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source != null && (this.isRelatedToEntity(source.getTrueSource()) || this.isRelatedToEntity(source.getImmediateSource()))) {
            return false;
        }

        return super.attackEntityFrom(source, amount);
    }

    /**
     * the vanilla attack entity method. is now used for damage calculations
     * use this when attacking an entity
     * */
    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
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
    public void attackEntityFromSprint(Entity entityIn, int basePower) {
        this.attackEntityFromMovement(entityIn, basePower);
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
     * such as if its tamed to its owner or if it is a herdmate
     * */
    public boolean isRelatedToEntity(@Nullable Entity entity) {
        if (entity instanceof MultiPartEntityPart hitboxPart) {
            Entity hitboxParent = (Entity) hitboxPart.parent;
            return this.isRelatedToEntity(hitboxParent);
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
        if (target != null && (this.getIsSleeping() || this.shouldFleeFrom(target))) return;
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
     * make sure this creature doesn't get dislocated when pushed
     * */
    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!this.creatureType.getCannotBePushed()) super.applyEntityCollision(entityIn);
    }

    /**
     * push other entities
     * */
    @Override
    public void collideWithEntity(Entity entityIn) {
        if (!this.creatureType.getCannotBePushed()) {
            super.collideWithEntity(entityIn);
            return;
        }

        //special cases where given entity cannot be pushed back
        if (entityIn == null || entityIn instanceof IProjectile || entityIn instanceof EntityFireball
                || entityIn.equals(this) || this.isRidingSameEntity(entityIn) || entityIn.noClip
                || (this.getCreatureMoveHelper().isLeaping() && entityIn.onGround)
        ) return;

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

    //-----sound management-----
    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
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

    //-----projectile management-----
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

    //-----sprint to attack management-----
    public boolean canSprintToAttack() {
        return this.sprintToAttackCooldown == 0;
    }

    public void removeSprintToAttackCooldown() {
        this.sprintToAttackCooldown = 0;
    }

    public void resetSprintToAttackCooldown() {
        this.sprintToAttackCooldown = this.world.rand.nextInt(5, 11) * 20;
    }

    //-----leap to attack management-----
    public boolean canLeapToAttack() {
        return this.leapToAttackCooldown == 0;
    }

    public void removeLeapToAttackCooldown() {
        this.leapToAttackCooldown = 0;
    }

    public void resetLeapToAttackCooldown() {
        this.leapToAttackCooldown = this.world.rand.nextInt(5, 11) * 20;
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
        this.setSprinting(false);
        this.setAttackTarget(null);
        this.pendingStaminaDrain = 0f;
        this.staminaDrainTicks = 0;
    }

    @Override
    protected boolean isMovementBlocked() {
        return this.getIsSleeping() || super.isMovementBlocked();
    }

    //-----taming management-----
    public void tameCreature(@NotNull EntityPlayer player) {
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

        PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
        if (playerParty != null && !playerParty.addPartyMember(this)) {
            player.sendStatusMessage(new TextComponentTranslation("party.warning.party_full"), false);
        }
    }

    public float getTamingProgress() {
        return this.dataManager.get(TAMING_PROGRESS);
    }

    public void setTamingProgress(float value) {
        this.dataManager.set(TAMING_PROGRESS, Math.clamp(value, 0f, 1f));
    }

    @NotNull
    public RiftCreatureEnums.TameTargeting getTameTargeting() {
        return RiftCreatureEnums.TameTargeting.values()[this.dataManager.get(TAME_TARGETING)];
    }

    public void setTameTargeting(@NotNull RiftCreatureEnums.TameTargeting value) {
        if (this.getTameTargeting() != value) this.setAttackTarget(null);
        this.dataManager.set(TAME_TARGETING, (byte) value.ordinal());
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
                if (planEntry == null) continue;

                boolean ordinaryJumpable = this.getCreaturePathNavigate().isStandardJumpable(planEntry, worldCollisionBounds);
                if (!ordinaryJumpable && worldCollisionBounds.intersects(frontBounds) && this.canBreakBlock(immutablePos)) {
                    blocks.add(immutablePos);
                }
            }
        }
        return blocks;
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

        if ((this.bodyTouchingLiquid() && !this.getNavigationBuilder().getCanSwim()) || !ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
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

        //reset some timers
        this.resetLeapToAttackCooldown();
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

    //-----stamina use management-----
    //getting stamina cost excempts special modifiers: only base stamina stat matters
    private float getStaminaCost(float nominalMaximumFraction) {
        if (nominalMaximumFraction <= 0f) return 0f;
        return nominalMaximumFraction * 100f;
    }

    public boolean canUseStamina(float nominalMaximumFraction) {
        if (nominalMaximumFraction < 0f) return false;
        if (nominalMaximumFraction == 0f) return true;
        float requiredStamina = this.getStaminaCost(nominalMaximumFraction + this.pendingStaminaDrain);
        return this.getStamina() + 1E-4f >= requiredStamina;
    }

    public boolean useStamina(float nominalMaximumFraction) {
        if (nominalMaximumFraction < 0f) return false;
        float staminaCost = this.getStaminaCost(nominalMaximumFraction);
        if (staminaCost <= 0f) return true;
        if (this.getStamina() + 1E-4f < staminaCost) return false;

        this.setStamina(this.getStamina() - staminaCost);
        this.staminaRegenerationDelay = 30;
        this.staminaRegenerationTicks = 0;
        return true;
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
        this.dataManager.set(DEPLOYMENT_TYPE, value != null ? (byte) value.ordinal() : (byte) -1);
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (!this.world.isRemote && this.getDeploymentType() == RiftCreatureEnums.CreatureDeployment.PARTY) {
            this.setDeploymentType(RiftCreatureEnums.CreatureDeployment.PARTY_INACTIVE);
            if (this.getOwner() instanceof EntityPlayer player) {
                PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
                if (playerParty != null) playerParty.updatePartyMember(this);
            }
        }
        super.onDeath(cause);
    }

    //-----nbt parsing related stuff-----
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

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
    @NotNull
    public AnimationDataEntity getAnimationData() {
        return this.animData;
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataEntity animationData) {
        //-----set scale-----
        animationData.setScale(holder -> this.scale());

        //-----create animation controllers-----
        //---for normal stuff---
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "movement", "default",
                new AnimationControllerState<AnimationDataEntity>("default")
                        .addStateTransition("moving", animData -> !this.getIsSleeping() && this.getCurrentMove().isEmpty()
                                && !this.isLeaping() && animData.isMoving()),
                new AnimationControllerState<AnimationDataEntity>("moving", 0.1)
                        .addAnimation("animation."+this.creatureType.getName()+".walk")
                        .addStateTransition("default", animData -> this.getIsSleeping() || !this.getCurrentMove().isEmpty()
                                || this.isLeaping() || !animData.isMoving())
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "sprintPosing", "default",
                new AnimationControllerState<AnimationDataEntity>("default", 0.2)
                        .addStateTransition("sprint", animData -> !this.getIsSleeping() && this.getCurrentMove().isEmpty()
                                && !this.isLeaping() && this.isSprinting()),
                new AnimationControllerState<AnimationDataEntity>("sprint", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".sprint_pose")
                        .addStateTransition("default", animData -> this.getIsSleeping() || !this.getCurrentMove().isEmpty()
                                || this.isLeaping() || !this.isSprinting())
        ));
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "sleeping", "default",
                new AnimationControllerState<AnimationDataEntity>("default", 0.2)
                        .addStateTransition("sleeping", animData -> this.getIsSleeping()),
                new AnimationControllerState<AnimationDataEntity>("sleeping", 0.2)
                        .addAnimation("animation."+this.creatureType.getName()+".sleep")
                        .addStateTransition("default", animData -> !this.getIsSleeping())
        ));
        if (this.creatureType.getNavigation().getCanLeap()) {
            animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, "leaping", "default",
                    new AnimationControllerState<AnimationDataEntity>("default", 0.1)
                            .addStateTransition("leaping", animData -> !this.getIsSleeping() && this.isLeaping()),
                    new AnimationControllerState<AnimationDataEntity>("leaping", 0.1)
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
                    String chargeupPhaseName = currentChargeupPhase.name().toLowerCase();
                    String controllerStateName = moveName + "_" + chargeupPhaseName;

                    //---add transition in initial state---
                    initialState.addStateTransition(controllerStateName, animData -> !this.getIsSleeping()
                            && this.getCreatureMoves().currentMoveMatches(moveName, currentChargeupPhase));

                    //---define corresponding state---
                    AnimationControllerState<AnimationDataEntity> stateToAdd = new AnimationControllerState<AnimationDataEntity>(controllerStateName)
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
                AnimationControllerState<AnimationDataEntity> moveState = new AnimationControllerState<AnimationDataEntity>(moveName)
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
        animationData.addAnimationController(new AnimationController<RiftCreature, AnimationDataEntity>(this, controllerName, "default",
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
