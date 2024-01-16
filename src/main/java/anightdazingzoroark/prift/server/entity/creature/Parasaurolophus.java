package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.ParasaurolophusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
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

public class Parasaurolophus extends RiftCreature {
    private static final DataParameter<Boolean> USING_HORN = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_HORN = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FLEEING = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);

    public Parasaurolophus(World worldIn) {
        super(worldIn, RiftCreatureType.PARASAUROLOPHUS);
        this.minCreatureHealth = ParasaurolophusConfig.getMinHealth();
        this.maxCreatureHealth = ParasaurolophusConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = ParasaurolophusConfig.parasaurolophusFavoriteFood;
        this.tamingFood = ParasaurolophusConfig.parasaurolophusTamingFood;
        this.experienceValue = 20;
        this.speed = 0.25D;
        this.attackWidth = 3.5f;
        this.saddleItem = ParasaurolophusConfig.parasaurolophusSaddleItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(USING_HORN, false);
        this.dataManager.register(CAN_USE_HORN, true);
        this.dataManager.register(FLEEING, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ParasaurolophusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, ParasaurolophusConfig.parasaurolophusTargets, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftParasaurolophusAlertHerd(this));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(6, new RiftHerdMemberFollow(this, 10D, 2D, 1D));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

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

    public boolean isUsingHorn() {
        return this.dataManager.get(USING_HORN);
    }

    public void setUsingHorn(boolean value) {
        this.dataManager.set(USING_HORN, value);
    }

    public boolean canUseHorn() {
        return this.dataManager.get(CAN_USE_HORN);
    }

    public void setCanUseHorn(boolean value) {
        this.dataManager.set(CAN_USE_HORN, value);
    }

    public boolean isFleeing() {
        return this.dataManager.get(FLEEING);
    }

    public void setFleeing(boolean value) {
        this.dataManager.set(FLEEING, value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::parasaurolophusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::parasaurolophusAttack));
        data.addAnimationController(new AnimationController(this, "hornUse", 0, this::parasaurolophusHorn));
    }

    private <E extends IAnimatable> PlayState parasaurolophusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusHorn(AnimationEvent<E> event) {
        if (this.isUsingHorn()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.call", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }
}
