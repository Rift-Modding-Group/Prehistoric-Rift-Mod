package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.SSRCompatUtils;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.PlayerJournalProgress;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftSac;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.*;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.*;
import com.google.common.base.Predicate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class RiftCreature extends EntityTameable implements IAnimatable {
    private static final DataParameter<Integer> LEVEL = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> XP = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RANGED_ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.<Boolean>createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> TURRET_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ENERGY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ACTING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_LEFT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_LEFT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEFT_CLICK_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEFT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CAN_USE_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> RIGHT_CLICK_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RIGHT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CAN_USE_MIDDLE_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_MIDDLE_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MIDDLE_CLICK_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CAN_USE_SPACEBAR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_SPACEBAR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SPACEBAR_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPACEBAR_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> JUST_SPAWNED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TAME_PROGRESS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_HOME_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> UNCLAIMED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> INCAPACITATED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> INCAP_COUNTDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> UNCLAIM_TIMER = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CLIMBING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private int energyMod;
    private int energyRegenMod;
    private int energyRegenModDelay;
    public int energyActionMod;
    private int energyActionModCountdown;
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
    protected EntityLivingBase ssrTarget;
    public double minCreatureHealth = 20D;
    public double maxCreatureHealth = 20D;
    protected double speed;
    protected double waterSpeed = 1.5D;
    protected int herdCheckCountdown;
    public float attackWidth;
    public float rangedWidth;
    private BlockPos homePosition;
    public double yFloatPos;
    public String[] favoriteFood;
    public String[] tamingFood;
    public int chargeCooldown;
    public int forcedChargePower;
    public int leapCooldown;
    public float maxRightClickCooldown;
    public String saddleItem;
    public int forcedBreakBlockRad = 0;
    public RiftCreaturePart headPart;
    public RiftCreaturePart bodyPart;
    public float oldScale;
    public boolean changeSitFlag;
    private int healthRegen;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;
    protected double attackDamage;
    public double healthLevelMultiplier;
    public double damageLevelMultiplier;
    protected int densityLimit;
    protected List<String> targetList;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.ignoreFrustumCheck = true;
        this.creatureType = creatureType;
        this.setSpeed(0f);
        this.setWaterSpeed(1f);
        this.setScaleForAge(false);
        this.initInventory();
        this.energyMod = 0;
        this.energyRegenMod = 0;
        this.energyRegenModDelay = 0;
        this.energyActionMod = 0;
        this.energyActionModCountdown = 0;
        this.eatFromInvCooldown = 0;
        this.eatFromInvForEnergyCooldown = 0;
        this.eatFromInvForGrowthCooldown = 0;
        this.informLowEnergy = false;
        this.informNoEnergy = false;
        this.cannotUseRightClick = true;
        this.heal((float)maxCreatureHealth);
        this.herdCheckCountdown = 0;
        this.yFloatPos = 0D;
        this.chargeCooldown = 0;
        this.maxRightClickCooldown = 100f;
        this.oldScale = 0;
        this.changeSitFlag = false;
        this.healthRegen = 0;
        this.resetParts(0);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(XP, 0);
        this.dataManager.register(ATTACKING, false);
        this.dataManager.register(RANGED_ATTACKING, false);
        this.dataManager.register(LEAPING, false);
        this.dataManager.register(VARIANT, rand.nextInt(4));
        this.dataManager.register(STATUS, (byte) TameStatusType.STAND.ordinal());
        this.dataManager.register(BEHAVIOR, (byte) TameBehaviorType.ASSIST.ordinal());
        this.dataManager.register(TURRET_TARGET, (byte) TurretModeTargeting.HOSTILES.ordinal());
        this.dataManager.register(SADDLED, false);
        this.dataManager.register(ENERGY, 20);
        this.dataManager.register(ACTING, false);
        this.dataManager.register(CAN_USE_LEFT_CLICK, true);
        this.dataManager.register(USING_LEFT_CLICK, false);
        this.dataManager.register(LEFT_CLICK_USE, 0);
        this.dataManager.register(LEFT_CLICK_COOLDOWN, 0);
        this.dataManager.register(CAN_USE_RIGHT_CLICK, false);
        this.dataManager.register(USING_RIGHT_CLICK, false);
        this.dataManager.register(RIGHT_CLICK_USE, 0);
        this.dataManager.register(RIGHT_CLICK_COOLDOWN, 0);
        this.dataManager.register(CAN_USE_MIDDLE_CLICK, true);
        this.dataManager.register(USING_MIDDLE_CLICK, false);
        this.dataManager.register(MIDDLE_CLICK_USE, 0);
        this.dataManager.register(CAN_USE_SPACEBAR, true);
        this.dataManager.register(USING_SPACEBAR, false);
        this.dataManager.register(SPACEBAR_USE, 0);
        this.dataManager.register(SPACEBAR_COOLDOWN, 0);
        this.dataManager.register(HAS_TARGET, false);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(JUST_SPAWNED, true);
        this.dataManager.register(TAME_PROGRESS, 0);
        this.dataManager.register(HAS_HOME_POS, false);
        this.dataManager.register(UNCLAIMED, false);
        this.dataManager.register(INCAPACITATED, false);
        this.dataManager.register(INCAP_COUNTDOWN, 1200);
        this.dataManager.register(SLEEPING, false);
        this.dataManager.register(UNCLAIM_TIMER, 0);
        this.dataManager.register(CLIMBING, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(@Nonnull DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        super.onInitialSpawn(difficulty, livingdata);
        this.setAgeInDays(1);
        //manage level based on distance from 0, 0
        double distFromCenter = Math.sqrt(this.posX * this.posX + this.posZ * this.posZ);
        double level = Math.floor((distFromCenter / GeneralConfig.levelingRadius)) * GeneralConfig.levelingRadisIncrement + RiftUtil.randomInRange(1, 10) + this.levelAddFromDifficulty();
        this.setLevel(RiftUtil.clamp((int) level, 0, 100));
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
        if (this.world.isRemote) this.setScaleForAge(false);
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        if (!this.world.isRemote) {
            this.setHasTarget(this.getAttackTarget() != null);
            this.setAgeInTicks(this.getAgeInTicks() + 1);
            this.manageAttributes();
            if (this.isTamed()) {
                if (this.isUnclaimed()) this.manageUnclaimed();
                this.updateEnergyMove();
                this.updateEnergyActions();
                this.resetEnergyActionMod();
                this.lowEnergyEffects();
                this.eatFromInventory();
                if (this.isBeingRidden()) this.informRiderEnergy();
                this.manageTargetingBySitting();
                if (this.canNaturalRegen()) {
                    if (this.getHealth() < this.getMaxHealth()) this.naturalRegen();
                    else this.healthRegen = 0;
                }
                this.manageXPAndLevel();
                this.manageIncapacitated();
            }
        }
        if (this.world.isRemote) this.setControls();
        if (this.canDoHerding()) this.manageHerding();
        this.updateParts();
        this.resetParts(this.getRenderSizeModifier());
    }

    @SideOnly(Side.CLIENT)
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (this.isBeingRidden()) {
            if (this.getControllingPassenger().equals(player)) {
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 0, settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 1, !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 2, settings.keyBindJump.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 3, !settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && settings.keyBindPickBlock.isKeyDown()));

                if (settings.keyBindAttack.isKeyDown() && !this.isActing() && this.getLeftClickCooldown() == 0) {
                    if (RiftUtil.isUsingSSR()) {
                        Entity toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                        if (this.hasLeftClickChargeBar()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                        }
                        else {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,0));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,0));
                            }
                        }
                    }
                    else {
                        if (this.hasLeftClickChargeBar()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                        }
                        else {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 0));
                        }
                    }
                }
                else if (settings.keyBindUseItem.isKeyDown() && !this.isActing() && this.getRightClickCooldown() == 0 && this.canUseRightClick() && !(player.getHeldItemMainhand().getItem() instanceof ItemFood) && !(player.getHeldItemMainhand().getItem() instanceof ItemMonsterPlacer) && !RiftUtil.checkInMountItemWhitelist(player.getHeldItemMainhand().getItem())) {
                    if (this.hasRightClickChargeBar()) {
                        if (this.alwaysShowRightClickUse()) RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, this.getRightClickUse()));
                        RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 1));
                    }
                    else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, 0));
                }
                else if (!settings.keyBindUseItem.isKeyDown() && !this.canUseRightClick()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(this, 1, true));
                }
                else if (settings.keyBindJump.isKeyDown() && this.getSpacebarCooldown() == 0) {
                    if (this.hasSpacebarChargeBar()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 2));
                    }
                }
                else if (settings.keyBindPickBlock.isKeyDown() && !this.isActing()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 3));
                }
                else if (!settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()) {
                    Entity toBeAttacked = null;
                    if (RiftUtil.isUsingSSR()) toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                    if (this.hasLeftClickChargeBar()) {
                        if (this.getLeftClickUse() > 0) {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,0, this.getLeftClickUse()));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,0, this.getLeftClickUse()));
                            }
                        }
                    }
                    if (this.hasRightClickChargeBar()) {
                        if (this.getRightClickUse() > 0 && !this.alwaysShowRightClickUse()) {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId, 1, this.getRightClickUse()));
                            }
                            else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, this.getRightClickUse()));
                        }
                    }
                    if (this.hasSpacebarChargeBar()) {
                        if (this.getSpacebarUse() > 0) RiftMessages.WRAPPER.sendToAll(new RiftMountControl(this, -1, 2, this.getSpacebarUse()));
                    }
                    if (this.getMiddleClickUse() > 0) {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 3));
                        this.setMiddleClickUse(0);
                    }
                }
            }
        }
    }

    private void manageIncapacitated() {
        if (this.isIncapacitated()) {
            this.setIncapTimer(this.getIncapTimer() - 1);
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

    public void updateParts() {
        if (this.headPart != null) this.headPart.onUpdate();
        if (this.bodyPart != null) this.bodyPart.onUpdate();
    }

    public abstract void resetParts(float scale);

    public void removeParts() {
        if (this.headPart != null) {
            this.world.removeEntityDangerously(this.headPart);
            this.headPart = null;
        }
        if (this.bodyPart != null) {
            this.world.removeEntityDangerously(this.bodyPart);
            this.bodyPart = null;
        }
    }

    private void naturalRegen() {
        if (this.healthRegen <= 100) {
            this.healthRegen++;
        }
        else {
            this.healthRegen = 0;
            this.heal(2f);
        }
    }

    @Override
    public void setScaleForAge(boolean child) {
        float scale = RiftUtil.clamp(Math.min((0.75f/24000f) * (this.getAgeInTicks() - 24000f) + 1f, 1f), 0.25f, 1f);
        this.setScale(scale);
    }

    private void manageUnclaimed() {
        this.setUnclaimTimer(this.getUnclaimTimer() + 1);
        if (this.getUnclaimTimer() >= 24000) {
            this.setTamed(false);
            this.setTameStatus(TameStatusType.WANDER);
            this.clearHomePos();
            this.setUnclaimTimer(0);
            for (int i = 0; i < this.creatureInventory.getSizeInventory(); i++) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                EntityItem entityItem = new EntityItem(this.world, this.posX, this.posY + this.getEyeHeight(), this.posZ, itemInSlot);
                if (this instanceof Apatosaurus) {
                    if (itemInSlot.getItem() == RiftItems.APATOSAURUS_PLATFORM) this.setSaddled(false);
                }
                else {
                    if (this.saddleItemEqual(itemInSlot)) this.setSaddled(false);
                }
                this.world.spawnEntity(entityItem);
                this.creatureInventory.setInventorySlotContents(i, new ItemStack(Items.AIR));
            }
        }
    }

    private void updateEnergyMove() {
        if (this.isMoving(false) && !this.isActing()) {
            this.energyMod++;
            this.energyRegenMod = 0;
            this.energyRegenModDelay = 0;
            if (this.isBeingRidden()) {
                boolean isSprinting = this.getControllingPassenger() != null && this.getControllingPassenger().isSprinting();
                if (this.energyMod > (int)((double)this.creatureType.getMaxEnergyModMovement(this.getLevel()) * (isSprinting ? 0.75D : 1D))) {
                    this.setEnergy(this.getEnergy() - 1);
                    this.energyMod = 0;
                }
            }
            else {
                if (this.energyMod > this.creatureType.getMaxEnergyModMovement(this.getLevel())) {
                    this.setEnergy(this.getEnergy() - 1);
                    this.energyMod = 0;
                }
            }
        }
        else if (!this.isMoving(false) && !this.isActing()) {
            this.energyMod = 0;
            if (this.energyRegenModDelay <= 20) this.energyRegenModDelay++;
            else this.energyRegenMod++;
            if (this.energyRegenMod > this.creatureType.getMaxEnergyRegenMod(this.getLevel())) {
                this.setEnergy(this.getEnergy() + 1);
                this.energyRegenMod = 0;
                this.energyActionMod = 0;
            }
        }
    }

    private void updateEnergyActions() {
        if (this.energyActionMod >= this.creatureType.getMaxEnergyModAction(this.getLevel())) {
            this.setEnergy(this.getEnergy() - 2);
            this.energyActionMod = 0;
        }
    }

    private void resetEnergyActionMod() {
        if (!this.isActing() && this.energyActionMod > 0) {
            this.energyActionModCountdown++;
            if (this.energyActionModCountdown > 60) {
                this.energyActionMod = 0;
                this.energyActionModCountdown = 0;
            }
        }
    }

    private void lowEnergyEffects() {
        if (this.getEnergy() > 0 && this.getEnergy() <= 6) this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 2));
        else if (this.getEnergy() == 0) this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 255));
    }

    private void eatFromInventory() {
        int minSlot = this.canBeSaddled() ? 1 : 0;
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

        if (this.getEnergy() < 20) {
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

    private void informRiderEnergy() {
        if (!this.informLowEnergy && this.getEnergy() <= 6 && this.getEnergy() > 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.low_energy", this.getName()), false);
            this.informLowEnergy = true;
        }
        if (this.informLowEnergy && this.getEnergy() > 6) {
            this.informLowEnergy = false;
        }
        if (!this.informNoEnergy && this.getEnergy() == 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.no_energy", this.getName()), false);
            this.informNoEnergy = true;
        }
        if (this.informNoEnergy && this.getEnergy() > 0) this.informNoEnergy = false;
    }

    private void manageTargetingBySitting() {
        if (!this.isBeingRidden()) this.setSitting(this.getTameStatus() == TameStatusType.SIT);
        else this.setSitting(this.getAttackTarget() == null);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) this.applyEnchantments(this, entityIn);
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    @Override
    protected boolean canDespawn() {
        return false;
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
        if (this.isTamed() && !this.isIncapacitated()) {
            if (this.getOwner() != null) {
                if (this.isOwner(player)) {
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
                    else if ((this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL) && this.getHealth() >= this.getMaxHealth() && !this.isBaby() && this.getTameStatus() != TameStatusType.SIT) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setInLove(player);
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if (this.isEnergyRegenItem(itemstack) && this.getEnergy() < 20) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setEnergy(this.getEnergy() + this.getEnergyRegenItemValue(itemstack));
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if (itemstack.getItem() instanceof ItemPotion) {
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
                        if (this instanceof IImpregnable) {
                            if (((IImpregnable)this).isPregnant() && player.isSneaking()) player.openGui(RiftInitialize.instance, ServerProxy.GUI_EGG, world, this.getEntityId() ,0, 0);
                            else player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId(), 0, 0);
                        }
                        else player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId() ,0, 0);
                    }
                    else if (itemstack.isEmpty() && this.isSaddled() && !player.isSneaking() && !this.isSleeping() && !this.getTameStatus().equals(TameStatusType.TURRET_MODE)) {
                        if (this instanceof IImpregnable) {
                            if (!((IImpregnable)this).isPregnant()) RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                            else player.openGui(RiftInitialize.instance, ServerProxy.GUI_EGG, world, this.getEntityId() ,0, 0);
                        }
                        else if ((this instanceof IWorkstationUser) || (this instanceof ILeadWorkstationUser)) {
                            boolean usingWorkstation = this instanceof IWorkstationUser && ((IWorkstationUser) this).isUsingWorkstation();
                            boolean usingLeadForWork = this instanceof ILeadWorkstationUser && ((ILeadWorkstationUser) this).isUsingLeadForWork();
                            if (!usingWorkstation && !usingLeadForWork) RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                        }
                        else RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                    }
                    else if (itemstack.isEmpty() && this.isSaddled() && player.isSneaking()) {
                        player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId() ,0, 0);
                    }
                }
                else {
                    player.sendStatusMessage(new TextComponentTranslation("reminder.not_creature_owner", this.getOwner().getName()), false);
                }
            }
            else {
                ClientProxy.popupFromRadial = PopupFromRadial.CLAIM;
                RiftMessages.WRAPPER.sendToServer(new RiftOpenPopupFromRadial(this));
            }
            return true;
        }
        else if (this.isTamed() && this.isIncapacitated()) {
            if (!itemstack.isEmpty() && itemstack.getItem().equals(RiftItems.REVIVAL_MIX)) {
                this.consumeItemFromStack(player, itemstack);
                this.setHealth((float)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue()/4f);
                this.setIncapTimer(1200);
                this.setIncapacitated(false);
                return true;
            }
        }
        else if (!this.isTamed() && !this.isSleeping()) {
            if (!itemstack.isEmpty() && (this.creatureType != RiftCreatureType.DODO) && (this.isTameableByFeeding() && this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL) && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                if (this.getTamingFoodAdd(itemstack) + this.getTameProgress() >= 100) {
                    this.consumeItemFromStack(player, itemstack);
                    this.spawnHeartParticles();
                    if (!this.world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished", new TextComponentString(this.getName())), false);
                    this.setTameProgress(0);
                    this.setTamedBy(player);
                    this.setAttackTarget(null);
                    if (this.isBaby()) this.setTameBehavior(TameBehaviorType.PASSIVE);
                    this.world.setEntityState(this, (byte)7);
                    if (!player.world.isRemote) {
                        PlayerJournalProgress journalProgress = EntityPropertiesHandler.INSTANCE.getProperties(player, PlayerJournalProgress.class);
                        if (!journalProgress.getUnlockedCreatures().contains(this.creatureType)) {
                            journalProgress.unlockCreature(this.creatureType);
                            player.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", this.creatureType.getTranslatedName(), RiftControls.openJournal.getDisplayName()), false);
                        }
                    }
                    this.enablePersistence();
                }
                else {
                    this.consumeItemFromStack(player, itemstack);
                    this.setTameProgress(this.getTameProgress() + this.getTamingFoodAdd(itemstack));
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    this.spawnItemCrackParticles(itemstack.getItem());
                }
                return true;
            }
            else if (!itemstack.isEmpty() && (this.creatureType == RiftCreatureType.DODO) && (this.isTamingFood(itemstack) || itemstack.getItem() == RiftItems.CREATIVE_MEAL)) {
                this.consumeItemFromStack(player, itemstack);
                this.setInLove(player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.spawnItemCrackParticles(itemstack.getItem());
                return true;
            }
        }
        return false;
    }

    protected void manageAttributes() {
        double healthValue = ((this.maxCreatureHealth - this.minCreatureHealth)/24000D) * (this.getAgeInTicks() - 24000D) + this.maxCreatureHealth;
        double baseHealthValue = RiftUtil.clamp(Math.floor(healthValue), this.minCreatureHealth, this.maxCreatureHealth);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(baseHealthValue + (this.healthLevelMultiplier) * (this.getLevel() - 1) * baseHealthValue);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.attackDamage + (double)Math.round((this.getLevel() - 1) * this.damageLevelMultiplier));
        if (this.justSpawned()) {
            this.heal((float) (this.maxCreatureHealth + (0.1D) * (this.getLevel() - 1) * this.maxCreatureHealth));
            this.setSpeed(this.speed);
            this.setWaterSpeed(this.waterSpeed);
            this.setJustSpawned(false);
        }
    }

    public boolean isFavoriteFood(ItemStack stack) {
        int matches = 0;
        for (String foodItem : this.favoriteFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId)) && (stack.getMetadata() == itemData) || (itemData == -1)) matches++;
        }
        return matches > 0;
    }

    public int getFavoriteFoodHeal(ItemStack stack) {
        for (String foodItem : this.favoriteFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            double percentage = Double.parseDouble(foodItem.substring(itemIdThird + 1));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == -1) return (int) (Math.ceil(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() * percentage));
                else if (stack.getMetadata() == itemData) {
                    return (int) (Math.ceil(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() * percentage));
                }
            }
        }
        return 0;
    }

    public boolean isTamingFood(ItemStack stack) {
        for (String foodItem : this.tamingFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) return (stack.getMetadata() == itemData) || (itemData == -1);
        }
        return false;
    }

    public int getTamingFoodAdd(ItemStack stack) {
        for (String foodItem : this.tamingFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            int adder = (int)(Double.parseDouble(foodItem.substring(itemIdThird + 1)) * 100);
            int levelMod = (int)Math.ceil((double)this.getLevel() / 10D);
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == -1) return adder / levelMod;
                else if (stack.getMetadata() == itemData) return adder / levelMod;
            }
        }
        return !stack.isEmpty() && stack.getItem() == RiftItems.CREATIVE_MEAL ? 100 : 0;
    }

    public int getFavoriteFoodGrowth(ItemStack stack) {
        for (String foodItem : this.favoriteFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            double percentage = Double.parseDouble(foodItem.substring(itemIdThird + 1)) / 2D;
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == -1) return (int)(24000 * percentage);
                else if (stack.getMetadata() == itemData) return (int)(24000 * percentage);
            }
        }
        return 0;
    }

    public boolean isEnergyRegenItem(ItemStack stack) {
        CreatureDiet diet = this.creatureType.getCreatureDiet();
        List<String> itemList = new ArrayList<>();
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);

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
        CreatureDiet diet = this.creatureType.getCreatureDiet();
        List<String> itemList = new ArrayList<>();
        if (diet == CreatureDiet.HERBIVORE || diet == CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (diet == CreatureDiet.CARNIVORE || diet == CreatureDiet.PISCIVORE || diet == CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);

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
        this.setEnergy(this.getEnergy() + this.getEnergyRegenItemValue(itemStack));
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

    public Item getSaddleItem() {
        int itemIdFirst = this.saddleItem.indexOf(":");
        int itemIdSecond = this.saddleItem.indexOf(":", itemIdFirst + 1);
        String itemId = this.saddleItem.substring(0, itemIdSecond);
        return Item.getByNameOrId(itemId);
    }

    public int getSaddleItemData() {
        int itemIdFirst = this.saddleItem.indexOf(":");
        int itemIdSecond = this.saddleItem.indexOf(":", itemIdFirst + 1);
        return Integer.parseInt(this.saddleItem.substring(itemIdSecond + 1));
    }

    public boolean saddleItemEqual(ItemStack itemStack) {
        int itemStackId = itemStack.getMetadata();
        if (this.getSaddleItemData() == -1) return itemStack.getItem().equals(this.getSaddleItem());
        else return this.getSaddleItemData() == itemStackId && itemStack.getItem().equals(this.getSaddleItem());
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
        compound.setInteger("Variant", this.getVariant());
        compound.setByte("TameStatus", (byte) this.getTameStatus().ordinal());
        compound.setByte("TameBehavior", (byte) this.getTameBehavior().ordinal());
        compound.setByte("TurretTargeting", (byte) this.getTurretTargeting().ordinal());
        compound.setBoolean("Saddled", this.isSaddled());
        if (this.creatureType != null) {
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
        if (this.homePosition != null && this.getHasHomePos()) {
            compound.setInteger("HomePosX", this.homePosition.getX());
            compound.setInteger("HomePosY", this.homePosition.getY());
            compound.setInteger("HomePosZ", this.homePosition.getZ());
        }
        compound.setBoolean("Unclaimed", this.isUnclaimed());
        compound.setInteger("UnclaimTimer", this.getUnclaimTimer());
        compound.setBoolean("Incapacitated", this.isIncapacitated());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLevel(compound.getInteger("Level"));
        this.setXP(compound.getInteger("XP"));
        this.setVariant(compound.getInteger("Variant"));
        if (compound.hasKey("TameStatus")) this.setTameStatus(TameStatusType.values()[compound.getByte("TameStatus")]);
        if (compound.hasKey("TameBehavior")) this.setTameBehavior(TameBehaviorType.values()[compound.getByte("TameBehavior")]);
        if (this.canDoTurretMode() && compound.hasKey("TurretTargeting")) this.setTurretModeTargeting(TurretModeTargeting.values()[compound.getByte("TurretTargeting")]);
        this.setSaddled(compound.getBoolean("Saddled"));
        if (this.creatureInventory != null) {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                int inventorySize = this.slotCount() + (this.canBeSaddled() ? 1 : 0);
                if (j < inventorySize) this.creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        else {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                this.creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        this.setEnergy(compound.getInteger("Energy"));
        this.setHasTarget(compound.getBoolean("HasTarget"));
        this.setAgeInTicks(compound.getInteger("AgeTicks"));
        this.setJustSpawned(compound.getBoolean("JustSpawned"));
        this.setTameProgress(compound.getInteger("TameProgress"));
        if (compound.getBoolean("HasHomePos")) this.setHomePos(compound.getInteger("HomePosX"), compound.getInteger("HomePosY"), compound.getInteger("HomePosZ"));
        this.setUnclaimed(compound.getBoolean("Unclaimed"));
        this.setUnclaimTimer(compound.getInteger("UnclaimTimer"));
        this.setIncapacitated(compound.getBoolean("Incapacitated"));
        if (this.isUnclaimed()) this.setTamed(true);
    }

    private void initInventory() {
        int inventorySize = this.slotCount() + (this.canBeSaddled() ? 1 : 0);
        this.creatureInventory = new RiftCreatureInventory("creatureInventory", inventorySize, this);
        this.creatureInventory.setCustomName(this.getName());
        if (this.creatureInventory != null) {
            for (int i = 0; i < inventorySize; ++i) {
                ItemStack itemStack = this.creatureInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) this.creatureInventory.setInventorySlotContents(i, itemStack.copy());
            }
        }
    }

    public List<String> getTargetList() {
        return this.targetList;
    }

    public abstract float getRenderSizeModifier();

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return this.inFrustrum(camera, this.headPart) || this.inFrustrum(camera, this.bodyPart);
    }

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
        if (this.headPart != null) return !this.headPart.isUnderwater();
        return false;
    }

    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        if (!this.world.isRemote && this.canBeSaddled()) this.setSaddled(this.saddleItemEqual(saddle) && !saddle.isEmpty());
    }

    //herdin stuff starts here
    public boolean canDoHerding() {
        return false;
    }

    public boolean hasHerdLeader() {
        return this.herdLeader != null && this.herdLeader.isEntityAlive();
    }

    public boolean hasNoHerdLeader() {
        return !this.hasHerdLeader();
    }

    public void addToHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
        this.herdLeader.herdSize++;
    }

    public void separateFromHerdLeader() {
        this.herdLeader.herdSize--;
        this.herdLeader = null;
    }

    private void manageHerding() {
        if (this.isHerdLeader() && !this.world.isRemote && this.rand.nextInt(200) == 1) {
            if (this.world.getEntitiesWithinAABB(this.getClass(), this.herdBoundingBox()).size() <= 1) {
                this.herdSize = 1;
            }
        }
    }

    public AxisAlignedBB herdBoundingBox() {
        return this.getEntityBoundingBox().grow(12D);
    }

    public boolean isHerdLeader() {
        return this.herdSize > 1;
    }

    public boolean canAddToHerd() {
        return this.isHerdLeader() && this.herdSize < this.maxHerdSize() && this.canDoHerding();
    }

    public boolean isNearHerdLeader() {
        return this.getDistanceSq(this.herdLeader) <= 144;
    }

    public void addCreatureToHerd(@Nonnull Stream<RiftCreature> stream) {
        try {
            stream.limit(this.maxHerdSize() - this.herdSize).filter(creature -> creature != this).forEach(creature -> creature.addToHerdLeader(this));
        }
        catch (Exception e) {}
    }

    public double followRange() {
        return 0.5D;
    }

    public void followLeader() {
        if (this.hasHerdLeader()) {
            if (!this.getEntityBoundingBox().intersects(this.herdLeader.getEntityBoundingBox().grow(this.followRange()))) {
                this.getMoveHelper().setMoveTo(this.herdLeader.posX, this.herdLeader.posY, this.herdLeader.posZ, 1D);
            }
        }
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public int maxHerdSize() {
        return 5;
    }

    public static class HerdData implements IEntityLivingData {
        public final RiftCreature herdLeader;

        public HerdData(@Nonnull RiftCreature creature) {
            this.herdLeader = creature;
        }
    }
    //herdin stuff stops here

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

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    public void setAttacking(boolean value) {
        this.dataManager.set(ATTACKING, value);
        this.setActing(value);
    }

    public boolean isRangedAttacking() {
        return this.dataManager.get(RANGED_ATTACKING);
    }

    public void setRangedAttacking(boolean value) {
        this.dataManager.set(RANGED_ATTACKING, value);
        this.setActing(value);
    }

    public boolean isLeaping() {
        return this.dataManager.get(LEAPING);
    }

    public void setLeaping(boolean value) {
        this.dataManager.set(LEAPING, value);
        this.setActing(value);
    }

    public TameStatusType getTameStatus() {
        return TameStatusType.values()[this.dataManager.get(STATUS).byteValue()];
    }

    public void setTameStatus(TameStatusType tameStatus) {
        this.dataManager.set(STATUS, (byte) tameStatus.ordinal());
        this.changeSitFlag = tameStatus.equals(TameStatusType.SIT);
    }

    public int getEnergy() {
        return RiftUtil.clamp(this.dataManager.get(ENERGY).intValue(), 0, 20);
    }

    public void setEnergy(int energy) {
        this.dataManager.set(ENERGY, RiftUtil.clamp(energy, 0, 20));
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

    public void resetSpeed() {
        this.setSpeed(this.speed);
        this.setWaterSpeed(this.waterSpeed);
    }

    public void removeSpeed() {
        this.setSpeed(0D);
        this.setWaterSpeed(0D);
    }

    public boolean canMove() {
        return this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() > 0D;
    }

    public TameBehaviorType getTameBehavior() {
        return TameBehaviorType.values()[this.dataManager.get(BEHAVIOR).byteValue()];
    }
    public void setTameBehavior(TameBehaviorType tameBehavior) {
        this.dataManager.set(BEHAVIOR, (byte) tameBehavior.ordinal());
    }

    public TurretModeTargeting getTurretTargeting() {
        return TurretModeTargeting.values()[this.dataManager.get(TURRET_TARGET).byteValue()];
    }
    public void setTurretModeTargeting(TurretModeTargeting turretModeTargeting) {
        this.dataManager.set(TURRET_TARGET, (byte) turretModeTargeting.ordinal());
    }

    public boolean isSaddled() {
        return this.dataManager.get(SADDLED);
    }

    public void setSaddled(boolean value) {
        this.dataManager.set(SADDLED, Boolean.valueOf(value));
    }

    public boolean isActing() {
        return this.dataManager.get(ACTING);
    }

    public void setActing(boolean value) {
        this.dataManager.set(ACTING, Boolean.valueOf(value));
    }

    public boolean canUseLeftClick() {
        return this.dataManager.get(CAN_USE_LEFT_CLICK);
    }

    public void setCanUseLeftClick(boolean value) {
        this.dataManager.set(CAN_USE_LEFT_CLICK, Boolean.valueOf(value));
    }

    public boolean isUsingLeftClick() {
        return this.dataManager.get(USING_LEFT_CLICK);
    }

    public void setUsingLeftClick(boolean value) {
        this.dataManager.set(USING_LEFT_CLICK, Boolean.valueOf(value));
    }

    public int getLeftClickUse() {
        return this.dataManager.get(LEFT_CLICK_USE).intValue();
    }

    public void setLeftClickUse(int value) {
        this.dataManager.set(LEFT_CLICK_USE, value);
    }

    public int getLeftClickCooldown() {
        return Math.max(0, this.dataManager.get(LEFT_CLICK_COOLDOWN));
    }

    public void setLeftClickCooldown(int value) {
        this.dataManager.set(LEFT_CLICK_COOLDOWN, Math.max(0, value));
    }

    public boolean canUseRightClick() {
        return this.dataManager.get(CAN_USE_RIGHT_CLICK);
    }

    public void setCanUseRightClick(boolean value) {
        this.dataManager.set(CAN_USE_RIGHT_CLICK, value);
    }

    public boolean isUsingRightClick() {
        return this.dataManager.get(USING_RIGHT_CLICK);
    }

    public void setUsingRightClick(boolean value) {
        this.dataManager.set(USING_RIGHT_CLICK, value);
    }

    public int getRightClickUse() {
        return this.dataManager.get(RIGHT_CLICK_USE).intValue();
    }

    public void setRightClickUse(int value) {
        this.dataManager.set(RIGHT_CLICK_USE, value);
    }

    public boolean alwaysShowRightClickUse() {
        return false;
    }

    public int getRightClickCooldown() {
        return this.dataManager.get(RIGHT_CLICK_COOLDOWN);
    }

    public void setRightClickCooldown(int value) {
        this.dataManager.set(RIGHT_CLICK_COOLDOWN, value);
    }

    public boolean canUseMiddleClick() {
        return this.dataManager.get(CAN_USE_MIDDLE_CLICK);
    }

    public void setCanUseMiddleClick(boolean value) {
        this.dataManager.set(CAN_USE_MIDDLE_CLICK, value);
    }

    public boolean isUsingMiddleClick() {
        return this.dataManager.get(USING_MIDDLE_CLICK);
    }

    public void setUsingMiddleClick(boolean value) {
        this.dataManager.set(USING_MIDDLE_CLICK, value);
    }

    public int getMiddleClickUse() {
        return this.dataManager.get(MIDDLE_CLICK_USE);
    }

    public void setMiddleClickUse(int value) {
        this.dataManager.set(MIDDLE_CLICK_USE, value);
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

    public int getSpacebarCooldown() {
        return this.dataManager.get(SPACEBAR_COOLDOWN);
    }

    public void setSpacebarCooldown(int value) {
        this.dataManager.set(SPACEBAR_COOLDOWN, value);
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

    public boolean isUnclaimed() {
        return this.dataManager.get(UNCLAIMED);
    }

    public void setUnclaimed(boolean value) {
        this.dataManager.set(UNCLAIMED, value);
    }

    public boolean isIncapacitated() {
        return this.dataManager.get(INCAPACITATED);
    }

    public void setIncapacitated(boolean value) {
        this.dataManager.set(INCAPACITATED, value);
    }

    public int getIncapTimer() {
        return this.dataManager.get(INCAP_COUNTDOWN);
    }

    public void setIncapTimer(int value) {
        this.dataManager.set(INCAP_COUNTDOWN, value);
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setSleeping(boolean value) {
        this.dataManager.set(SLEEPING, value);
        this.removePassengers();
    }

    public int getUnclaimTimer() {
        return this.dataManager.get(UNCLAIM_TIMER);
    }

    public void setUnclaimTimer(int value) {
        this.dataManager.set(UNCLAIM_TIMER, value);
    }

    public boolean isClimbing() {
        return this.dataManager.get(CLIMBING);
    }

    public void setClimbing(boolean value) {
        this.dataManager.set(CLIMBING, value);
    }

    public boolean justSpawned() {
        return this.dataManager.get(JUST_SPAWNED);
    }

    public void setJustSpawned(boolean value) {
        this.dataManager.set(JUST_SPAWNED, value);
    }

    public void setHomePos() {
        this.dataManager.set(HAS_HOME_POS, true);
        this.homePosition = new BlockPos(this);
    }

    public void setHomePos(int x, int y, int z) {
        this.dataManager.set(HAS_HOME_POS, true);
        this.homePosition = new BlockPos(x, y, z);
    }

    public void clearHomePos() {
        this.dataManager.set(HAS_HOME_POS, false);
        this.homePosition = null;
    }

    public boolean getHasHomePos() {
        return this.dataManager.get(HAS_HOME_POS);
    }

    public BlockPos getHomePos() {
        return this.homePosition;
    }

    public boolean busyAtWork() {
        boolean usingWorkstation = (this instanceof IWorkstationUser) && ((IWorkstationUser)this).isUsingWorkstation();
        boolean usingLeadForWork = (this instanceof ILeadWorkstationUser) && ((ILeadWorkstationUser)this).isUsingLeadForWork();
        return  usingWorkstation || usingLeadForWork;
    }

    public boolean isBaby() {
        return this.getAgeInDays() < 1;
    }

    public boolean isMoving(boolean includeY) {
        double fallMotion = !this.onGround && includeY ? this.motionY : 0;
        return Math.sqrt((this.motionX * this.motionX) + (fallMotion * fallMotion) + (this.motionZ * this.motionZ)) > 0;
    }

    public boolean isInCave() {
        BlockPos pos = new BlockPos(this);
        return !this.world.canSeeSky(pos.up()) && pos.getY() <= 56;
    }

    public boolean canNaturalRegen() {
        return true;
    }

    public boolean isTameableByFeeding() {
        return true;
    }

    public boolean canBeSaddled() {
        return false;
    }

    public int slotCount() {
        return 0;
    }

    public void updatePassenger(Entity passenger) {
        if (this.canBeSteered()) {
            this.rotationYaw = passenger.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = passenger.rotationPitch * 0.5f;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
        }

        passenger.setPosition(riderPos().x, riderPos().y + passenger.height, riderPos().z);

        ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
        if (this.isDead) passenger.dismountRidingEntity();
    }

    public abstract Vec3d riderPos();

    public abstract void controlInput(int control, int holdAmount, EntityLivingBase target);

    public abstract boolean hasLeftClickChargeBar();

    public abstract boolean hasRightClickChargeBar();

    public abstract boolean hasSpacebarChargeBar();

    public boolean canDoTurretMode() {
        return false;
    }

    public boolean checkBasedOnStrength(Block block, IBlockState blockState) {
        switch (this.creatureType.getBlockBreakTier()) {
            case DIRT:
                return RiftUtil.blockWeakerThanDirt(block, blockState);
            case WOOD:
                return RiftUtil.blockWeakerThanWood(block, blockState);
            case STONE:
                return RiftUtil.blockWeakerThanStone(block, blockState);
        }
        return false;
    }

    public void controlAttack() {
        //attack entity
        EntityLivingBase target;
        if (this.ssrTarget == null) target = this.getControlAttackTargets(this.attackWidth);
        else target = this.ssrTarget;
        if (target != null) {
            if (this.isTamed() && target instanceof EntityPlayer) {
                if (!target.getUniqueID().equals(this.getOwnerId())) this.attackEntityAsMob(target);
            }
            else if (this.isTamed() && target instanceof EntityTameable) {
                if (((EntityTameable) target).isTamed()) {
                    if (!((EntityTameable) target).getOwner().equals(this.getOwner())) this.attackEntityAsMob(target);
                }
                else this.attackEntityAsMob(target);
            }
            else this.attackEntityAsMob(target);
        }
        this.ssrTarget = null;

        //break blocks
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        int height = (int)(Math.ceil(this.height)) + (this.isBeingRidden() ? (this.getControllingPassenger() != null ? (int)(Math.ceil(this.getControllingPassenger().height)) : 0) : 0);
        int radius = (int)(Math.ceil(this.width)) + this.forcedBreakBlockRad;
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos tempPos = pos.add(x, y, z);
                    IBlockState iblockstate = this.world.getBlockState(tempPos);
                    Block block = iblockstate.getBlock();
                    if (iblockstate.getMaterial() != Material.AIR && this.checkBasedOnStrength(block, iblockstate)) {
                        this.world.destroyBlock(tempPos, true);
                    }
                }
            }
        }
    }

    public EntityLivingBase getControlAttackTargets(double attackDetectWidth) {
        if (RiftUtil.isUsingSSR()) return null;
        double dist = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX + attackDetectWidth;
        Vec3d vec3d = this.getPositionEyes(1.0F);
        Vec3d vec3d1 = this.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        Entity pointedEntity = null;
        Entity rider = this.getControllingPassenger();
        List<Entity> passengers = this.getPassengers();
        List<Entity> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(1.0D, 1.0D, 1.0D), new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                return !passengers.contains(entity);
            }
        });
        double d2 = d1;
        for (Entity potentialTarget : list) {
            AxisAlignedBB axisalignedbb = potentialTarget.getEntityBoundingBox().grow((double) potentialTarget.getCollisionBorderSize() + 2F);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (potentialTarget != this && potentialTarget != rider) {
                if (axisalignedbb.contains(vec3d)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = potentialTarget;
                        d2 = 0.0D;
                    }
                }
                else if (raytraceresult != null) {
                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        if (potentialTarget.getLowestRidingEntity() == rider.getLowestRidingEntity() && !rider.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = potentialTarget;
                            }
                        }
                        else {
                            pointedEntity = potentialTarget;
                            d2 = d3;
                        }
                    }
                }
            }
        }
        return (EntityLivingBase) pointedEntity;
    }

    public void controlRangedAttack(double strength) {}

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
        if (this.isSaddled() && this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null) {
                if (this.getAttackTarget() != null) {
                    this.setAttackTarget(null);
                    this.getNavigator().clearPath();
                }
                if (!this.canBeSteered()) {
                    this.rotationYaw = controller.rotationYaw;
                    this.prevRotationYaw = this.rotationYaw;
                    this.rotationPitch = controller.rotationPitch * 0.5f;
                    this.setRotation(this.rotationYaw, this.rotationPitch);
                    this.renderYawOffset = this.rotationYaw;
                }
                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;
                this.stepHeight = 1.0F;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                this.fallDistance = 0;
                float moveSpeedMod = (this.getEnergy() > 6 ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = ((float)(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) - riderSpeed) * moveSpeedMod;
                this.setAIMoveSpeed(this.onGround ? moveSpeed + (controller.isSprinting() && this.getEnergy() > 6 ? moveSpeed * 0.3f : 0) : moveSpeed);

                if (forward > 0) {
                    if (this.bodyPart != null) {
                        if (this.bodyPart.isInWater()) {
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
                super.travel(strafe, vertical, forward);
            }
        }
        else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            if (forward > 0) {
                if (this.bodyPart != null) {
                    if (this.bodyPart.isInWater()) {
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
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        boolean leashOperatingFlag = true;
        if (this instanceof IWorkstationUser) {
            leashOperatingFlag = !((IWorkstationUser)this).isUsingWorkstation();
        }
        return !this.getLeashed() && this.isTamed() && !this.getTameStatus().equals(TameStatusType.SIT) && !this.getTameStatus().equals(TameStatusType.TURRET_MODE) && leashOperatingFlag;
    }

    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote && this.creatureInventory != null) {
            for (int i = 0; i < this.creatureInventory.getSizeInventory(); i++) {
                ItemStack itemStack = this.creatureInventory.getStackInSlot(i);
                boolean hasSaddleFlag = !this.canBeSaddled() || i != 0;
                if (!itemStack.isEmpty() && hasSaddleFlag) {
                    this.entityDropItem(itemStack, 0.0f);
                    this.creatureInventory.setInventorySlotContents(i, new ItemStack(Items.AIR));
                }
            }
        }
        this.setTameStatus(TameStatusType.STAND);
    }

    //start of radial menu stuff
    public List<RiftTameRadialChoice> mainRadialChoices() {
        List<RiftTameRadialChoice> list = new ArrayList<>();
        list.add(RiftTameRadialChoice.INVENTORY);
        list.add(RiftTameRadialChoice.STATE);
        if (this.canBeSaddled() && this.isRideable) list.add(RiftTameRadialChoice.RIDE);
        list.add(RiftTameRadialChoice.OPTIONS);
        list.add(RiftTameRadialChoice.BEHAVIOR);
        return list;
    }

    public List<RiftTameRadialChoice> stateRadialChoices() {
        List<RiftTameRadialChoice> list = new ArrayList<>();
        list.add(RiftTameRadialChoice.BACK);
        list.add(RiftTameRadialChoice.STAND);
        list.add(RiftTameRadialChoice.SIT);
        list.add(RiftTameRadialChoice.WANDER);
        if (this.canDoTurretMode()) list.add(RiftTameRadialChoice.TURRET_MODE);
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
        list.add(RiftTameRadialChoice.CHANGE_NAME);
        list.add(RiftTameRadialChoice.SET_HOME);
        list.add(RiftTameRadialChoice.UNCLAIM);
        if (this instanceof IWorkstationUser) list.add(RiftTameRadialChoice.SET_WORKSTATION);
        if (this instanceof IHarvestWhenWandering) list.add(RiftTameRadialChoice.SET_WANDER_HARVEST);
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

    private void sendIncapMessage() {
        if (this.isTamed() && !this.world.isRemote && this.getOwner() instanceof EntityPlayerMP) {
            ((EntityPlayer)this.getOwner()).sendStatusMessage(new TextComponentTranslation("reminder.incapacitated", this.getDisplayName()), false);
        }
    }

    protected void onDeathUpdate() {
        if (GeneralConfig.minRevivalDiff.equals("PEACEFUL") || GeneralConfig.minRevivalDiff.equals("EASY") || GeneralConfig.minRevivalDiff.equals("NORMAL") || GeneralConfig.minRevivalDiff.equals("HARD")) {
            EnumDifficulty diff = EnumDifficulty.valueOf(GeneralConfig.minRevivalDiff);
            if (this.world.getDifficulty().getId() < diff.getId()) {
                if (this.isTamed()) {
                    if (this.getIncapTimer() > 0) {
                        this.deathTime = 0;
                        if (!this.isIncapacitated()) this.sendIncapMessage();
                        this.setIncapacitated(true);
                    }
                    else {
                        this.setIncapacitated(false);
                        if (this.deathTime == 0) this.sendDeathMessage();
                        super.onDeathUpdate();
                    }
                }
                else {
                    if (this.deathTime == 0) this.sendDeathMessage();
                    super.onDeathUpdate();
                }
            }
            else {
                if (this.deathTime == 0) this.sendDeathMessage();
                super.onDeathUpdate();
            }
        }
        else {
            if (GeneralConfig.minRevivalDiff.equals("NONE")) {
                if (this.isTamed()) {
                    if (this.getIncapTimer() > 0) {
                        this.deathTime = 0;
                        if (!this.isIncapacitated()) this.sendIncapMessage();
                        this.setIncapacitated(true);
                    }
                    else {
                        this.setIncapacitated(false);
                        if (this.deathTime == 0) this.sendDeathMessage();
                        super.onDeathUpdate();
                    }
                }
                else {
                    if (this.deathTime == 0) this.sendDeathMessage();
                    super.onDeathUpdate();
                }
            }
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        CreatureCategory category = this.creatureType.getCreatureCategory();
        if (category.equals(CreatureCategory.DINOSAUR) || category.equals(CreatureCategory.REPTILE) || category.equals(CreatureCategory.BIRD) || this.creatureType.equals(RiftCreatureType.DIMETRODON)) {
            RiftEgg egg = new RiftEgg(this.world);
            egg.setCreatureType(this.creatureType);
            egg.setOwnerId(this.getOwnerId());
            egg.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
            egg.enablePersistence();
            egg.setHatchTime(this.creatureType.getHatchTime() * 20);
            return egg;
        }
        else if (category.equals(CreatureCategory.INVERTEBRATE)) {
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
        data.addAnimationController(new AnimationController(this, "incapacitance", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (isIncapacitated() || isSleeping()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".incapacitated", true));
                    return PlayState.CONTINUE;
                }
                event.getController().clearAnimationCache();
                return PlayState.STOP;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public class RiftCreatureInventory extends ContainerHorseChest {
        public RiftCreatureInventory(String inventoryTitle, int slotCount, RiftCreature creature) {
            super(inventoryTitle, slotCount);
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
            for (int i = canBeSaddled() ? 1 : 0; i < getSizeInventory(); ++i) {
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
    }

    class RiftCreatureInvListener implements IInventoryChangedListener {
        RiftCreature creature;

        public RiftCreatureInvListener(RiftCreature creature) {
            this.creature = creature;
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {
            creature.refreshInventory();
        }
    }
}
