package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.Vec3d;

public abstract class RiftCreatureMoveHelperBase extends EntityMoveHelper {
    protected final RiftCreature creature;
    protected CreatureAction creatureAction = CreatureAction.WAIT;

    //for angle based moveTo
    protected Float angleToMoveTo; //class version of float is used for angle in angle-based moveTo
    protected boolean moveUpwards;
    protected VerticalMoveOption verticalMoveOption = VerticalMoveOption.NONE;

    //charge related stuff
    public double oldChargeDistNoY = Double.MAX_VALUE;
    public double oldChargeDistWithY = Double.MAX_VALUE;

    //leap related stuff
    protected double maxLeapHeight;
    protected Vec3d leapStartPoint;
    protected Vec3d leapMidPoint; //this should contain the coordinates for the leap midpoint for the creature
    protected int leapTime;

    public RiftCreatureMoveHelperBase(RiftCreature creature) {
        super(creature);
        this.creature = creature;
    }

    //reminder to self that this is meant to be executed every tick
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn) {
        this.angleToMoveTo = null;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.MOVE_TO;
    }

    public void setMoveTo(float angle, VerticalMoveOption verticalMoveOption, double speed) {
        this.angleToMoveTo = angle;
        this.verticalMoveOption = verticalMoveOption;
        this.speed = speed;
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

    public void setLeapTo(double x, double y, double z, double maxLeapHeight) {
        if (this.creature.stopLeapFlag) return;

        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = 8D; //leap speed is always gonna be fixed to 8,
        this.maxLeapHeight = maxLeapHeight;
        this.creatureAction = CreatureAction.LEAP;

        //create leap startpoint if its null
        if (this.leapStartPoint == null) this.leapStartPoint = this.creature.getPositionVector();

        //create leap midpoint if its null
        if (this.leapMidPoint == null) {
            this.leapMidPoint = new Vec3d(
                    (x - this.creature.posX) / 2 + this.creature.posX,
                    this.creature.posY + maxLeapHeight,
                    (z - this.creature.posZ) / 2 + this.creature.posZ
            );
        }
    }

    public void eraseLeapInformation() {
        this.leapStartPoint = null;
        this.leapMidPoint = null;
        this.leapTime = 0;
    }

    public enum CreatureAction {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMP, //classic jump upwards to move upwards
        CHARGE, //charge
        LEAP;
    }

    public enum VerticalMoveOption {
        UPWARDS,
        DOWNWARDS,
        NONE
    }
}
