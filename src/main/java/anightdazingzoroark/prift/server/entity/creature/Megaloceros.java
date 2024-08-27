package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.MegalocerosConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.*;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.List;

public class Megaloceros extends RiftCreature implements IChargingMob, IImpregnable, IHarvestWhenWandering, ILeapingMob, IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/megaloceros"));
    private static final DataParameter<Boolean> LEAPING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(Megaloceros.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOWER_HEAD = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_CHARGE = EntityDataManager.<Boolean>createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> START_CHARGING = EntityDataManager.<Boolean>createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.<Boolean>createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> END_CHARGING = EntityDataManager.<Boolean>createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    private RiftCreaturePart frontBodyPart;
    private float leapPower;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Megaloceros(World worldIn) {
        super(worldIn, RiftCreatureType.MEGALOCEROS);
        this.setSize(1f, 1.5f);
        this.favoriteFood = ((MegalocerosConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((MegalocerosConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.experienceValue = 10;
        this.speed = 0.35D;
        this.isRideable = true;
        this.saddleItem = ((MegalocerosConfig) RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 1.4f, 0, 1.3f, 0.7f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.75f, 1f, 0.75f, 1f);
        this.frontBodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.75f, 0.75f, 0.75f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.frontBodyPart
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEAPING, false);
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
        this.dataManager.register(LOWER_HEAD, false);
        this.dataManager.register(CAN_CHARGE, true);
        this.dataManager.register(START_CHARGING, false);
        this.dataManager.register(CHARGING, false);
        this.dataManager.register(END_CHARGING, false);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftControlledCharge(this, 0.24f, 4f));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.52F, 0.36F));
        this.tasks.addTask(4, new RiftChargeAttack(this, 2f, 0.24f, 4f, 2f));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.52F, 0.36F));
        this.tasks.addTask(6, new RiftHarvestOnWander(this, 0.52F, 0.36F));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(8, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(9, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(10, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(11, new RiftHerdMemberFollow(this));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //managing ability to charge
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanCharge(true);

        if (!this.world.isRemote) {
            //manage birthin related stuff
            this.createBaby(this);

            //manage leaping
            if (this.onGround() && this.isLeaping()) this.setLeaping(false);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.55f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.frontBodyPart != null) this.frontBodyPart.setPositionAndUpdate(this.frontBodyPart.posX, this.frontBodyPart.posY + sitOffset, this.frontBodyPart.posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writePregnancyDataToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readPregnancyDataFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public RiftCreature getHerder() {
        return this;
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

    public double followRange() {
        return 4D;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.125f);
    }

    public float attackWidth() {
        return 2.5f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.125f) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.125f) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
    }

    public boolean isLeaping() {
        return this.dataManager.get(LEAPING);
    }

    public void setLeaping(boolean value) {
        this.dataManager.set(LEAPING, value);
        this.setActing(value);
    }

    public float getLeapPower() {
        return this.leapPower;
    }

    public void setLeapPower(float value) {
        this.leapPower = value;
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

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return true;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return true;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.forcedAttackTarget = target;
                    this.forcedBreakPos = pos;
                    this.setAttacking(true);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6) {
                if (this.getRightClickCooldown() == 0) {
                    if (!this.isActing()) {
                        this.setActing(true);
                        this.forcedChargePower = this.chargeCooldown = holdAmount;
                    }
                }
                else this.setRightClickUse(0);
            }
            else {
                ((EntityPlayer) this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
                this.setRightClickUse(0);
                this.setRightClickCooldown(0);
            }
        }
        if (control == 2) {
            final float leapHeight = Math.min(6f, 0.25f * holdAmount + 1);
            if (this.getEnergy() > 6 && !this.isLeaping() && !this.isInWater()) {
                this.setLeapPower((float) Math.sqrt(2f * leapHeight * RiftUtil.gravity));
                this.setEnergy(this.getEnergy() - Math.min(6, (int)(0.25D * holdAmount + 1D)));
            }
            else if (this.getEnergy() <= 6) ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
            this.setSpacebarUse(0);
        }
    }

    @Override
    public List<String> blocksToHarvest() {
        return ((MegalocerosConfig) RiftConfigHandler.getConfig(this.creatureType)).general.harvestableBlocks;
    }

    public int harvestRange() {
        return 3;
    }

    public void setHarvesting(boolean value) {
        this.setAttacking(value);
    }

    public boolean isHarvesting() {
        return this.isAttacking();
    }

    public void setCanHarvest(boolean value) {
        this.dataManager.set(CAN_HARVEST, value);
    }

    public boolean canHarvest() {
        return this.dataManager.get(CAN_HARVEST);
    }

    @Override
    public AxisAlignedBB breakRange() {
        return new AxisAlignedBB(-1, -1, -1, 1, 1, 1);
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

    public double chargeBoost() {
        return 2D;
    }

    public float chargeWidth() {
        return 20f;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::megalocerosMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::megalocerosAttack));
        data.addAnimationController(new AnimationController(this, "charge", 0, this::megalocerosCharge));
        data.addAnimationController(new AnimationController(this, "controlledCharge", 0, this::megalocerosControlledCharge));
    }

    private <E extends IAnimatable> PlayState megalocerosMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isCharging()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.walk", true));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState megalocerosAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.attack", false));
            return PlayState.CONTINUE;
        }
        else event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState megalocerosCharge(AnimationEvent<E> event) {
        if (!this.isBeingRidden()) {
            if (this.isLoweringHead()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charge_start", true));
                return PlayState.CONTINUE;
            }
            else if (this.isStartCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charge_charging", true));
                return PlayState.CONTINUE;
            }
            else if (this.isCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charging", true));
                return PlayState.CONTINUE;
            }
            else if (this.isEndCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charge_end", true));
                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState megalocerosControlledCharge(AnimationEvent<E> event) {
        if (this.isBeingRidden()) {
            if (this.getRightClickCooldown() == 0) {
                if (this.getRightClickUse() > 0 && this.getEnergy() > 6) {
                    if (this.isLoweringHead()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charge_start", true));
                        return PlayState.CONTINUE;
                    }
                    else if (this.isStartCharging() && this.getEnergy() > 6) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charge_charging", true));
                        return PlayState.CONTINUE;
                    }
                    else if (this.isCharging()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megaloceros.charging", true));
                        return PlayState.CONTINUE;
                    }
                }
            }
        }
        return PlayState.STOP;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.MEGALOCEROS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.MEGALOCEROS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.MEGALOCEROS_DEATH;
    }
}
