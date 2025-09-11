package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.potion.PotionEffect;

public class RiftParalyzingPowderMove extends RiftCreatureMove {
    public RiftParalyzingPowderMove() {
        super(CreatureMove.PARALYZING_POWDER);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0 && !RiftUtil.hasPotionEffect(target, RiftEffects.PARALYSIS);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override //todo: make it so it applies to all surrounding entities
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        RiftUtil.addPotionEffect(target, new PotionEffect(RiftEffects.PARALYSIS, 100));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
