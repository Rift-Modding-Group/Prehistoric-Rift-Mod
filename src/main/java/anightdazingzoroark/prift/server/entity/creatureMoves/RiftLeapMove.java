package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftLeapMove extends RiftCreatureMove {
    private boolean notOnGroundFlag = false;

    public RiftLeapMove() {
        super(CreatureMove.POUNCE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return false;
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
        if (!user.onGround && !this.notOnGroundFlag) this.notOnGroundFlag = true;

        if (this.notOnGroundFlag) this.forceStopFlag = user.onGround || user.isInWater();
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        double dx = user.getLookVec().normalize().scale(user.rangedWidth()).x;
        double dz = user.getLookVec().normalize().scale(user.rangedWidth()).z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        double velY = Math.sqrt(2 * RiftUtil.gravity * 6);
        double totalTime = velY / RiftUtil.gravity;
        double velXZ = dist * 2 / totalTime;

        double angleToTarget = Math.atan2(dz, dx);
        user.setLeapDirection((float) (velXZ * Math.cos(angleToTarget)), (float) velY, (float) (velXZ * Math.sin(angleToTarget)));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.enableCanRotateMounted();
    }
}
