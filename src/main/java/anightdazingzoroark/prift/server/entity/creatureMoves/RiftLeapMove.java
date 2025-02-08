package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public class RiftLeapMove extends RiftCreatureMove {
    public RiftLeapMove() {
        super(CreatureMove.LEAP);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.getDistance(target) > user.attackWidth() + 1 && user.getDistance(target) <= user.rangedWidth()) {
            return MovePriority.HIGH;
        }
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        if (target != null && user.getDistance(target) <= user.rangedWidth()) {
            double dx = target.posX - user.posX;
            double dz = target.posZ - user.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * 6);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);

            user.motionX = velXZ * Math.cos(angleToTarget);
            user.motionZ = velXZ * Math.sin(angleToTarget);
            user.motionY = velY;
            user.velocityChanged = true;
        }
        else {
            double dx = user.getLookVec().normalize().scale(user.rangedWidth()).x;
            double dz = user.getLookVec().normalize().scale(user.rangedWidth()).z;
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * 6);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);

            user.motionX = velXZ * Math.cos(angleToTarget);
            user.motionZ = velXZ * Math.sin(angleToTarget);
            user.motionY = velY;
            user.velocityChanged = true;
        }
    }

    @Override
    public void whileExecuting(RiftCreature user) {
        if (!user.onGround) {
            //stop if it hits a mob
            AxisAlignedBB leapHitbox = user.getEntityBoundingBox().grow(2D);
            List<Entity> leapedIntoMobs = user.world.getEntitiesWithinAABB(Entity.class, leapHitbox, new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity entity) {
                    if (entity instanceof RiftCreaturePart) {
                        RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                        return !parent.equals(user) && RiftUtil.checkForNoAssociations(user, parent);
                    }
                    else if (entity instanceof EntityLivingBase) return RiftUtil.checkForNoAssociations(user, entity) && !entity.equals(user);
                    else return false;
                }
            });
            if (!leapedIntoMobs.isEmpty()) {
                for (Entity entity : leapedIntoMobs) user.attackEntityAsMob(entity);
                this.forceStopFlag = true;
            }
        }
        else this.forceStopFlag = true;
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {

    }
}
