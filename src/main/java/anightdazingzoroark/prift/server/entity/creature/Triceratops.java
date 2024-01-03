package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.TriceratopsConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IChargingMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
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

public class Triceratops extends RiftCreature implements IChargingMob {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/triceratops"));

    public Triceratops(World worldIn) {
        super(worldIn, RiftCreatureType.TRICERATOPS);
        this.minCreatureHealth = TriceratopsConfig.getMinHealth();
        this.maxCreatureHealth = TriceratopsConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = TriceratopsConfig.triceratopsFavoriteFood;
        this.tamingFood = TriceratopsConfig.triceratopsTamingFood;
        this.experienceValue = 20;
        this.speed = 0.15D;
        this.attackWidth = 4.875f;
        this.chargeWidth = 20f;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(TriceratopsConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledCharge(this, 1.75f, 0.24f, 4f));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.72F, 0.48F));
        this.tasks.addTask(3, new RiftChargeAttack(this, 1.75f, 0.24f, 4f, 8f));
        this.tasks.addTask(4, new RiftAttack(this, 1.0D, 0.72F, 0.48F));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(6, new RiftHerdMemberFollow(this, 10D, 2D, 1D));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanCharge();
    }

    private void manageCanCharge() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanCharge(true);
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
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
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::triceratopsMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::triceratopsAttack));
        data.addAnimationController(new AnimationController(this, "charge", 0, this::triceratopsCharge));
        data.addAnimationController(new AnimationController(this, "controlledCharge", 0, this::triceratopsControlledCharge));
    }

    private <E extends IAnimatable> PlayState triceratopsMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isCharging()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.walk", true));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState triceratopsAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.attack", false));
            return PlayState.CONTINUE;
        }
        else event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState triceratopsCharge(AnimationEvent<E> event) {
        if (!this.isBeingRidden()) {
            if (this.isLoweringHead()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charge_start", true));
                return PlayState.CONTINUE;
            }
            else if (this.isStartCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charge_charging", true));
                return PlayState.CONTINUE;
            }
            else if (this.isCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charging", true));
                return PlayState.CONTINUE;
            }
            else if (this.isEndCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charge_end", true));
                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState triceratopsControlledCharge(AnimationEvent<E> event) {
        if (this.isBeingRidden()) {
            if (this.getRightClickCooldown() == 0) {
                if (this.getRightClickUse() > 0 && this.getEnergy() > 6) {
                    if (this.isLoweringHead()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charge_start", true));
                        return PlayState.CONTINUE;
                    }
                    else if (this.isStartCharging() && this.getEnergy() > 6) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charge_charging", true));
                        return PlayState.CONTINUE;
                    }
                    else if (this.isCharging()) {
                        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.charging", true));
                        return PlayState.CONTINUE;
                    }
                }
            }
        }
        return PlayState.STOP;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.TRICERATOPS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.TRICERATOPS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.TRICERATOPS_DEATH;
    }
}
