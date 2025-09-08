package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftThagomizeMove extends RiftCreatureMove {
    public RiftThagomizeMove() {
        super(CreatureMove.THAGOMIZE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (target != null) {
            user.attackEntityAsMobWithAdditionalDamage(target, (float) RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 0, 4) * 5f);
            NonPotionEffectsHelper.setBleeding(target,
                    (int) RiftUtil.slopeResult(user.getLevel(), true, 0, 100, 0, 4),
                    (int) RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 100, 600));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
