package com.anightdazingzoroark.rift.server.entities.goals;

import com.anightdazingzoroark.rift.server.entities.RiftCreature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Predicate;

public class RiftAttackAnimalsGoal extends TargetGoal {
    protected final RiftCreature mob;
    protected TargetingConditions targetConditions;
    protected final int randomInterval;
    protected LivingEntity target;
    protected final Class[] animals = {
            Pig.class,
            Sheep.class,
            Cow.class,
            MushroomCow.class,
            Chicken.class,
            Horse.class,
            Donkey.class,
            Mule.class,
            Llama.class,
            Rabbit.class,
            Frog.class,
            Strider.class,
            Goat.class,
            Parrot.class,
            Fox.class,
            Wolf.class,
            Ocelot.class,
            Cat.class,
            Panda.class
    };

    public RiftAttackAnimalsGoal(RiftCreature mob, boolean mustSee, boolean mustReach) {
        this(mob, mustSee, mustReach, (Predicate<LivingEntity>)null);
    }

    public RiftAttackAnimalsGoal(RiftCreature mob, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> conditions) {
        super(mob, mustSee, mustReach);
        this.mob = mob;
        this.targetConditions = TargetingConditions.forCombat().range(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE)).selector(conditions);
        this.randomInterval = reducedTickDelay(10);
    }

    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
        else {
            this.findTarget();
            if (this.target == null) {
                return false;
            }
            else {
                return Arrays.asList(animals).contains(this.target.getClass());
            }
        }
    }

    protected void findTarget() {
        this.target = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(LivingEntity.class, this.getTargetSearchArea(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE)), (p_148152_) -> {
            return true;
        }), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }

    protected AABB getTargetSearchArea(double p_26069_) {
        return this.mob.getBoundingBox().inflate(p_26069_, 4.0D, p_26069_);
    }

    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }
}
