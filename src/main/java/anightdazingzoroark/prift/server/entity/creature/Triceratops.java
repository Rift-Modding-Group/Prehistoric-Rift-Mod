package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.TriceratopsConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IChargingMob;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
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

public class Triceratops extends RiftCreature implements IChargingMob {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/triceratops"));
    private RiftCreaturePart hipPart;
    private RiftCreaturePart leftBackLegPart;
    private RiftCreaturePart rightBackLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Triceratops(World worldIn) {
        super(worldIn, RiftCreatureType.TRICERATOPS);
        this.minCreatureHealth = TriceratopsConfig.getMinHealth();
        this.maxCreatureHealth = TriceratopsConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = TriceratopsConfig.triceratopsFavoriteFood;
        this.tamingFood = TriceratopsConfig.triceratopsTamingFood;
        this.experienceValue = 20;
        this.speed = 0.15D;
        this.isRideable = true;
        this.attackWidth = 4.875f;
        this.chargeWidth = 20f;
        this.saddleItem = TriceratopsConfig.triceratopsSaddleItem;
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
        this.tasks.addTask(6, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(7, new RiftHerdMemberFollow(this));
        this.tasks.addTask(8, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
        this.tasks.addTask(10, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanCharge();
    }

    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 2.875f, 0, 1.2f, scale, 1.25f * scale, 0f);
            this.bodyPart = new RiftMainBodyPart(this, 1.125f, 0, 0.8f, scale, 0.875f * scale, 1f);
            this.hipPart = new RiftCreaturePart(this, -0.25f, 0, 0.7f, scale, scale, 1f);
            this.leftBackLegPart = new RiftCreaturePart(this, 0.9f, -115, 0, 0.6f * scale, 1.125f * scale, 0.5f);
            this.rightBackLegPart = new RiftCreaturePart(this, 0.9f, 115, 0, 0.6f * scale, 1.125f * scale, 0.5f);
            this.tail0Part = new RiftCreaturePart(this, -1.5f, 0, 0.8f, 0.8f * scale, 0.75f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -2.5f, 0, 1f, 0.6f * scale, 0.6f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -3.375f, 0, 0.9f, 0.6f * scale, 0.5f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.hipPart != null) this.hipPart.onUpdate();
        if (this.leftBackLegPart != null) this.leftBackLegPart.onUpdate();
        if (this.rightBackLegPart != null) this.rightBackLegPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.65f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.hipPart != null) {
            this.world.removeEntityDangerously(this.hipPart);
            this.hipPart = null;
        }
        if (this.leftBackLegPart != null) {
            this.world.removeEntityDangerously(this.leftBackLegPart);
            this.leftBackLegPart = null;
        }
        if (this.rightBackLegPart != null) {
            this.world.removeEntityDangerously(this.rightBackLegPart);
            this.rightBackLegPart = null;
        }
        if (this.tail0Part != null) {
            this.world.removeEntityDangerously(this.tail0Part);
            this.tail0Part = null;
        }
        if (this.tail1Part != null) {
            this.world.removeEntityDangerously(this.tail1Part);
            this.tail1Part = null;
        }
        if (this.tail2Part != null) {
            this.world.removeEntityDangerously(this.tail2Part);
            this.tail2Part = null;
        }
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

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return super.shouldRender(camera) || this.inFrustrum(camera, this.hipPart) || this.inFrustrum(camera, this.leftBackLegPart) || this.inFrustrum(camera, this.rightBackLegPart) || this.inFrustrum(camera, this.tail0Part) || this.inFrustrum(camera, this.tail1Part) || this.inFrustrum(camera, this.tail2Part);
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
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.75f);
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public double followRange() {
        return 4D;
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
    public boolean canBeLeashedTo(EntityPlayer player) {
        return !this.getLeashed() && this.isTamed() && !this.getTameStatus().equals(TameStatusType.SIT);
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
