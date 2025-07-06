package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RiftWaterCreatureMoveHelper extends EntityMoveHelper {
    private final RiftWaterCreature creature;

    public RiftWaterCreatureMoveHelper(RiftWaterCreature creature) {
        super(creature);
        this.creature = creature;
    }

    @Override
    public void onUpdateMoveHelper() {
        if (this.action == Action.MOVE_TO) {
            if (!this.creature.getNavigator().noPath() && this.creature.isInWater()) {
                //make normalized vector based on diff between moveto pos and creature pos
                Vec3d moveVector = new Vec3d(this.posX - this.creature.posX, this.posY - this.creature.posY, this.posZ - this.creature.posZ);
                Vec3d mvNormalized = moveVector.normalize();

                //make speed from vector and creature speed
                double creatureSpeed = (float) (this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                Vec3d finalVectors = mvNormalized.scale(creatureSpeed);

                //set speed
                this.creature.setAIMoveSpeed((float) Math.sqrt(finalVectors.x * finalVectors.x + finalVectors.z * finalVectors.z));
                this.creature.setMoveVertical((float) finalVectors.y);

                //set look angle
                if (Math.abs(finalVectors.x) + Math.abs(finalVectors.z) > 0) {
                    float f = (float) (MathHelper.atan2(finalVectors.z, finalVectors.x) * 180 / Math.PI - 90);
                    this.creature.rotationYaw = this.limitAngle(this.creature.rotationYaw, f, 90);
                    this.creature.setRenderYawOffset(this.creature.rotationYaw);
                }
            }
        }
        else {
            this.creature.setAIMoveSpeed(0);
            this.creature.setMoveVertical(0);
        }
    }
}
