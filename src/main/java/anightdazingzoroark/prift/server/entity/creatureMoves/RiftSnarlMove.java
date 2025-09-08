package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;

public class RiftSnarlMove extends RiftCreatureMove {
    public RiftSnarlMove() {
        super(CreatureMove.SNARL);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {}

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (target instanceof EntityLivingBase) ((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {

    }
}
