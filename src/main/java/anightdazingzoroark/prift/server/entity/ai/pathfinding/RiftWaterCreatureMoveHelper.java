package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RiftWaterCreatureMoveHelper extends RiftCreatureMoveHelperBase {
    private final RiftWaterCreature waterCreature;

    public RiftWaterCreatureMoveHelper(RiftWaterCreature creature) {
        super(creature);
        this.waterCreature = creature;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.creatureAction == CreatureAction.MOVE_TO) {
            if (!this.waterCreature.getNavigator().noPath() && this.waterCreature.isInWater()) {
                //make normalized vector based on diff between moveto pos and waterCreature pos
                Vec3d moveVector = new Vec3d(this.posX - this.waterCreature.posX, this.posY - this.waterCreature.posY, this.posZ - this.waterCreature.posZ);
                Vec3d mvNormalized = moveVector.normalize();

                //make speed from vector and waterCreature speed
                double creatureSpeed = (float) (this.speed * this.waterCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                Vec3d finalVectors = mvNormalized.scale(creatureSpeed);

                //set look angle
                float f = (float) (MathHelper.atan2(finalVectors.z, finalVectors.x) * 180 / Math.PI - 90);
                this.waterCreature.rotationYaw = this.limitAngle(this.waterCreature.rotationYaw, f, 90);
                this.waterCreature.setRenderYawOffset(this.waterCreature.rotationYaw);

                //set speed
                this.waterCreature.setAIMoveSpeed((float) Math.sqrt(finalVectors.x * finalVectors.x + finalVectors.z * finalVectors.z));
                this.waterCreature.setMoveVertical((float) finalVectors.y);
            }
        }
        else if (this.creatureAction == CreatureAction.CHARGE) {
            //the reason why setChargeTo is to be executed every tick
            this.creatureAction = CreatureAction.WAIT;

            //get dist from pos to move to
            Vec3d chargeVec = new Vec3d(this.posX - this.waterCreature.posX, this.posY - this.waterCreature.posY, this.posZ - this.waterCreature.posZ);
            double dist = chargeVec.length();

            //stop when dist becomes bigger than oldDist
            if (dist > this.oldDist) {
                this.waterCreature.stopChargeFlag = true;
                this.waterCreature.setIsCharging(false);
                this.waterCreature.setAIMoveSpeed(0f);
                return;
            }
            else this.oldDist = dist;

            //move in direction towards the pos to move to
            this.waterCreature.setAIMoveSpeed((float)(this.speed * this.waterCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
        }
        else {
            this.waterCreature.setAIMoveSpeed(0);
            this.waterCreature.setMoveVertical(0);
        }
    }

    public RiftCreatureMoveHelper convertToMoveHelper(RiftCreature creature) {
        RiftCreatureMoveHelper creatureMoveHelper = new RiftCreatureMoveHelper(creature);
        creatureMoveHelper.creatureAction = this.creatureAction;
        creatureMoveHelper.oldDist = this.oldDist;
        return creatureMoveHelper;
    }
}
