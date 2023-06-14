package com.anightdazingzoroark.rift.server.entities.creatures;

import com.anightdazingzoroark.rift.client.sounds.RiftSoundRegistry;
import com.anightdazingzoroark.rift.server.blocks.RiftBlockRegistry;
import com.anightdazingzoroark.rift.server.entities.RiftCreature;
import com.anightdazingzoroark.rift.server.entities.RiftEgg;
import com.anightdazingzoroark.rift.server.entities.goals.RiftAttackAnimalsGoal;
import com.anightdazingzoroark.rift.server.entities.goals.RiftAttackGoal;
import com.anightdazingzoroark.rift.server.entities.goals.RiftPickUpItems;
import com.anightdazingzoroark.rift.server.items.RiftItemRegistry;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class TyrannosaurusEntity extends RiftCreature implements GeoEntity {
    private static final Predicate<Entity> ROAR_TARGETS = (entity) -> {
        return entity.isAlive() && !(entity instanceof TyrannosaurusEntity);
    };
    private static final Predicate<Entity> WEAKNESS_TARGETS = (entity) -> {
        return entity.isAlive() && !(entity instanceof TyrannosaurusEntity) && !(entity instanceof RiftEgg);
    };
    public static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(TyrannosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(TyrannosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> HUNTINGTICK = SynchedEntityData.defineId(TyrannosaurusEntity.class, EntityDataSerializers.INT);
    private final RiftAttackGoal attackGoal = new RiftAttackGoal(this, 0.5, 0.5, 1, true);
    private final NearestAttackableTargetGoal attackPlayerGoal = new NearestAttackableTargetGoal<>(this, Player.class, true);
    public final RiftAttackAnimalsGoal attackAnimalsGoal = new RiftAttackAnimalsGoal(this, true, true);
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public TyrannosaurusEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        this.setHunting(!this.isBaby() && new Random().nextBoolean());
        this.setHuntingTick(new Random().nextInt(900, 1500));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(5, new RiftPickUpItems(this, getFavoriteTreats()));
        this.targetSelector.addGoal(5, new RiftPickUpItems(this, getFavoriteFoodItems()));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        if (!this.isBaby()) {
            this.goalSelector.addGoal(1, new TyrannosaurusRoarGoal(this));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1D)
                .add(Attributes.FOLLOW_RANGE, 16D);
    }

    @Override
    public void tick() {
        super.tick();
        //for changing attributes when roaring
        if (!this.isRoaring()) {
            this.goalSelector.addGoal(2, this.attackGoal);
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25D);
        }
        else {
            this.goalSelector.removeGoal(this.attackGoal);
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0D);
        }

        //for casting weakness
        if (!this.isBaby()) {
            for (LivingEntity livingentity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0D), WEAKNESS_TARGETS)) {
                if (!livingentity.hasEffect(MobEffects.WEAKNESS) || livingentity.getEffect(MobEffects.WEAKNESS).getAmplifier() <= 1) {
                    livingentity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 1));
                }
            }
        }

        //for managing hunting system
        if (!this.isBaby()) {
            if (this.isHunting()) {
                this.goalSelector.addGoal(4, this.attackAnimalsGoal);
                this.goalSelector.addGoal(4, this.attackPlayerGoal);
            }
            else {
                this.goalSelector.removeGoal(this.attackPlayerGoal);
                this.goalSelector.removeGoal(this.attackAnimalsGoal);
            }

            if (this.getHuntingTick() <= 0 && this.isHunting()) {
                this.setHunting(false);
                this.setHuntingTick(new Random().nextInt(900, 1500));
            }
            else if (this.getHuntingTick() <= 0 && !this.isHunting()) {
                this.setHunting(true);
                this.setHuntingTick(new Random().nextInt(900, 1500));
            }

            this.setHuntingTick(this.getHuntingTick() - 1);
        }
        else {
            this.goalSelector.addGoal(4, this.attackAnimalsGoal);
        }
    }

    public boolean canTakeItem(ItemStack item) {
        return item.is(RiftItemRegistry.Tags.TYRANNOSAURUS_FAVORITE_FOOD) || item.is(RiftItemRegistry.Tags.TYRANNOSAURUS_FAVORITE_TREATS);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "movement", 0, this::movement));
        controllerRegistrar.add(new AnimationController<>(this, "attacking", 0, this::attacking));
        controllerRegistrar.add(new AnimationController<>(this, "roaring", 0, this::roaring));
    }

    private PlayState movement(software.bernie.geckolib.core.animation.AnimationState<TyrannosaurusEntity> tyrannosaurusEntityAnimationState) {
        if (tyrannosaurusEntityAnimationState.isMoving()) {
            tyrannosaurusEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.tyrannosaurus.walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
    }

    private PlayState attacking(software.bernie.geckolib.core.animation.AnimationState<TyrannosaurusEntity> tyrannosaurusEntityAnimationState) {
        if (this.entityData.get(ATTACKING)) {
            tyrannosaurusEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.tyrannosaurus.attack", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
    }

    private PlayState roaring(software.bernie.geckolib.core.animation.AnimationState<TyrannosaurusEntity> tyrannosaurusEntityAnimationState) {
        if (this.entityData.get(ROARING)) {
            tyrannosaurusEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.tyrannosaurus.roar", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public Predicate<ItemEntity> getFavoriteFoodItems() {
        return (entity) -> {
            return entity.isAlive() && entity.getItem().is(RiftItemRegistry.Tags.TYRANNOSAURUS_FAVORITE_FOOD);
        };
    }

    @Override
    public Predicate<ItemEntity> getFavoriteTreats() {
        return (entity) -> {
            return entity.isAlive() && entity.getItem().is(RiftItemRegistry.Tags.TYRANNOSAURUS_FAVORITE_TREATS);
        };
    }

    protected @NotNull Vec3i getPickupReach() {
        return new Vec3i(3, 3, 3);
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public void setRoaring(boolean bool) {
        this.entityData.set(ROARING, bool);
    }

    public boolean isHunting() {
        return this.entityData.get(HUNTING);
    }

    public void setHunting(boolean bool) {
        this.entityData.set(HUNTING, bool);
    }
    public int getHuntingTick() {
        return this.entityData.get(HUNTINGTICK);
    }

    public void setHuntingTick(int huntingTick) {
        this.entityData.set(HUNTINGTICK, huntingTick);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROARING, false);
        this.entityData.define(HUNTING, false);
        this.entityData.define(HUNTINGTICK, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("IsHunting", this.isHunting());
        compoundTag.putInt("HuntingTick", this.getHuntingTick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setHunting(compoundTag.getBoolean("IsHunting"));
        this.setHuntingTick(compoundTag.getInt("HuntingTick"));
    }

    class TyrannosaurusRoarGoal extends Goal {
        protected final TyrannosaurusEntity mob;
        private int roarTick;
        private int roarCooldown;

        public TyrannosaurusRoarGoal(TyrannosaurusEntity mob) {
            this.mob = mob;
            this.roarCooldown = 0;
        }

        @Override
        public boolean canUse() {
            if (this.mob.getLastHurtByMob() != null && this.roarCooldown >= 100) {
                int roarChance = new Random().nextInt(3);
                return this.mob.getLastHurtByMob() != null && this.roarTick == 0 && roarChance == 0 && !this.mob.isBaby();
            }
            else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !(this.roarTick > 60);
        }

        @Override
        public void start() {
            this.mob.setRoaring(true);
            this.roarCooldown = 0;
        }

        @Override
        public void stop() {
            this.roarTick = 0;
            this.roarCooldown = 0;
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
                            livingentity.hurt(damageSources().mobAttack(this.mob), 2.0F);
                        }
                        this.strongKnockback(livingentity);
                        this.destroyBlocks();
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
            this.roarCooldown++;
            this.roarTick++;
        }

        private void strongKnockback(Entity target) {
            double d0 = target.getX() - this.mob.getX();
            double d1 = target.getZ() - this.mob.getZ();
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            target.push(d0 / d2 * 8.0D, 0.2D, d1 / d2 * 8.0D);
        }

        private void destroyBlocks() {
            for (double x = this.mob.getX() - 8D; x <= this.mob.getX() + 8D; x++) {
                for (double y = this.mob.getY(); y <= this.mob.getY() + 8D; y++) {
                    for (double z = this.mob.getZ() - 8D; z <= this.mob.getZ() + 8D; z++) {
                        BlockPos blockPos = BlockPos.containing(x, y, z);
                        BlockState blockstate = this.mob.level.getBlockState(blockPos);
                        if (!blockstate.isAir() && blockstate.is(RiftBlockRegistry.Tags.WOOD_AND_WEAKER)) {
                          this.mob.level.destroyBlock(blockPos, true, this.mob);
                      }
                    }
                }
            }
        }
    }
}
