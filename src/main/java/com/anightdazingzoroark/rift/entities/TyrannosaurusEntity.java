package com.anightdazingzoroark.rift.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

public class TyrannosaurusEntity extends TameableEntity implements IAnimatable, Angerable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public TyrannosaurusEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 160)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 35);
    }

    protected void initGoals() {
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1, true));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0D));
        this.findTargets();
    }

    private void findTargets() {
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

    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
