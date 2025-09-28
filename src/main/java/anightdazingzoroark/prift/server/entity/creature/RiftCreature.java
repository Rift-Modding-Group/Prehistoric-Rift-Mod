package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.*;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.*;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.enums.RiftTameRadialChoice;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.*;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBoxHelper;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import anightdazingzoroark.riftlib.hitboxLogic.IMultiHitboxUser;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RiftCreature extends EntityTameable implements IAnimatable, IMultiHitboxUser, IDynamicRideUser {
    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> XP = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LOVE_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RANGED_ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> LARGE_WEAPON = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> ENERGY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CAN_USE_LEFT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_MIDDLE_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_SPACEBAR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_SPACEBAR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SPACEBAR_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> JUST_SPAWNED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TAME_PROGRESS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_HOME_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FORCED_AWAKE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CLIMBING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> DEPLOYMENT_TYPE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> TURRET_MODE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> TURRET_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> INCAPACITATED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_ROTATE_MOUNTED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CLOAKED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BURROWING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_MOVE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> WARNING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_WARN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    private static final DataParameter<CreatureAcquisitionInfo> ACQUISITION_INFO = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.ACQUISITION_INFO);

    private static final DataParameter<Integer> PROPOSED_TARGETLESS_MOVE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CURRENT_MOVE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> CAN_USE_MOVE_ONE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_MOVE_ONE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MOVE_ONE_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT); //for charging only
    private static final DataParameter<Integer> MOVE_ONE_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> CAN_USE_MOVE_TWO = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_MOVE_TWO = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MOVE_TWO_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT); //for charging only
    private static final DataParameter<Integer> MOVE_TWO_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> CAN_USE_MOVE_THREE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_MOVE_THREE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MOVE_THREE_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT); //for charging only
    private static final DataParameter<Integer> MOVE_THREE_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Integer> PLAY_CHARGED_MOVE_ANIM = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT); //0-4 will represent each stage in playing the charged move anim, -1 means its not being played
    private static final DataParameter<Boolean> PLAY_INFINITE_MOVE_ANIM = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    private static final DataParameter<List<CreatureMove>> MOVE_LIST = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.LIST_CREATURE_MOVE);
    private static final DataParameter<List<CreatureMove>> LEARNABLE_MOVE_LIST = EntityDataManager.createKey(RiftCreature.class, RiftDataSerializers.LIST_CREATURE_MOVE);

    private static final DataParameter<Boolean> BREAK_BLOCK_MODE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_SET_BLOCK_BREAK_MODE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Boolean> USING_LARGE_WEAPON = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LARGE_WEAPON_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LARGE_WEAPON_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> FIRING_CATAPULT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> USED_MOVE_TYPE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> USED_MULTISTEP_MOVE_STEP = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Float> LEAP_X_VELOCITY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LEAP_Y_VELOCITY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LEAP_Z_VELOCITY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.FLOAT);

    private static final DataParameter<BlockPos> HOME_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BLOCK_POS);

    private int herdSize = 1;
    private RiftCreature herdLeader;
    private int boxReviveTime;
    private int energyMod;
    private int energyRegenMod;
    private int energyRegenModDelay;
    private int eatFromInvCooldown;
    private int eatFromInvForEnergyCooldown;
    private int eatFromInvForGrowthCooldown;
    private boolean informLowEnergy;
    private boolean informNoEnergy;
    public boolean cannotUseRightClick;
    public final RiftCreatureType creatureType;
    public AnimationFactory factory = new AnimationFactory(this);
    public boolean isRideable;
    public RiftCreatureInventory creatureInventory;
    private boolean steerable = true;
    public double minCreatureHealth = 20D;
    public double maxCreatureHealth = 20D;
    protected double speed;
    protected double waterSpeed = 1.5D;
    protected int herdCheckCountdown;
    public double yFloatPos;
    public List<RiftCreatureConfig.Food> favoriteFood;
    public List<RiftCreatureConfig.Meal> tamingFood;
    public List<String> breedingFood;
    public String saddleItem;
    public Entity[] hitboxArray = {};
    private List<Vec3d> ridePositions;
    public float oldScale;
    private int healthRegen;
    protected double attackDamage;
    private final int maxEnergy;
    private final double healthLevelMultiplier;
    private final double damageLevelMultiplier;
    private double damageMultiplier = 1D;
    protected int densityLimit;
    protected List<String> targetList;
    public boolean isFloatingOnWater;
    private boolean recentlyHit;
    private float recentlyHitDamage;
    private Entity grabVictim;
    private int chosenAnimFromMultiple = -1;
    private int resetEnergyTick;
    public int lastIntervalForMoveCall;
    private int ticksSinceHitboxRemoval = 0;
    //for updating hud stuff when creature is deployed from party
    private int cachedHealthBucket = -1;
    private int cachedEnergyBucket = -1;
    private int cachedXPBucket = -1;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.minCreatureHealth = ((double) RiftConfigHandler.getConfig(creatureType).stats.baseHealth)/8;
        this.maxCreatureHealth = RiftConfigHandler.getConfig(creatureType).stats.baseHealth;
        this.attackDamage = RiftConfigHandler.getConfig(creatureType).stats.baseDamage;
        this.maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;
        this.healthLevelMultiplier = RiftConfigHandler.getConfig(creatureType).stats.healthMultiplier;
        this.damageLevelMultiplier = RiftConfigHandler.getConfig(creatureType).stats.damageMultiplier;
        this.ignoreFrustumCheck = true;
        this.setSpeed(0f);
        this.setWaterSpeed(1f);
        this.setScaleForAge(false);
        this.energyMod = 0;
        this.energyRegenMod = 0;
        this.energyRegenModDelay = 0;
        this.eatFromInvCooldown = 0;
        this.eatFromInvForEnergyCooldown = 0;
        this.eatFromInvForGrowthCooldown = 0;
        this.informLowEnergy = false;
        this.informNoEnergy = false;
        this.cannotUseRightClick = true;
        this.heal((float)maxCreatureHealth);
        this.herdCheckCountdown = 0;
        this.yFloatPos = 0D;
        this.oldScale = 0;
        this.healthRegen = 0;
        this.isFloatingOnWater = false;
        this.initInventory();
        this.initializeHitboxes(this);
        this.initializeRiderPositions(this);

        //ais are now initialized here
        if (!worldIn.isRemote) this.initRiftCreatureAI();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(XP, 0);
        this.dataManager.register(LOVE_COOLDOWN, 0);
        this.dataManager.register(ATTACKING, false);
        this.dataManager.register(RANGED_ATTACKING, false);
        this.dataManager.register(VARIANT, rand.nextInt(4));
        this.dataManager.register(SITTING, false);
        this.dataManager.register(BEHAVIOR, (byte) TameBehaviorType.ASSIST.ordinal());
        this.dataManager.register(SADDLED, false);
        this.dataManager.register(LARGE_WEAPON, (byte) RiftLargeWeaponType.NONE.ordinal());
        this.dataManager.register(ENERGY, this.getMaxEnergy());
        this.dataManager.register(CAN_USE_LEFT_CLICK, true);
        this.dataManager.register(CAN_USE_RIGHT_CLICK, false);
        this.dataManager.register(CAN_USE_MIDDLE_CLICK, true);
        this.dataManager.register(CAN_USE_SPACEBAR, true);
        this.dataManager.register(USING_SPACEBAR, false);
        this.dataManager.register(SPACEBAR_USE, 0);
        this.dataManager.register(HAS_TARGET, false);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(JUST_SPAWNED, true);
        this.dataManager.register(TAME_PROGRESS, 0);
        this.dataManager.register(HAS_HOME_POS, false);
        this.dataManager.register(SLEEPING, false);
        this.dataManager.register(FORCED_AWAKE, false);
        this.dataManager.register(CLIMBING, false);
        this.dataManager.register(DEPLOYMENT_TYPE, (byte) PlayerTamedCreatures.DeploymentType.NONE.ordinal());
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
        this.dataManager.register(TURRET_MODE, false);
        this.dataManager.register(TURRET_TARGET, (byte) TurretModeTargeting.HOSTILES.ordinal());
        this.dataManager.register(INCAPACITATED, false);
        this.dataManager.register(CAN_ROTATE_MOUNTED, true);
        this.dataManager.register(CLOAKED, false);
        this.dataManager.register(BURROWING, false);
        this.dataManager.register(CAN_MOVE, true);
        this.dataManager.register(WARNING, false);
        this.dataManager.register(CAN_WARN, true);

        this.dataManager.register(ACQUISITION_INFO, new CreatureAcquisitionInfo(null, 0L));

        this.dataManager.register(PROPOSED_TARGETLESS_MOVE, -1);
        this.dataManager.register(CURRENT_MOVE, -1);

        this.dataManager.register(CAN_USE_MOVE_ONE, true);
        this.dataManager.register(USING_MOVE_ONE, false);
        this.dataManager.register(MOVE_ONE_USE, 0);
        this.dataManager.register(MOVE_ONE_COOLDOWN, 0);

        this.dataManager.register(CAN_USE_MOVE_TWO, true);
        this.dataManager.register(USING_MOVE_TWO, false);
        this.dataManager.register(MOVE_TWO_USE, 0);
        this.dataManager.register(MOVE_TWO_COOLDOWN, 0);

        this.dataManager.register(CAN_USE_MOVE_THREE, true);
        this.dataManager.register(USING_MOVE_THREE, false);
        this.dataManager.register(MOVE_THREE_USE, 0);
        this.dataManager.register(MOVE_THREE_COOLDOWN, 0);

        this.dataManager.register(PLAY_CHARGED_MOVE_ANIM, -1);
        this.dataManager.register(PLAY_INFINITE_MOVE_ANIM, false);

        this.dataManager.register(MOVE_LIST, new ArrayList<>());
        this.dataManager.register(LEARNABLE_MOVE_LIST, new ArrayList<>());

        this.dataManager.register(BREAK_BLOCK_MODE, false);
        this.dataManager.register(CAN_SET_BLOCK_BREAK_MODE, true);

        this.dataManager.register(USING_LARGE_WEAPON, false);
        this.dataManager.register(LARGE_WEAPON_USE, 0);
        this.dataManager.register(LARGE_WEAPON_COOLDOWN, 0);

        this.dataManager.register(FIRING_CATAPULT, false);

        this.dataManager.register(USED_MOVE_TYPE, -1);
        this.dataManager.register(USED_MULTISTEP_MOVE_STEP, 0);

        this.dataManager.register(LEAP_X_VELOCITY, 0.0f);
        this.dataManager.register(LEAP_Y_VELOCITY, 0.0f);
        this.dataManager.register(LEAP_Z_VELOCITY, 0.0f);

        this.dataManager.register(HOME_POS, new BlockPos(0, 0, 0));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
        if (this.canBeKnockedBack()) this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    protected void initRiftCreatureAI() {
        List<EntityAITarget> targetBehaviors = this.createTargetBehaviors();
        for (int x = 0; x < targetBehaviors.size(); x++) this.targetTasks.addTask(x, targetBehaviors.get(x));

        List<EntityAIBase> behaviors = this.createBehaviors();
        for (int x = 0; x < behaviors.size(); x++) this.tasks.addTask(x, behaviors.get(x));
    }

    //note to self: priority is based on when a behavior is being added
    //if placed early it means its priority is high
    //those placed later have lower priority
    private List<EntityAITarget> createTargetBehaviors() {
        List<EntityAITarget> toReturn = new ArrayList<>();
        if (this.creatureType == null) return toReturn;

        if (this.creatureType.isTameable) toReturn.add(new RiftTurretModeTargeting(this, true));
        if (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.DOCILE))
            toReturn.add(new RiftHurtByTarget(this, this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.HERDER)));
        if (!(this instanceof RiftWaterCreature)
                && (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.AGGRESSIVE)
                || this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.AGGRESSIVE_TO_HUMANS)))
            toReturn.add(new RiftGetTargets(this, this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.HERDER), true));
        if (this instanceof RiftWaterCreature
                && (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.AGGRESSIVE)
                || this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.AGGRESSIVE_TO_HUMANS)))
            toReturn.add(new RiftGetTargets.RiftGetTargetsWater(this, this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.HERDER), true));
        if (this.creatureType.isTameable) toReturn.add(new RiftAggressiveModeGetTargets(this, true));
        if (this.creatureType.isTameable) toReturn.add(new RiftProtectOwner(this));
        if (this.creatureType.isTameable) toReturn.add(new RiftAttackForOwner(this));

        return toReturn;
    }

    private List<EntityAIBase> createBehaviors() {
        List<EntityAIBase> toReturn = new ArrayList<>();
        if (this.creatureType == null) return toReturn;

        if (this.creatureType.isTameable && this instanceof IWorkstationUser) toReturn.add(new RiftCreatureOperateWorkstation(this));
        if (this.creatureType.isTameable) toReturn.add(new RiftUseLeadPoweredCrank(this));
        if (!(this instanceof RiftWaterCreature)) toReturn.add(new RiftLandDwellerSwim(this));
        toReturn.add(new RiftFleeFromEntities(this, 1.5F));
        if (this.creatureType.isTameable) toReturn.add(new RiftMate(this));
        if (this.creatureType.isTameable) toReturn.add(new RiftCreatureUseMoveMounted(this));
        //toReturn.add(new RiftCreatureWarnTarget(this, 1.25f, 0.5f));
        if (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.BLOCK_BREAKER))
            toReturn.add(new RiftBreakBlockWhilePursuingTarget(this));
        toReturn.add(new RiftCreatureUseMoveUnmounted(this));
        if (this.creatureType.isTameable && !(this instanceof RiftWaterCreature))
            toReturn.add(new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        if (this.creatureType.isTameable && this instanceof RiftWaterCreature)
            toReturn.add(new RiftWaterCreatureFollowOwner((RiftWaterCreature) this, 1.0D, 8.0F, 4.0F));
        if (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.HERDER))
            toReturn.add(new RiftHerdDistanceFromOtherMembers(this, 1.5D));
        if (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.HERDER))
            toReturn.add(new RiftHerdMemberFollow(this));
        if (!(this instanceof RiftWaterCreature)) toReturn.add(new RiftGoToLandFromWater(this, 16, 1.0D));
        if (this instanceof RiftWaterCreature && ((RiftWaterCreature) this).isAmphibious())
            toReturn.add(new RiftGoToWater(this, 16, 1.0D));
        if (!(this instanceof RiftWaterCreature)) new RiftWander(this, 1.0D);
        if (this instanceof RiftWaterCreature) new RiftWanderWater((RiftWaterCreature) this, 1.0D);
        toReturn.add(new RiftLookAround(this));

        return toReturn;
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(@Nonnull DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        super.onInitialSpawn(difficulty, livingdata);
        this.setAgeInDays(1);
        this.setEnergy(this.getMaxEnergy());
        //manage level based on distance from 0, 0
        double distFromCenter = Math.sqrt(this.posX * this.posX + this.posZ * this.posZ);
        double level = Math.floor((distFromCenter / GeneralConfig.levelingRadius)) * GeneralConfig.levelingRadisIncrement + RiftUtil.randomInRange(1, 10) + this.levelAddFromDifficulty();
        this.setLevel(RiftUtil.clamp((int) level, 0, 100));
        //manage herding
        if (this.canDoHerding()) {
            if (!(livingdata instanceof HerdData)) return new HerdData(this);
            this.addToHerdLeader(((HerdData)livingdata).herdLeader);
        }
        return livingdata;
    }

    private int levelAddFromDifficulty() {
        int easyIncrement = 0, normalIncrement = 0, hardIncrement = 0;
        for (String diffString : GeneralConfig.difficultyIncrement) {
            int getColon = diffString.indexOf(":");
            int increment = Integer.parseInt(diffString.substring(getColon + 1));
            switch (EnumDifficulty.valueOf(diffString.substring(0, getColon))) {
                case EASY:
                    easyIncrement = increment;
                    break;
                case NORMAL:
                    normalIncrement = increment;
                    break;
                case HARD:
                    hardIncrement = increment;
                    break;
            }
        }

        switch (this.world.getDifficulty()) {
            case EASY:
                return easyIncrement;
            case NORMAL:
                return easyIncrement + normalIncrement;
            case HARD:
                return easyIncrement + normalIncrement + hardIncrement;
        }
        return 0;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //disable default growth system
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        if (!this.world.isRemote) {
            this.setScaleForAge(false);
            this.setHasTarget(this.getAttackTarget() != null);
            this.setAgeInTicks(this.getAgeInTicks() + 1);
            this.manageAttributes();
            this.manageDiscoveryByPlayer();
            this.manageMoveAndWeaponCooldown();
            this.hasNoTargetManagement();
            this.manageCloaking();
            if (this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.NOCTURNAL)) this.manageSleepSchedule();
            if (this.canBePregnant()) this.createBabyWhenPregnant();

            //move conditions ticking, mainly for moves that dont require target to use
            this.manageMovesThatDontNeedTarget();

            if (this.isTamed()) {
                this.updateEnergyMove();

                this.lowEnergyEffects();
                this.manageLoveCooldown();
                if (GeneralConfig.creatureEatFromInventory) this.eatFromInventory();
                this.manageSittingFromEnergy();
                if (this.isBeingRidden()) this.informRiderEnergy();
                if (this.canNaturalRegen()) {
                    if (this.getHealth() < this.getMaxHealth() && this.getHealth() > 0 && GeneralConfig.naturalCreatureRegen) this.naturalRegen();
                    else this.healthRegen = 0;
                }
                this.manageXPAndLevel();
                if (!this.hasTarget() && this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE && !this.creatureBoxWithinReach()) {
                    BlockPos teleportPos = RiftTileEntityCreatureBoxHelper.creatureCreatureSpawnPoint(this.getHomePos(), this.world, this);
                    if (teleportPos != null) this.setPosition(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ());
                }
                //when creature is deployed from party and is visible on the quick summon/dismiss widget
                //update when health, energy, or xp is at a certain percent threshold
                this.updateForPartyHUD();
            }
            else {
                if (this.canDoHerding()) this.manageHerding();
            }
        }
        else {
            this.setControls();

            //set sprinting based on if rider is sprinting
            RiftMessages.WRAPPER.sendToServer(new RiftSetSprinting(this, this.getControllingPassenger() != null && this.getControllingPassenger().isSprinting()));
        }
        if (this.isBurrowing()) this.manageBurrowingEffects();

        //deal with hitbox and creature removal upon being dismissed
        if (this.getDeploymentType() != PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                && this.getDeploymentType() != PlayerTamedCreatures.DeploymentType.BASE_INACTIVE) {
            this.updateParts();
            this.ticksSinceHitboxRemoval = 0;
        }
        else {
            this.deleteAllHitboxes();
            this.ticksSinceHitboxRemoval++;
            if (this.ticksSinceHitboxRemoval > 2 && !this.world.isRemote) {
                this.world.removeEntity(this);
            }
        }
        this.manageGrabVictim();
    }

    private void updateForPartyHUD() {
        //update health
        int currentHealthBucket = (int)((this.getHealth() / this.getMaxHealth()) * 10);
        if (currentHealthBucket != this.cachedHealthBucket) {
            this.cachedHealthBucket = currentHealthBucket;
            PlayerTamedCreaturesHelper.updateIndividualPartyMemClient((EntityPlayer) this.getOwner(), this);
        }

        //energy
        int energyBucket = (int)(((float) this.getEnergy() / this.getMaxEnergy()) * 10);
        if (energyBucket != this.cachedEnergyBucket) {
            this.cachedEnergyBucket = energyBucket;
            PlayerTamedCreaturesHelper.updateIndividualPartyMemClient((EntityPlayer) this.getOwner(), this);
        }

        //xp
        int xpBucket = (int)(((float) this.getXP() / this.getMaxXP()) * 10);
        if (xpBucket != this.cachedXPBucket) {
            this.cachedXPBucket = xpBucket;
            PlayerTamedCreaturesHelper.updateIndividualPartyMemClient((EntityPlayer) this.getOwner(), this);
        }
    }

    @SideOnly(Side.CLIENT)
    private void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (this.isBeingRidden() && this.getControllingPassenger().equals(player)) {
            //thru server events, the 2nd move, which is activated via right click, is disabled
            //this line here ensures that when right click is released, the 2nd move can be
            //used again
            if (!this.canUseRightClick()
                    && !settings.keyBindUseItem.isKeyDown()
                    && this.currentCreatureMove() == null) RiftMessages.WRAPPER.sendToServer(new RiftCanUseMoveTriggerButton(this, 1, true));

            //for using large weapons
            if (this.creatureType.canHoldLargeWeapon
                    && this.getLargeWeapon() != RiftLargeWeaponType.NONE
                    && player.getHeldItemMainhand().getItem() == RiftItems.COMMAND_CONSOLE) {
                RiftMessages.WRAPPER.sendToServer(new RiftManualUseLargeWeapon(this, settings.keyBindAttack.isKeyDown() && this.getLargeWeaponCooldown() == 0));
            }
            //for using moves and controlling movement
            else {
                boolean leftClickOnly = settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown() && this.canUseLeftClick();
                boolean rightClickOnly = !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown() && this.canUseRightClick();
                boolean middleClickOnly = !settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && settings.keyBindPickBlock.isKeyDown() && this.canUseMiddleClick();
                boolean jump = settings.keyBindJump.isKeyDown();
                boolean descend = RiftControls.descend.isKeyDown();
                boolean toggleBetweenAttackOrBreak = RiftControls.toggleAttackOrBlockBreak.isKeyDown();

                //for using jump in navigating
                if (this instanceof RiftWaterCreature) {
                    RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, jump, descend));
                }

                //for switching between attack or block break
                RiftMessages.WRAPPER.sendToServer(new RiftManageBlockBreakControl(this, toggleBetweenAttackOrBreak));

                //holding certain items will prevent mouse related moves from being used
                if (RiftUtil.itemCanOverrideMoveControls(((EntityPlayer)this.getControllingPassenger()).getHeldItemMainhand().getItem()))
                    return;

                //for using moves
                if (leftClickOnly && this.getMoveOneCooldown() == 0)
                    this.playerUseMove(0);
                else if (rightClickOnly && this.getMoveTwoCooldown() == 0)
                    this.playerUseMove(1);
                else if (middleClickOnly && this.getMoveThreeCooldown() == 0)
                    this.playerUseMove(2);
                else
                    this.playerUseMove(-1);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void playerUseMove(int control) {
        //check if move is available
        boolean canUse = false;
        switch (control) {
            case -1:
                canUse = true;
                break;
            case 0:
                canUse = !this.getLearnedMoves().isEmpty();
                break;
            case 1:
                canUse = this.getLearnedMoves().size() >= 2;
                break;
            case 2:
                canUse = this.getLearnedMoves().size() >= 3;
                break;
        }

        //move usability based on move charge type
        if (canUse) {
            CreatureMove creatureMoveToSend = control >= 0 ? this.getLearnedMoves().get(control) : null;
            RiftMessages.WRAPPER.sendToServer(new RiftManualUseMove(this, control, creatureMoveToSend));
        }
    }

    private void manageMoveAndWeaponCooldown() {
        if (this.getMoveOneCooldown() > 0) this.setMoveOneCooldown(this.getMoveOneCooldown() - 1);
        if (this.getMoveTwoCooldown() > 0) this.setMoveTwoCooldown(this.getMoveTwoCooldown() - 1);
        if (this.getMoveThreeCooldown() > 0) this.setMoveThreeCooldown(this.getMoveThreeCooldown() - 1);
        if (this.getLargeWeaponCooldown() > 0) this.setLargeWeaponCooldown(this.getLargeWeaponCooldown() - 1);
    }

    //checks for large weapon use start here
    public boolean canFireLargeWeapon() {
        switch (this.getLargeWeapon()) {
            case CANNON:
                return this.canFireCannon();
            case MORTAR:
                return this.canFireMortar();
            case CATAPULT:
                return this.canFireCatapult();
        }
        return false;
    }

    private boolean canFireCannon() {
        EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
        if (rider == null) return false;
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CANNONBALL)) {
                    flag1 = true;
                    break;
                }
            }
        }
        return flag1 || flag2;
    }

    private boolean canFireMortar() {
        EntityPlayer rider = (EntityPlayer)this.getControllingPassenger();
        if (rider == null) return false;
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.MORTAR_SHELL)) {
                    flag1 = true;
                    break;
                }
            }
        }
        return flag1 || flag2;
    }

    private boolean canFireCatapult() {
        EntityPlayer rider = (EntityPlayer)this.getControllingPassenger();
        if (rider == null) return false;
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CATAPULT_BOULDER)) {
                    flag1 = true;
                    break;
                }
            }
        }
        return flag1 || flag2;
    }
    //checks for large weapon use end here

    private void manageDiscoveryByPlayer() {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(this.posX - 12, this.posY - 8, this.posZ - 12, this.posX + 12, this.posY + 8, this.posZ + 12);
        for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, axisAlignedBB, new Predicate<EntityPlayer>() {
            @Override
            public boolean apply(@Nullable EntityPlayer player) {
                return !PlayerJournalProgressHelper.getUnlockedCreatures(player).containsKey(creatureType);
            }
        })) {
            if (this.creatureType.isTameable) {
                PlayerJournalProgressHelper.discoverCreature(player, this.creatureType);
                player.sendStatusMessage(new TextComponentTranslation("reminder.discovered_journal_entry", this.creatureType.getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
            }
            else {
                PlayerJournalProgressHelper.unlockCreature(player, this.creatureType);
                player.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", this.creatureType.getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
            }
        }
    }

    private void manageXPAndLevel() {
        if (this.getXP() >= this.getMaxXP() && this.getLevel() < 100) {
            int tempXp = this.getXP() - this.getMaxXP();
            this.setXP(tempXp);
            this.setLevel(this.getLevel() + 1);
            ((EntityPlayer)(this.getOwner())).sendStatusMessage(new TextComponentTranslation("reminder.level_up", this.getName(false), this.getLevel()), false);
        }
    }

    private void naturalRegen() {
        if (this.hurtTime <= 0) {
            if (this.healthRegen <= 100) this.healthRegen++;
            else {
                this.healthRegen = 0;
                this.heal(2f);
            }
        }
        else this.healthRegen = 0;
    }

    @Override
    public void setScaleForAge(boolean child) {
        float scale = RiftUtil.clamp(Math.min((0.75f/24000f) * (this.getAgeInTicks() - 24000f) + 1f, 1f), 0.25f, 1f);
        this.setScale(scale);
    }

    private void updateEnergyMove() {
        //when sprinting and not using a move, energy reduces every 1 second when sprinting
        if (this.isMoving(false)
                && this.currentCreatureMove() == null
                && this.isSprinting()) {
            this.energyMod++;
            this.energyRegenMod = 0;
            this.energyRegenModDelay = 0;

            if (this.energyMod >= 20) {
                this.setEnergy(Math.max(this.getEnergy() - 1, 0));
                this.energyMod = 0;
            }
        }
        //when idle and not using a move, energy regenerates
        else if (!this.isMoving(false) && this.currentCreatureMove() == null) {
            this.energyMod = 0;
            //delay for energy regeneration immediately upon being idle
            if (this.energyRegenModDelay < 20) this.energyRegenModDelay++;
            //once delay is over the regen starts
            else this.energyRegenMod++;

            //regen energy every 1 second
            if (this.energyRegenModDelay >= 20 && this.energyRegenMod >= this.creatureType.energyRechargeSpeed()) {
                this.setEnergy(Math.min(this.getEnergy() + 1, this.getMaxEnergy()));
                this.energyRegenMod = 0;
            }
        }
    }

    private void lowEnergyEffects() {
        if (this.getEnergy() > 0 && this.getEnergy() <= this.getWeaknessEnergy()) this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 2));
        else if (this.getEnergy() == 0) this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 255));
    }

    private void manageLoveCooldown() {
        if (this.getLoveCooldown() > 0) this.setLoveCooldown(this.getLoveCooldown() - 1);
    }

    private void eatFromInventory() {
        int minSlot = this.creatureType.gearSlotCount();
        if (this.getHealth() < this.getMaxHealth()) {
            this.eatFromInvCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (this.isFavoriteFood(itemInSlot) && this.eatFromInvCooldown > 60  && !this.isEnergyRegenItem(itemInSlot)) {
                    this.eatFoodForHealing(itemInSlot);
                    this.eatFromInvCooldown = 0;
                }
            }
        }
        else this.eatFromInvCooldown = 0;

        if (this.getEnergy() < this.getMaxEnergy()) {
            this.eatFromInvForEnergyCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (this.isEnergyRegenItem(itemInSlot) && this.eatFromInvForEnergyCooldown > 60) {
                    this.eatFoodForEnergyRegen(itemInSlot);
                    this.eatFromInvForEnergyCooldown = 0;
                }
            }
        }
        else this.eatFromInvForEnergyCooldown = 0;

        if (this.isBaby() && this.getHealth() == this.getMaxHealth()) {
            this.eatFromInvForGrowthCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (this.isFavoriteFood(itemInSlot) && this.eatFromInvForGrowthCooldown > 60  && !this.isEnergyRegenItem(itemInSlot)) {
                    this.setAgeInTicks(this.getAgeInTicks() + this.getFavoriteFoodGrowth(itemInSlot));
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    this.spawnItemCrackParticles(itemInSlot.getItem());
                    itemInSlot.setCount(itemInSlot.getCount() - 1);
                    this.eatFromInvForGrowthCooldown = 0;
                }
            }
        }
        else this.eatFromInvForGrowthCooldown = 0;
    }

    protected void manageSittingFromEnergy() {
        if (this.getEnergy() <= this.getWeaknessEnergy()
            && !this.isSitting()
            && this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) this.setSitting(true);
        if (this.getEnergy() > this.getWeaknessEnergy()
            && this.isSitting()
            && this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) this.setSitting(false);
    }

    private void informRiderEnergy() {
        if (!this.informLowEnergy && this.getEnergy() <= this.getWeaknessEnergy() && this.getEnergy() > 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.low_energy", this.getName()), false);
            this.informLowEnergy = true;
        }
        if (this.informLowEnergy && this.getEnergy() > this.getWeaknessEnergy()) {
            this.informLowEnergy = false;
        }
        if (!this.informNoEnergy && this.getEnergy() == 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.no_energy", this.getName()), false);
            this.informNoEnergy = true;
        }
        if (this.informNoEnergy && this.getEnergy() > 0) this.informNoEnergy = false;
    }

    private void instaRegenAfterNoTarget() {
        if (this.getAttackTarget() == null) {
            this.resetEnergyTick++;
            if (this.resetEnergyTick >= 300) {
                this.setEnergy(this.getMaxEnergy());
                this.resetEnergyTick = 0;
            }
        }
        else this.resetEnergyTick = 0;
    }

    private void hasNoTargetManagement() {
        if (this.getAttackTarget() == null) {
            this.setCanWarn(true);

            if (!this.isTamed() && this.getEnergy() < this.getMaxEnergy()) {
                this.resetEnergyTick++;
                if (this.resetEnergyTick >= 300) {
                    this.setEnergy(this.getMaxEnergy());
                    this.resetEnergyTick = 0;
                }
            }
        }
        else this.resetEnergyTick = 0;
    }

    public void attackEntityAsMobEffects(Entity entityIn) {}

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) {
            this.applyEnchantments(this, entityIn);
            this.attackEntityAsMobEffects(entityIn);
        }
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    public boolean attackEntityAsMobWithAdditionalDamage(Entity entityIn, float damageAdditional) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()) + damageAdditional);
        if (flag) {
            this.applyEnchantments(this, entityIn);
            this.attackEntityAsMobEffects(entityIn);
        }
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    public boolean attackEntityAsMobWithMultiplier(Entity entityIn, float multiplier) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()) * multiplier);
        if (flag) {
            this.applyEnchantments(this, entityIn);
            this.attackEntityAsMobEffects(entityIn);
        }
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed();
    }

    @Override
    public int getExperiencePoints(EntityPlayer player) {
        return this.experienceValue;
    }

    @Override
    public String getName() {
        return this.getName(true);
    }

    public String getName(boolean includeLevel) {
        if (this.hasCustomName()) return this.getCustomNameTag() + (includeLevel ? " ("+ new TextComponentTranslation("tametrait.level", this.getLevel()).getFormattedText()+")" : "");
        else {
            String s = EntityList.getEntityString(this);
            if (s == null) s = "generic";
            return new TextComponentTranslation("entity." + s + ".name").getFormattedText() + (includeLevel ? " ("+ new TextComponentTranslation("tametrait.level", this.getLevel()).getFormattedText()+")" : "");
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        super.processInteract(player, hand);
        if (this.isTamed()) {
            if (this.getOwner() != null) {
                if (this.isOwner(player) && !this.isPassenger(player)) {
                    if (this.isFavoriteFood(itemstack) && !itemstack.isEmpty() && this.isBaby() && this.getHealth() == this.getMaxHealth()) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setAgeInTicks(this.getAgeInTicks() + this.getFavoriteFoodGrowth(itemstack));
                        this.showGrowthParticles();
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if (this.isFavoriteFood(itemstack) && !this.isEnergyRegenItem(itemstack) && this.getHealth() < this.getMaxHealth()) {
                        this.consumeItemFromStack(player, itemstack);
                        this.heal((float) this.getFavoriteFoodHeal(itemstack));
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if ((this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL) && this.getHealth() >= this.getMaxHealth() && !this.isBaby() && this.getLoveCooldown() == 0 && !this.isSitting() && (PlayerTamedCreaturesHelper.canAddToParty(player) || PlayerTamedCreaturesHelper.canAddCreatureToBox(player))) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setInLove(player);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if ((this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL) && this.getHealth() >= this.getMaxHealth() && !this.isBaby() && this.getLoveCooldown() == 0 && !this.isSitting() && !PlayerTamedCreaturesHelper.canAddToParty(player) && !PlayerTamedCreaturesHelper.canAddCreatureToBox(player)) {
                        player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_breed_more_creatures"), false);
                    }
                    else if (this.isEnergyRegenItem(itemstack) && this.getEnergy() < this.getMaxEnergy()) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setEnergy(Math.min(this.getEnergy() + this.getEnergyRegenItemValue(itemstack), this.getMaxEnergy()));
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if (itemstack.getItem() instanceof ItemPotion && !(itemstack.getItem() instanceof ItemSplashPotion) && !(itemstack.getItem() instanceof ItemLingeringPotion)) {
                        for (PotionEffect effect : PotionUtils.getEffectsFromStack(itemstack)) {
                            this.addPotionEffect(new PotionEffect(effect));
                        }
                        this.consumeItemFromStack(player, itemstack);
                        if (itemstack.isEmpty()) player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
                        else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                    else if (itemstack.getItem() == Items.MILK_BUCKET && !this.getActivePotionEffects().isEmpty()) {
                        this.curePotionEffects(itemstack);
                        this.consumeItemFromStack(player, itemstack);
                        if (itemstack.isEmpty()) player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                        else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.BUCKET))) player.dropItem(new ItemStack(Items.BUCKET), false);
                    }
                    else if (itemstack.isEmpty() && !this.isSaddled()) {
                        if (this.canBePregnant()) {
                            if (this.isPregnant() && player.isSneaking()) player.openGui(RiftInitialize.instance, RiftGui.GUI_EGG, world, this.getEntityId() ,0, 0);
                            else player.openGui(RiftInitialize.instance, RiftGui.GUI_DIAL, world, this.getEntityId(), 0, 0);
                        }
                        else player.openGui(RiftInitialize.instance, RiftGui.GUI_DIAL, world, this.getEntityId() ,0, 0);
                    }
                    else if (itemstack.isEmpty() && this.isSaddled() && !player.isSneaking() && !this.isSleeping() && (!this.canEnterTurretMode() || !this.isTurretMode()) && !this.getDeploymentType().equals(PlayerTamedCreatures.DeploymentType.BASE)) {
                        if (this.canBePregnant()) {
                            if (!this.isPregnant()) RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                            else player.openGui(RiftInitialize.instance, RiftGui.GUI_EGG, world, this.getEntityId() ,0, 0);
                        }
                        else if ((this instanceof IWorkstationUser) || (this instanceof ILeadWorkstationUser)) {
                            boolean usingWorkstation = this instanceof IWorkstationUser && ((IWorkstationUser) this).hasWorkstation();
                            boolean usingLeadForWork = this instanceof ILeadWorkstationUser && ((ILeadWorkstationUser) this).isUsingLeadForWork();
                            if (!usingWorkstation && !usingLeadForWork) RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                        }
                        else RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                    }
                    else if (itemstack.isEmpty() && this.isSaddled() && player.isSneaking()) {
                        player.openGui(RiftInitialize.instance, RiftGui.GUI_DIAL, world, this.getEntityId() ,0, 0);
                    }
                }
                else if (!this.isOwner(player)) {
                    player.sendStatusMessage(new TextComponentTranslation("reminder.not_creature_owner", this.getOwner().getName()), false);
                }
            }
            return true;
        }
        else if (this.isTamed()) {
            if (!itemstack.isEmpty() && itemstack.getItem().equals(RiftItems.REVIVAL_MIX)) {
                this.consumeItemFromStack(player, itemstack);
                this.setHealth((float)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue()/4f);
                return true;
            }
        }
        else if (!this.isTamed() && !this.isSleeping()) {
            if (!itemstack.isEmpty() && (this.creatureType != RiftCreatureType.DODO) && (this.creatureType.isTameableByFeeding && this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL) && (PlayerTamedCreaturesHelper.canAddToParty(player) || PlayerTamedCreaturesHelper.canAddCreatureToBox(player)) && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                if (this.getTamingFoodAdd(itemstack) + this.getTameProgress() >= 100) {
                    this.consumeItemFromStack(player, itemstack);
                    this.tameCreature(player);
                }
                else {
                    this.consumeItemFromStack(player, itemstack);
                    this.setTameProgress(this.getTameProgress() + this.getTamingFoodAdd(itemstack));
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    this.spawnItemCrackParticles(itemstack.getItem());
                    player.sendStatusMessage(new TextComponentTranslation("reminder.creature_wants_more", this.creatureType.getTranslatedName()), false);
                }
                return true;
            }
            else if (!itemstack.isEmpty() && (this.creatureType != RiftCreatureType.DODO) && (this.creatureType.isTameableByFeeding && this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL) && !PlayerTamedCreaturesHelper.canAddToParty(player) && !PlayerTamedCreaturesHelper.canAddCreatureToBox(player) && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                player.sendStatusMessage(new TextComponentTranslation("reminder.cannot_tame_more_creatures"), false);
                return true;
            }
            else if (!itemstack.isEmpty() && (this.creatureType == RiftCreatureType.DODO) && this.isBaby() && this.isFavoriteFood(itemstack)) {
                this.consumeItemFromStack(player, itemstack);
                this.setAgeInTicks(this.getAgeInTicks() + this.getFavoriteFoodGrowth(itemstack));
                this.showGrowthParticles();
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.spawnItemCrackParticles(itemstack.getItem());
                return true;
            }
            else if (!itemstack.isEmpty() && this.creatureType == RiftCreatureType.DODO && !this.isBaby() && this.getLoveCooldown() == 0 && (this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL)) {
                this.consumeItemFromStack(player, itemstack);
                this.setInLove(player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.spawnItemCrackParticles(itemstack.getItem());
                return true;
            }
        }
        return false;
    }

    public void tameCreature(EntityPlayer player) {
        this.spawnHeartParticles();
        this.setTameProgress(0);
        this.setTamedBy(player);
        this.setAttackTarget(null);
        this.setAcquisitionInfo(CreatureAcquisitionInfo.AcquisitionMethod.TAMED_FROM_WILD, System.currentTimeMillis() / 1000L);
        if (this.isBaby()) this.setTameBehavior(TameBehaviorType.PASSIVE);

        this.world.setEntityState(this, (byte)7);

        if (!this.world.isRemote) {
            //update journal
            if (PlayerJournalProgressHelper.getUnlockedCreatures(player).containsKey(this.creatureType) && !PlayerJournalProgressHelper.getUnlockedCreatures(player).get(this.creatureType)) {
                PlayerJournalProgressHelper.unlockCreature(player, this.creatureType);
                player.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", this.creatureType.getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
            }

            //update party of player that tamed the creature
            if (PlayerTamedCreaturesHelper.canAddToParty(player)) {
                this.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                PlayerTamedCreaturesHelper.addCreatureToParty(player, this);
                player.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished_to_party", new TextComponentString(this.getName())), false);
            }
            //update box of player that tamed the creature
            else if (PlayerTamedCreaturesHelper.canAddCreatureToBox(player)) {
                this.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                PlayerTamedCreaturesHelper.addCreatureToBox(player, this);
                player.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished_to_box", new TextComponentString(this.getName())), false);
            }
        }

        this.enablePersistence();
    }

    public void setAcquisitionInfo(CreatureAcquisitionInfo.AcquisitionMethod method, long time) {
        this.dataManager.set(ACQUISITION_INFO, new CreatureAcquisitionInfo(method, time));
    }

    public void setAcquisitionInfo(CreatureAcquisitionInfo value) {
        this.dataManager.set(ACQUISITION_INFO, value);
    }

    public CreatureAcquisitionInfo getAcquisitionInfo() {
        return this.dataManager.get(ACQUISITION_INFO);
    }

    protected void manageAttributes() {
        double healthValue = ((this.maxCreatureHealth - this.minCreatureHealth)/24000D) * (this.getAgeInTicks() - 24000D) + this.maxCreatureHealth;
        double baseHealthValue = RiftUtil.clamp(Math.floor(healthValue), this.minCreatureHealth, this.maxCreatureHealth);

        double leveledHealthValue = baseHealthValue + (this.healthLevelMultiplier) * (this.getLevel() - 1) * baseHealthValue;
        double leveledDamageValue = this.attackDamage + (double)Math.round((this.getLevel() - 1) * this.damageLevelMultiplier);

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(leveledHealthValue);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(leveledDamageValue * this.damageMultiplier);

        if (this.justSpawned()) {
            this.setLearnedMoves(this.possibleStartingMoves().next());

            //initialize learnable moves
            List<CreatureMove> learnableMoves = this.initialLearnableMoves();
            //remove learnable moves already learnt by user
            learnableMoves.removeAll(this.getLearnedMoves());
            this.setLearnableMoves(learnableMoves);
            this.heal((float) (this.maxCreatureHealth + (0.1D) * (this.getLevel() - 1) * this.maxCreatureHealth));
            this.setSpeed(this.speed);
            this.setWaterSpeed(this.waterSpeed);
            this.setJustSpawned(false);
        }
    }

    public void changeAttackByMultiplier(double value) {
        this.damageMultiplier = value;
    }

    public boolean isFavoriteFood(ItemStack stack) {
        boolean flag = false;
        for (RiftCreatureConfig.Food food : this.favoriteFood) {
            if (!flag) flag = RiftUtil.itemStackEqualToString(stack, food.itemId);
        }
        return flag;
    }

    public int getFavoriteFoodHeal(ItemStack stack) {
        RiftCreatureConfig.Food foodToHeal = new RiftCreatureConfig.Food("", 0);
        boolean flag = false;
        for (RiftCreatureConfig.Food food : this.favoriteFood) {
            if (!flag) {
                flag = RiftUtil.itemStackEqualToString(stack, food.itemId);
                foodToHeal = food;
            }
        }
        if (flag) return (int)Math.ceil(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * foodToHeal.percentageHeal);
        return 0;
    }

    public boolean isTamingFood(ItemStack stack) {
        boolean flag = false;
        if (this.tamingFood != null) {
            for (RiftCreatureConfig.Meal meal : this.tamingFood) {
                if (!flag) flag = RiftUtil.itemStackEqualToString(stack, meal.itemId);
            }
        }
        else if (this.breedingFood != null) {
            for (String food : this.breedingFood) {
                if (!flag) flag = RiftUtil.itemStackEqualToString(stack, food);
            }
        }
        return flag;
    }

    public int getTamingFoodAdd(ItemStack stack) {
        if (stack.getItem() == RiftItems.CREATIVE_MEAL) return 100;
        else {
            RiftCreatureConfig.Meal mealToTame = new RiftCreatureConfig.Meal("", 0);
            boolean flag = false;
            if (this.tamingFood != null) {
                for (RiftCreatureConfig.Meal meal : this.tamingFood) {
                    if (!flag) {
                        flag = RiftUtil.itemStackEqualToString(stack, meal.itemId);
                        mealToTame = meal;
                    }
                }
                if (flag) {
                    int levelMod = (int)Math.ceil((double)this.getLevel() / 10D);
                    int adder = (int)(mealToTame.tameMultiplier * 100);
                    return adder / levelMod;
                }
            }
            return 0;
        }
    }

    public int getFavoriteFoodGrowth(ItemStack stack) {
        RiftCreatureConfig.Food foodToGrow = new RiftCreatureConfig.Food("", 0);
        boolean flag = false;
        for (RiftCreatureConfig.Food food : this.favoriteFood) {
            if (!flag) {
                flag = RiftUtil.itemStackEqualToString(stack, food.itemId);
                foodToGrow = food;
            }
        }
        if (flag) return (int)Math.ceil(24000 * foodToGrow.percentageHeal);
        return 0;
    }

    public boolean isEnergyRegenItem(ItemStack stack) {
        RiftCreatureType.CreatureDiet diet = this.creatureType.getCreatureDiet();
        List<String> itemList = new ArrayList<>();
        if (diet == RiftCreatureType.CreatureDiet.HERBIVORE || diet == RiftCreatureType.CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.CARNIVORE || diet == RiftCreatureType.CreatureDiet.PISCIVORE || diet == RiftCreatureType.CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.OMNIVORE) {
            itemList = new ArrayList<>(Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods));
            itemList.addAll(Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods));
        }

        for (String foodItem : itemList) {
            int first = foodItem.indexOf(":");
            int second = foodItem.indexOf(":", first + 1);
            int third = foodItem.indexOf(":", second + 1);
            String itemId = foodItem.substring(0, second);
            int itemData = Integer.parseInt(foodItem.substring(second + 1, third));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId)) && (itemData == -1 || itemData == stack.getMetadata())) return true;
        }

        return false;
    }

    public int getEnergyRegenItemValue(ItemStack stack) {
        RiftCreatureType.CreatureDiet diet = this.creatureType.getCreatureDiet();
        List<String> itemList = new ArrayList<>();
        if (diet == RiftCreatureType.CreatureDiet.HERBIVORE || diet == RiftCreatureType.CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.CARNIVORE || diet == RiftCreatureType.CreatureDiet.PISCIVORE || diet == RiftCreatureType.CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);
        else if (diet == RiftCreatureType.CreatureDiet.OMNIVORE) {
            itemList = new ArrayList<>(Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods));
            itemList.addAll(Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods));
        }

        for (String itemEntry : itemList) {
            int first = itemEntry.indexOf(":");
            int second = itemEntry.indexOf(":", first + 1);
            int third = itemEntry.indexOf(":", second + 1);
            String itemId = itemEntry.substring(0, second);
            int itemData = Integer.parseInt(itemEntry.substring(second + 1, third));
            if (stack.getItem().equals(Item.getByNameOrId(itemId)) && (itemData == -1 || itemData == stack.getMetadata())) {
                return Integer.parseInt(itemEntry.substring(third + 1));
            }
        }
        return 0;
    }

    public void eatFoodForHealing(ItemStack itemStack) {
        this.heal((float) this.getFavoriteFoodHeal(itemStack));
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
        this.spawnItemCrackParticles(itemStack.getItem());
        itemStack.setCount(itemStack.getCount() - 1);
    }

    public void eatFoodForEnergyRegen(ItemStack itemStack) {
        this.setEnergy(Math.min(this.getEnergy() + this.getEnergyRegenItemValue(itemStack), this.getMaxEnergy()));
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
        this.spawnItemCrackParticles(itemStack.getItem());
        itemStack.setCount(itemStack.getCount() - 1);
    }

    private void showGrowthParticles() {
        double motionX = getRNG().nextGaussian() * 0.07D;
        double motionY = getRNG().nextGaussian() * 0.07D;
        double motionZ = getRNG().nextGaussian() * 0.07D;
        float f = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX) + this.getEntityBoundingBox().minX);
        float f1 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) + this.getEntityBoundingBox().minY);
        float f2 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ) + this.getEntityBoundingBox().minZ);
        if (world.isRemote) this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, f, f1, f2, motionX, motionY, motionZ);
    }

    public void spawnItemCrackParticles(Item item) {
        for (int i = 0; i < 15; i++) {
            double motionX = getRNG().nextGaussian() * 0.07D;
            double motionY = getRNG().nextGaussian() * 0.07D;
            double motionZ = getRNG().nextGaussian() * 0.07D;
            float f = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX) + this.getEntityBoundingBox().minX);
            float f1 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) + this.getEntityBoundingBox().minY);
            float f2 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ) + this.getEntityBoundingBox().minZ);
            if (this.world.isRemote) {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, f, f1, f2, motionX, motionY, motionZ, Item.getIdFromItem(item));
            }
        }
    }

    public void spawnHeartParticles() {
        EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;
        for (int i = 0; i < 7; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            if (this.world.isRemote) {
                this.world.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
            }
        }
    }

    public ItemStack saddleItemStack() {
        return RiftUtil.getItemStackFromString(this.saddleItem);
    }

    public boolean itemStackIsLargeWeapon(ItemStack itemStack) {
        return itemStack.getItem() == RiftItems.CANNON || itemStack.getItem() == RiftItems.MORTAR || itemStack.getItem() == RiftItems.CATAPULT;
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        if (!this.isTamed() && this.canPickUpLoot()) {
            ItemStack itemstack = itemEntity.getItem();
            EntityEquipmentSlot entityequipmentslot = getSlotForItemStack(itemstack);

            if (this.isFavoriteFood(itemstack) && this.canEquipItem(itemstack)) {
                this.setItemStackToSlot(entityequipmentslot, new ItemStack(Items.AIR));
                this.onItemPickup(itemEntity, itemstack.getCount());
                itemEntity.setDead();
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Level", this.getLevel());
        compound.setInteger("XP", this.getXP());
        compound.setInteger("LoveCooldown", this.getLoveCooldown());
        compound.setInteger("Variant", this.getVariant());
        compound.setByte("CreatureType", (byte) this.creatureType.ordinal());
        compound.setByte("TameBehavior", (byte) this.getTameBehavior().ordinal());
        compound.setBoolean("Saddled", this.isSaddled());
        compound.setByte("LargeWeapon", (byte) this.getLargeWeapon().ordinal());
        if (this.creatureInventory != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.creatureInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.creatureInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.setByte("Slot", (byte) i);
                    itemstack.writeToNBT(nbttagcompound);
                    nbttaglist.appendTag(nbttagcompound);
                }
            }
            compound.setTag("Items", nbttaglist);
        }
        compound.setInteger("Energy", this.getEnergy());
        compound.setBoolean("HasTarget", this.hasTarget());
        compound.setInteger("AgeTicks", this.getAgeInTicks());
        compound.setBoolean("JustSpawned", this.justSpawned());
        compound.setInteger("TameProgress", this.getTameProgress());
        compound.setBoolean("HasHomePos", this.getHasHomePos());
        if (this.getHasHomePos()) {
            compound.setInteger("HomePosX", this.getHomePos().getX());
            compound.setInteger("HomePosY", this.getHomePos().getY());
            compound.setInteger("HomePosZ", this.getHomePos().getZ());
        }
        compound.setInteger("DeploymentType", this.getDeploymentType().ordinal());
        compound.setInteger("BoxReviveTime", this.boxReviveTime);

        //for moves
        NBTTagList nbttaglist = new NBTTagList();
        for (CreatureMove learnedMove : this.getLearnedMoves()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("Move", learnedMove.ordinal());
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("LearnedMoves", nbttaglist);

        //for learnable moves
        NBTTagList learnableMovesTagList = new NBTTagList();
        for (CreatureMove learnableMove : this.getLearnableMoves()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("Move", learnableMove.ordinal());
            learnableMovesTagList.appendTag(nbttagcompound);
        }
        compound.setTag("LearnableMoves", learnableMovesTagList);

        compound.setInteger("CooldownMoveOne", this.getMoveOneCooldown());
        compound.setInteger("CooldownMoveTwo", this.getMoveTwoCooldown());
        compound.setInteger("CooldownMoveThree", this.getMoveThreeCooldown());
        //for pregnancy
        if (this.canBePregnant()) {
            compound.setInteger("PregnancyTime", this.getPregnancyTimer());
            compound.setBoolean("IsPregnancy", this.isPregnant());
        }
        //for turret mode
        compound.setBoolean("TurretMode", this.isTurretMode());
        compound.setByte("TurretTargeting", (byte) this.getTurretTargeting().ordinal());
        //for acquisition
        compound.setTag("AcquisitionInfo", this.getAcquisitionInfo().getNBT());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLevel(compound.getInteger("Level"));
        this.setXP(compound.getInteger("XP"));
        this.setLoveCooldown(compound.getInteger("LoveCooldown"));
        this.setVariant(compound.getInteger("Variant"));
        if (compound.hasKey("TameBehavior")) this.setTameBehavior(TameBehaviorType.values()[compound.getByte("TameBehavior")]);
        this.setSaddled(compound.getBoolean("Saddled"));
        this.setLargeWeapon(RiftLargeWeaponType.values()[compound.getByte("LargeWeapon")]);
        if (this.creatureInventory != null) {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                int inventorySize = this.slotCount() + this.creatureType.gearSlotCount();
                if (j < inventorySize) this.creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        else {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                this.initInventory();
                this.creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        this.setEnergy(compound.getInteger("Energy"));
        this.setHasTarget(compound.getBoolean("HasTarget"));
        this.setAgeInTicks(compound.getInteger("AgeTicks"));
        this.setJustSpawned(compound.getBoolean("JustSpawned"));
        this.setTameProgress(compound.getInteger("TameProgress"));
        if (compound.getBoolean("HasHomePos")) this.setHomePos(compound.getInteger("HomePosX"), compound.getInteger("HomePosY"), compound.getInteger("HomePosZ"));
        this.setDeploymentType(PlayerTamedCreatures.DeploymentType.values()[compound.getInteger("DeploymentType")]);
        this.setBoxReviveTime(compound.getInteger("BoxReviveTime"));

        //for learned moves
        NBTTagList nbtTagList = compound.getTagList("LearnedMoves", 10);
        List<CreatureMove> moveList = new ArrayList<>();
        for (int i = 0; i < nbtTagList.tagCount(); i++) {
            NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
            CreatureMove moveToAdd = CreatureMove.values()[nbttagcompound.getInteger("Move")];
            if (!this.getLearnedMoves().contains(moveToAdd)) moveList.add(moveToAdd);
        }
        this.setLearnedMoves(moveList);

        //for learnable moves
        NBTTagList learnableMovesTagList = compound.getTagList("LearnableMoves", 10);
        List<CreatureMove> learnableMoveList = new ArrayList<>();
        for (int i = 0; i < learnableMovesTagList.tagCount(); i++) {
            NBTTagCompound nbttagcompound = learnableMovesTagList.getCompoundTagAt(i);
            CreatureMove moveToAdd = CreatureMove.values()[nbttagcompound.getInteger("Move")];
            if (!this.getLearnedMoves().contains(moveToAdd)) learnableMoveList.add(moveToAdd);
        }
        this.setLearnableMoves(learnableMoveList);

        this.setMoveOneCooldown(compound.getInteger("CooldownMoveOne"));
        this.setMoveTwoCooldown(compound.getInteger("CooldownMoveTwo"));
        this.setMoveThreeCooldown(compound.getInteger("CooldownMoveThree"));
        //for pregnancy
        if (this.canBePregnant()) this.setPregnant(compound.getBoolean("IsPregnancy"), compound.getInteger("PregnancyTime"));
        //for turret mode
        this.setTurretMode(compound.getBoolean("TurretMode"));
        this.setTurretModeTargeting(TurretModeTargeting.values()[compound.getByte("TurretTargeting")]);
        //for acquisition
        NBTTagCompound acquisitionNBT = compound.getCompoundTag("AcquisitionInfo");
        this.setAcquisitionInfo(new CreatureAcquisitionInfo(acquisitionNBT));
    }

    public boolean isWarning() {
        return this.dataManager.get(WARNING);
    }

    public void setIsWarning(boolean value) {
        this.dataManager.set(WARNING, value);
    }

    public boolean canWarn() {
        return this.dataManager.get(CAN_WARN);
    }

    public void setCanWarn(boolean value) {
        this.dataManager.set(CAN_WARN, value);
    }

    //move related stuff starts here
    private void manageMovesThatDontNeedTarget() {
        if (this.proposedTargetlessMove() != null || this.currentCreatureMove() != null) return;
        for (CreatureMove move : this.getLearnedMovesNoNeedForTarget()) {
            if (this.getAttackTarget() != null && move.targetRequirement == CreatureMove.TargetRequirement.HAS_NO_TARGET) continue;

            RiftCreatureMove invokedMove = move.invokeMove();
            if (invokedMove.canBeExecutedUnmounted(this, null)) {
                this.setProposedTargetlessMove(move);
                break;
            }
        }
    }

    public abstract WeightedList<List<CreatureMove>> possibleStartingMoves();

    public List<CreatureMove> initialLearnableMoves() {
        List<List<CreatureMove>> possibleStartingMoves = this.possibleStartingMoves().possibleOutcomes();
        List<CreatureMove> toOutput = new ArrayList<>();
        for (List<CreatureMove> creatureMoves : possibleStartingMoves) {
            toOutput = RiftUtil.uniteTwoLists(toOutput, creatureMoves);
        }
        //delete duplicates
        Set<CreatureMove> toOutputNoDuplicates = new HashSet<>(toOutput);
        return new ArrayList<>(toOutputNoDuplicates);
    }

    public List<CreatureMove> getLearnedMoves() {
        return this.dataManager.get(MOVE_LIST);
    }

    public List<CreatureMove> getLearnedMovesNoNeedForTarget() {
        return this.getLearnedMoves().stream()
                .filter(m -> m.targetRequirement != CreatureMove.TargetRequirement.HAS_TARGET)
                .collect(Collectors.toList());
    }

    public List<CreatureMove> getLearnedMovesRequireTarget() {
        return this.getLearnedMoves().stream()
                .filter(m -> m.targetRequirement == CreatureMove.TargetRequirement.HAS_TARGET
                        || m.targetRequirement == CreatureMove.TargetRequirement.TARGET_DOESNT_MATTER)
                .collect(Collectors.toList());
    }

    public void changeLearnedMove(int pos, CreatureMove move) {
        List<CreatureMove> moveList = this.dataManager.get(MOVE_LIST);
        moveList.set(pos, move);
        this.setLearnedMoves(moveList);
    }

    public void setLearnedMoves(List<CreatureMove> values) {
        this.dataManager.set(MOVE_LIST, values);
    }

    public CreatureMove proposedTargetlessMove() {
        if (this.dataManager.get(PROPOSED_TARGETLESS_MOVE) >= 0) return CreatureMove.values()[this.dataManager.get(PROPOSED_TARGETLESS_MOVE)];
        else return null;
    }

    public void setProposedTargetlessMove(CreatureMove value) {
        if (value != null) this.dataManager.set(PROPOSED_TARGETLESS_MOVE, value.ordinal());
        else this.dataManager.set(PROPOSED_TARGETLESS_MOVE, -1);
    }

    public CreatureMove currentCreatureMove() {
        if (this.dataManager.get(CURRENT_MOVE) >= 0) return CreatureMove.values()[this.dataManager.get(CURRENT_MOVE)];
        else return null;
    }

    public void setCurrentCreatureMove(CreatureMove value) {
        if (value != null) this.dataManager.set(CURRENT_MOVE, value.ordinal());
        else this.dataManager.set(CURRENT_MOVE, -1);
    }

    public void setLearnableMoves(List<CreatureMove> value) {
        this.dataManager.set(LEARNABLE_MOVE_LIST, value);
    }

    public List<CreatureMove> getLearnableMoves() {
        return this.dataManager.get(LEARNABLE_MOVE_LIST);
    }

    public int getMoveUse(int pos) {
        switch (pos) {
            case 0:
                return this.getMoveOneUse();
            case 1:
                return this.getMoveTwoUse();
            case 2:
                return this.getMoveThreeUse();
        }
        return 0;
    }

    public int getMoveUse(CreatureMove value) {
        int index = this.getLearnedMoves().indexOf(value);
        if (index == -1) return 0;
        return this.getMoveUse(index);
    }

    public int getCurrentMoveUse() {
        if (this.dataManager.get(CURRENT_MOVE) < 0) return 0;
        int movePos = this.getLearnedMoves().indexOf(CreatureMove.values()[this.dataManager.get(CURRENT_MOVE)]);
        switch (movePos) {
            case 0:
                return this.getMoveOneUse();
            case 1:
                return this.getMoveTwoUse();
            case 2:
                return this.getMoveThreeUse();
        }
        return 0;
    }

    public void setMoveUse(int pos, int value) {
        if (pos < 0 || pos >= this.getLearnedMoves().size()) return;
        switch (pos) {
            case 0:
                this.setMoveOneUse(value);
                break;
            case 1:
                this.setMoveTwoUse(value);
                break;
            case 2:
                this.setMoveThreeUse(value);
                break;
        }
    }

    public void setCurrentMoveUse(int value) {
        if (this.dataManager.get(CURRENT_MOVE) < 0) return;
        int movePos = this.getLearnedMoves().indexOf(CreatureMove.values()[this.dataManager.get(CURRENT_MOVE)]);
        switch (movePos) {
            case 0:
                this.setMoveOneUse(value);
                break;
            case 1:
                this.setMoveTwoUse(value);
                break;
            case 2:
                this.setMoveThreeUse(value);
                break;
        }
    }

    public void setMoveCooldown(int moveCooldown) {
        if (this.currentCreatureMove() == null) return;
        int movePos = this.getLearnedMoves().indexOf(this.currentCreatureMove());
        this.setMoveCooldown(movePos, moveCooldown);
    }

    public void setMoveCooldown(int index, int moveCooldown) {
        switch (index) {
            case 0:
                this.setMoveOneCooldown(moveCooldown);
                break;
            case 1:
                this.setMoveTwoCooldown(moveCooldown);
                break;
            case 2:
                this.setMoveThreeCooldown(moveCooldown);
                break;
        }
    }

    public int getMoveCooldown() {
        if (this.currentCreatureMove() == null) return 0;
        return this.getMoveCooldown(this.getLearnedMoves().indexOf(this.currentCreatureMove()));
    }

    public int getMoveCooldown(CreatureMove move) {
        if (!this.getLearnedMoves().contains(move)) return 0;
        int index = this.getLearnedMoves().indexOf(move);
        switch (index) {
            case 0:
                return this.getMoveOneCooldown();
            case 1:
                return this.getMoveTwoCooldown();
            case 2:
                return this.getMoveThreeCooldown();
        }
        return 0;
    }

    public int getMoveCooldown(int index) {
        switch (index) {
            case 0:
                return this.getMoveOneCooldown();
            case 1:
                return this.getMoveTwoCooldown();
            case 2:
                return this.getMoveThreeCooldown();
        }
        return 0;
    }


    public boolean canUseMoveOne() {
        return this.dataManager.get(CAN_USE_MOVE_ONE);
    }

    public void setCanUseMoveOne(boolean value) {
        this.dataManager.set(CAN_USE_MOVE_ONE, value);
    }

    public boolean usingMoveOne() {
        return this.dataManager.get(USING_MOVE_ONE);
    }

    public void setUsingMoveOne(boolean value) {
        this.dataManager.set(USING_MOVE_ONE, value);
    }

    public int getMoveOneUse() {
        return this.dataManager.get(MOVE_ONE_USE);
    }

    public void setMoveOneUse(int value) {
        this.dataManager.set(MOVE_ONE_USE, value);
    }

    public int getMoveOneCooldown() {
        return this.dataManager.get(MOVE_ONE_COOLDOWN);
    }

    public void setMoveOneCooldown(int value) {
        this.dataManager.set(MOVE_ONE_COOLDOWN, value);
    }



    public boolean canUseMoveTwo() {
        return this.dataManager.get(CAN_USE_MOVE_TWO);
    }

    public void setCanUseMoveTwo(boolean value) {
        this.dataManager.set(CAN_USE_MOVE_TWO, value);
    }

    public boolean usingMoveTwo() {
        return this.dataManager.get(USING_MOVE_TWO);
    }

    public void setUsingMoveTwo(boolean value) {
        this.dataManager.set(USING_MOVE_TWO, value);
    }

    public int getMoveTwoUse() {
        return this.dataManager.get(MOVE_TWO_USE);
    }

    public void setMoveTwoUse(int value) {
        this.dataManager.set(MOVE_TWO_USE, value);
    }

    public int getMoveTwoCooldown() {
        return this.dataManager.get(MOVE_TWO_COOLDOWN);
    }

    public void setMoveTwoCooldown(int value) {
        this.dataManager.set(MOVE_TWO_COOLDOWN, value);
    }



    public boolean canUseMoveThree() {
        return this.dataManager.get(CAN_USE_MOVE_THREE);
    }

    public void setCanUseMoveThree(boolean value) {
        this.dataManager.set(CAN_USE_MOVE_THREE, value);
    }

    public boolean usingMoveThree() {
        return this.dataManager.get(USING_MOVE_THREE);
    }

    public void setUsingMoveThree(boolean value) {
        this.dataManager.set(USING_MOVE_THREE, value);
    }

    public int getMoveThreeUse() {
        return this.dataManager.get(MOVE_THREE_USE);
    }

    public void setMoveThreeUse(int value) {
        this.dataManager.set(MOVE_THREE_USE, value);
    }

    public int getMoveThreeCooldown() {
        return this.dataManager.get(MOVE_THREE_COOLDOWN);
    }

    public void setMoveThreeCooldown(int value) {
        this.dataManager.set(MOVE_THREE_COOLDOWN, value);
    }

    public int getPlayingChargedMoveAnim() {
        return this.dataManager.get(PLAY_CHARGED_MOVE_ANIM);
    }

    public void setPlayingChargedMoveAnim(int value) {
        this.dataManager.set(PLAY_CHARGED_MOVE_ANIM, value);
    }

    public boolean getPlayingInfiniteMoveAnim() {
        return this.dataManager.get(PLAY_INFINITE_MOVE_ANIM);
    }

    public void setPlayingInfiniteMoveAnim(boolean value) {
        this.dataManager.set(PLAY_INFINITE_MOVE_ANIM, value);
    }

    public abstract Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType();

    public boolean inBlockBreakMode() {
        return this.dataManager.get(BREAK_BLOCK_MODE);
    }

    public void setBlockBreakMode(boolean value) {
        this.dataManager.set(BREAK_BLOCK_MODE, value);
    }

    public boolean canSetBlockBreakMode() {
        return this.dataManager.get(CAN_SET_BLOCK_BREAK_MODE);
    }

    public void setCanSetBlockBreakMode(boolean value) {
        this.dataManager.set(CAN_SET_BLOCK_BREAK_MODE, value);
    }

    public boolean getUsingLargeWeapon() {
        return this.dataManager.get(USING_LARGE_WEAPON);
    }

    public void setUsingLargeWeapon(boolean value) {
        this.dataManager.set(USING_LARGE_WEAPON, value);
    }

    public int getLargeWeaponUse() {
        return this.dataManager.get(LARGE_WEAPON_USE);
    }

    public void setLargeWeaponUse(int value) {
        this.dataManager.set(LARGE_WEAPON_USE, value);
    }

    public int getLargeWeaponCooldown() {
        return this.dataManager.get(LARGE_WEAPON_COOLDOWN);
    }

    public void setLargeWeaponCooldown(int value) {
        this.dataManager.set(LARGE_WEAPON_COOLDOWN, value);
    }

    public boolean isFiringCatapult() {
        return this.dataManager.get(FIRING_CATAPULT);
    }

    public void setFiringCatapult(boolean value) {
        this.dataManager.set(FIRING_CATAPULT, value);
    }

    public EnumFacing creatureHorizontalDirection() {
        if ((this.rotationYaw >= 135 && this.rotationYaw <= 180) || (this.rotationYaw <= -135 && this.rotationYaw >= -180))
            return EnumFacing.NORTH;
        else if (this.rotationYaw >= -45 && this.rotationYaw <= 45)
            return EnumFacing.SOUTH;
        else if (this.rotationYaw > 45 && this.rotationYaw < 135)
            return EnumFacing.WEST;
        else if (this.rotationYaw > -135 && this.rotationYaw < -45)
            return EnumFacing.EAST;
        return null;
    }

    private void initInventory() {
        RiftCreatureInventory tempInventory = this.creatureInventory;
        int inventorySize = this.slotCount() + this.creatureType.gearSlotCount();
        this.creatureInventory = new RiftCreatureInventory("creatureInventory", inventorySize, this);
        this.creatureInventory.setCustomName(this.getName());
        if (tempInventory != null) {
            for (int i = 0; i < inventorySize; i++) {
                ItemStack itemStack = tempInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.creatureInventory.setInventorySlotContents(i, itemStack.copy());
                }
            }
        }
    }

    public List<String> getTargetList() {
        return this.targetList;
    }

    @Override
    public float scale() {
        return RiftUtil.setModelScale(this, this.ageScaleParams()[0], this.ageScaleParams()[1]);
    }

    public abstract float[] ageScaleParams();

    public boolean inFrustrum(ICamera camera, Entity entity) {
        return camera != null && entity != null && camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox());
    }

    @Override
    public boolean isOnLadder() {
        return this.isClimbing();
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.getCanSpawnOnBlock() && this.world.getLight(this.getPosition()) > 7 && !this.world.getBlockState(this.getPosition()).getMaterial().isLiquid() && this.testOtherCreatures();
    }

    protected boolean getCanSpawnOnBlock() {
        List<String> blockSpawn = Arrays.asList(GeneralConfig.universalSpawnBlocks);
        IBlockState belowState = this.world.getBlockState(this.getPosition().down());
        boolean flag = false;
        for (String blockVal : blockSpawn) {
            int colData = blockVal.indexOf(":", blockVal.indexOf(":") + 1);
            int blockData = Integer.parseInt(blockVal.substring(colData + 1));
            String blockName = blockVal.substring(0, colData);
            if (!flag) flag = Block.getBlockFromName(blockName) == belowState.getBlock() && (blockData == - 1 || belowState.getBlock().getMetaFromState(belowState) == blockData);
        }
        return flag;
    }

    protected boolean testOtherCreatures() {
        List<RiftCreature> creatureList = this.world.getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(64D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature input) {
                return !input.isTamed();
            }
        });
        return creatureList.size() < this.densityLimit;
    }

    protected boolean isBrightnessLevel(int min, int max) {
        return this.world.getLight(this.getPosition()) >= min && this.world.getLight(this.getPosition()) <= max;
    }

    public boolean canBreatheUnderwater() {
        return !this.headIsUnderwater();
    }

    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(this.creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE));
        ItemStack largeWeapon = this.creatureInventory.getStackInSlot(this.creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON));
        if (this.creatureType.canBeSaddled) this.setSaddled(this.saddleItemStack().getItem() == saddle.getItem() && this.saddleItemStack().getMetadata() == saddle.getMetadata() && !saddle.isEmpty());
        if (this.creatureType.canHoldLargeWeapon) {
            if (largeWeapon.isEmpty()) this.setLargeWeapon(RiftLargeWeaponType.NONE);
            else if (largeWeapon.getItem() == RiftItems.CANNON) this.setLargeWeapon(RiftLargeWeaponType.CANNON);
            else if (largeWeapon.getItem() == RiftItems.CATAPULT) this.setLargeWeapon(RiftLargeWeaponType.CATAPULT);
            else if (largeWeapon.getItem() == RiftItems.MORTAR) this.setLargeWeapon(RiftLargeWeaponType.MORTAR);
            else this.setLargeWeapon(RiftLargeWeaponType.NONE);
        }
    }

    //pregnancy related stuff starts here
    public boolean canBePregnant() {
        return this.creatureType.getCreatureCategory() == RiftCreatureType.CreatureCategory.MAMMAL && this.creatureType != RiftCreatureType.DIMETRODON;
    }

    public void setPregnant(boolean value, int timer) {
        this.dataManager.set(PREGNANT, value);
        this.dataManager.set(PREGNANCY_TIMER, timer);
    }

    public boolean isPregnant() {
        return this.dataManager.get(PREGNANT);
    }

    public void setPregnancyTimer(int value) {
        this.dataManager.set(PREGNANCY_TIMER, value);
    }

    public int getPregnancyTimer() {
        return this.dataManager.get(PREGNANCY_TIMER);
    }

    public void createBabyWhenPregnant() {
        if (this.getPregnancyTimer() > 0) {
            this.setPregnancyTimer(this.getPregnancyTimer() - 1);
            if (this.getPregnancyTimer() == 0) {
                RiftCreature baby = this.creatureType.invokeClass(this.world);
                baby.setHealth((float) (this.minCreatureHealth + (0.1) * (this.getLevel()) * (this.minCreatureHealth)));
                baby.setAgeInDays(0);
                baby.setTamed(true);
                baby.setOwnerId(this.getOwnerId());
                this.setAcquisitionInfo(CreatureAcquisitionInfo.AcquisitionMethod.BORN, System.currentTimeMillis() / 1000L);
                baby.setTameBehavior(TameBehaviorType.PASSIVE);
                baby.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
                this.setPregnant(false, 0);
                this.setSitting(false);

                EntityPlayer owner = (EntityPlayer) this.getOwner();

                //update journal
                if (PlayerJournalProgressHelper.getUnlockedCreatures(owner).containsKey(baby.creatureType) && !PlayerJournalProgressHelper.getUnlockedCreatures(owner).get(baby.creatureType)) {
                    PlayerJournalProgressHelper.unlockCreature(owner, baby.creatureType);
                    owner.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", baby.creatureType.getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
                }

                //update player tamed creatures
                if (PlayerTamedCreaturesHelper.canAddToParty(owner)) {
                    baby.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    this.world.spawnEntity(baby);
                    PlayerTamedCreaturesHelper.addCreatureToParty(owner, baby);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.baby_birthed_to_party", this.getName()), false);
                }
                else if (PlayerTamedCreaturesHelper.canAddCreatureToBox(owner)) {
                    baby.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                    PlayerTamedCreaturesHelper.addCreatureToBox(owner, baby);
                    owner.sendStatusMessage(new TextComponentTranslation("prift.notify.baby_birthed_to_box", this.getName()), false);
                }
            }
        }
    }

    public int[] getBirthTimeMinutes() {
        int minutes = (int)((float)this.getPregnancyTimer() / 1200F);
        int seconds = (int)((float)this.getPregnancyTimer() / 20F);
        seconds = seconds - (minutes * 60);
        return new int[]{minutes, seconds};
    }
    //pregnancy related stuff ends here

    //herding related stuff starts here
    public boolean canDoHerding() {
        return this.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.HERDER) && !this.isTamed();
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
    }

    public int getHerdSize() {
        return this.herdSize;
    }

    public void setHerdSize(int value) {
        this.herdSize = value;
    }

    public boolean hasHerdLeader() {
        return this.herdLeader != null && this.herdLeader.isEntityAlive() && this.canDoHerding();
    }
    public void addToHerdLeader(RiftCreature creature) {
        this.setHerdLeader(creature);
        this.herdLeader.setHerdSize(this.herdLeader.getHerdSize() + 1);
    }

    public void separateFromHerdLeader() {
        this.herdLeader.setHerdSize(this.herdLeader.getHerdSize() - 1);
        this.setHerdLeader(null);
    }

    public void manageHerding() {
        if (this.isHerdLeader() && this.world.rand.nextInt(200) == 1) {
            if (this.world.getEntitiesWithinAABB(this.getClass(), this.herdBoundingBox()).size() <= 1) {
                this.setHerdSize(1);
            }
        }
    }

    public AxisAlignedBB herdBoundingBox() {
        return this.getEntityBoundingBox().grow(12D);
    }

    public boolean isHerdLeader() {
        return this.getHerdSize() > 1 && this.canDoHerding();
    }

    public boolean canAddToHerd() {
        return this.isHerdLeader() && this.getHerdSize() < this.maxHerdSize() && this.canDoHerding();
    }

    public boolean isNearHerdLeader() {
        return this.getDistanceSq(this.getHerdLeader()) <= 144;
    }

    public void addCreatureToHerd(@Nonnull Stream<RiftCreature> stream) {
        try {
            stream.limit(this.maxHerdSize() - this.getHerdSize())
                    .filter(creature -> creature != this && creature.canDoHerding())
                    .forEach(creature -> creature.addToHerdLeader(this));
        }
        catch (Exception e) {}
    }

    public double herdFollowRange() {
        return 0.5D;
    }

    public void herderFollowLeader() {
        if (this.hasHerdLeader()) {
            if (!this.getEntityBoundingBox().intersects(this.getHerdLeader().getEntityBoundingBox().grow(this.herdFollowRange()))) {
                this.getNavigator().tryMoveToEntityLiving(this.getHerdLeader(), 1D);
            }
        }
    }

    public int maxHerdSize() {
        return 5;
    }
    //herding related stuff ends here

    //turret mode stuff starts here
    public boolean canEnterTurretMode() {
        return this.getLearnedMoves().stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED);
    }

    public boolean isTurretMode() {
        return this.dataManager.get(TURRET_MODE);
    }

    public void setTurretMode(boolean value) {
        this.dataManager.set(TURRET_MODE, value);
    }

    public TurretModeTargeting getTurretTargeting() {
        return TurretModeTargeting.values()[this.dataManager.get(TURRET_TARGET)];
    }

    public void setTurretModeTargeting(TurretModeTargeting turretModeTargeting) {
        this.dataManager.set(TURRET_TARGET, (byte) turretModeTargeting.ordinal());
    }
    //turret mode stuff ends here

    public int getLevel() {
        return this.dataManager.get(LEVEL);
    }

    public void setLevel(int variant) {
        this.dataManager.set(LEVEL, variant);
    }

    public int getXP() {
        return this.dataManager.get(XP);
    }

    public void setXP(int value) {
        if (this.getLevel() == 100) this.dataManager.set(XP, RiftUtil.clamp(value, 0, this.getMaxXP()));
        else this.dataManager.set(XP, value);
    }

    public int getMaxXP() {
        return (int)Math.round((double)this.getLevel() * this.creatureType.getLevelupRate().getRate() * 25D);
    }

    public int getLoveCooldown() {
        return this.dataManager.get(LOVE_COOLDOWN);
    }

    public void setLoveCooldown(int value) {
        this.dataManager.set(LOVE_COOLDOWN, value);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    //move anims start here
    //for setting anim type of move
    public void setUsingUnchargedAnim(boolean value) {
        if (this.currentCreatureMove() == null || this.currentCreatureMove().chargeType.requiresCharge()) {
            this.dataManager.set(USED_MOVE_TYPE, -1); //-1 means theres no move
        }

        if (value) this.dataManager.set(USED_MOVE_TYPE, this.currentCreatureMove().moveAnimType.ordinal());
    }

    //for getting anim type of move
    public CreatureMove.MoveAnimType getUnchargedAnimType() {
        if (this.dataManager.get(USED_MOVE_TYPE) < 0) return null;
        else return CreatureMove.MoveAnimType.values()[this.dataManager.get(USED_MOVE_TYPE)];
    }

    //multistep moves are uncharged one time use moves
    //whose anims are in multiple steps
    public void setUnchargedMultistepMoveStep(int step) {
        //0 is for no anim progress
        //multistep moves have 3 steps
        this.dataManager.set(USED_MULTISTEP_MOVE_STEP, step);
    }

    public int getUnchargedMultistepMoveStep() {
        return this.dataManager.get(USED_MULTISTEP_MOVE_STEP);
    }
    //move anims end here

    //mob grabbing management starts here
    private boolean targetIsGrabbable(Entity target) {
        if (target == null) return false;
        else if (target instanceof MultiPartEntityPart) {
            //if an entity part was targeted, test the parent
            Entity partParent = (Entity) ((MultiPartEntityPart) target).parent;
            return this.targetIsGrabbable(partParent);
        }
        else return RiftUtil.checkForNoAssociations(this, target)
                && MobSize.getMobSize(this).isAppropriateSize(target);
    }

    public Entity getGrabVictim() {
        return this.grabVictim;
    }

    public void setGrabVictim(Entity entity) {
        if (this.targetIsGrabbable(entity)) {
            this.grabVictim = entity;
            NonPotionEffectsHelper.setGrabbed(entity, true);
        }
        else {
            NonPotionEffectsHelper.setGrabbed(this.grabVictim, false);
            this.grabVictim = null;
        }
    }

    public void manageGrabVictim() {
        if (this.grabVictim != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            if (!this.getGrabVictim().isEntityAlive()) {
                RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, null));
            }
            else RiftMessages.WRAPPER.sendToServer(new RiftGrabbedEntitySetPos(this, this.grabVictim));
        }
        else if (!this.canBeKnockedBack()) this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0);
    }

    public Vec3d grabLocation() {
        return new Vec3d(this.getBodyHitbox().posX, this.getBodyHitbox().posY, this.getBodyHitbox().posZ);
    }
    //mob grabbing management ends here

    //cloaking management starts here
    public boolean isCloaked() {
        return this.dataManager.get(CLOAKED);
    }

    public void setCloaked(boolean value) {
        this.dataManager.set(CLOAKED, value);
    }

    private void manageCloaking() {
        //when the creature is cloaked and uses an attack move, the cloak gets undone
        //cloak also gets undone if creature is recently hit
        //and there will be a cooldown on cloaking until it will come back
        if ((this.currentCreatureMove() != null && this.isCloaked() && this.currentCreatureMove().moveAnimType != CreatureMove.MoveAnimType.STATUS)
        || (this.isCloaked() && this.isRecentlyHit())) {
            this.setCloaked(false);

            //find move slot that has cloaking move then put a cooldown on it
            int cloakMovePos = this.getLearnedMoves().indexOf(CreatureMove.CLOAK);
            if (cloakMovePos > -1 && this.getMoveCooldown(cloakMovePos) == 0) {
                this.setMoveCooldown(cloakMovePos, 200);
            }
        }
    }

    //cloaking management ends here

    //old move anims start here
    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    public void setAttacking(boolean value) {
        this.dataManager.set(ATTACKING, value);
    }
    //old move anims end here

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean value) {
        this.dataManager.set(SITTING, value);
    }

    public int getEnergy() {
        return this.dataManager.get(ENERGY);
    }

    public void setEnergy(int energy) {
        this.dataManager.set(ENERGY, energy);
    }

    public int getMaxEnergy() {
        return this.maxEnergy;
    }

    public int getWeaknessEnergy() {
        return Math.min(20, (int) (0.3 * this.getMaxEnergy()));
    }

    public int getTameProgress() {
        return RiftUtil.clamp(this.dataManager.get(TAME_PROGRESS), 0, 100);
    }

    public void setTameProgress(int value) {
        this.dataManager.set(TAME_PROGRESS, RiftUtil.clamp(value, 0, 100));
    }

    protected void setSpeed(double value) {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(value);
    }

    protected void setWaterSpeed(double value) {
        this.getEntityAttribute(EntityLivingBase.SWIM_SPEED).setBaseValue(value);
    }

    public void changeSpeedByMultiplier(double multiplier) {
        this.setSpeed(this.speed * multiplier);
        this.setWaterSpeed(this.waterSpeed * multiplier);
    }

    public double getSpeed() {
        return this.speed;
    }

    public double getWaterSpeed() {
        return this.waterSpeed;
    }

    public void setCanMove(boolean value) {
        this.dataManager.set(CAN_MOVE, value);
    }

    public boolean canMove() {
        return this.dataManager.get(CAN_MOVE);
    }

    //burrowing stuff starts here
    public boolean isBurrowing() {
        return this.dataManager.get(BURROWING);
    }

    public void setBurrowing(boolean value) {
        this.dataManager.set(BURROWING, value);
    }

    private void manageBurrowingEffects() {
        IBlockState stateBelow = this.world.getBlockState(this.getPosition().down());
        for (int x = 0; x < 16; x++) {
            if (stateBelow.getMaterial() != Material.AIR) {
                //play sounds
                this.playSound(stateBelow.getBlock().getSoundType().getHitSound(), 1f, 1f);

                //show particles
                if (this.world.isRemote) this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        this.posX,
                        this.posY,
                        this.posZ,
                        4.0D * ((double) this.rand.nextFloat() - 0.5D),
                        0.5D,
                        4.0D * ((double) this.rand.nextFloat() - 0.5D),
                        Block.getStateId(stateBelow));
            }
        }
    }
    //burrowing stuff ends here

    public TameBehaviorType getTameBehavior() {
        return TameBehaviorType.values()[this.dataManager.get(BEHAVIOR).byteValue()];
    }
    public void setTameBehavior(TameBehaviorType tameBehavior) {
        this.dataManager.set(BEHAVIOR, (byte) tameBehavior.ordinal());
    }

    public void setLeapDirection(float velocityX, float velocityY, float velocityZ) {
        this.dataManager.set(LEAP_X_VELOCITY, velocityX);
        this.dataManager.set(LEAP_Y_VELOCITY, velocityY);
        this.dataManager.set(LEAP_Z_VELOCITY, velocityZ);
    }

    public Vec3d getLeapDirection() {
        return new Vec3d(this.dataManager.get(LEAP_X_VELOCITY), this.dataManager.get(LEAP_Y_VELOCITY), this.dataManager.get(LEAP_Z_VELOCITY));
    }

    //for multi hitbox stuff
    @Override
    public Entity getMultiHitboxUser() {
        return this;
    }

    @Override
    public Entity[] getParts() {
        return this.hitboxArray;
    }

    @Override
    public void setParts(Entity[] hitboxes) {
        this.hitboxArray = hitboxes;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    private void deleteAllHitboxes() {
        for (Entity entity : this.hitboxArray) {
            EntityHitbox hitbox = (EntityHitbox) entity;
            this.world.removeEntityDangerously(hitbox);
        }
    }

    public EntityHitbox getHeadHitbox() {
        for (Entity entity : this.hitboxArray) {
            EntityHitbox hitboxToTest = (EntityHitbox) entity;
            if (hitboxToTest.partName.equals("head")) return hitboxToTest;
        }
        return null;
    }

    public boolean headIsUnderwater() {
        if (this.getHeadHitbox() == null) return false;

        BlockPos thisPos = new BlockPos(this.getHeadHitbox().posX, this.getHeadHitbox().posY, this.getHeadHitbox().posZ);
        BlockPos abovePos = thisPos.up();
        return this.world.getBlockState(thisPos).getMaterial().isLiquid() && this.world.getBlockState(abovePos).getMaterial().isLiquid();
    }

    public EntityHitbox getBodyHitbox() {
        for (Entity entity : this.hitboxArray) {
            EntityHitbox hitboxToTest = (EntityHitbox) entity;
            if (hitboxToTest.partName.equals("body")) return hitboxToTest;
        }
        return null;
    }

    public boolean hitboxUseHWYLA() {
        return false;
    }
    //end of multi hitbox stuff

    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        if (!this.isTamed()) super.dropLoot(wasRecentlyHit, lootingModifier, source);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        //set a boolean that determines whether or not the creature has been recently hit
        this.recentlyHit = true;

        //additionally, get the damage received for use later
        this.recentlyHitDamage = amount;

        //apply check hit condition if it has move with said condition
        /*
        if ((RiftUtil.checkForNoAssociations(this, source.getImmediateSource()) || RiftUtil.checkForNoAssociations(this, source.getTrueSource()))
                && (RiftUtil.checkForNoHerdAssociations(this, source.getImmediateSource()) || RiftUtil.checkForNoHerdAssociations(this, source.getTrueSource())))
            this.addToConditionStack(CreatureMoveCondition.Condition.CHECK_HIT);
         */

        //make it so that anything trying to attack the mobs main hitbox ends up attacking the nearest hitbox instead
        if (source.getImmediateSource() instanceof EntityLivingBase && !(source.getImmediateSource() instanceof EntityPlayer)) {
            Entity attacker = source.getImmediateSource();
            EntityHitbox closestPart = null;
            float closestDist = RiftUtil.funnyNumber;
            for (Entity testEntity : this.hitboxArray) {
                EntityHitbox testHitbox = (EntityHitbox) testEntity;
                if (attacker.getDistance(testHitbox) <= closestDist && !testHitbox.isDisabled()) {
                    closestPart = testHitbox;
                    closestDist = attacker.getDistance(testHitbox);
                }
            }
            if (closestPart != null) {
                if (closestPart.damageSourceWithinDamageDefinitions(source)) {
                    amount *= closestPart.getDamageMultiplierForSource(source);
                }
                else {
                    amount *= closestPart.getDamageMultiplier();
                }
                super.attackEntityFrom(source, amount);
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    public boolean isRecentlyHit() {
        boolean valueToReturn = this.recentlyHit;
        this.recentlyHit = false;
        return valueToReturn;
    }

    public float getRecentlyHitDamage() {
        float valueToReturn = this.recentlyHitDamage;
        this.recentlyHitDamage = 0;
        return valueToReturn;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    public boolean isSaddled() {
        return this.dataManager.get(SADDLED);
    }

    public void setSaddled(boolean value) {
        this.dataManager.set(SADDLED, value);
    }

    public RiftLargeWeaponType getLargeWeapon() {
        return RiftLargeWeaponType.values()[this.dataManager.get(LARGE_WEAPON)];
    }

    public void setLargeWeapon(RiftLargeWeaponType value) {
        this.dataManager.set(LARGE_WEAPON, (byte) value.ordinal());
    }

    public void setCanUseButtonForMove(boolean value) {
        if (this.currentCreatureMove() == null) return;
        int movePos = this.getLearnedMoves().indexOf(CreatureMove.values()[this.dataManager.get(CURRENT_MOVE)]);
        switch (movePos) {
            case 0:
                this.setCanUseLeftClick(value);
                break;
            case 1:
                this.setCanUseRightClick(value);
                break;
            case 2:
                this.setCanUseMiddleClick(value);
                break;
        }
    }

    public void resetUseButtonsForMove() {
        this.setCanUseLeftClick(true);
        this.setCanUseRightClick(true);
        this.setCanUseMiddleClick(true);
    }

    public boolean canUseLeftClick() {
        return this.dataManager.get(CAN_USE_LEFT_CLICK);
    }

    public void setCanUseLeftClick(boolean value) {
        this.dataManager.set(CAN_USE_LEFT_CLICK, Boolean.valueOf(value));
    }

    public boolean canUseRightClick() {
        return this.dataManager.get(CAN_USE_RIGHT_CLICK);
    }

    public void setCanUseRightClick(boolean value) {
        this.dataManager.set(CAN_USE_RIGHT_CLICK, value);
    }

    public boolean canUseMiddleClick() {
        return this.dataManager.get(CAN_USE_MIDDLE_CLICK);
    }

    public void setCanUseMiddleClick(boolean value) {
        this.dataManager.set(CAN_USE_MIDDLE_CLICK, value);
    }

    public boolean canUseSpacebar() {
        return this.dataManager.get(CAN_USE_SPACEBAR);
    }

    public void setCanUseSpacebar(boolean value) {
        this.dataManager.set(CAN_USE_SPACEBAR, value);
    }

    public boolean isUsingSpacebar() {
        return this.dataManager.get(USING_SPACEBAR);
    }

    public void setUsingSpacebar(boolean value) {
        this.dataManager.set(USING_SPACEBAR, value);
    }

    public int getSpacebarUse() {
        return this.dataManager.get(SPACEBAR_USE);
    }

    public void setSpacebarUse(int value) {
        this.dataManager.set(SPACEBAR_USE, value);
    }

    public boolean hasTarget() {
        return this.dataManager.get(HAS_TARGET);
    }

    public void setHasTarget(boolean value) {
        this.dataManager.set(HAS_TARGET, value);
    }

    public int getAgeInTicks() {
        return this.dataManager.get(AGE_TICKS);
    }

    public int getAgeInDays() {
        return this.dataManager.get(AGE_TICKS) / 24000;
    }

    public void setAgeInTicks(int value) {
        this.dataManager.set(AGE_TICKS, value);
    }

    public void setAgeInDays(int value) {
        this.dataManager.set(AGE_TICKS, value * 24000);
    }

    //this is for managing nocturnal creatures
    private void manageSleepSchedule() {
        if (this.world.isDaytime() && !this.isInCave()) {
            //if the creature somehow ends up in water, it forcibly wakes up
            //this is here to prioritize waking up over everything else
            if (this.isInWater()) {
                this.setSleeping(false);
                this.setForcedAwake(true);
            }
            //manage whether or not creature is sleeping or forced awake
            else if (this.getAttackTarget() == null && this.getRevengeTarget() == null) {
                this.setSleeping(true);
                this.setForcedAwake(false);
                this.getNavigator().clearPath();
                if (!this.isTamed()) this.setTameProgress(0);
            }
            else {
                this.setSleeping(false);
                this.setForcedAwake(true);
            }


            //when a creature is forced awake, their stats get nerfed
            if (this.isForcedAwake()) {
                this.changeSpeedByMultiplier(0.5);
                this.changeAttackByMultiplier(0.1);
            }
            else {
                this.changeSpeedByMultiplier(1);
                this.changeAttackByMultiplier(1);
            }
        }
        else if (this.isSleeping() || this.isForcedAwake()) {
            this.setSleeping(false);
            this.setForcedAwake(false);
            this.changeSpeedByMultiplier(1);
            this.changeAttackByMultiplier(1);
        }
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setSleeping(boolean value) {
        this.dataManager.set(SLEEPING, value);
        this.removePassengers();
    }

    public boolean isForcedAwake() {
        return this.dataManager.get(FORCED_AWAKE);
    }

    public void setForcedAwake(boolean value) {
        this.dataManager.set(FORCED_AWAKE, value);
    }
    //nocturnal creature management ends here

    public boolean canBeKnockedBack() {
        return false;
    }

    public boolean fleesFromDanger() {
        return false;
    }

    public boolean isIncapacitated() {
        return this.dataManager.get(INCAPACITATED);
    }

    public void setIncapacitated(boolean value) {
        this.dataManager.set(INCAPACITATED, value);
    }

    public boolean isClimbing() {
        return this.dataManager.get(CLIMBING);
    }

    public void setClimbing(boolean value) {
        this.dataManager.set(CLIMBING, value);
    }

    public PlayerTamedCreatures.DeploymentType getDeploymentType() {
        return PlayerTamedCreatures.DeploymentType.values()[(int)this.dataManager.get(DEPLOYMENT_TYPE)];
    }

    public void setDeploymentType(PlayerTamedCreatures.DeploymentType value) {
        this.dataManager.set(DEPLOYMENT_TYPE, (byte)value.ordinal());
    }

    public boolean justSpawned() {
        return this.dataManager.get(JUST_SPAWNED);
    }

    public void setJustSpawned(boolean value) {
        this.dataManager.set(JUST_SPAWNED, value);
    }

    public boolean canRotateMounted() {
        return this.dataManager.get(CAN_ROTATE_MOUNTED);
    }

    public void disableCanRotateMounted() {
        this.dataManager.set(CAN_ROTATE_MOUNTED, false);
    }

    public void enableCanRotateMounted() {
        this.dataManager.set(CAN_ROTATE_MOUNTED, true);
    }

    public void setHomePos() {
        this.dataManager.set(HAS_HOME_POS, true);
        this.dataManager.set(HOME_POS, new BlockPos(this));
    }

    public void setHomePos(int x, int y, int z) {
        this.dataManager.set(HAS_HOME_POS, true);
        this.dataManager.set(HOME_POS, new BlockPos(x, y, z));
    }

    public void clearHomePos() {
        this.dataManager.set(HAS_HOME_POS, false);
        this.dataManager.set(HOME_POS, new BlockPos(0, 0, 0));
    }

    public boolean getHasHomePos() {
        return this.dataManager.get(HAS_HOME_POS);
    }

    public BlockPos getHomePos() {
        return this.dataManager.get(HOME_POS);
    }

    public boolean busyAtWork() {
        boolean usingWorkstation = (this instanceof IWorkstationUser) && ((IWorkstationUser)this).hasWorkstation();
        boolean usingLeadForWork = (this instanceof ILeadWorkstationUser) && ((ILeadWorkstationUser)this).isUsingLeadForWork();
        return usingWorkstation || usingLeadForWork;
    }

    public boolean busyAtWorkWithNoTargets() {
        return this.busyAtWork() && this.getAttackTarget() == null && this.getRevengeTarget() == null;
    }

    public boolean busyAtTurretMode() {
        return this.canEnterTurretMode() && this.isTurretMode();
    }

    public boolean busyAtWanderOnHarvest() {
        return this instanceof IHarvestWhenWandering && ((IHarvestWhenWandering)this).isHarvesting();
    }

    public boolean isBaby() {
        return this.getAgeInDays() < 1;
    }

    public boolean isMoving(boolean includeY) {
        double fallMotion = !this.onGround && includeY ? this.motionY : 0;
        return Math.sqrt((this.motionX * this.motionX) + (fallMotion * fallMotion) + (this.motionZ * this.motionZ)) > 0;
    }

    public boolean onGround() {
        IBlockState blockState = this.world.getBlockState(this.getPosition().down());
        return blockState.getMaterial() != Material.AIR && blockState.getMaterial() != Material.WATER;
    }

    public boolean isInCave() {
        BlockPos pos = new BlockPos(this);
        return !this.world.canSeeSky(pos.up()) && pos.getY() <= 56;
    }

    public boolean creatureBoxWithinReach() {
        if (this.getHomePos() == null) return false;

        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.world.getTileEntity(this.getHomePos());

        if (creatureBox == null) return false;

        return creatureBox.posWithinDeploymentRange(this.getPosition());
    }

    public boolean canNaturalRegen() {
        return true;
    }

    public int slotCount() {
        return 0;
    }

    //ride pos management starts here
    public EntityLiving getDynamicRideUser() {
        return this;
    }

    public void createRidePositions(List<Vec3d> value) {
        this.ridePositions = value;
    }

    public List<Vec3d> ridePositions() {
        return this.ridePositions;
    }

    public void setRidePosition(int index, Vec3d value) {
        this.ridePositions.set(index, value);
    }

    public void updatePassenger(Entity passenger) {
        IDynamicRideUser.super.updatePassenger(passenger);
    }
    //ride pos management ends here

    public abstract float attackWidth();

    public float rangedWidth() {
        return 16f;
    }

    public boolean checkIfCanBreakBlock(BlockPos blockPos) {
        return this.checkIfCanBreakBlock(this.world.getBlockState(blockPos));
    }

    public boolean checkIfCanBreakBlock(IBlockState blockState) {
        Block block = blockState.getBlock();

        //some blocks cannot be broken by creatures
        boolean excemptedBlocks = block instanceof RiftCreatureBox;
        if (excemptedBlocks) return false;

        //the ones that have a mining level of -1 can be broken easily w/out
        //additional checks
        if (block.getHarvestLevel(blockState) < 0) return true;
        //check if block requires specific tool to be broken
        for (String blockBreakLevels : RiftConfigHandler.getConfig(this.creatureType).general.blockBreakLevels) {
            String tool = blockBreakLevels.substring(0, blockBreakLevels.indexOf(":"));
            int level = Integer.parseInt(blockBreakLevels.substring(blockBreakLevels.indexOf(":")+1));
            if (block.isToolEffective(tool, blockState)
                    && level >= block.getHarvestLevel(blockState)) return true;
        }
        return false;
    }

    //create an AABB in front of the creature based on the head hitbox's position
    //and the head's width
    public AxisAlignedBB frontOfCreatureAABB(double zGrowth) {
        Vec3d look = this.getLookVec().normalize();
        double perpX = -look.z;
        double perpZ = look.x;
        double offset = Math.ceil(this.width / 2.0) + 0.5;

        /*
        boolean facingNorth = (this.rotationYaw >= 135 && this.rotationYaw <= 180) || (this.rotationYaw <= -135 && this.rotationYaw >= -180);
        boolean facingEast = this.rotationYaw > -135 && this.rotationYaw < -45;
        boolean facingSouth = this.rotationYaw >= -45 && this.rotationYaw <= 45;
        boolean facingWest = this.rotationYaw > 45 && this.rotationYaw < 135;
        */

        return new AxisAlignedBB(
                this.posX + look.x - perpX * offset,
                this.posY,
                this.posZ + look.z - perpZ * offset,
                this.posX + (this.attackWidth() + this.width + zGrowth) * look.x + perpX * offset,
                this.posY + this.height,
                this.posZ + (this.attackWidth() + this.width + zGrowth) * look.z + perpZ * offset
        );
    }

    public AxisAlignedBB frontOfCreatureAABB() {
        return new AxisAlignedBB(
                this.getHeadHitbox().width >= 1 ? this.getHeadHitbox().getEntityBoundingBox().minX : this.getHeadHitbox().posX - 0.5D,
                this.posY,
                this.getHeadHitbox().width >= 1 ? this.getHeadHitbox().getEntityBoundingBox().minZ : this.getHeadHitbox().posZ - 0.5D,
                this.getHeadHitbox().width >= 1 ? this.getHeadHitbox().getEntityBoundingBox().maxX : this.getHeadHitbox().posX + 0.5D,
                Math.max(this.posY + this.height, this.getHeadHitbox().getEntityBoundingBox().maxY),
                this.getHeadHitbox().width >= 1 ? this.getHeadHitbox().getEntityBoundingBox().maxZ : this.getHeadHitbox().posZ + 0.5D
        );
    }

    public List<Entity> getAllTargetsInFront() {
        return this.getAllTargetsInFront(false);
    }

    //create a series of aabbs based on creature width and attack width
    //entities that are inside will be considered as targets and will be attacked
    public List<Entity> getAllTargetsInFront(boolean useRanged) {
        //if this creature has grabbed something, the grabbed creature will always be targeted
        if (this.grabVictim != null && this.grabVictim.isEntityAlive()) return Collections.singletonList(this.grabVictim);

        RiftCreature user = this;
        List<AxisAlignedBB> aabbList = new ArrayList<>();
        List<Entity> entityList = new ArrayList<>();
        BlockPos firstAABBPos = new BlockPos(
                this.posX + (this.width / 2) * Math.cos(Math.atan2(this.getLookVec().z, this.getLookVec().x)),
                this.posY,
                this.posZ + (this.width / 2) * Math.sin(Math.atan2(this.getLookVec().z, this.getLookVec().x))
        );
        BlockPos lastAABBPos = new BlockPos(
                this.posX + ((useRanged ? (this.rangedWidth() + this.width) : (this.attackWidth() + this.width)) - (this.width / 2)) * Math.cos(Math.atan2(this.getLookVec().z, this.getLookVec().x)),
                this.posY,
                this.posZ + ((useRanged ? (this.rangedWidth() + this.width) : (this.attackWidth() + this.width)) - (this.width / 2)) * Math.sin(Math.atan2(this.getLookVec().z, this.getLookVec().x))
        );

        //fill aabb list
        double aabbStepSize = this.width - 0.5; //each aabb overlaps by at most 0.5 blocks
        double aabbDist = Math.sqrt(firstAABBPos.distanceSq(lastAABBPos.getX(), lastAABBPos.getY(), lastAABBPos.getZ()));
        int steps = (int) Math.ceil(aabbDist / aabbStepSize); //no of aabbs needed

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double interpX = firstAABBPos.getX() + t * (lastAABBPos.getX() - firstAABBPos.getX());
            double interpZ = firstAABBPos.getZ() + t * (lastAABBPos.getZ() - firstAABBPos.getZ());

            AxisAlignedBB aabb = new AxisAlignedBB(
                    interpX - this.width / 2, this.posY, interpZ - this.width / 2,
                    interpX + this.width / 2, this.posY + this.height, interpZ + this.width / 2
            );
            aabbList.add(aabb);
        }

        //find entities detected in aabbs in aabblist
        for (AxisAlignedBB aabb : aabbList) {
            List<Entity> tempEntityList = this.world.getEntitiesWithinAABB(Entity.class, aabb, new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity entity) {
                    return RiftUtil.checkForNoAssociations(user, entity) && !user.equals(entity) && user.canEntityBeSeen(entity);
                }
            });
            entityList.addAll(tempEntityList);
        }

        //prioritize nearest hitbox of a creature to this creature over the targeted creature
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : entityList) {
            if (entity instanceof MultiPartEntityPart) {
                //get parent of multipart and make array of all multiparts with same parent
                MultiPartEntityPart hitbox = (MultiPartEntityPart) entity;
                Entity hitboxParent = (Entity) hitbox.parent;

                //get all hitboxes that share the same parent
                List<Entity> hitboxesOfSameParent = entityList.stream().filter(
                        e -> e instanceof MultiPartEntityPart && ((MultiPartEntityPart)e).parent.equals(hitboxParent)
                ).collect(Collectors.toList());

                //if parent is no longer in entitiesToRemove or if there is already a closest hitbox, skip
                if (entitiesToRemove.contains(hitboxParent) ||
                        entitiesToRemove.stream().anyMatch(e -> e instanceof MultiPartEntityPart && ((MultiPartEntityPart) e).parent.equals(hitboxParent))) continue;

                //now for getting the closest hitbox
                MultiPartEntityPart closestHitbox = null;
                for (Entity hitboxForTest : hitboxesOfSameParent) {
                    if (closestHitbox == null) closestHitbox = (MultiPartEntityPart) hitboxForTest;
                    else if (closestHitbox.getDistance(this) >= hitboxForTest.getDistance(this)) closestHitbox = (MultiPartEntityPart) hitboxForTest;
                }

                //remove extra hitboxes and the parent from the tempEntityList
                if (closestHitbox != null) {
                    MultiPartEntityPart finalClosestHitbox = closestHitbox;
                    hitboxesOfSameParent = hitboxesOfSameParent.stream().filter(e -> !e.equals(finalClosestHitbox)).collect(Collectors.toList());
                }
                entitiesToRemove.add(hitboxParent);
                if (!hitboxesOfSameParent.isEmpty()) entitiesToRemove.addAll(hitboxesOfSameParent);
            }
        }
        entityList.removeAll(entitiesToRemove);

        //remove duplicates
        entityList = entityList.stream().distinct().collect(Collectors.toList());

        return entityList;
    }

    public Entity getClosestTargetInFront() {
        return this.getClosestTargetInFront(false);
    }

    public Entity getClosestTargetInFront(boolean useRanged) {
        List<Entity> targetsInFront = this.getAllTargetsInFront(useRanged);
        Entity closestTargetInFront = null;
        for (Entity entity : targetsInFront) {
            if (closestTargetInFront == null) closestTargetInFront = entity;
            else if (closestTargetInFront.getDistance(this) >= entity.getDistance(this)) closestTargetInFront = entity;
        }
        return closestTargetInFront;
    }

    @Override
    public boolean canBeSteered() {
        return this.steerable;
    }

    public void setCanBeSteered(boolean value) {
        this.steerable = value;
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer && this.getAttackTarget() != passenger) {
                EntityPlayer player = (EntityPlayer) passenger;
                if (this.isTamed() && this.isOwner(player)) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.canMove() || this.isTurretMode()) {
            super.travel(0, vertical, 0);
            return;
        }

        //for leaping
        if (this.getLeapDirection().length() > 0f && this.onGround()) {
            this.motionX = this.getLeapDirection().x;
            this.motionY = this.getLeapDirection().y;
            this.motionZ = this.getLeapDirection().z;
        }
        else this.setLeapDirection(0f, 0f, 0f);

        if (this.isSaddled() && this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null) {
                if (this.getAttackTarget() != null) {
                    this.setAttackTarget(null);
                    this.getNavigator().clearPath();
                }

                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;

                if (forward <= 0.0F) forward *= 0.25F;

                //movement
                this.stepHeight = 1.0F;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                this.fallDistance = 0;
                float moveSpeedMod = (this.getEnergy() > this.getWeaknessEnergy() ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = (float)(Math.max(0, this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() - riderSpeed)) * moveSpeedMod;
                this.setAIMoveSpeed(this.onGround ? moveSpeed + (this.isSprinting() && this.getEnergy() > this.getWeaknessEnergy() ? moveSpeed * 0.25f : 0) : moveSpeed);

                //for getting out of bodies of water easily
                if (forward > 0) {
                    if (this.getBodyHitbox() != null) {
                        if (this.getBodyHitbox().isInWater()) {
                            if (this.posY >= RiftUtil.highestWaterPos(this) - 2 && this.posY <= RiftUtil.highestWaterPos(this) + 2) {
                                double xMove = this.width * Math.sin(-Math.toRadians(this.rotationYaw));
                                double zMove = this.width * Math.cos(Math.toRadians(this.rotationYaw));

                                //if creature is in water, is breaching, and if bottom of their body hitbox is in front of
                                //solid block with air on top, it will leave water
                                BlockPos aheadBodyHitbox = new BlockPos(this.posX + xMove, RiftUtil.highestWaterPos(this), this.posZ + zMove);
                                BlockPos aboveAheadBodyHitbox = aheadBodyHitbox.up();
                                if (this.world.getBlockState(aheadBodyHitbox).getMaterial().isSolid() && !this.world.getBlockState(aboveAheadBodyHitbox).getMaterial().isSolid()) {
                                    RiftMessages.WRAPPER.sendToServer(new RiftForceChangePos(this, this.posX + xMove, RiftUtil.highestWaterPos(this) + 1.0, this.posZ + zMove));
                                }

                                //if creature is in water, is breaching, and if bottom of their main collision hitbox is in front of
                                //solid block with water on top, it will go there
                                BlockPos aheadMainHitbox = new BlockPos(this.posX + xMove, this.posY, this.posZ + zMove);
                                BlockPos aboveAheadMainHitbox = aheadMainHitbox.up();
                                if (this.world.getBlockState(aheadMainHitbox).getMaterial().isSolid() && this.world.getBlockState(aboveAheadMainHitbox).getMaterial().isLiquid()) {
                                    RiftMessages.WRAPPER.sendToServer(new RiftForceChangePos(this, this.posX + xMove, RiftUtil.highestWaterPos(this) + 1.0, this.posZ + zMove));
                                }
                            }
                        }
                    }
                }

                //float above water
                if (this.isFloatingOnWater) this.motionY += 0.1D;

                super.travel(strafe, vertical, forward);
            }
        }
        else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;

            //get out of 2 block or more deep water pits
            if (forward > 0) {
                if (this.getBodyHitbox() != null) {
                    if (this.getBodyHitbox().isInWater()) {
                        if (this.posY >= RiftUtil.highestWaterPos(this) - 2 && this.posY <= RiftUtil.highestWaterPos(this) + 2) {
                            double xMove = (this.width)*Math.sin(-Math.toRadians(this.rotationYaw));
                            double zMove = (this.width)*Math.cos(Math.toRadians(this.rotationYaw));
                            BlockPos ahead = new BlockPos(this.posX + xMove, RiftUtil.highestWaterPos(this), this.posZ + zMove);
                            BlockPos above = ahead.up();
                            if (this.world.getBlockState(ahead).getMaterial().isSolid() && !this.world.getBlockState(above).getMaterial().isSolid()) {
                                RiftMessages.WRAPPER.sendToServer(new RiftForceChangePos(this, this.posX + xMove, RiftUtil.highestWaterPos(this) + 1.0, this.posZ + zMove));
                            }
                        }
                    }
                }
            }

            //float above water
            if (this.isFloatingOnWater) this.motionY += 0.1D;

            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        boolean leashOperatingFlag = true;
        if (this instanceof IWorkstationUser) {
            leashOperatingFlag = !((IWorkstationUser)this).hasWorkstation();
        }
        return !this.getLeashed() && this.isTamed() && !this.isSitting() && (!this.canEnterTurretMode() || !this.isTurretMode()) && leashOperatingFlag;
    }

    public void onDeath(DamageSource cause) {
        //for dropping inventory of creature upon death
        if (!this.world.isRemote && this.creatureInventory != null) {
            for (int i = 0; i < this.creatureInventory.getSizeInventory(); i++) {
                ItemStack itemStack = this.creatureInventory.getStackInSlot(i);
                boolean hasSaddleFlag = !this.creatureType.canBeSaddled || i != this.creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE);
                boolean hasLargeWeaponFlag = !this.creatureType.canHoldLargeWeapon || i != this.creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON);
                if (!itemStack.isEmpty() && (hasSaddleFlag || hasLargeWeaponFlag)) {
                    this.entityDropItem(itemStack, 0.0f);
                    this.creatureInventory.setInventorySlotContents(i, new ItemStack(Items.AIR));
                }
            }
        }

        //for releasing entities grabbed by this creature
        if (this.grabVictim != null) NonPotionEffectsHelper.setGrabbed(this.grabVictim, false);
        if (this.getAttackTarget() != null) NonPotionEffectsHelper.setGrabbed(this.getAttackTarget(), false);

        //for undeploying a creature
        this.setSitting(false);
        this.getActivePotionEffects().clear();
        this.setBoxReviveTime(GeneralConfig.creatureBoxReviveTime);
        if (this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
            this.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            PlayerTamedCreaturesHelper.updateIndividualPartyMemServer((EntityPlayer) this.getOwner(), this);
            PlayerTamedCreaturesHelper.updateIndividualPartyMemClient((EntityPlayer) this.getOwner(), this);
        }
        if (this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) {
            PlayerTamedCreaturesHelper.updateIndividualBoxDeployedCreatureServer((EntityPlayer) this.getOwner(), this);
            PlayerTamedCreaturesHelper.updateIndividualBoxDeployedCreatureClient((EntityPlayer) this.getOwner(), this);
        }

        super.onDeath(cause);
    }

    //start of radial menu stuff
    public List<RiftTameRadialChoice> mainRadialChoices() {
        List<RiftTameRadialChoice> list = new ArrayList<>();
        list.add(RiftTameRadialChoice.INVENTORY);
        if (this.creatureType.canBeSaddled && this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY && this.isRideable) list.add(RiftTameRadialChoice.RIDE);
        if (this.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) list.add(RiftTameRadialChoice.OPTIONS);
        list.add(RiftTameRadialChoice.BEHAVIOR);
        return list;
    }

    public List<RiftTameRadialChoice> behaviorRadialChoices() {
        List<RiftTameRadialChoice> list = new ArrayList<>();
        list.add(RiftTameRadialChoice.BACK);
        list.add(RiftTameRadialChoice.ASSIST);
        list.add(RiftTameRadialChoice.NEUTRAL);
        list.add(RiftTameRadialChoice.AGGRESSIVE);
        list.add(RiftTameRadialChoice.PASSIVE);
        return list;
    }

    public List<RiftTameRadialChoice> optionsRadialChoices() {
        List<RiftTameRadialChoice> list = new ArrayList<>();
        list.add(RiftTameRadialChoice.BACK);
        if (this instanceof IWorkstationUser && !((IWorkstationUser)this).getWorkstations().isEmpty()) list.add(RiftTameRadialChoice.SET_WORKSTATION);
        if (this instanceof IHarvestWhenWandering) list.add(RiftTameRadialChoice.SET_WANDER_HARVEST);
        if (this.canEnterTurretMode()) list.add(RiftTameRadialChoice.SET_TURRET_MODE);
        return list;
    }

    public List<RiftTameRadialChoice> turretRadialChoices() {
        List<RiftTameRadialChoice> list = new ArrayList<>();
        list.add(RiftTameRadialChoice.BACK);
        list.add(RiftTameRadialChoice.PLAYERS);
        list.add(RiftTameRadialChoice.PLAYERS_AND_OTHER_TAMES);
        list.add(RiftTameRadialChoice.HOSTILES);
        list.add(RiftTameRadialChoice.ALL);
        return list;
    }
    //end of radial menu stuff

    private void sendDeathMessage() {
        if (this.isTamed() && !this.world.isRemote && this.world.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
            this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
        }
    }

    public int getBoxReviveTime() {
        return this.boxReviveTime;
    }

    public void setBoxReviveTime(int time) {
        this.boxReviveTime = time;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        if (!this.isTamed()) return null;

        RiftCreatureType.CreatureCategory category = this.creatureType.getCreatureCategory();
        if (category.equals(RiftCreatureType.CreatureCategory.DINOSAUR) || category.equals(RiftCreatureType.CreatureCategory.REPTILE) || category.equals(RiftCreatureType.CreatureCategory.BIRD) || this.creatureType.equals(RiftCreatureType.DIMETRODON)) {
            RiftEgg egg = new RiftEgg(this.world);
            egg.setCreatureType(this.creatureType);
            egg.setOwnerId(this.getOwnerId());
            egg.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
            egg.enablePersistence();
            egg.setHatchTime(this.creatureType.getHatchTime() * 20);
            return egg;
        }
        else if (category.equals(RiftCreatureType.CreatureCategory.INVERTEBRATE)) {
            RiftSac sac = new RiftSac(this.world);
            sac.setCreatureType(this.creatureType);
            sac.setOwnerId(this.getOwnerId());
            sac.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
            sac.enablePersistence();
            sac.setHatchTime(this.creatureType.getHatchTime() * 20);
            return sac;
        }
        else return null;
    }

    @Override
    public void registerControllers(AnimationData data) {
        RiftCreature user = this;
        //for movement
        data.addAnimationController(new AnimationController(this, "movement", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (currentCreatureMove() == null) {
                    //for warning
                    if (isWarning()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".warn", false));
                        return PlayState.CONTINUE;
                    }
                    //sitting
                    else if (isSitting() && !isBeingRidden() && !hasTarget() && !isInWater()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".sitting", true));
                        return PlayState.CONTINUE;
                    }
                    //swimming for water creatures
                    else if (user instanceof RiftWaterCreature && user.isInWater()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".swim", true));
                        return PlayState.CONTINUE;
                    }
                    //flopping for water creatures
                    else if (user instanceof RiftWaterCreature && ((RiftWaterCreature)user).canFlop() && !user.isInWater()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".flop", true));
                        return PlayState.CONTINUE;
                    }
                    //walking
                    else if (event.isMoving() || (isSitting() && hasTarget())) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".walk", true));
                        return PlayState.CONTINUE;
                    }
                    else {
                        event.getController().clearAnimationCache();
                        return PlayState.STOP;
                    }
                }
                else {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
            }
        }));
        //for sleeping
        data.addAnimationController(new AnimationController(this, "sleep", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (isSleeping()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".sleeping", true));
                    return PlayState.CONTINUE;
                }
                else if (isForcedAwake()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".tired_pose", true));
                    return PlayState.CONTINUE;
                }
                event.getController().clearAnimationCache();
                return PlayState.STOP;
            }
        }));
        //for use of uncharged moves
        data.addAnimationController(new AnimationController(this, "useUnchargedMove", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (currentCreatureMove() != null && !currentCreatureMove().chargeType.requiresCharge()) {
                    String multiNameNum = "";
                    if (animatorsForMoveType().get(currentCreatureMove().moveAnimType).getNumberOfAnims() > 1
                    && chosenAnimFromMultiple == -1) {
                        chosenAnimFromMultiple = RiftUtil.randomInRange(0, 1);
                        multiNameNum = "_"+chosenAnimFromMultiple;
                    }

                    if (getUnchargedAnimType() != null) {
                        if (currentCreatureMove().useTimeIsInfinite) {
                            if (getUnchargedMultistepMoveStep() > 0) {
                                boolean animCanLoop = getUnchargedMultistepMoveStep() == 2;
                                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_"+getUnchargedAnimType().toString().toLowerCase()+"_type_move_pt"+getUnchargedMultistepMoveStep(), animCanLoop));
                            }
                        }
                        else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_"+getUnchargedAnimType().toString().toLowerCase()+"_type_move"+multiNameNum, false));
                        return PlayState.CONTINUE;
                    }
                    else {
                        chosenAnimFromMultiple = -1;
                        event.getController().clearAnimationCache();
                        return PlayState.STOP;
                    }
                }
                else if (currentCreatureMove() == null) {
                    chosenAnimFromMultiple = -1;
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
                return PlayState.CONTINUE;
            }
        }));
        //for use of charged moves
        data.addAnimationController(new AnimationController(this, "useChargedMove", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                int movePos = currentCreatureMove() != null ? getLearnedMoves().indexOf(currentCreatureMove()) : -1;
                if (currentCreatureMove() != null && currentCreatureMove().chargeType.requiresCharge()) {
                    if (movePos == 0) {
                        if (getPlayingChargedMoveAnim() == 0) {
                            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_start", false));
                        }
                        else if (getPlayingChargedMoveAnim() == 1) {
                            if (currentCreatureMove().useTimeIsInfinite) {
                                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1", true));
                            }
                            else {
                                if (getMoveOneUse() > 0 && getMoveOneUse() < currentCreatureMove().maxUse) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1", false));
                                }
                                else if (getMoveOneUse() >= currentCreatureMove().maxUse) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+ creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1_hold", true));
                                }
                            }
                        }
                        else if (getPlayingChargedMoveAnim() == 2 || getPlayingChargedMoveAnim() == 3 || getPlayingChargedMoveAnim() == 4) {
                            if (currentCreatureMove().useTimeIsInfinite) {
                                if (getPlayingInfiniteMoveAnim()) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_infinite_" + currentCreatureMove().moveTypeName() + "_move_pt2", true));
                                else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_infinite_" + currentCreatureMove().moveTypeName() + "_move_pt3", false));
                            }
                            else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_" + currentCreatureMove().moveTypeName() + "_move_pt2", false));
                        }
                        else event.getController().clearAnimationCache();
                    }
                    else if (movePos == 1) {
                        if (getPlayingChargedMoveAnim() == 0) {
                            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_start", false));
                        }
                        else if (getPlayingChargedMoveAnim() == 1) {
                            if (currentCreatureMove().useTimeIsInfinite) {
                                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1", true));
                            }
                            else {
                                if (getMoveTwoUse() > 0 && getMoveTwoUse() < currentCreatureMove().maxUse) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1", false));
                                }
                                else if (getMoveTwoUse() >= currentCreatureMove().maxUse) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+ creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1_hold", true));
                                }
                            }
                        }
                        else if (getPlayingChargedMoveAnim() == 2 || getPlayingChargedMoveAnim() == 3 || getPlayingChargedMoveAnim() == 4) {
                            if (currentCreatureMove().useTimeIsInfinite) {
                                if (getPlayingInfiniteMoveAnim()) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_infinite_" + currentCreatureMove().moveTypeName() + "_move_pt2", true));
                                else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_infinite_" + currentCreatureMove().moveTypeName() + "_move_pt3", false));
                            }
                            else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_" + currentCreatureMove().moveTypeName() + "_move_pt2", false));
                        }
                        else event.getController().clearAnimationCache();
                    }
                    else if (movePos == 2) {
                        if (getPlayingChargedMoveAnim() == 0) {
                            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_start", false));
                        }
                        else if (getPlayingChargedMoveAnim() == 1) {
                            if (currentCreatureMove().useTimeIsInfinite) {
                                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1", true));
                            }
                            else {
                                if (getMoveThreeUse() > 0 && getMoveThreeUse() < currentCreatureMove().maxUse) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1", false));
                                }
                                else if (getMoveThreeUse() >= currentCreatureMove().maxUse) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+ creatureType.toString().toLowerCase()+".use_charged_"+currentCreatureMove().moveTypeName()+"_move_pt1_hold", true));
                                }
                            }
                        }
                        else if (getPlayingChargedMoveAnim() == 2 || getPlayingChargedMoveAnim() == 3 || getPlayingChargedMoveAnim() == 4) {
                            if (currentCreatureMove().useTimeIsInfinite) {
                                if (getPlayingChargedMoveAnim() == 3) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_infinite_" + currentCreatureMove().moveTypeName() + "_move_pt2", true));
                                }
                                else if (getPlayingChargedMoveAnim() == 4) {
                                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_infinite_" + currentCreatureMove().moveTypeName() + "_move_pt3", false));
                                }
                            }
                            else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".use_charged_" + currentCreatureMove().moveTypeName() + "_move_pt2", false));
                        }
                        else event.getController().clearAnimationCache();
                    }
                    return PlayState.CONTINUE;
                }
                else {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
            }
        }));
        //for use of large weapons
        data.addAnimationController(new AnimationController(this, "useLargeWeapon", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                //for use of catapult
                if (getLargeWeapon() == RiftLargeWeaponType.CATAPULT) {
                    if (getUsingLargeWeapon() && canFireLargeWeapon()) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".charge_catapult", true));
                    else {
                        if (isFiringCatapult()) {
                            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation." + creatureType.toString().toLowerCase() + ".launch_catapult", false));
                            return PlayState.CONTINUE;
                        }
                        else {
                            event.getController().clearAnimationCache();
                            return PlayState.STOP;
                        }
                    }
                }

                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public SoundEvent getWarnSound() {
        return null;
    }

    public class RiftCreatureInventory extends InventoryBasic {
        public RiftCreatureInventory(String inventoryTitle, int slotCount, RiftCreature creature) {
            super(inventoryTitle, false, slotCount);
            this.addInventoryChangeListener(new RiftCreatureInvListener(creature));
        }

        public void setInventoryFromData(RiftChangeInventoryFromMenu.RiftCreatureInvData data) {
            ItemStack[] contents = data.getInventoryContents();

            if (contents.length != getSizeInventory()) {
                throw new IllegalArgumentException("Invalid inventory size");
            }

            for (int i = 0; i < getSizeInventory(); i++) {
                setInventorySlotContents(i, contents[i]);
            }
        }

        @Override
        public ItemStack addItem(ItemStack stack) {
            ItemStack itemstack = stack.copy();
            for (int i = creatureType.gearSlotCount(); i < getSizeInventory(); ++i) {
                ItemStack itemstack1 = this.getStackInSlot(i);

                if (itemstack1.isEmpty()) {
                    this.setInventorySlotContents(i, itemstack);
                    this.markDirty();
                    return ItemStack.EMPTY;
                }

                if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
                    int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
                    int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

                    if (k > 0) {
                        itemstack1.grow(k);
                        itemstack.shrink(k);

                        if (itemstack.isEmpty()) {
                            this.markDirty();
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (itemstack.getCount() != stack.getCount()) this.markDirty();

            return itemstack;
        }

        public void removeItemStackStartingFromLast(ItemStack stack, int decrementAmount) {
            ItemStack itemStack = stack.copy();
            for (int x = this.getSizeInventory() - 1; x >= creatureType.gearSlotCount(); x--) {
                if (this.getStackInSlot(x).getItem() == itemStack.getItem() && this.getStackInSlot(x).getMetadata() == itemStack.getMetadata()) {
                    if (itemStack.getCount() - decrementAmount >= 0) this.getStackInSlot(x).setCount(itemStack.getCount() - decrementAmount);
                    else this.getStackInSlot(x).setCount(0);
                }
            }
        }

        public boolean isEmptyExceptSaddle() {
            for (int i = 0; i < getSizeInventory(); ++i) {
                if (i == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE)) continue;
                if (!this.getStackInSlot(i).isEmpty()) return false;
            }
            return true;
        }

        //this is just here for testing purposes
        @Override
        public String toString() {
            String toReturn = "[";
            for (int x = 0; x < slotCount(); x++) {
                if (!this.getStackInSlot(x).isEmpty()) {
                    toReturn += "{Slot:"+x+",Item:"+this.getStackInSlot(x).getItem().getTranslationKey()+",Count:"+this.getStackInSlot(x).getCount()+"}";
                    if (x < slotCount() - 1) toReturn += ",";
                }
            }
            toReturn += "]";
            return toReturn;
        }
    }

    class RiftCreatureInvListener implements IInventoryChangedListener {
        private RiftCreature creature;

        public RiftCreatureInvListener(RiftCreature creature) {
            this.creature = creature;
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {
            this.creature.refreshInventory();
        }
    }


    class HerdData implements IEntityLivingData {
        public final RiftCreature herdLeader;

        public HerdData(@Nonnull RiftCreature creature) {
            this.herdLeader = creature;
        }
    }
}
