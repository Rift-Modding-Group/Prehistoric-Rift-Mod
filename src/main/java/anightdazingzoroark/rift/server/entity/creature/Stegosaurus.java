package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Stegosaurus extends RiftCreature implements IAnimatable {
    private static final DataParameter<Boolean> STRONG_ATTACKING = EntityDataManager.<Boolean>createKey(Stegosaurus.class, DataSerializers.BOOLEAN);

    public Stegosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.STEGOSAURUS);
        this.setSize(2.125f, 2.5f);
        this.isRideable = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STRONG_ATTACKING, Boolean.FALSE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.175D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.tasks.addTask(1, new RiftAttack(this, 1.0D, false, 0.96F, 0.36F));
        this.tasks.addTask(3, new RiftWander(this, 1.0D));
        this.tasks.addTask(4, new RiftLookAround(this));
    }

    @Override
    public void updateEnergyActions() {}

    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY + 2.125, this.posZ);
    }

    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

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
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }
}
