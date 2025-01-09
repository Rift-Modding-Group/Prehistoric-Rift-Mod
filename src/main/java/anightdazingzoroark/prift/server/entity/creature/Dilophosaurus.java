package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.DilophosaurusConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IRangedAttacker;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpit;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

public class Dilophosaurus extends RiftCreature implements IRangedAttacker, ITurretModeUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dilophosaurus"));
    private static final DataParameter<Boolean> LEFT_CLAW = EntityDataManager.<Boolean>createKey(Dilophosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT_CLAW = EntityDataManager.<Boolean>createKey(Dilophosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> TURRET_MODE = EntityDataManager.createKey(Dilophosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> TURRET_TARGET = EntityDataManager.createKey(Dilophosaurus.class, DataSerializers.BYTE);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Dilophosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.DILOPHOSAURUS);
        this.setSize(1f, 1.75f);
        this.experienceValue = 20;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.2D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 2f, 0, 1.7f, 1f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.9f, 1f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 1.5f, 0, 1.2f, 0.7f, 0.7f, 1.5f);
        this.hipPart = new RiftCreaturePart(this, 0, 0, 0.7f, 1f, 1f, 1f);
        this.tail0Part = new RiftCreaturePart(this, -0.9f, 0, 1f, 0.7f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.5f, 0, 0.95f, 0.6f, 0.6f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -2.1f, 0, 0.9f, 0.6f, 0.6f, 0.5f);

        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.hipPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEFT_CLAW, false);
        this.dataManager.register(RIGHT_CLAW, false);
        this.dataManager.register(TURRET_MODE, false);
        this.dataManager.register(TURRET_TARGET, (byte) TurretModeTargeting.HOSTILES.ordinal());
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTurretModeTargeting(this, true));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftControlledRangedAttack(this, 2.64F, 0.72F, 20));
        this.tasks.addTask(3, new RiftDilophosaurusControlledClawAttack(this));
        this.tasks.addTask(4, new RiftRangedAttack(this, false, 1.0D, 2.64F, 0.72F));
        this.tasks.addTask(5, new RiftAttack.DilophosaurusAttack(this, 1.0D));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanForcedUseSpit();
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.45f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
    }

    private void manageCanForcedUseSpit() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeTurretModeDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readTurretModeDataFromNBT(compound);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1f};
    }

    @Override
    public float attackWidth() {
        return 2f;
    }

    @Override
    public float rangedWidth() {
        return 12f;
    }

    public boolean isUsingLeftClaw() {
        return this.dataManager.get(LEFT_CLAW);
    }

    public void setUsingLeftClaw(boolean value) {
        this.dataManager.set(LEFT_CLAW, value);
        this.setActing(value);
    }

    public boolean isUsingRightClaw() {
        return this.dataManager.get(RIGHT_CLAW);
    }

    public void setUsingRightClaw(boolean value) {
        this.dataManager.set(RIGHT_CLAW, value);
        this.setActing(value);
    }

    @Override
    public boolean isTurretMode() {
        return this.dataManager.get(TURRET_MODE);
    }

    @Override
    public void setTurretMode(boolean value) {
        this.dataManager.set(TURRET_MODE, value);
    }

    @Override
    public TurretModeTargeting getTurretTargeting() {
        return TurretModeTargeting.values()[this.dataManager.get(TURRET_TARGET)];
    }

    @Override
    public void setTurretModeTargeting(TurretModeTargeting turretModeTargeting) {
        this.dataManager.set(TURRET_TARGET, (byte) turretModeTargeting.ordinal());
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        DilophosaurusSpit dilophosaurusSpit = new DilophosaurusSpit(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 6.0F) - dilophosaurusSpit.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        dilophosaurusSpit.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.5F, 1.0F);
        dilophosaurusSpit.setDamage(2D + (double)(this.getLevel())/10D);
        this.world.spawnEntity(dilophosaurusSpit);
    }

    @Override
    public void controlRangedAttack(double strength) {
        DilophosaurusSpit dilophosaurusSpit = new DilophosaurusSpit(this.world, this, (EntityPlayer)this.getControllingPassenger());
        dilophosaurusSpit.setDamage(2D + (double)(this.getLevel())/10D);
        dilophosaurusSpit.shoot(this, this.rotationPitch, this.rotationYaw, 0.0F, 1.5f, 1.0F);
        this.world.spawnEntity(dilophosaurusSpit);
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
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (0.05) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (0.05) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.forcedAttackTarget = target;
                    this.forcedBreakPos = pos;
                    if (RiftUtil.randomInRange(0, 1) == 0) this.setUsingLeftClaw(true);
                    else this.setUsingRightClaw(true);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.setRangedAttacking(true);
                    this.setRightClickUse(0);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
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
        return false;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "frillSetup", 0, this::dilophosaurusFrillSetup));
        data.addAnimationController(new AnimationController(this, "sacSetup", 0, this::dilophosaurusSacSetup));
        data.addAnimationController(new AnimationController(this, "movement", 0, this::dilophosaurusMovement));
        data.addAnimationController(new AnimationController(this, "clawAttack", 0, this::dilophosaurusClawAttack));
        data.addAnimationController(new AnimationController(this, "spitAttack", 0, this::dilophosaurusSpitAttack));
    }

    private <E extends IAnimatable> PlayState dilophosaurusFrillSetup(AnimationEvent event) {
        if (!this.isSleeping() && !this.isRangedAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.frill_setup", true));
            return PlayState.CONTINUE;
        }
        else {
            event.getController().clearAnimationCache();
            return PlayState.STOP;
        }
    }

    private <E extends IAnimatable> PlayState dilophosaurusSacSetup(AnimationEvent event) {
        if (!this.isSleeping() && !this.isRangedAttacking()) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.sac_setup", true));
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState dilophosaurusMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.sitting", true));
                return PlayState.CONTINUE;
            }
            if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.walk", true));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState dilophosaurusClawAttack(AnimationEvent<E> event) {
        if (this.isUsingLeftClaw()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.claw_two", false));
        }
        else if (this.isUsingRightClaw()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.claw_one", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState dilophosaurusSpitAttack(AnimationEvent<E> event) {
        if (this.isRangedAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.spit", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.DILOPHOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DILOPHOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DILOPHOSAURUS_DEATH;
    }

    public SoundEvent rangedAttackSound() {
        return RiftSounds.DILOPHOSAURUS_SPIT;
    }
}
