package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityMoveHelper;
import org.jetbrains.annotations.NotNull;

public abstract class RiftCreatureMoveHelperBase extends EntityMoveHelper {
    @NotNull
    protected final RiftCreature creature;
    @NotNull
    protected final RiftCreatureLeapHelper leapHelper;
    @NotNull
    protected CreatureAction creatureAction = CreatureAction.WAIT;

    public RiftCreatureMoveHelperBase(@NotNull RiftCreature creature) {
        super(creature);
        this.creature = creature;
        this.leapHelper = new RiftCreatureLeapHelper(this, creature);
    }

    //-----for setting actions-----
    @Override
    public void setMoveTo(double x, double y, double z, double speedIn) {
        if (this.creature.isBoxSitting()) return;
        if (this.isLeaping()) return;
        if (!this.creature.isWithinDeploymentRange(x, y, z)) {
            this.creatureAction = CreatureAction.WAIT;
            this.stopMovement();
            return;
        }
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.MOVE_TO;
    }

    public void setChargeTo(double x, double y, double z, double speedIn) {
        if (this.creature.isBoxSitting() || this.creature.isParalyzed()) return;
        if (this.isLeaping()) return;
        if (!this.creature.isWithinDeploymentRange(x, y, z)) {
            this.creatureAction = CreatureAction.WAIT;
            this.stopMovement();
            return;
        }
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.CHARGE;
    }

    public boolean setLeapTo(double x, double y, double z) {
        if (this.creature.isBoxSitting()) return false;
        if (!this.leapHelper.prepareLeapTo(x, y, z)) return false;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.creatureAction = CreatureAction.LEAP;
        if (!this.creature.world.isRemote) this.creature.setLeaping(true);
        this.creature.setUnableToPathToTarget(false);
        return true;
    }

    @Override
    public void strafe(float forward, float strafe) {
        if (this.creature.isBoxSitting()) return;
        if (this.isLeaping()) return;
        this.creatureAction = CreatureAction.STRAFE;
        this.moveForward = forward;
        this.moveStrafe = strafe;
        this.speed = 0.25f;
    }

    //-----for describing what this move helper will do for each CreatureAction-----
    protected abstract void onWait();

    protected abstract void onMoveTo();

    protected abstract void onStrafe();

    protected abstract void onJumping();

    protected abstract void onCharge();

    protected abstract void onLeap();

    @Override
    public final void onUpdateMoveHelper() {
        if (this.creature.isBoxSitting() || this.creature.isParalyzed()) {
            this.creatureAction = CreatureAction.WAIT;
            this.stopMovement();
            return;
        }
        if (this.creatureAction != CreatureAction.LEAP && !this.creature.getCurrentMove().isEmpty()) {
            this.creatureAction = CreatureAction.WAIT;
        }

        switch (this.creatureAction) {
            case WAIT -> this.onWait();
            case MOVE_TO -> this.onMoveTo();
            case STRAFE -> this.onStrafe();
            case JUMPING -> this.onJumping();
            case CHARGE -> this.onCharge();
            case LEAP -> this.onLeap();
        }
    }

    //-----everything else-----
    @Override
    public boolean isUpdating() {
        return this.creatureAction == CreatureAction.MOVE_TO
                || this.creatureAction == CreatureAction.CHARGE
                || this.creatureAction == CreatureAction.LEAP;
    }

    @Override
    public void read(EntityMoveHelper that) {
        this.posX = that.getX();
        this.posY = that.getY();
        this.posZ = that.getZ();
        this.speed = Math.max(that.getSpeed(), 1);
        this.moveForward = that.moveForward;
        this.moveStrafe = that.moveStrafe;
        if (that instanceof RiftCreatureMoveHelperBase helper) {
            this.creatureAction = helper.creatureAction;
            this.leapHelper.read(helper.leapHelper);
            if (!this.creature.world.isRemote) this.creature.setLeaping(this.isLeaping());
        }
    }

    public boolean isLeaping() {
        return this.leapHelper.isLeaping();
    }

    public void stopMovement() {
        this.leapHelper.cancelLeap();
        this.stopWalkingControls();
    }

    @NotNull
    public RiftCreatureLeapHelper getLeapHelper() {
        return this.leapHelper;
    }

    protected void stopWalkingControls() {
        this.creature.setAIMoveSpeed(0f);
        this.creature.setMoveForward(0f);
        this.creature.setMoveStrafing(0f);
    }

    public enum CreatureAction {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING, //classic jump upwards to move upwards. is strictly vertical movement
        CHARGE, //charge
        LEAP; //jumping with verticality and horizontality
    }
}
