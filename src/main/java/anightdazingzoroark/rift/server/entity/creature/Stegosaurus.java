package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.client.RiftSounds;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.RiftEntityProperties;
import anightdazingzoroark.rift.server.entity.ai.*;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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

public class Stegosaurus extends RiftCreature implements IAnimatable, IRangedAttackMob {
    public static final ResourceLocation LOOT = LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/stegosaurus"));
    private static final DataParameter<Boolean> STRONG_ATTACKING = EntityDataManager.<Boolean>createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    public int strongAttackCharge;

    public Stegosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.STEGOSAURUS);
        this.setSize(2.125f, 2.5f);
        this.experienceValue = 20;
        this.speed = 0.175D;
        this.isRideable = true;
        this.attackWidth = 7.5f;
        this.rangedWidth = 12f;
        this.strongAttackCharge = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STRONG_ATTACKING, Boolean.FALSE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftResetAnimatedPose(this, 0.56F, 1));
        this.tasks.addTask(2, new RiftRangedAttack(this, false, 1.0D, 1.52F, 1.04F));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.96F, 0.36F));
        this.tasks.addTask(2, new RiftStegosaurusControlledStrongAttack(this, 0.72F, 0.12F));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 0.96F, 0.36F));
        this.tasks.addTask(4, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(5, new RiftHerdMemberFollow(this, 10D, 2D, 1D));
        this.tasks.addTask(6, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanStrongAttack();
        this.manageCanControlledPlateFling();
    }

    private void manageCanStrongAttack() {
        if (this.getLeftClickCooldown() > 0) this.setLeftClickCooldown(this.getLeftClickCooldown() - 1);
    }

    private void manageCanControlledPlateFling() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
    }

    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - thrownStegoPlate.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        thrownStegoPlate.setVariant(this.getVariant());
        thrownStegoPlate.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 5F);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(thrownStegoPlate);
    }

    public void setSwingingArms(boolean swingingArms) {}

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + 0.6, zOffset);
    }

    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (this.getLeftClickCooldown() == 0) {
                    if (target == null) {
                        if (!this.isActing()) {
                            if (holdAmount <= 10)  this.setAttacking(true);
                            else {
                                this.setIsStrongAttacking(true);
                                this.strongAttackCharge = RiftUtil.clamp(holdAmount, 10, 100);
                                this.setLeftClickCooldown(holdAmount * 2);
                            }
                        }
                    }
                    else {
                        if (!this.isActing()) {
                            this.ssrTarget = target;
                            if (holdAmount <= 10) this.setAttacking(true);
                            else {
                                this.setIsStrongAttacking(true);
                                this.strongAttackCharge = RiftUtil.clamp(holdAmount, 10, 100);
                                this.setLeftClickCooldown(holdAmount * 2);
                            }
                        }
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("rift.notify.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6) {
                if (this.getRightClickCooldown() == 0) {
                    if (!this.isActing()) {
                        this.setActing(true);
                        this.controlRangedAttack(RiftUtil.clamp(holdAmount, 0, 100));
                        this.setRightClickCooldown(holdAmount * 2);
                        this.setEnergy(this.getEnergy() - (int)(0.09D * (double)holdAmount + 1D));
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("rift.notify.insufficient_energy", this.getName()), false);
        }
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return true;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return true;
    }

    public void strongControlAttack() {
        EntityLivingBase target;
        if (this.ssrTarget == null) target = this.getControlAttackTargets();
        else target = this.ssrTarget;
        if (target != null) {
            if (this.isTamed() && target instanceof EntityPlayer) {
                if (!target.getUniqueID().equals(this.getOwnerId())) this.attackEntityAsMobStrong(target);
            }
            else if (this.isTamed() && target instanceof EntityTameable) {
                if (((EntityTameable) target).isTamed()) {
                    if (!((EntityTameable) target).getOwner().equals(this.getOwner())) this.attackEntityAsMobStrong(target);
                }
                else this.attackEntityAsMobStrong(target);
            }
            else this.attackEntityAsMobStrong(target);
        }
    }

    @Override
    public void controlRangedAttack(double strength) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(this.world, this, (EntityPlayer)this.getControllingPassenger());
        thrownStegoPlate.setDamage(strength * 0.04D + 4D);
        thrownStegoPlate.setIsCritical(strength >= 50);
        float velocity = (float) strength * 0.015f + 1.5f;
        thrownStegoPlate.shoot(this, this.rotationPitch, this.rotationYaw, 0.0F, velocity, 1.0F);
        this.world.spawnEntity(thrownStegoPlate);
    }


    private boolean attackEntityAsMobStrong(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), ((float) this.strongAttackCharge - 100f)/3f + 30f + (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (flag) {
            RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entityIn, RiftEntityProperties.class);
            this.applyEnchantments(this, entityIn);
            properties.setBleeding(0, 200);
        }
        return flag;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    @Override
    public boolean isTameableByFeeding() {
        return true;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 2.125f);
    }

    public boolean isStrongAttacking() {
        return this.dataManager.get(STRONG_ATTACKING);
    }

    public void setIsStrongAttacking(boolean value) {
        this.dataManager.set(STRONG_ATTACKING, value);
        this.setUsingLeftClick(true);
        this.setActing(value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::stegosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::stegosaurusAttack));
        data.addAnimationController(new AnimationController(this, "controlled_plate_fling", 0, this::stegosaurusControlledPlateFling));
    }

    private <E extends IAnimatable> PlayState stegosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState stegosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.attack", false));
        }
        else if (this.isStrongAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.strong_attack", false));
        }
        else if (this.isRangedAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.plate_fling", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState stegosaurusControlledPlateFling(AnimationEvent<E> event) {
        if (this.getRightClickCooldown() == 0) {
            if (this.getRightClickUse() > 0 && this.getRightClickUse() < 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.use_plate_fling_p1", false));
            else if (this.getRightClickUse() >= 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.use_plate_fling_p1_hold", true));
        }
        else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.use_plate_fling_p2", false));
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.STEGOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound() {
        return RiftSounds.STEGOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.STEGOSAURUS_DEATH;
    }
}
