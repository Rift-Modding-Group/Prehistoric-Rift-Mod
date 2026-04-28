package anightdazingzoroark.prift.server.entity.aiNew.pathfinding;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.MathHelper;

public class RiftCreatureMoveHelperNew extends RiftCreatureMoveHelperBaseNew {
    public RiftCreatureMoveHelperNew(RiftCreatureNew creature) {
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

    @Override
    public void strafe(float forward, float strafe) {
        this.creatureAction = CreatureAction.STRAFE;
        this.moveForward = forward;
        this.moveStrafe = strafe;
        this.speed = 0.25;
    }

    @Override
    public void read(EntityMoveHelper that) {
        this.action = that.action;
        this.posX = that.getX();
        this.posY = that.getY();
        this.posZ = that.getZ();
        this.speed = Math.max(that.getSpeed(), 1);
        this.moveForward = that.moveForward;
        this.moveStrafe = that.moveStrafe;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.creatureAction == CreatureAction.STRAFE) {
            float creatureSpeed = (float)this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            float finalSpeed = (float) this.speed * creatureSpeed;
            float forwardMove = this.moveForward;
            float strafeMove = this.moveStrafe;
            float slantedMove = MathHelper.sqrt(forwardMove * forwardMove + strafeMove * strafeMove);
            float move = Math.min(slantedMove, 1f);

            move = finalSpeed / move;
            forwardMove = forwardMove * move;
            strafeMove = strafeMove * move;
            float f5 = MathHelper.sin(this.creature.rotationYaw * 0.017453292F);
            float f6 = MathHelper.cos(this.creature.rotationYaw * 0.017453292F);
            float f7 = forwardMove * f6 - strafeMove * f5;
            float f8 = strafeMove * f6 + forwardMove * f5;
            PathNavigate pathnavigate = this.creature.getNavigator();

            NodeProcessor nodeprocessor = pathnavigate.getNodeProcessor();

            if (nodeprocessor.getPathNodeType(this.creature.world, MathHelper.floor(this.creature.posX + (double) f7), MathHelper.floor(this.creature.posY), MathHelper.floor(this.creature.posZ + (double) f8)) != PathNodeType.WALKABLE) {
                this.moveForward = 1f;
                this.moveStrafe = 0f;
                finalSpeed = creatureSpeed;
            }

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

            if (totalDispSq < 2.5000003E-7F) {
                this.creature.setMoveForward(0.0F);
                return;
            }

            float newRotationYaw = (float)(MathHelper.atan2(dispZ, dispX) * 180f / (float) Math.PI) - 90f;
            this.creature.rotationYaw = this.limitAngle(this.creature.rotationYaw, newRotationYaw, 90f);
            this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

            if (dispY > this.creature.stepHeight && horizontalDispSq < Math.max(1f, this.creature.width)) {
                this.creature.getJumpHelper().setJumping();
                this.creatureAction = CreatureAction.JUMPING;
            }
        }
        else if (this.creatureAction == CreatureAction.JUMPING) {
            this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
            if (this.creature.onGround) this.creatureAction = CreatureAction.WAIT;
        }
        else this.creature.setMoveForward(0.0F);
    }
}
