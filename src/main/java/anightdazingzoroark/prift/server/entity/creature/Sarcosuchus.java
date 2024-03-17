package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.config.MegapiranhaConfig;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSarcosuchusSpinTargeting;
import com.google.common.base.Predicate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Sarcosuchus extends RiftWaterCreature {
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.<Boolean>createKey(Sarcosuchus.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/sarcosuchus"));
    private EntityLivingBase forcedSpinVictim;
    private int spinTime;
    private boolean messageSent;
    private RiftCreaturePart snoutPart;
    private RiftCreaturePart frontBodyPart;
    private RiftCreaturePart tail0;
    private RiftCreaturePart tail1;
    private RiftCreaturePart tail2;
    private RiftCreaturePart tail3;
    private RiftCreaturePart tail4;

    public Sarcosuchus(World worldIn) {
        super(worldIn, RiftCreatureType.SARCOSUCHUS);
        this.setSize(1.25f, 1.25f);
        this.minCreatureHealth = SarcosuchusConfig.getMinHealth();
        this.maxCreatureHealth = SarcosuchusConfig.getMaxHealth();
        this.experienceValue = 10;
        this.favoriteFood = SarcosuchusConfig.sarcosuchusFavoriteFood;
        this.tamingFood = SarcosuchusConfig.sarcosuchusTamingFood;
        this.isRideable = true;
        this.attackWidth = 3f;
        this.saddleItem = SarcosuchusConfig.sarcosuchusSaddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 10D;
        this.spinTime = 0;
        this.messageSent = true;
        this.attackDamage = SarcosuchusConfig.damage;
        this.healthLevelMultiplier = SarcosuchusConfig.healthMultiplier;
        this.damageLevelMultiplier = SarcosuchusConfig.damageMultiplier;
        this.densityLimit = SarcosuchusConfig.sarcosuchusDensityLimit;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
        this.dataManager.register(SPINNING, false);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, SarcosuchusConfig.sarcosuchusTargets, SarcosuchusConfig.sarcosuchusTargetBlacklist, true, true, true));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, SarcosuchusConfig.sarcosuchusFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.52F));
        this.tasks.addTask(3, new RiftAttack.SarcosuchusAttack(this, 4.0D, 0.52f, 0.52f));
        this.tasks.addTask(4, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftGoToWater(this, 16, 1.0D));
        this.tasks.addTask(7, new RiftWanderWater(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.isBeingRidden() && this.canUseRightClick() && this.getRightClickCooldown() == 0 && this.isUsingRightClick() && (this.getRightClickUse() >= 0 && this.getRightClickUse() <= 100) && this.getEnergy() > 6) this.forcedSpinAttack();
        else if (!this.isBeingRidden() || !this.canUseRightClick() || this.getRightClickCooldown() > 0 || !this.isUsingRightClick()) {
            this.spinTime = 0;
            if (this.forcedSpinVictim != null) EntityPropertiesHandler.INSTANCE.getProperties(this.forcedSpinVictim, RiftEntityProperties.class).isCaptured = false;
            this.forcedSpinVictim = null;
            if (this.getRightClickCooldown() > 0) {
                this.setRightClickCooldown(this.getRightClickCooldown() - 1);
            }
        }
        if (this.isBeingRidden() && this.getEnergy() <= 6 && this.isUsingRightClick() && !this.messageSent) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
            this.messageSent = true;
        }
        else if (!this.isUsingRightClick() && this.messageSent) this.messageSent = false;
        if (this.isBeingRidden()) {
            if (this.getRightClickUse() > 100) {
                if (this.forcedSpinVictim != null) EntityPropertiesHandler.INSTANCE.getProperties(this.forcedSpinVictim, RiftEntityProperties.class).isCaptured = false;
                this.forcedSpinVictim = null;
            }
            if (this.forcedSpinVictim == null) this.setIsSpinning(false);
        }
    }

    private void forcedSpinAttack() {
        if (this.forcedSpinVictim != null) {
            if (this.forcedSpinVictim.isEntityAlive()) {
                if (!this.isSpinning()) this.setIsSpinning(true);
                RiftMessages.WRAPPER.sendToServer(new RiftSarcosuchusSpinTargeting(this, this.forcedSpinVictim));
                if (this.isTamed() && this.spinTime % 10 == 0) this.setEnergy(this.getEnergy() - 1);
                this.spinTime++;
            }
            else {
                this.setIsSpinning(false);
                this.setCanUseRightClick(false);
            }
        }
        else {
            UUID ownerID = this.getOwnerId();
            List<String> blackList = Arrays.asList(SarcosuchusConfig.sarcosuchusSpinBlacklist);
            List<EntityLivingBase> potTargetListM = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(this.attackWidth).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(input, RiftEntityProperties.class);
                    if (input instanceof EntityPlayer) {
                        if (!SarcosuchusConfig.sarcosuchusSpinWhitelist && !blackList.contains("minecraft:player")) {
                            return !input.getUniqueID().equals(ownerID) && !properties.isCaptured;
                        }
                        else if (SarcosuchusConfig.sarcosuchusSpinWhitelist && blackList.contains("minecraft:player")) {
                            return !input.getUniqueID().equals(ownerID) && !properties.isCaptured;
                        }
                    }
                    else {
                        if (!SarcosuchusConfig.sarcosuchusSpinWhitelist && !blackList.contains(EntityList.getKey(input).toString())) {
                            if (input instanceof EntityTameable) {
                                EntityTameable inpTameable = (EntityTameable)input;
                                if (inpTameable.isTamed()) {
                                    return !ownerID.equals(inpTameable.getOwnerId()) && !properties.isCaptured;
                                }
                                else return !properties.isCaptured;
                            }
                            return !properties.isCaptured;
                        }
                        else if (SarcosuchusConfig.sarcosuchusSpinWhitelist && blackList.contains(EntityList.getKey(input).toString())) {
                            if (input instanceof EntityTameable) {
                                EntityTameable inpTameable = (EntityTameable)input;
                                if (inpTameable.isTamed()) {
                                    return !ownerID.equals(inpTameable.getOwnerId()) && !properties.isCaptured;
                                }
                                else return !properties.isCaptured;
                            }
                            return !properties.isCaptured;
                        }
                    }
                    return false;
                }
            });
            System.out.println(potTargetListM);
            if (!potTargetListM.isEmpty()) this.forcedSpinVictim = potTargetListM.get(0);
        }
    }

    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.125f, scale * 0.75f, scale * 0.675f, 1f);
            this.headPart = new RiftCreaturePart(this, 1.625f, 0, 0.125f, scale * 0.625f, scale * 0.625f, 1.5f);
            this.snoutPart = new RiftCreaturePart(this, 2.5f, 0, 0.2f, scale * 0.55f, scale * 0.5f, 1.5f);
            this.frontBodyPart = new RiftCreaturePart(this, 0.75f, 0, 0.125f, scale * 0.75f, scale * 0.65f, 1f);
            this.tail0 = new RiftCreaturePart(this, -0.75f, 0, 0.125f, scale * 0.75f, scale * 0.65f, 0.5f);
            this.tail1 = new RiftCreaturePart(this, -1.625f, 0, 0.2f, scale * 0.575f, scale * 0.525f, 0.5f);
            this.tail2 = new RiftCreaturePart(this, -2.375f, 0, 0.225f, scale * 0.525f, scale * 0.475f, 0.5f);
            this.tail3 = new RiftCreaturePart(this, -3.125f, 0, 0.225f, scale * 0.525f, scale * 0.475f, 0.5f);
            this.tail4 = new RiftCreaturePart(this, -3.75f, 0, 0.25f, scale * 0.475f, scale * 0.425f, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.snoutPart != null) this.snoutPart.onUpdate();
        if (this.frontBodyPart != null) this.frontBodyPart.onUpdate();
        if (this.tail0 != null) this.tail0.onUpdate();
        if (this.tail1 != null) this.tail1.onUpdate();
        if (this.tail2 != null) this.tail2.onUpdate();
        if (this.tail3 != null) this.tail3.onUpdate();
        if (this.tail4 != null) this.tail4.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.175f : 0;
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.snoutPart != null) this.snoutPart.setPositionAndUpdate(this.snoutPart.posX, this.snoutPart.posY + sitOffset, this.snoutPart.posZ);
        if (this.frontBodyPart != null) this.frontBodyPart.setPositionAndUpdate(this.frontBodyPart.posX, this.frontBodyPart.posY + sitOffset, this.frontBodyPart.posZ);
        if (this.tail0 != null) this.tail0.setPositionAndUpdate(this.tail0.posX, this.tail0.posY + sitOffset, this.tail0.posZ);
        if (this.tail1 != null) this.tail1.setPositionAndUpdate(this.tail1.posX, this.tail1.posY + sitOffset, this.tail1.posZ);
        if (this.tail2 != null) this.tail2.setPositionAndUpdate(this.tail2.posX, this.tail2.posY + sitOffset, this.tail2.posZ);
        if (this.tail3 != null) this.tail3.setPositionAndUpdate(this.tail3.posX, this.tail3.posY + sitOffset, this.tail3.posZ);
        if (this.tail4 != null) this.tail4.setPositionAndUpdate(this.tail4.posX, this.tail4.posY + sitOffset, this.tail4.posZ);
    }

    public void removeParts() {
        super.removeParts();
        if (this.snoutPart != null) {
            this.world.removeEntityDangerously(this.snoutPart);
            this.snoutPart = null;
        }
        if (this.frontBodyPart != null) {
            this.world.removeEntityDangerously(this.frontBodyPart);
            this.frontBodyPart = null;
        }
        if (this.tail0 != null) {
            this.world.removeEntityDangerously(this.tail0);
            this.tail0 = null;
        }
        if (this.tail1 != null) {
            this.world.removeEntityDangerously(this.tail1);
            this.tail1 = null;
        }
        if (this.tail2 != null) {
            this.world.removeEntityDangerously(this.tail2);
            this.tail2 = null;
        }
        if (this.tail3 != null) {
            this.world.removeEntityDangerously(this.tail3);
            this.tail3 = null;
        }
        if (this.tail4 != null) {
            this.world.removeEntityDangerously(this.tail4);
            this.tail4 = null;
        }
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.5f);
    }

    public boolean isSpinning() {
        return this.dataManager.get(SPINNING);
    }

    public void setIsSpinning(boolean value) {
        this.dataManager.set(SPINNING, value);
        if (value) this.removeSpeed();
        else this.resetSpeed();
//        this.setActing(value);
    }

    public void setRightClickUse(int value) {
       if (this.getRightClickUse() > value) super.setRightClickUse(value);
       else {
           if (this.forcedSpinVictim != null) {
               if (this.forcedSpinVictim.isEntityAlive()) super.setRightClickUse(value);
           }
       }
    }

    public boolean attackEntityUsingSpin(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()/4D)));
        if (flag) this.applyEnchantments(this, entityIn);
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
        if (!this.isSpinning()) super.knockBack(entityIn, strength, xRatio, zRatio);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (this.forcedSpinVictim != null) EntityPropertiesHandler.INSTANCE.getProperties(this.forcedSpinVictim, RiftEntityProperties.class).isCaptured = false;
        if (this.getAttackTarget() != null) EntityPropertiesHandler.INSTANCE.getProperties(this.getAttackTarget(), RiftEntityProperties.class).isCaptured = false;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX - (0.3) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ - (0.3) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 1.375, zOffset);
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
    public boolean isAmphibious() {
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

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return super.shouldRender(camera) || this.inFrustrum(camera, this.snoutPart) || this.inFrustrum(camera, this.frontBodyPart) || this.inFrustrum(camera, this.tail0) || this.inFrustrum(camera, this.tail1) || this.inFrustrum(camera, this.tail2) || this.inFrustrum(camera, this.tail3) || this.inFrustrum(camera, this.tail4);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing() && !this.isUsingRightClick()) this.setAttacking(true);
                }
                else {
                    if (!this.isActing() && !this.isUsingRightClick()) {
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
                    this.setRightClickCooldown(holdAmount * 2);
                    this.setRightClickUse(0);
                }
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::sarcosuchusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::sarcosuchusAttack));
    }

    private <E extends IAnimatable> PlayState sarcosuchusMovement(AnimationEvent<E> event) {
        if (this.isInWater()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.swim", true));
            return PlayState.CONTINUE;
        }
        else {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.sitting", true));
                return PlayState.CONTINUE;
            }
            if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.walk", true));
                return PlayState.CONTINUE;
            }
            event.getController().clearAnimationCache();
            return PlayState.STOP;
        }
    }

    private <E extends IAnimatable> PlayState sarcosuchusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.attack", false));
            return PlayState.CONTINUE;
        }
        if (this.isSpinning()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.spin_attack", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.SARCOSUCHUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.SARCOSUCHUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.SARCOSUCHUS_DEATH;
    }
}
