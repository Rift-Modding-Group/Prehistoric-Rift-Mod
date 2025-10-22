package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityMoveHelper;

public abstract class RiftCreatureMoveHelperBase extends EntityMoveHelper {
    protected final RiftCreature creature;
    protected CreatureAction creatureAction = CreatureAction.WAIT;

    //charge related stuff
    public double oldDist = Double.MAX_VALUE;

    public RiftCreatureMoveHelperBase(RiftCreature creature) {
        super(creature);
        this.creature = creature;
    }

    //reminder to self that this is meant to be executed every tick
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.MOVE_TO;
    }

    @Override
    public void strafe(float forward, float strafe) {
        this.creatureAction = CreatureAction.STRAFE;
        this.moveForward = forward;
        this.moveStrafe = strafe;
        this.speed = 0.25D;
    }

    //this is meant to be executed every tick too
    public void setChargeTo(double x, double y, double z, double speedIn) {
        if (this.creature.stopChargeFlag) return;

        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.CHARGE;
    }

    public enum CreatureAction {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMP, //classic jump upwards to move upwards
        CHARGE, //charge
        LEAP;
    }
}
