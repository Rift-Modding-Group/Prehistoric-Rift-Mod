package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class RiftShellterMove extends RiftCreatureMove {
    public RiftShellterMove() {
        super(CreatureMove.SHELLTER);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && (user.getHealth()/user.getMaxHealth() <= 0.25f || user.getEnergy() <= user.getWeaknessEnergy());
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();
        user.setEntityInvulnerable(true);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        if (!RiftUtil.hasPotionEffect(user, MobEffects.REGENERATION)) user.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5 * 20, 4));
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {

    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.setEntityInvulnerable(false);
        user.enableCanRotateMounted();
        user.removePotionEffect(MobEffects.REGENERATION);
    }
}
