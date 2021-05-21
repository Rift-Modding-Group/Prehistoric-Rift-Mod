package com.anightdazingzoroark.rift.entities;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

public class TyrannosaurusEntity extends TameableEntity implements IAnimatable, Angerable {
    private static final TrackedData<Boolean> ATTACKING = DataTracker.registerData(TyrannosaurusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final AnimationFactory factory = new AnimationFactory(this);

    public TyrannosaurusEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 160)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 19);
    }

    protected void initGoals() {
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1, true));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(5, new LookAroundGoal(this));
        this.findTargets();
    }

    private void findTargets() {
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new FollowTargetGoal(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, VillagerEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, WanderingTraderEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, PigEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, SheepEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, CowEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, ChickenEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, WolfEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, CatEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, OcelotEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, FoxEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, PolarBearEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, HorseEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, DonkeyEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, MuleEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, ParrotEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal(this, PandaEntity.class, true));
    }

    private <E extends IAnimatable> PlayState controller(AnimationEvent<E> event) {
        if (event.isMoving() == true && this.dataTracker.get(ATTACKING) == false) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        else if (event.isMoving() == false && this.dataTracker.get(ATTACKING) == false) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.standing", true));
            return PlayState.CONTINUE;
        }

        if(this.dataTracker.get(ATTACKING) == true) {
            System.out.println("attack anim!");
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.attack", true));
            return PlayState.CONTINUE;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.look_at_target", true));
        return PlayState.CONTINUE;
    }

    @Environment(EnvType.CLIENT)
    public boolean isAttacking() {
        return (Boolean) this.dataTracker.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.dataTracker.set(ATTACKING, attacking);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ATTACKING, false);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public int getAngerTime() {
        return 60;
    }

    @Override
    public void setAngerTime(int ticks) {

    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return null;
    }

    @Override
    public void setAngryAt(@Nullable UUID uuid) {

    }

    @Override
    public void chooseRandomAngerTime() {

    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0.0f, this::controller));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
