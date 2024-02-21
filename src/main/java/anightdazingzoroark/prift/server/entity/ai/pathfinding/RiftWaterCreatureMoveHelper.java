package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

public class RiftWaterCreatureMoveHelper extends EntityMoveHelper {
    private final RiftWaterCreature creature;

    public RiftWaterCreatureMoveHelper(RiftWaterCreature creature) {
        super(creature);
        this.creature = creature;
    }

    @Override
    public void onUpdateMoveHelper() {
        if(this.creature.isInsideOfMaterial(Material.WATER)) this.creature.motionY += 0.005;
        if(this.action == Action.MOVE_TO && !this.creature.getNavigator().noPath()) {
            final double newSpeed = this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            this.creature.setAIMoveSpeed(this.creature.getAIMoveSpeed() + (float)(newSpeed - this.creature.getAIMoveSpeed()) * 0.125f);

            final double x = this.posX - this.creature.posX;
            final double y = this.posY - this.creature.posY;
            final double z = this.posZ - this.creature.posZ;

            if (y != 0) this.creature.motionY += this.creature.getAIMoveSpeed() * y / Math.sqrt(x * x + y * y + z * z) * 0.1;
            if (x != 0 || z != 0) {
                this.creature.rotationYaw = limitAngle(this.creature.rotationYaw, (float)(MathHelper.atan2(z, x) * 180 / Math.PI - 90), 90);
                this.creature.setRenderYawOffset(this.creature.rotationYaw);
            }
        }

        else this.creature.setAIMoveSpeed(0);
    }
}
