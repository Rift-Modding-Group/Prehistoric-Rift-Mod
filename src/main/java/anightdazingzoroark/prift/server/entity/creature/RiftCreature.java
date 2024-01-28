package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.SSRCompatUtils;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.enums.PopupFromRadial;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.*;
import com.google.common.base.Predicate;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class RiftCreature extends EntityTameable implements IAnimatable {
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RANGED_ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOWER_HEAD = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_CHARGE = EntityDataManager.<Boolean>createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> START_CHARGING = EntityDataManager.<Boolean>createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.<Boolean>createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> END_CHARGING = EntityDataManager.<Boolean>createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.<Boolean>createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
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
    private static final DataParameter<Boolean> CAN_USE_SPACEBAR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_SPACEBAR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SPACEBAR_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPACEBAR_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> JUST_SPAWNED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> HERD_LEADER_UUID = EntityDataManager.createKey(RiftCreature.class, DataSerializers.STRING);
    private static final DataParameter<Integer> TAME_PROGRESS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_HOME_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> UNCLAIMED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> UNCLAIM_TIMER = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CLIMBING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> CREATURE_UUID = EntityDataManager.createKey(RiftCreature.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> USING_WORKSTATION = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WORKSTATION_X_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Y_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Z_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
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
    public EntityLivingBase ssrTarget;
    public double minCreatureHealth = 20D;
    public double maxCreatureHealth = 20D;
    public double speed;
    private int herdCheckCountdown;
    public float attackWidth;
    public float rangedWidth;
    public float chargeWidth;
    public float leapWidth;
    private int tickUse;
    private BlockPos homePosition;
    public boolean isFloating;
    private double waterLevel;
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
    public RiftMainBodyPart bodyPart;
    public float oldScale;
    public boolean changeSitFlag;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.setSpeed(0f);
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
        this.tickUse = 0;
        this.isFloating = false;
        this.yFloatPos = 0D;
        this.chargeCooldown = 0;
        this.maxRightClickCooldown = 100f;
        this.oldScale = 0;
        this.changeSitFlag = false;
        this.resetParts(0);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, Boolean.FALSE);
        this.dataManager.register(RANGED_ATTACKING, Boolean.FALSE);
        this.dataManager.register(LOWER_HEAD, Boolean.FALSE);
        this.dataManager.register(CAN_CHARGE, Boolean.TRUE);
        this.dataManager.register(START_CHARGING, Boolean.FALSE);
        this.dataManager.register(CHARGING, Boolean.FALSE);
        this.dataManager.register(END_CHARGING, Boolean.FALSE);
        this.dataManager.register(LEAPING, Boolean.FALSE);
        this.dataManager.register(VARIANT, rand.nextInt(4));
        this.dataManager.register(STATUS, (byte) TameStatusType.STAND.ordinal());
        this.dataManager.register(BEHAVIOR, (byte) TameBehaviorType.ASSIST.ordinal());
        this.dataManager.register(SADDLED, Boolean.FALSE);
        this.dataManager.register(ENERGY, 20);
        this.dataManager.register(ACTING, Boolean.FALSE);
        this.dataManager.register(CAN_USE_LEFT_CLICK, Boolean.TRUE);
        this.dataManager.register(USING_LEFT_CLICK, Boolean.FALSE);
        this.dataManager.register(LEFT_CLICK_USE, 0);
        this.dataManager.register(LEFT_CLICK_COOLDOWN, 0);
        this.dataManager.register(CAN_USE_RIGHT_CLICK, Boolean.FALSE);
        this.dataManager.register(USING_RIGHT_CLICK, Boolean.FALSE);
        this.dataManager.register(RIGHT_CLICK_USE, 0);
        this.dataManager.register(RIGHT_CLICK_COOLDOWN, 0);
        this.dataManager.register(CAN_USE_SPACEBAR, true);
        this.dataManager.register(USING_SPACEBAR, false);
        this.dataManager.register(SPACEBAR_USE, 0);
        this.dataManager.register(SPACEBAR_COOLDOWN, 0);
        this.dataManager.register(HAS_TARGET, Boolean.FALSE);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(JUST_SPAWNED, true);
        this.dataManager.register(HERD_LEADER_UUID, "");
        this.dataManager.register(TAME_PROGRESS, 0);
        this.dataManager.register(HAS_HOME_POS, Boolean.FALSE);
        this.dataManager.register(UNCLAIMED, Boolean.FALSE);
        this.dataManager.register(UNCLAIM_TIMER, 0);
        this.dataManager.register(CLIMBING, false);
        this.dataManager.register(CREATURE_UUID, UUID.randomUUID().toString());
        this.dataManager.register(USING_WORKSTATION, false);
        this.dataManager.register(WORKSTATION_X_POS, 0);
        this.dataManager.register(WORKSTATION_Y_POS, 0);
        this.dataManager.register(WORKSTATION_Z_POS, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setAgeInDays(1);
        return livingdata;
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
//            this.controlWaterMovement();
            if (this.isTamed()) {
                if (this.isUnclaimed()) this.manageUnclaimed();
                this.updateEnergyMove();
                this.updateEnergyActions();
                this.resetEnergyActionMod();
                this.lowEnergyEffects();
                this.eatFromInventory();
                if (this.isBeingRidden()) this.informRiderEnergy();
                this.manageTargetingBySitting();
            }
        }
        if (this.world.isRemote) {
            this.setControls();
        }
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
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 0, settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 1, !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 2, settings.keyBindJump.isKeyDown()));

                if (settings.keyBindAttack.isKeyDown() && !this.isActing() && this.getLeftClickCooldown() == 0) {
                    if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) {
                        if (ShoulderInstance.getInstance().doShoulderSurfing()) {
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
                else if (!settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown()) {
                    Entity toBeAttacked = null;
                    if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
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
                        if (this.getRightClickUse() > 0) {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId, 1, this.getRightClickUse()));
                            }
                            else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, this.getRightClickUse()));
                        }
                    }
                    if (this.hasSpacebarChargeBar()) {
                        if (this.getSpacebarUse() > 0) RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 2, this.getSpacebarUse()));
                    }
                }
            }
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
        if (this.isMoving() && !this.isActing()) {
            this.energyMod++;
            this.energyRegenMod = 0;
            this.energyRegenModDelay = 0;
            if (this.isBeingRidden()) {
                boolean isSprinting = this.getControllingPassenger() != null ? this.getControllingPassenger().isSprinting() : false;
                if (this.energyMod > (int)((double)this.creatureType.getMaxEnergyModMovement() * (isSprinting ? 0.75D : 1D))) {
                    this.setEnergy(this.getEnergy() - 1);
                    this.energyMod = 0;
                }
            }
            else {
                if (this.energyMod > this.creatureType.getMaxEnergyModMovement()) {
                    this.setEnergy(this.getEnergy() - 1);
                    this.energyMod = 0;
                }
            }
        }
        else if (!this.isMoving() && !this.isActing()) {
            this.energyMod = 0;
            if (this.energyRegenModDelay <= 20) this.energyRegenModDelay++;
            else this.energyRegenMod++;
            if (this.energyRegenMod > this.creatureType.getMaxEnergyRegenMod()) {
                this.setEnergy(this.getEnergy() + 1);
                this.energyRegenMod = 0;
                this.energyActionMod = 0;
            }
        }
    }

    private void updateEnergyActions() {
        if (this.energyActionMod >= this.creatureType.getMaxEnergyModAction()) {
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
                if (this.isFavoriteFood(itemInSlot) && this.eatFromInvCooldown > 60  && !RiftUtil.isEnergyRegenItem(itemInSlot.getItem(), this.creatureType.getCreatureDiet())) {
                    this.heal((float) this.getFavoriteFoodHeal(itemInSlot));
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    this.spawnItemCrackParticles(itemInSlot.getItem());
                    itemInSlot.setCount(itemInSlot.getCount() - 1);
                    this.eatFromInvCooldown = 0;
                }
            }
        }
        else this.eatFromInvCooldown = 0;

        if (this.getEnergy() < 20) {
            this.eatFromInvForEnergyCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (RiftUtil.isEnergyRegenItem(itemInSlot.getItem(), this.creatureType.getCreatureDiet()) && this.eatFromInvForEnergyCooldown > 60) {
                    this.setEnergy(this.getEnergy() + RiftUtil.getEnergyRegenItemValue(itemInSlot.getItem(), this.creatureType.getCreatureDiet()));
                    this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                    this.spawnItemCrackParticles(itemInSlot.getItem());
                    itemInSlot.setCount(itemInSlot.getCount() - 1);
                    this.eatFromInvForEnergyCooldown = 0;
                }
            }
        }
        else this.eatFromInvForEnergyCooldown = 0;

        if (this.isBaby() && this.getHealth() == this.getMaxHealth()) {
            this.eatFromInvForGrowthCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (this.isFavoriteFood(itemInSlot) && this.eatFromInvForGrowthCooldown > 60  && !RiftUtil.isEnergyRegenItem(itemInSlot.getItem(), this.creatureType.getCreatureDiet())) {
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
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed()) {
            try {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    if (this.isFavoriteFood(itemstack) && !itemstack.isEmpty() && this.isBaby() && this.getHealth() == this.getMaxHealth()) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setAgeInTicks(this.getAgeInTicks() + this.getFavoriteFoodGrowth(itemstack));
                        this.showGrowthParticles();
                        this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                        this.spawnItemCrackParticles(itemstack.getItem());
                    }
                    else if (this.isFavoriteFood(itemstack) && !RiftUtil.isEnergyRegenItem(itemstack.getItem(), this.creatureType.getCreatureDiet()) && this.getHealth() < this.getMaxHealth()) {
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
                    else if (RiftUtil.isEnergyRegenItem(itemstack.getItem(), this.creatureType.getCreatureDiet()) && this.getEnergy() < 20) {
                        this.consumeItemFromStack(player, itemstack);
                        this.setEnergy(this.getEnergy() + RiftUtil.getEnergyRegenItemValue(itemstack.getItem(), this.creatureType.getCreatureDiet()));
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
                        player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId() ,0, 0);
                    }
                    else if (itemstack.isEmpty() && this.isSaddled() && !player.isSneaking() && !this.isUsingWorkstation()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                    }
                    else if (itemstack.isEmpty() && this.isSaddled() && player.isSneaking()) {
                        player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId() ,0, 0);
                    }
                }
            }
            catch (Exception e) {
                if (this.getOwnerId() == null) {
                    ClientProxy.popupFromRadial = PopupFromRadial.CLAIM;
                    RiftMessages.WRAPPER.sendToServer(new RiftOpenPopupFromRadial(this));
                }
                else {
                    player.sendStatusMessage(new TextComponentTranslation("reminder.not_creature_owner", this.getOwner().getName()), false);
                }
            }
            return true;
        }
        else {
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

    public void manageAttributes() {
        double healthValue = ((this.maxCreatureHealth - this.minCreatureHealth)/24000D) * (this.getAgeInTicks() - 24000D) + this.maxCreatureHealth;
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(RiftUtil.clamp(Math.floor(healthValue), this.minCreatureHealth, this.maxCreatureHealth));
        if (this.justSpawned()) {
            this.heal((float) this.maxCreatureHealth);
            this.setSpeed(this.speed);
            this.setJustSpawned(false);
        }
    }

    public boolean isFavoriteFood(ItemStack stack) {
        for (String foodItem : this.favoriteFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) return (stack.getMetadata() == itemData) || (itemData == -1);
        }
        return false;
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
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == -1) return adder;
                else if (stack.getMetadata() == itemData) return adder;
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
        compound.setInteger("Variant", this.getVariant());
        compound.setByte("TameStatus", (byte) this.getTameStatus().ordinal());
        compound.setByte("TameBehavior", (byte) this.getTameBehavior().ordinal());
        compound.setBoolean("Saddled", this.isSaddled());
        if (creatureType != null) {
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
        compound.setString("UUID", this.getUUID().toString());
        if (this.canDoHerding()) {
            if (this.getHerdLeaderId() != null) compound.setString("LeaderUUID", this.getHerdLeaderId().toString());
        }
        compound.setBoolean("UsingWorkstation", this.isUsingWorkstation());
        if (compound.getBoolean("UsingWorkstation")) {
            compound.setInteger("WorkstationX", this.getWorkstationPos().getX());
            compound.setInteger("WorkstationY", this.getWorkstationPos().getY());
            compound.setInteger("WorkstationZ", this.getWorkstationPos().getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        if (compound.hasKey("TameStatus")) this.setTameStatus(TameStatusType.values()[compound.getByte("TameStatus")]);
        if (compound.hasKey("TameBehavior")) this.setTameBehavior(TameBehaviorType.values()[compound.getByte("TameBehavior")]);
        this.setSaddled(compound.getBoolean("Saddled"));
        if (creatureInventory != null) {
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
        this.setUUID(UUID.fromString(compound.getString("UUID")));
        if (this.canDoHerding() && !compound.getString("LeaderUUID").isEmpty()) {
            this.setHerdLeader(UUID.fromString(compound.getString("LeaderUUID")));
        }
        if (this.isUnclaimed()) this.setTamed(true);
        if (compound.getBoolean("UsingWorkstation")) this.setUseWorkstation(compound.getInteger("WorkstationX"), compound.getInteger("WorkstationY"), compound.getInteger("WorkstationZ"));
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

    public boolean canBreatheUnderwater() {
        if (this.headPart != null) return !this.headPart.isUnderwater();
        return false;
    }

    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        if (!this.world.isRemote) this.setSaddled(this.saddleItemEqual(saddle) && !saddle.isEmpty());
    }

    //herdin stuff starts here
    public boolean canDoHerding() {
        return false;
    }

    public RiftCreature getHerdLeader() {
        RiftCreature leader = RiftUtil.getCreatureFromUUID(this.world, this.getHerdLeaderId());
        if (leader != null) return leader;
        return this;
    }

    public UUID getHerdLeaderId() {
        if (this.dataManager.get(HERD_LEADER_UUID).isEmpty()) return null;
        return UUID.fromString(this.dataManager.get(HERD_LEADER_UUID));
    }

    public boolean isHerdLeader() {
        return this.getUUID().equals(this.getHerdLeaderId()) && !this.getHerdMembers(false).isEmpty();
    }

    public boolean isHerdMember() {
        return this.getHerdMembers(true).size() > 1;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.dataManager.set(HERD_LEADER_UUID, creature.getUUID().toString());
    }

    public void setHerdLeader(UUID uuid) {
        this.dataManager.set(HERD_LEADER_UUID, uuid.toString());
    }

    private void manageHerding() {
        this.herdCheckCountdown--;
        if (this.herdCheckCountdown <= 0) {
            //add members to herd
            List<RiftCreature> potentialHerders = this.world.getEntitiesWithinAABB(this.getClass(), this.getHerdBoundingBox(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature input) {
                    return !input.isTamed();
                }
            });
            int herdLeaderId = Collections.min(potentialHerders.stream().map(RiftCreature::getEntityId).collect(Collectors.toList()));
            this.setHerdLeader((RiftCreature) this.world.getEntityByID(herdLeaderId));
            this.herdCheckCountdown = RiftUtil.randomInRange(10, 15) * 20;
        }
    }

    public double getHerdDist() {
        return 24D;
    }

    public double getDoubleHerdDist() {
        return this.getHerdDist() * this.getHerdDist();
    }

    public boolean isNearHerdLeader() {
        return this.getDistanceSq(this.getHerdLeader()) <= this.getDoubleHerdDist();
    }

    public AxisAlignedBB getHerdBoundingBox() {
        return this.getEntityBoundingBox().grow(this.getHerdDist() * 2, this.getHerdDist() * 2, this.getHerdDist() * 2);
    }

    public List<RiftCreature> getHerdMembers(boolean includeLeader) {
        RiftCreature thisHerdLeader = this.getHerdLeader();
        List<RiftCreature> herders = this.world.getEntitiesWithinAABB(this.getClass(), this.getHerdBoundingBox(), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature input) {
                return !input.isTamed() && input.getHerdLeader().equals(thisHerdLeader);
            }
        });
        if (!includeLeader) herders.remove(thisHerdLeader);
        return herders;
    }

    //herdin stuff stops here

    public int getVariant() {
        return this.dataManager.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    public void setAttacking(boolean value) {
        this.dataManager.set(ATTACKING, Boolean.valueOf(value));
        this.setActing(value);
    }

    public boolean isRangedAttacking() {
        return this.dataManager.get(RANGED_ATTACKING);
    }

    public void setRangedAttacking(boolean value) {
        this.dataManager.set(RANGED_ATTACKING, Boolean.valueOf(value));
        this.setActing(value);
    }

    public boolean isLoweringHead() {
        return this.dataManager.get(LOWER_HEAD);
    }

    public void setLowerHead(boolean value) {
        this.dataManager.set(LOWER_HEAD, value);
    }

    public boolean canCharge() {
        return this.dataManager.get(CAN_CHARGE);
    }

    public void setCanCharge(boolean value) {
        this.dataManager.set(CAN_CHARGE, value);
    }

    public boolean isStartCharging() {
        return this.dataManager.get(START_CHARGING);
    }

    public void setStartCharging(boolean value) {
        this.dataManager.set(START_CHARGING, value);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    public void setIsCharging(boolean value) {
        this.dataManager.set(CHARGING, value);
    }

    public boolean isEndCharging() {
        return this.dataManager.get(END_CHARGING);
    }

    public void setEndCharging(boolean value) {
        this.dataManager.set(END_CHARGING, value);
    }

    public boolean isLeaping() {
        return this.dataManager.get(LEAPING);
    }

    public void setLeaping(boolean value) {
        this.dataManager.set(LEAPING, value);
        this.setActing(value);
    }

    public boolean isUtilizingCharging() {
        return this.isLoweringHead() || this.isStartCharging() || this.isCharging() || this.isEndCharging();
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

    private void setSpeed(double value) {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(value);
    }

    public void resetSpeed() {
        this.setSpeed(this.speed);
    }

    public void removeSpeed() {
        this.setSpeed(0D);
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
        this.tickUse = 0;
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
        this.dataManager.set(USING_RIGHT_CLICK, Boolean.valueOf(value));
    }

    public int getRightClickUse() {
        return this.dataManager.get(RIGHT_CLICK_USE).intValue();
    }

    public void setRightClickUse(int value) {
        this.dataManager.set(RIGHT_CLICK_USE, value);
        this.tickUse = 0;
    }

    public int getRightClickCooldown() {
        return this.dataManager.get(RIGHT_CLICK_COOLDOWN);
    }

    public void setRightClickCooldown(int value) {
        this.dataManager.set(RIGHT_CLICK_COOLDOWN, value);
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

    public void setUUID(UUID value) {
        this.dataManager.set(CREATURE_UUID, value.toString());
    }

    public UUID getUUID() {
        return UUID.fromString(this.dataManager.get(CREATURE_UUID));
    }

    public BlockPos getHomePos() {
        return this.homePosition;
    }

    public boolean isBaby() {
        return this.getAgeInDays() < 1;
    }

    public void setUseWorkstation(double x, double y, double z) {
        this.dataManager.set(USING_WORKSTATION, true);
        this.dataManager.set(WORKSTATION_X_POS, (int)x);
        this.dataManager.set(WORKSTATION_Y_POS, (int)y);
        this.dataManager.set(WORKSTATION_Z_POS, (int)z);
    }

    public void clearWorkstation(boolean destroyed) {
        this.dataManager.set(USING_WORKSTATION, false);
        this.dataManager.set(WORKSTATION_X_POS, 0);
        this.dataManager.set(WORKSTATION_Y_POS, 0);
        this.dataManager.set(WORKSTATION_Z_POS, 0);
        EntityPlayer owner = (EntityPlayer) this.getOwner();
        if (destroyed) owner.sendStatusMessage(new TextComponentTranslation("action.creature_workstation_destroyed"), false);
        else owner.sendStatusMessage(new TextComponentTranslation("action.clear_creature_workstation"), false);
    }

    public boolean isUsingWorkstation() {
        return this.dataManager.get(USING_WORKSTATION);
    }

    public BlockPos getWorkstationPos() {
        return new BlockPos(this.dataManager.get(WORKSTATION_X_POS), this.dataManager.get(WORKSTATION_Y_POS), this.dataManager.get(WORKSTATION_Z_POS));
    }

    public boolean isMoving() {
        double fallMotion = !this.onGround ? this.motionY : 0;
        return Math.sqrt((this.motionX * this.motionX) + (fallMotion * fallMotion) + (this.motionZ * this.motionZ)) > 0;
    }

    public boolean isTameableByFeeding() {
        return false;
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
        double dist = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX + attackDetectWidth;
        Vec3d vec3d = this.getPositionEyes(1.0F);
        Vec3d vec3d1 = this.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        Entity pointedEntity = null;
        Entity rider = this.getControllingPassenger();
        List<Entity> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(1.0D, 1.0D, 1.0D), null);
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
    public boolean canPassengerSteer() {
        return false;
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
        if (this.isSaddled() && this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null) {
                this.rotationYaw = controller.rotationYaw;
                this.prevRotationYaw = this.rotationYaw;
                this.rotationPitch = controller.rotationPitch * 0.5f;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.renderYawOffset = this.rotationYaw;
                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;
                this.stepHeight = 1.0F;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                this.fallDistance = 0;
                float moveSpeedMod = (this.getEnergy() > 6 ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = ((float)(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) - riderSpeed) * moveSpeedMod;
                this.setAIMoveSpeed(this.onGround ? moveSpeed + (controller.isSprinting() && this.getEnergy() > 6 ? moveSpeed * 0.3f : 0) : 2);

                if (this.isFloating && forward > 0) {
                    if (this.bodyPart != null) {
                        BlockPos ahead = new BlockPos(this.posX + Math.sin(-rotationYaw * 0.017453292F), this.bodyPart.posY, this.posZ + Math.cos(rotationYaw * 0.017453292F));
                        BlockPos above = ahead.up();
                        if (this.world.getBlockState(ahead).getMaterial().isSolid() && !this.world.getBlockState(above).getMaterial().isSolid()) {
                            this.setPosition(this.posX, this.posY + this.bodyPart.height + 1.0, this.posZ);
                        }
                    }
                }

                super.travel(strafe, vertical, forward);
            }
        }
        else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            if (this.isFloating && forward > 0) {
                if (this.bodyPart != null) {
                    BlockPos ahead = new BlockPos(this.posX + Math.sin(-rotationYaw * 0.017453292F), this.bodyPart.posY, this.posZ + Math.cos(rotationYaw * 0.017453292F));
                    BlockPos above = ahead.up();
                    if (this.world.getBlockState(ahead).getMaterial().isSolid() && !this.world.getBlockState(above).getMaterial().isSolid()) {
                        this.setPosition(this.posX, this.posY + this.bodyPart.height + 1.0, this.posZ);
                    }
                }
            }
            super.travel(strafe, vertical, forward);
        }
    }

    public boolean isEntityInsideOpaqueBlock() {
        return !this.isFloating && super.isEntityInsideOpaqueBlock();
    }

    //manage floating on water stuff
    private void controlWaterMovement() {
        if (!this.isFloating && this.isInWater()) {
            this.yFloatPos = this.getHighestWaterLevel();
            if (this.posY <= this.yFloatPos) this.isFloating = true;
        }
        else if (this.isFloating && this.isInWater()) {
            if (this.posY < this.yFloatPos) {
                this.motionY = 0.1;
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            }
            else {
                this.motionY = 0;
                this.setPosition(this.posX, this.yFloatPos, this.posZ);
                this.stepHeight = 3;
                this.fallDistance = 0;
            }
        }
        else if (this.isFloating && !this.isInWater()) {
            this.isFloating = false;
        }
    }

    public float getHighestWaterLevel() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = 256;
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        try {
            for (int k1 = k; k1 < l; ++k1) {
                for (int l1 = i; l1 < j; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        blockpos$pooledmutableblockpos.setPos(l1, k1, i2);
                        IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);

                        if (iblockstate.getMaterial() == Material.WATER) {
                            // Check if the block above is not water
                            IBlockState iblockstateAbove = this.world.getBlockState(blockpos$pooledmutableblockpos.up());
                            if (iblockstateAbove.getMaterial() != Material.WATER) {
                                return (float) k1;
                            }
                        }
                    }
                }
            }
            return (float) l;
        }
        finally {
            blockpos$pooledmutableblockpos.release();
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        RiftEgg egg = new RiftEgg(this.world);
        egg.setCreatureType(this.creatureType);
        egg.setOwnerId(this.getOwnerId());
        egg.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
        egg.enablePersistence();
        egg.setHatchTime(this.creatureType.getHatchTime() * 20);
        return egg;
    }

    @Override
    public abstract void registerControllers(AnimationData data);

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
