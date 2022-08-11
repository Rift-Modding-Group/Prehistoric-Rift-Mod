package com.anightdazingzoroark.rift.server.entities.creatures;

import com.anightdazingzoroark.rift.server.entities.RiftCreature;
import com.anightdazingzoroark.rift.server.entities.goals.RiftAttackGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public class TyrannosaurusEntity extends RiftCreature implements IAnimatable {
    private static final Predicate<Entity> ROAR_TARGETS = (entity) -> {
        return entity.isAlive() && !(entity instanceof TyrannosaurusEntity);
    };
    public static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(TyrannosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimationFactory factory = new AnimationFactory(this);

    public TyrannosaurusEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TyrannosaurusRoarGoal(this, 2.08, 0.64));
        this.goalSelector.addGoal(2, new RiftAttackGoal(this, 0.5, 0.5, 1, true));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Pig.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Ravager.class, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1D);
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
        protected final double animationTime;
        protected final double roarTime;
        private int roarTick;

        public TyrannosaurusRoarGoal(TyrannosaurusEntity mob, double animationTime, double roarTime) {
            this.mob = mob;
            this.animationTime = Math.floor(animationTime * 20);
            this.roarTime = Math.floor(roarTime * 20);
        }
        @Override
        public boolean canUse() {
            if (this.mob.getLastHurtByMob() != null && this.mob.hurtTime == 0) {
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (this.roarTick > this.roarTime) {
                return false;
            }
            else if (this.mob.getLastHurtByMob() == null) {
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        public void start() {
            this.roarTick = 0;
            this.mob.setRoaring(true);
            System.out.println("start");
        }

        @Override
        public void stop() {
            this.mob.setRoaring(false);
            System.out.println("stop");
        }

        @Override
        public void tick() {
            this.roarTick++;
            if (this.roarTick == this.roarTime) {
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
        }

        private void strongKnockback(Entity target) {
            double d0 = target.getX() - this.mob.getX();
            double d1 = target.getZ() - this.mob.getZ();
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            target.push(d0 / d2 * 4.0D, 0.2D, d1 / d2 * 4.0D);
        }
    }
}
