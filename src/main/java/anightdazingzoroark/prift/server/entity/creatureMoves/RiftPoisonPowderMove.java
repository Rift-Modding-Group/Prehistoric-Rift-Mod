package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class RiftPoisonPowderMove extends RiftCreatureMove {
    public RiftPoisonPowderMove() {
        super(CreatureMove.POISON_POWDER);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0 && !RiftUtil.hasPotionEffect(target, MobEffects.POISON);
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

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        RiftUtil.addPotionEffect(target, new PotionEffect(MobEffects.POISON, 200));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
