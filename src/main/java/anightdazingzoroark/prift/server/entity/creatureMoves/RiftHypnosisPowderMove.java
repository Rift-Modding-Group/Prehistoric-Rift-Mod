package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;

public class RiftHypnosisPowderMove extends RiftCreatureMove {
    public RiftHypnosisPowderMove() {
        super(CreatureMove.HYPNOSIS_POWDER);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        //return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0;
        return false;
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
        //todo: make it so it hypnotizes various monsters surrounding the user
        if (target instanceof EntityCreature) NonPotionEffectsHelper.setHypnotized((EntityCreature) target, user);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
