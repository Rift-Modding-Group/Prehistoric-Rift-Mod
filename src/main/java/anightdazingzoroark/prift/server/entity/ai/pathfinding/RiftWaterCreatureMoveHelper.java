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
            //the reason why setChargeTo is to be executed every tick
            this.creatureAction = CreatureAction.WAIT;

            if (this.waterCreature.isInWater()) {
                //angle based movement
                if (this.angleToMoveTo != null) {
                    //set look angle
                    this.waterCreature.rotationYaw = this.limitAngle(this.waterCreature.rotationYaw, this.angleToMoveTo, 30f);
                    this.waterCreature.setRenderYawOffset(this.waterCreature.rotationYaw);

                    //set speed
                    float creatureSpeed = (float) (this.speed * this.waterCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());

                    //set horizontal speed
                    this.waterCreature.setAIMoveSpeed(creatureSpeed);

                    //set vertical speed
                    switch (this.verticalMoveOption) {
                        case UPWARDS:
                            this.waterCreature.setMoveVertical(creatureSpeed / 2f);
                            break;
                        case DOWNWARDS:
                            this.waterCreature.setMoveVertical(-creatureSpeed / 2f);
                            break;
                        case NONE:
                            this.waterCreature.setMoveVertical(0);
                            break;
                    }
                }
                //non angle based movement
                else {
                    //make normalized vector based on diff between moveto pos and waterCreature pos
                    Vec3d moveVector = new Vec3d(this.posX - this.waterCreature.posX, this.posY - this.waterCreature.posY, this.posZ - this.waterCreature.posZ);
                    Vec3d mvNormalized = moveVector.normalize();

                    //make speed from vector and waterCreature speed
                    double creatureSpeed = (float) (this.speed * this.waterCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                    Vec3d finalVectors = mvNormalized.scale(creatureSpeed);

                    //set look angle
                    float f = (float) (MathHelper.atan2(finalVectors.z, finalVectors.x) * 180 / Math.PI);
                    this.waterCreature.rotationYaw = this.limitAngle(this.waterCreature.rotationYaw, f, 360);
                    this.waterCreature.setRenderYawOffset(this.waterCreature.rotationYaw);

                    //set speed
                    this.waterCreature.setAIMoveSpeed((float) Math.sqrt(finalVectors.x * finalVectors.x + finalVectors.z * finalVectors.z));
                    this.waterCreature.setMoveVertical((float) finalVectors.y);
                }
            }
        }
        else if (this.creatureAction == CreatureAction.CHARGE) {
            //the reason why setChargeTo is to be executed every tick
            this.creatureAction = CreatureAction.WAIT;

            //get distWithY from pos to move to
            Vec3d chargeVec = new Vec3d(this.posX - this.waterCreature.posX, this.posY - this.waterCreature.posY, this.posZ - this.waterCreature.posZ);
            double distWithY = chargeVec.length();
            double distNoY = new Vec3d(chargeVec.x, 0, chargeVec.z).length();

            //when in water, stop when distWithY becomes bigger than oldChargeDistWithY
            if (this.waterCreature.isInWater() && distWithY > this.oldChargeDistWithY) {
                this.waterCreature.stopChargeFlag = true;
                this.waterCreature.setIsCharging(false);
                this.waterCreature.setAIMoveSpeed(0f);
                this.waterCreature.setMoveVertical(0f);
                return;
            }
            //when on land, stop when distNoY becomes bigger than oldChargeDistNoY
            else if (!this.waterCreature.isInWater() && distNoY > this.oldChargeDistNoY) {
                this.waterCreature.stopChargeFlag = true;
                this.waterCreature.setIsCharging(false);
                this.waterCreature.setAIMoveSpeed(0f);
                this.waterCreature.setMoveVertical(0f);
                return;
            }
            else {
                this.oldChargeDistWithY = distWithY;
                this.oldChargeDistNoY = distNoY;
            }

            //make speed from vector and waterCreature speed
            double creatureSpeed = (float) (this.speed * this.waterCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            Vec3d finalVectors = chargeVec.normalize().scale(creatureSpeed);

            //move in direction towards the pos to move to
            this.waterCreature.setAIMoveSpeed((float) Math.sqrt(finalVectors.x * finalVectors.x + finalVectors.z * finalVectors.z));
            this.waterCreature.setMoveVertical((float) finalVectors.y);
        }
        else {
            this.waterCreature.setAIMoveSpeed(0);
            this.waterCreature.setMoveVertical(0);
        }
    }

    public RiftCreatureMoveHelper convertToMoveHelper(RiftCreature creature) {
        RiftCreatureMoveHelper creatureMoveHelper = new RiftCreatureMoveHelper(creature);
        creatureMoveHelper.creatureAction = this.creatureAction;
        creatureMoveHelper.oldChargeDistNoY = this.oldChargeDistNoY;
        creatureMoveHelper.oldChargeDistWithY = this.oldChargeDistWithY;
        return creatureMoveHelper;
    }
}
