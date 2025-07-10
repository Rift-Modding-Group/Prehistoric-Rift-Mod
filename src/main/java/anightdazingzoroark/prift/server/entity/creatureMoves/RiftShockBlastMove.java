package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nullable;

public class RiftShockBlastMove extends RiftCreatureMove {
    public RiftShockBlastMove() {
        super(CreatureMove.SHOCK_BLAST);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return user.world.rand.nextInt(4) == 0 && user.isRecentlyHit();
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
        for (EntityLivingBase entity : user.world.getEntitiesWithinAABB(EntityLivingBase.class, user.getEntityBoundingBox().grow(8D), this.generalEntityPredicate(user, true))) {
            entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 255));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
