package com.anightdazingzoroark.rift.entities.Creatures;

import com.anightdazingzoroark.rift.entities.EntityGoals.*;
import com.anightdazingzoroark.rift.entities.RiftCreature;
import com.anightdazingzoroark.rift.registry.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;
import java.util.UUID;

public class TyrannosaurusEntity extends RiftCreature implements IAnimatable, Angerable {
    private static final TrackedData<Boolean> ATTACKING = DataTracker.registerData(TyrannosaurusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ROARING = DataTracker.registerData(TyrannosaurusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HUNTING = DataTracker.registerData(TyrannosaurusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final FollowTargetGoal attackPlayerGoal = new FollowTargetGoal(this, PlayerEntity.class, true);
    private final FollowTargetGoal attackVillagerGoal = new FollowTargetGoal(this, VillagerEntity.class, true);
    private final FollowTargetGoal attackWanderingTraderGoal = new FollowTargetGoal(this, WanderingTraderEntity.class, true);
    private final FollowTargetGoal attackPigGoal = new FollowTargetGoal(this, PigEntity.class, true);
    private final FollowTargetGoal attackSheepGoal = new FollowTargetGoal(this, SheepEntity.class, true);
    private final FollowTargetGoal attackCowGoal = new FollowTargetGoal(this, CowEntity.class, true);
    private final FollowTargetGoal attackChickenGoal = new FollowTargetGoal(this, ChickenEntity.class, true);
    private final FollowTargetGoal attackGoatGoal = new FollowTargetGoal(this, GoatEntity.class, true);
    private final FollowTargetGoal attackRabbitGoal = new FollowTargetGoal(this, RabbitEntity.class, true);
    private final FollowTargetGoal attackWolfGoal = new FollowTargetGoal(this, WolfEntity.class, true);
    private final FollowTargetGoal attackCatGoal = new FollowTargetGoal(this, CatEntity.class, true);
    private final FollowTargetGoal attackOcelotGoal = new FollowTargetGoal(this, OcelotEntity.class, true);
    private final FollowTargetGoal attackFoxGoal = new FollowTargetGoal(this, FoxEntity.class, true);
    private final FollowTargetGoal attackPolarBearGoal = new FollowTargetGoal(this, PolarBearEntity.class, true);
    private final FollowTargetGoal attackHorseGoal = new FollowTargetGoal(this, HorseEntity.class, true);
    private final FollowTargetGoal attackDonkeyGoal = new FollowTargetGoal(this, DonkeyEntity.class, true);
    private final FollowTargetGoal attackMuleGoal = new FollowTargetGoal(this, MuleEntity.class, true);
    private final FollowTargetGoal attackLlamaGoal = new FollowTargetGoal(this, LlamaEntity.class, true);
    private final FollowTargetGoal attackParrotGoal = new FollowTargetGoal(this, ParrotEntity.class, true);
    private final FollowTargetGoal attackPandaGoal = new FollowTargetGoal(this, PandaEntity.class, true);
    private final RevengeGoal revenge = new RevengeGoal(this);
    private final TyrannosaurusWildRoarGoal wildRoarGoal = new TyrannosaurusWildRoarGoal(this);
    private final AnimationFactory factory = new AnimationFactory(this);
    private int huntingTick = 0;
    private int ageTick = 0;
    Random rand = new Random();

    public TyrannosaurusEntity(EntityType<? extends RiftCreature> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setVariant(rand.nextInt(4));
        this.setHunting(rand.nextBoolean());
        System.out.println(this.isHunting());
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D);
    }

    protected void initGoals() {
        this.goalSelector.add(3, new DelayedAttackGoal(this, 1, false, 0.5, 0.5));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0D, 1));
        this.goalSelector.add(5, new LookAroundGoal(this));
    }

    private void stopFindingTargets() {
        this.targetSelector.remove(this.attackPlayerGoal);
        this.targetSelector.remove(this.attackVillagerGoal);
        this.targetSelector.remove(this.attackWanderingTraderGoal);
        this.targetSelector.remove(this.attackPigGoal);
        this.targetSelector.remove(this.attackSheepGoal);
        this.targetSelector.remove(this.attackCowGoal);
        this.targetSelector.remove(this.attackChickenGoal);
        this.targetSelector.remove(this.attackGoatGoal);
        this.targetSelector.remove(this.attackRabbitGoal);
        this.targetSelector.remove(this.attackWolfGoal);
        this.targetSelector.remove(this.attackCatGoal);
        this.targetSelector.remove(this.attackOcelotGoal);
        this.targetSelector.remove(this.attackFoxGoal);
        this.targetSelector.remove(this.attackPolarBearGoal);
        this.targetSelector.remove(this.attackHorseGoal);
        this.targetSelector.remove(this.attackDonkeyGoal);
        this.targetSelector.remove(this.attackMuleGoal);
        this.targetSelector.remove(this.attackLlamaGoal);
        this.targetSelector.remove(this.attackParrotGoal);
        this.targetSelector.remove(this.attackPandaGoal);
    }

