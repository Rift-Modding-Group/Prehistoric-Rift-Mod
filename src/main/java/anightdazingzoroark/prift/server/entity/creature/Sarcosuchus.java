package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import com.google.common.base.Predicate;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Sarcosuchus extends RiftWaterCreature {
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.<Boolean>createKey(Sarcosuchus.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/sarcosuchus"));
    private EntityLivingBase forcedSpinVictim;
    private int spinTime;
    private boolean messageSent;

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
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(SarcosuchusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isBeingRidden() && this.canUseRightClick() && this.getRightClickCooldown() == 0 && this.isUsingRightClick() && (this.getRightClickUse() >= 0 && this.getRightClickUse() <= 100) && this.getEnergy() > 6) this.forcedSpinAttack();
        else if (!this.isBeingRidden() || !this.canUseRightClick() || this.getRightClickCooldown() > 0 || !this.isUsingRightClick()) {
            this.spinTime = 0;
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
            if (this.getRightClickUse() > 100) this.forcedSpinVictim = null;
            if (this.forcedSpinVictim == null) this.setIsSpinning(false);
        }
    }

    private void forcedSpinAttack() {
        if (this.forcedSpinVictim != null) {
            if (this.forcedSpinVictim.isEntityAlive()) {
                if (!this.isSpinning()) this.setIsSpinning(true);
                double angleToTarget = Math.atan2(this.getLookVec().z, this.getLookVec().x);
                this.forcedSpinVictim.setPosition(2 * Math.cos(angleToTarget) + this.posX, this.posY, 2 * Math.sin(angleToTarget) + this.posZ);
                this.getLookHelper().setLookPositionWithEntity(this.forcedSpinVictim, 30.0F, 30.0F);
                this.attackEntityUsingSpin(this.forcedSpinVictim);
                this.forcedSpinVictim.motionX = 0;
                this.forcedSpinVictim.motionY = 0;
                this.forcedSpinVictim.motionZ = 0;
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
                    if (input instanceof EntityPlayer) {
                        if (!SarcosuchusConfig.sarcosuchusSpinWhitelist && !blackList.contains("player")) {
                            return !input.getUniqueID().equals(ownerID);
                        }
                        else if (SarcosuchusConfig.sarcosuchusSpinWhitelist && blackList.contains("player")) {
                            return !input.getUniqueID().equals(ownerID);
                        }
                    }
                    else {
                        if (!SarcosuchusConfig.sarcosuchusSpinWhitelist && !blackList.contains(EntityList.getKey(input).toString())) {
                            if (input instanceof EntityTameable) {
                                EntityTameable inpTameable = (EntityTameable)input;
                                if (inpTameable.isTamed()) {
                                    return !ownerID.equals(inpTameable.getOwnerId());
                                }
                                else return true;
                            }
                            return true;
                        }
                        else if (SarcosuchusConfig.sarcosuchusSpinWhitelist && blackList.contains(EntityList.getKey(input).toString())) {
                            if (input instanceof EntityTameable) {
                                EntityTameable inpTameable = (EntityTameable)input;
                                if (inpTameable.isTamed()) {
                                    return !ownerID.equals(inpTameable.getOwnerId());
                                }
                                else return true;
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (!potTargetListM.isEmpty()) this.forcedSpinVictim = potTargetListM.get(0);
        }
    }

    @Override
    public void resetParts(float scale) {}

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
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 1.375, this.posZ);
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
}
