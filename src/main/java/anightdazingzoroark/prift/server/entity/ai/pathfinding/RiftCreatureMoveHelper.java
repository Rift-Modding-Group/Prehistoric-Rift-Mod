package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RiftCreatureMoveHelper extends RiftCreatureMoveHelperBase {
    public RiftCreatureMoveHelper(RiftCreature creature) {
        super(creature);
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.creatureAction == CreatureAction.STRAFE) {
            float creatureSpeed = (float)this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            float finalSpeed = (float) this.speed * creatureSpeed;
            float forwardMove = this.moveForward;
            float strafeMove = this.moveStrafe;
            float move = Math.max(MathHelper.sqrt(forwardMove * forwardMove + strafeMove * strafeMove), 1f);

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
            //the reason why setMoveTo is to be executed every tick
            this.creatureAction = CreatureAction.WAIT;

            //get dist from pos to move to
            Vec3d moveVector = new Vec3d(this.posX - this.creature.posX, this.posY - this.creature.posY, this.posZ - this.creature.posZ);
            Vec3d moveVectorNoHeight = new Vec3d(moveVector.x, 0, moveVector.z);
            double dist = moveVector.length();
            double distNoHeight = moveVectorNoHeight.length();

            //stop when creature reaches pos
            if (dist <= 0) {
                this.creature.setMoveForward(0f);
                return;
            }

            //move in direction towards the pos to move to
            float newRotationYaw = (float)(MathHelper.atan2(moveVector.z, moveVector.x) * (180D / Math.PI)) - 90f;
            this.creature.rotationYaw = this.limitAngle(this.creature.rotationYaw, newRotationYaw, 90f);
            this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

            //jump
            if (moveVector.y > (double)this.creature.stepHeight && distNoHeight < (double) Math.max(1f, this.creature.width)) {
                this.creature.getJumpHelper().setJumping();
                this.action = EntityMoveHelper.Action.JUMPING;
            }
        }
        else if (this.creatureAction == CreatureAction.JUMP) {
            this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

            if (this.creature.onGround) this.creatureAction = CreatureAction.WAIT;
        }
        else if (this.creatureAction == CreatureAction.CHARGE) {
            //the reason why setChargeTo is to be executed every tick
            this.creatureAction = CreatureAction.WAIT;

            //get distNoY from pos to move to, note that we don't care about y pos here
            Vec3d chargeVec = new Vec3d(this.posX - this.creature.posX, this.posY - this.creature.posY, this.posZ - this.creature.posZ);
            double distWithY = chargeVec.length();
            double distNoY = new Vec3d(chargeVec.x, 0, chargeVec.z).length();

            //stop when distNoY becomes bigger than oldChargeDistNoY
            if (distNoY > this.oldChargeDistNoY) {
                this.creature.stopChargeFlag = true;
                this.creature.setIsCharging(false);
                this.creature.setAIMoveSpeed(0f);
                return;
            }
            else {
                this.oldChargeDistWithY = distWithY;
                this.oldChargeDistNoY = distNoY;
            }

            //move in direction towards the pos to move to
            this.creature.setAIMoveSpeed((float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
        }
        else this.creature.setMoveForward(0f);
    }

    public RiftWaterCreatureMoveHelper convertToWaterMoveHelper(RiftWaterCreature waterCreature) {
        RiftWaterCreatureMoveHelper waterCreatureMoveHelper = new RiftWaterCreatureMoveHelper(waterCreature);
        waterCreatureMoveHelper.creatureAction = this.creatureAction;
        waterCreatureMoveHelper.oldChargeDistNoY = this.oldChargeDistNoY;
        waterCreatureMoveHelper.oldChargeDistWithY = this.oldChargeDistWithY;
        return waterCreatureMoveHelper;
    }
}
