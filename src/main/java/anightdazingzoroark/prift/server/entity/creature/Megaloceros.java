package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.MegalocerosConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IChargingMob;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
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

public class Megaloceros extends RiftCreature implements IChargingMob, IImpregnable {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/megaloceros"));
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(Megaloceros.class, DataSerializers.VARINT);
    private RiftCreaturePart frontBodyPart;

    public Megaloceros(World worldIn) {
        super(worldIn, RiftCreatureType.MEGALOCEROS);
        this.minCreatureHealth = MegalocerosConfig.getMinHealth();
        this.maxCreatureHealth = MegalocerosConfig.getMaxHealth();
        this.setSize(1f, 1.5f);
        this.favoriteFood = MegalocerosConfig.megalocerosFavoriteFood;
        this.tamingFood = MegalocerosConfig.megalocerosTamingFood;
        this.experienceValue = 10;
        this.speed = 0.35D;
        this.isRideable = true;
        this.attackWidth = 2.5f;
        this.chargeWidth = 20f;
        this.saddleItem = MegalocerosConfig.megalocerosSaddleItem;
        this.attackDamage = MegalocerosConfig.damage;
        this.healthLevelMultiplier = MegalocerosConfig.healthMultiplier;
        this.damageLevelMultiplier = MegalocerosConfig.damageMultiplier;
        this.densityLimit = MegalocerosConfig.megalocerosDensityLimit;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftControlledCharge(this, 2f, 0.24f, 4f));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.52F, 0.36F));
        this.tasks.addTask(4, new RiftChargeAttack(this, 2f, 0.24f, 4f, 2f));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.52F, 0.36F));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(10, new RiftHerdMemberFollow(this));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //managing ability to charge
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanCharge(true);

        //manage birthin related stuff
        if (!this.world.isRemote) this.createBaby(this);
    }

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 1.4f, 0, 1.3f, 0.7f * scale, 0.6f * scale, 1.5f);
            this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.75f, scale, 0.75f * scale, 1f);
            this.frontBodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.75f, scale * 0.75f, scale * 0.75f, 1f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.frontBodyPart != null) this.frontBodyPart.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.55f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.frontBodyPart != null) this.frontBodyPart.setPositionAndUpdate(this.frontBodyPart.posX, this.frontBodyPart.posY + sitOffset, this.frontBodyPart.posZ);
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.frontBodyPart != null) {
            this.world.removeEntityDangerously(this.frontBodyPart);
            this.frontBodyPart = null;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("PregnancyTime", this.getPregnancyTimer());
        compound.setBoolean("IsPregnancy", this.isPregnant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setPregnant(compound.getBoolean("IsPregnancy"), compound.getInteger("PregnancyTime"));
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
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

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.125f) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.125f) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
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
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing()) this.setAttacking(true);
                }
                else {
                    if (!this.isActing()) {
                        this.ssrTarget = target;
                        this.setAttacking(true);
                    }
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
            final float g = 0.08f;
            if (this.getEnergy() > 6) {
                if (this.isMoving(false)) {
                    double dx = (16 * Math.sin(-Math.toRadians(this.rotationYaw)));
                    double dz = (16 * Math.cos(Math.toRadians(this.rotationYaw)));

                    double velY = Math.sqrt(2 * g * leapHeight);
                    double totalTime = velY / g;

                    this.motionX = this.motionX + dx / totalTime;
                    this.motionZ = this.motionZ + dz / totalTime;
                    this.motionY = velY;
                }
                else this.motionY = Math.sqrt(2 * g * leapHeight);
                this.setEnergy(this.getEnergy() - Math.min(6, (int)(0.25D * holdAmount + 1D)));
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
            this.setSpacebarUse(0);
        }
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
