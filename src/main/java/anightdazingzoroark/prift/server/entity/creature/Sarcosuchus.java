package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.SSRCompatUtils;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSarcosuchusSpinTargeting;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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
        this.setSize(1.25f, 0.5f);
        this.experienceValue = 10;
        this.favoriteFood = ((SarcosuchusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((SarcosuchusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = ((SarcosuchusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 10D;
        this.spinTime = 0;
        this.messageSent = true;
        this.targetList = RiftUtil.creatureTargets(((SarcosuchusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.targetWhitelist, ((SarcosuchusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.targetBlacklist, true);

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.125f, 0.75f, 0.675f, 1f);
        this.headPart = new RiftCreaturePart(this, 1.625f, 0, 0.125f, 0.625f, 0.625f, 1.5f);
        this.snoutPart = new RiftCreaturePart(this, 2.5f, 0, 0.2f, 0.55f, 0.5f, 1.5f);
        this.frontBodyPart = new RiftCreaturePart(this, 0.75f, 0, 0.125f, 0.75f, 0.65f, 1f);
        this.tail0 = new RiftCreaturePart(this, -0.75f, 0, 0.125f, 0.75f, 0.65f, 0.5f);
        this.tail1 = new RiftCreaturePart(this, -1.625f, 0, 0.2f, 0.575f, 0.525f, 0.5f);
        this.tail2 = new RiftCreaturePart(this, -2.375f, 0, 0.225f, 0.525f, 0.475f, 0.5f);
        this.tail3 = new RiftCreaturePart(this, -3.125f, 0, 0.225f, 0.525f, 0.475f, 0.5f);
        this.tail4 = new RiftCreaturePart(this, -3.75f, 0, 0.25f, 0.475f, 0.425f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.bodyPart,
            this.headPart,
            this.snoutPart,
            this.frontBodyPart,
            this.tail0,
            this.tail1,
            this.tail2,
            this.tail3,
            this.tail4
        };
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
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        //this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.52F));
        this.tasks.addTask(3, new RiftAttack.SarcosuchusAttack(this, 4.0D, 0.52f, 0.52f));
        this.tasks.addTask(4, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        //this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftGoToWater(this, 16, 1.0D));
        this.tasks.addTask(7, new RiftWanderWater(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.isBeingRidden() && this.canUseRightClick() && this.getRightClickCooldown() == 0 && this.isUsingRightClick() && (this.getRightClickUse() >= 0 && this.getRightClickUse() <= 100) && this.getEnergy() > 6) this.forcedSpinAttack();
        else if (!this.isBeingRidden() || !this.canUseRightClick() || this.getRightClickCooldown() > 0 || !this.isUsingRightClick()) {
            this.spinTime = 0;
            if (this.forcedSpinVictim != null) NonPotionEffectsHelper.setCaptured(this.forcedSpinVictim, false);
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
                if (this.forcedSpinVictim != null) NonPotionEffectsHelper.setCaptured(this.forcedSpinVictim, false);;
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
            MobSize spinMaxSize = MobSize.safeValueOf(((SarcosuchusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.maximumSpinAttackTargetSize);
            if (RiftUtil.isUsingSSR()) {
                EntityLivingBase target = (EntityLivingBase) SSRCompatUtils.getEntities(this).entityHit;
                if (target != null) {
                    boolean canSpinFlag;
                    if (target instanceof EntityPlayer) {
                        canSpinFlag = RiftUtil.isAppropriateSize(target, spinMaxSize) && !target.getUniqueID().equals(this.getOwnerId()) && !NonPotionEffectsHelper.isCaptured(target);
                    }
                    else {
                        if (target instanceof EntityTameable) {
                            EntityTameable inpTameable = (EntityTameable)target;
                            if (inpTameable.isTamed()) {
                                canSpinFlag = RiftUtil.isAppropriateSize(target, spinMaxSize) && !target.getUniqueID().equals(inpTameable.getOwnerId()) && !NonPotionEffectsHelper.isCaptured(target);
                            }
                            else canSpinFlag = RiftUtil.isAppropriateSize(target, spinMaxSize) && !NonPotionEffectsHelper.isCaptured(target);
                        }
                        else canSpinFlag = RiftUtil.isAppropriateSize(target, spinMaxSize) && !NonPotionEffectsHelper.isCaptured(target);
                    }
                    if (canSpinFlag) this.forcedSpinVictim = target;
                }
            }
            else {
                UUID ownerID = this.getOwnerId();
                List<EntityLivingBase> potTargetListM = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(this.attackWidth()).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
                    @Override
                    public boolean apply(@Nullable EntityLivingBase input) {
                        if (input instanceof EntityPlayer) {
                            return RiftUtil.isAppropriateSize(input, spinMaxSize) && !input.getUniqueID().equals(ownerID) && !NonPotionEffectsHelper.isCaptured(input);
                        }
                        else {
                            if (input instanceof EntityTameable) {
                                EntityTameable inpTameable = (EntityTameable)input;
                                if (inpTameable.isTamed()) {
                                    return RiftUtil.isAppropriateSize(inpTameable, spinMaxSize) && !ownerID.equals(inpTameable.getOwnerId()) && !NonPotionEffectsHelper.isCaptured(input);
                                }
                                else return RiftUtil.isAppropriateSize(inpTameable, spinMaxSize) && !NonPotionEffectsHelper.isCaptured(input);
                            }
                            return RiftUtil.isAppropriateSize(input, spinMaxSize) && !NonPotionEffectsHelper.isCaptured(input);
                        }
                    }
                });
                potTargetListM.remove(this);
                if (!potTargetListM.isEmpty()) this.forcedSpinVictim = potTargetListM.get(0);
            }
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.175f : 0;
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
        if (this.forcedSpinVictim != null) NonPotionEffectsHelper.setCaptured(this.forcedSpinVictim, false);
        if (this.getAttackTarget() != null) NonPotionEffectsHelper.setCaptured(this.getAttackTarget(), false);
    }

    public float attackWidth() {
        return 3f;
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

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (!this.isActing() && !this.isUsingRightClick()) {
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
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::sarcosuchusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::sarcosuchusAttack));
    }

    private <E extends IAnimatable> PlayState sarcosuchusMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
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
        event.getController().clearAnimationCache();
        return PlayState.STOP;
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