    private void findTargets() {
        if (!this.isBaby()) {
            this.targetSelector.add(2, this.attackPlayerGoal);
            this.targetSelector.add(2, this.attackVillagerGoal);
            this.targetSelector.add(2, this.attackWanderingTraderGoal);
        }

        this.targetSelector.add(2, this.attackPigGoal);
        this.targetSelector.add(2, this.attackSheepGoal);
        this.targetSelector.add(2, this.attackCowGoal);
        this.targetSelector.add(2, this.attackChickenGoal);
        this.targetSelector.add(2, this.attackGoatGoal);
        this.targetSelector.add(2, this.attackRabbitGoal);
        this.targetSelector.add(2, this.attackWolfGoal);
        this.targetSelector.add(2, this.attackCatGoal);
        this.targetSelector.add(2, this.attackOcelotGoal);
        this.targetSelector.add(2, this.attackFoxGoal);
        this.targetSelector.add(2, this.attackPolarBearGoal);
        this.targetSelector.add(2, this.attackHorseGoal);
        this.targetSelector.add(2, this.attackDonkeyGoal);
        this.targetSelector.add(2, this.attackMuleGoal);
        this.targetSelector.add(2, this.attackLlamaGoal);
        this.targetSelector.add(2, this.attackParrotGoal);
        this.targetSelector.add(2, this.attackPandaGoal);
    }

    @Override
    protected void mobTick() {
        this.huntingSystem();
        if (this.isBaby()) {
            this.age();
        }
    }

    private void huntingSystem() {
        this.huntingTick++;
        System.out.println(this.huntingTick * 0.05);

        if ((this.huntingTick * 0.05 <= 90) && this.isHunting()) {
            this.findTargets();
        }
        if ((this.huntingTick * 0.05 <= 90) && !this.isHunting()) {
            this.stopFindingTargets();
        }
        if ((this.huntingTick * 0.05 >= 90) && this.isHunting()) {
            this.stopFindingTargets();
            this.setHunting(false);
            this.huntingTick = 0;
        }
        if ((this.huntingTick * 0.05 >= 90) && !this.isHunting()) {
            this.findTargets();
            this.setHunting(true);
            this.huntingTick = 0;
        }
    }

    private void age() {
        this.ageTick++;
        System.out.println(this.ageTick);
        if (ageTick >= 1200) {
            this.setBaby(false);
        }
    }

    @Override
    protected void onGrowUp() {
        if (!this.isBaby()) {
            this.targetSelector.add(1, this.revenge);
            this.goalSelector.add(2, this.wildRoarGoal);
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(150);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(20);
        }
        else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(8);
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        if (this.isBaby()) {
            return super.getDimensions(pose).scaled(1f, 1f);
        }
        else {
            return super.getDimensions(pose).scaled(2f, 2f);
        }
    }

    @Override
    public boolean isBaby() {
        return super.isBaby();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean isAttacking() {
        return this.dataTracker.get(ATTACKING);
    }

    @Override
    public void setAttacking(boolean attacking) {
        this.dataTracker.set(ATTACKING, attacking);
    }

    @Override
    public boolean isRoaring() {
        return this.dataTracker.get(ROARING);
    }

    @Override
    public void setRoaring(boolean roaring) {
        this.dataTracker.set(ROARING, roaring);
    }

    @Override
    public boolean isHunting() {
        return this.dataTracker.get(HUNTING);
    }

    @Override
    public void setHunting(boolean hunting) {
        this.dataTracker.set(HUNTING, hunting);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ATTACKING, false);
        this.dataTracker.startTracking(ROARING, false);
        this.dataTracker.startTracking(HUNTING, false);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.random.nextInt(100) > 75 ? ModSounds.AMBIENT_TYRANNOSAURUS_EVENT : super.getAmbientSound();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HURT_TYRANNOSAURUS_EVENT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DEATH_TYRANNOSAURUS_EVENT;
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

    //animations
    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "movement", 0.0f, this::movement));
        animationData.addAnimationController(new AnimationController<>(this, "attack", 0.0f, this::attack));
        animationData.addAnimationController(new AnimationController<>(this, "roar", 0.0f, this::roar));
    }

    private <E extends IAnimatable> PlayState movement(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        else if (!event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.standing", true));
            return PlayState.CONTINUE;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState attack(AnimationEvent<E> event) {
        if (this.dataTracker.get(ATTACKING)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.attack", false));
            return PlayState.CONTINUE;
        }
        else if (!this.dataTracker.get(ATTACKING)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.null", false));
            return PlayState.CONTINUE;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState roar(AnimationEvent<E> event) {
        if (this.dataTracker.get(ROARING)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.roar", false));
            return PlayState.CONTINUE;
        }
        else if (!this.dataTracker.get(ROARING)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.null", false));
            return PlayState.CONTINUE;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}