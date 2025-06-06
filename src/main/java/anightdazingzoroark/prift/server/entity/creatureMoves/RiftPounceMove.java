package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.stream.Collectors;

public class RiftPounceMove extends RiftCreatureMove {
    private boolean notOnGroundFlag = false;

    public RiftPounceMove() {
        super(CreatureMove.POUNCE);
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user.onGround && !user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.disableCanRotateMounted();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        if ((!user.onGround || user.isInWater()) && !this.notOnGroundFlag) this.notOnGroundFlag = true;

        if (this.notOnGroundFlag) {
            if (!user.onGround || user.isInWater()) {
                //stop if it hits a mob
                AxisAlignedBB leapHitbox = user.getEntityBoundingBox().grow(2D);
                List<Entity> leapedIntoMobs = user.world.getEntitiesWithinAABB(Entity.class, leapHitbox, this.generalEntityPredicate(user));
                //remove duplicates
                leapedIntoMobs = leapedIntoMobs.stream().distinct().collect(Collectors.toList());

                if (!leapedIntoMobs.isEmpty()) {
                    for (Entity entity : leapedIntoMobs) user.attackEntityAsMob(entity);
                    this.forceStopFlag = true;
                }
            }
            else this.forceStopFlag = true;
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (target != null && user.getDistance(target) <= user.rangedWidth()) {
            double dx = target.posX - user.posX;
            double dz = target.posZ - user.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * 6);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);
            user.setLeapDirection((float) (velXZ * Math.cos(angleToTarget)), (float) velY, (float) (velXZ * Math.sin(angleToTarget)));
        }
        else {
            double dx = user.getLookVec().normalize().scale(user.rangedWidth()).x;
            double dz = user.getLookVec().normalize().scale(user.rangedWidth()).z;
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * 6);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);
            user.setLeapDirection((float) (velXZ * Math.cos(angleToTarget)), (float) velY, (float) (velXZ * Math.sin(angleToTarget)));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.enableCanRotateMounted();
    }
}
