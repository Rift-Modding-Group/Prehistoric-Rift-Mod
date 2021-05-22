package com.anightdazingzoroark.rift.entities.EntityGoals;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;

import java.util.EnumSet;

public class DelayedAttackGoal extends Goal {
    protected final PathAwareEntity mob;
    private final double speed;
    private final boolean pauseWhenMobIdle;
    private final double attackAnimLength;
    private final double attackTime;
    private Path path;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int updateCountdownTicks;
    private int field_24667;
    private final int attackIntervalTicks = 20;
    private long lastUpdateTime;

    public DelayedAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, double attackAnimLength, double attackTime) {
        this.mob = mob;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.attackAnimLength = attackAnimLength;
        this.attackTime = attackTime;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        long l = this.mob.world.getTime();
        if (l - this.lastUpdateTime < 20L) {
            return false;
        } else {
            this.lastUpdateTime = l;
            LivingEntity livingEntity = this.mob.getTarget();
            if (livingEntity == null) {
                return false;
            } else if (!livingEntity.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().findPathTo((Entity)livingEntity, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getSquaredMaxAttackDistance(livingEntity) >= this.mob.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                }
            }
        }
    }

    public boolean shouldContinue() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        } else if (!livingEntity.isAlive()) {
            return false;
        } else if (!this.pauseWhenMobIdle) {
            return !this.mob.getNavigation().isIdle();
        } else if (!this.mob.isInWalkTargetRange(livingEntity.getBlockPos())) {
            return false;
        } else {
            return !(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator() && !((PlayerEntity)livingEntity).isCreative();
        }
    }

    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, this.speed);
        this.updateCountdownTicks = 0;
        this.field_24667 = 0;
    }

    public void stop() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(livingEntity)) {
            this.mob.setTarget((LivingEntity)null);
        }

        this.mob.getNavigation().stop();
        this.mob.setAttacking(false);
    }

    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        this.mob.getLookControl().lookAt(livingEntity, 30.0F, 30.0F);
        double d = this.mob.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());

        this.field_24667 = Math.max(this.field_24667 - 1, 0);

        if (d <= this.getSquaredMaxAttackDistance(livingEntity)) {
            ++this.updateCountdownTicks;
            if ((this.pauseWhenMobIdle || this.mob.getVisibilityCache().canSee(livingEntity)) && this.updateCountdownTicks <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || livingEntity.squaredDistanceTo(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.mob.getRandom().nextFloat() < 0.05F)) {
                this.targetX = livingEntity.getX();
                this.targetY = livingEntity.getY();
                this.targetZ = livingEntity.getZ();
            }
            this.field_24667 = Math.max(this.field_24667 - 1, 0);
            this.mob.setAttacking(true);

            if ((this.updateCountdownTicks * 0.05) == this.attackTime){
                this.method_28346();
                this.mob.swingHand(Hand.MAIN_HAND);
                this.mob.tryAttack(livingEntity);
            }
        }
        if ((this.updateCountdownTicks * 0.05) >= this.attackAnimLength) {
            this.mob.setAttacking(false);
            this.updateCountdownTicks = 0;
        }
    }

    protected void method_28346() {
        this.field_24667 = 20;
    }

    protected boolean method_28347() {
        return this.field_24667 <= 0;
    }

    protected int method_28348() {
        return this.field_24667;
    }

    protected int method_28349() {
        return 20;
    }

    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        return (double)(this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F + entity.getWidth());
    }
}
