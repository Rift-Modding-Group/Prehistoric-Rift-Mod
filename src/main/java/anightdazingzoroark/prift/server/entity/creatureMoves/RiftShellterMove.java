package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class RiftShellterMove extends RiftCreatureMove {
    public RiftShellterMove() {
        super(CreatureMove.SHELLTER);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.getHealth()/user.getMaxHealth() <= 0.5f || user.getEnergy() <= 6) return MovePriority.HIGH;
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
        user.disableCanRotateMounted();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {
        if (!RiftUtil.hasPotionEffect(user, MobEffects.REGENERATION)) user.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5 * 20, 4));
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {

    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
        user.enableCanRotateMounted();
    }
}
