package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class RiftPoisonClawMove extends RiftCreatureMove {
    public RiftPoisonClawMove() {
        super(CreatureMove.SCRATCH);
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
            user.attackEntityAsMob(target);
            if (target instanceof EntityLivingBase) {
                ((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.POISON, 30 * 20));
            }
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
