package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.ai.*;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Stegosaurus extends RiftCreature implements IAnimatable, IRangedAttackMob {
    private static final DataParameter<Boolean> STRONG_ATTACKING = EntityDataManager.<Boolean>createKey(Stegosaurus.class, DataSerializers.BOOLEAN);

    public Stegosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.STEGOSAURUS);
        this.setSize(2.125f, 2.5f);
        this.speed = 0.175f;
        this.isRideable = true;
        this.attackWidth = 7.5f;
        this.rangedWidth = 12f;
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
        this.tasks.addTask(1, new RiftRangedAttack(this, false, 1.0D, 1.52F, 1.04F));
        this.tasks.addTask(1, new RiftControlledAttack(this, 0.96F, 0.36F));
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, 0.96F, 0.36F));
        this.tasks.addTask(3, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(3, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(4, new RiftHerdMemberFollow(this, 10D, 2D, 1D));
        this.tasks.addTask(5, new RiftWander(this, 1.0D));
        this.tasks.addTask(6, new RiftLookAround(this));
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
            if (target == null) {
                if (!this.isAttacking()) this.setAttacking(true);
            }
            else {
                if (!this.isAttacking()) {
                    this.ssrTarget = target;
                    this.setAttacking(true);
                }
            }
        }
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

    public void setStrongAttacking(boolean value) {
        this.dataManager.set(STRONG_ATTACKING, Boolean.valueOf(value));
        this.setActing(value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::stegosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::stegosaurusAttack));
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
}
