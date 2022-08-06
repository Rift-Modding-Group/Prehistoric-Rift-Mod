package com.anightdazingzoroark.rift.server.entities.creatures;

import com.anightdazingzoroark.rift.server.entities.RiftCreature;
import com.anightdazingzoroark.rift.server.entities.goals.RiftAttackGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TyrannosaurusEntity extends RiftCreature implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public TyrannosaurusEntity(EntityType<? extends TamableAnimal> type, Level world) {
        super(type, world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RiftAttackGoal(this, 0.52, 0.52, 1.0D, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Pig.class, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
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
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.attack", false));
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
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
