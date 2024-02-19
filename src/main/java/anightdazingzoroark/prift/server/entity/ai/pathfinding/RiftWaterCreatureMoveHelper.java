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
        if (this.action == EntityMoveHelper.Action.MOVE_TO && !this.creature.getNavigator().noPath() && this.creature.isInWater()) {
            if (this.creature.isInsideOfMaterial(Material.WATER)) this.creature.motionY += 0.005;

            double d0 = this.posX - this.creature.posX;
            double d1 = this.posY - this.creature.posY;
            double d2 = this.posZ - this.creature.posZ;
            double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d1 = d1 / d3;
            float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            this.creature.rotationYaw = this.limitAngle(this.creature.rotationYaw, f, 90.0F);
            this.creature.renderYawOffset = this.creature.rotationYaw;

            float f1 = (float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            this.creature.setAIMoveSpeed(this.creature.getAIMoveSpeed() + (f1 - this.creature.getAIMoveSpeed()) * 0.125F);

            this.creature.motionY += (double)this.creature.getAIMoveSpeed() * d1 * 0.1D;
        }

        if (!this.creature.isInWater()) this.creature.setAIMoveSpeed(0.0F);
        else super.onUpdateMoveHelper();
    }
}
