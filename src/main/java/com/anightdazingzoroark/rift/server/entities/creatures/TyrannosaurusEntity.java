package com.anightdazingzoroark.rift.server.entities.creatures;

import com.anightdazingzoroark.rift.client.sounds.RiftSoundRegistry;
import com.anightdazingzoroark.rift.server.entities.RiftCreature;
import com.anightdazingzoroark.rift.server.entities.goals.RiftAttackAnimalsGoal;
import com.anightdazingzoroark.rift.server.entities.goals.RiftAttackGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class TyrannosaurusEntity extends RiftCreature implements IAnimatable {
    private static final Predicate<Entity> ROAR_TARGETS = (entity) -> {
        return entity.isAlive() && !(entity instanceof TyrannosaurusEntity);
    };
    public static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(TyrannosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    private final RiftAttackGoal attackGoal = new RiftAttackGoal(this, 0.5, 0.5, 1, true);
    private final AnimationFactory factory = new AnimationFactory(this);

    public TyrannosaurusEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TyrannosaurusRoarGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new RiftAttackAnimalsGoal(this, true, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1D)
                .add(Attributes.FOLLOW_RANGE, 35D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isRoaring()) {
            this.goalSelector.addGoal(2, this.attackGoal);
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25D);
        }
        else {
            this.goalSelector.removeGoal(this.attackGoal);
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0D);
        }
    }

    private <E extends IAnimatable> PlayState movement(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
    }

    private <E extends IAnimatable> PlayState attacking(AnimationEvent<E> event) {
        if (this.entityData.get(ATTACKING)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.attack", true));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
    }

    private <E extends IAnimatable> PlayState roaring(AnimationEvent<E> event) {
        if (this.entityData.get(ROARING)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.roar", true));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::movement));
        data.addAnimationController(new AnimationController(this, "attacking", 0, this::attacking));
        data.addAnimationController(new AnimationController(this, "roaring", 0, this::roaring));
    }

    protected SoundEvent getAmbientSound() {
        return RiftSoundRegistry.TYRANNOSAURUS_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSoundRegistry.TYRANNOSAURUS_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return RiftSoundRegistry.TYRANNOSAURUS_DEATH.get();
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public void setRoaring(boolean bool) {
        this.entityData.set(ROARING, bool);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROARING, false);
    }

    class TyrannosaurusRoarGoal extends Goal {
        protected final TyrannosaurusEntity mob;
        private int roarTick;
        private int roarChance;

        public TyrannosaurusRoarGoal(TyrannosaurusEntity mob) {
            this.mob = mob;
            this.roarChance = new Random().nextInt(4);
        }

        @Override
        public boolean canUse() {
            return this.mob.getLastHurtByMob() != null && this.roarTick == 0 && this.roarChance == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !(this.roarTick > 60);
        }

        @Override
        public void start() {
            this.mob.setRoaring(true);
        }

        @Override
        public void stop() {
            this.roarTick = 0;
        }

        @Override
        public void tick() {
            if (this.roarTick == 7) {
                playSound(RiftSoundRegistry.TYRANNOSAURUS_ROAR.get());
            }
            if (this.roarTick == 8) {
                if (this.mob.isAlive()) {
                    for(LivingEntity livingentity : this.mob.level.getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(25.0D), ROAR_TARGETS)) {
                        if (!(livingentity instanceof TyrannosaurusEntity)) {
                            livingentity.hurt(DamageSource.mobAttack(this.mob), 2.0F);
                        }
                        this.strongKnockback(livingentity);
                    }
                    Vec3 vec3 = this.mob.getBoundingBox().getCenter();
                    for(int i = 0; i < 40; ++i) {
                        double d0 = this.mob.random.nextGaussian() * 0.2D;
                        double d1 = this.mob.random.nextGaussian() * 0.2D;
                        double d2 = this.mob.random.nextGaussian() * 0.2D;
                        this.mob.level.addParticle(ParticleTypes.POOF, vec3.x, vec3.y, vec3.z, d0, d1, d2);
                    }
                }
            }
            if (this.roarTick > 20) {
                this.mob.setRoaring(false);
            }
            this.roarTick++;
        }

        private void strongKnockback(Entity target) {
            double d0 = target.getX() - this.mob.getX();
            double d1 = target.getZ() - this.mob.getZ();
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            target.push(d0 / d2 * 8.0D, 0.2D, d1 / d2 * 8.0D);
        }
    }
}
