package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftThagomizeMove extends RiftCreatureMove {
    public RiftThagomizeMove() {
        super(CreatureMove.THAGOMIZE);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.world.rand.nextInt(4) == 0) return MovePriority.HIGH;
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (target != null) {
            user.attackEntityAsMob(target, (float) RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 0, 4) * 5f);
            NonPotionEffectsHelper.setBleeding(target,
                    (int) RiftUtil.slopeResult(user.getLevel(), true, 0, 100, 0, 4),
                    (int) RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 100, 600));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }
}
