package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

public class RiftCreatureMoveHelper extends RiftCreatureMoveHelperBase {
    public RiftCreatureMoveHelper(RiftCreature creature) {
        super(creature);
    }

    @Override
    public void setMoveTo(double x, double y, double z, double speedIn) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.MOVE_TO;
    }

    public void setChargeTo(double x, double y, double z, double speedIn) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.speed = speedIn;
        this.creatureAction = CreatureAction.CHARGE;
    }

    @Override
    public void strafe(float forward, float strafe) {
        this.creatureAction = CreatureAction.STRAFE;
        this.moveForward = forward;
        this.moveStrafe = strafe;
        this.speed = 0.25f;
    }

    @Override
    public void read(EntityMoveHelper that) {
        this.posX = that.getX();
        this.posY = that.getY();
        this.posZ = that.getZ();
        this.speed = Math.max(that.getSpeed(), 1);
        this.moveForward = that.moveForward;
        this.moveStrafe = that.moveStrafe;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (!this.creature.getCurrentMove().isEmpty()) {
            this.creatureAction = CreatureAction.WAIT;
            this.creature.setAIMoveSpeed(0f);
            this.creature.setMoveForward(0f);
            this.creature.setMoveStrafing(0f);
        }
        else if (this.creatureAction == CreatureAction.STRAFE) {
            float creatureSpeed = (float)this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            float finalSpeed = (float) this.speed * creatureSpeed;

            this.creature.setAIMoveSpeed(finalSpeed);
            this.creature.setMoveForward(this.moveForward);
            this.creature.setMoveStrafing(this.moveStrafe);
            this.creatureAction = CreatureAction.WAIT;
        }
        else if (this.creatureAction == CreatureAction.MOVE_TO) {
            this.creatureAction = CreatureAction.WAIT;

            double dispX = this.posX - this.entity.posX;
            double dispZ = this.posZ - this.entity.posZ;
            double dispY = this.posY - this.entity.posY;
            double horizontalDispSq = dispX * dispX + dispZ * dispZ;
            double totalDispSq = horizontalDispSq + dispY * dispY;

            if (totalDispSq < 2.5e-7D) {
                this.creature.setAIMoveSpeed(0f);
                this.creature.setMoveForward(0f);
                this.creature.setMoveStrafing(0f);
            }
            else {
                float newRotationYaw = (float)(MathHelper.atan2(dispZ, dispX) * 180f / (float) Math.PI) - 90f;
                this.creature.rotationYaw = this.limitAngle(this.creature.rotationYaw, newRotationYaw, 90f);
                this.creature.setMoveStrafing(0f);
                this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

                if (dispY > this.creature.stepHeight && horizontalDispSq < Math.max(1f, this.creature.width)) {
                    this.creature.getJumpHelper().setJumping();
                    this.creatureAction = CreatureAction.JUMPING;
                }
            }
        }
        else if (this.creatureAction == CreatureAction.JUMPING) {
            this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            if (this.creature.onGround) this.creatureAction = CreatureAction.WAIT;
        }
        else {
            this.creature.setAIMoveSpeed(0f);
            this.creature.setMoveForward(0f);
            this.creature.setMoveStrafing(0f);
        }
    }
}
