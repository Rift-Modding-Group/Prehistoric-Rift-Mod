package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ILeapingMob {
    float leapWidth();
    EntityLivingBase getControlledLeapTarget();
    void setControlledLeapTarget(EntityLivingBase value);
    default void leapToControlledTargetLoc(RiftCreature creature) {
        if (!creature.world.isRemote) {
            creature.setLeaping(true);
            UUID ownerID =  creature.getOwnerId();
            List<EntityLivingBase> potTargetListL = creature.world.getEntitiesWithinAABB(EntityLivingBase.class, creature.getEntityBoundingBox().grow(this.leapWidth()), new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    if (input instanceof EntityTameable) {
                        EntityTameable inpTameable = (EntityTameable)input;
                        if (inpTameable.isTamed()) {
                            return !ownerID.equals(inpTameable.getOwnerId());
                        }
                        else return true;
                    }
                    return true;
                }
            });
            potTargetListL.remove(this);
            potTargetListL.remove(creature.getControllingPassenger());

            if (!potTargetListL.isEmpty()) {
                this.setControlledLeapTarget(RiftUtil.findClosestEntity(creature, potTargetListL));
                double dx = this.getControlledLeapTarget().posX - creature.posX;
                double dz = this.getControlledLeapTarget().posZ - creature.posZ;
                double dist = Math.sqrt(dx * dx + dz * dz);

                double velY = Math.sqrt(2 * RiftUtil.gravity * 6f);
                double totalTime = velY / RiftUtil.gravity;
                double velXZ = dist * 2 / totalTime;

                double angleToTarget = Math.atan2(dz, dx);

                creature.motionX = velXZ * Math.cos(angleToTarget);
                creature.motionZ = velXZ * Math.sin(angleToTarget);
                creature.motionY = velY;
            }
            else creature.setAttacking(true);
        }
    }

    default void leapToControlledTargetLoc(RiftCreature creature, EntityLivingBase target) {
        if (!creature.world.isRemote) {
            creature.setLeaping(true);
            boolean canLeapFlag = true;

            if (target instanceof EntityTameable) {
                canLeapFlag = ((EntityTameable)target).isTamed();
            }

            if (canLeapFlag) {
                this.setControlledLeapTarget(target);

                double dx = this.getControlledLeapTarget().posX - creature.posX;
                double dz = this.getControlledLeapTarget().posZ - creature.posZ;
                double dist = Math.sqrt(dx * dx + dz * dz);

                double velY = Math.sqrt(2 * RiftUtil.gravity * 6f);
                double totalTime = velY / RiftUtil.gravity;
                double velXZ = dist * 2 / totalTime;

                double angleToTarget = Math.atan2(dz, dx);

                creature.motionX = velXZ * Math.cos(angleToTarget);
                creature.motionZ = velXZ * Math.sin(angleToTarget);
                creature.motionY = velY;
            }
        }
    }
}
