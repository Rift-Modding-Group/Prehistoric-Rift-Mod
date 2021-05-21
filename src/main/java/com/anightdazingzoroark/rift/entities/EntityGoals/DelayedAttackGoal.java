package com.anightdazingzoroark.rift.entities.EntityGoals;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.EnumSet;

public class DelayedAttackGoal extends Goal {
    protected final PathAwareEntity mob;
    private final double speed;
    private final boolean pauseWhenMobIdle;
    private final double delay;
    private Path path;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int updateCountdownTicks;
    private int field_24667;
    private final int attackIntervalTicks = 20;
    private long lastUpdateTime;

    public DelayedAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, double delay) {
        this.mob = mob;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.delay = delay;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return false;
    }
}
