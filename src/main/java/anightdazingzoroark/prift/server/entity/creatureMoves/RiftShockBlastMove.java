package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
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
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.world.rand.nextInt(4) == 0 && user.recentlyHit) return MovePriority.HIGH;
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        for (EntityLivingBase entity : user.world.getEntitiesWithinAABB(EntityLivingBase.class, user.getEntityBoundingBox().grow(8D), new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                return RiftUtil.isAppropriateSize(entityLivingBase, RiftUtil.getMobSize(user)) && RiftUtil.checkForNoAssociations(user, entityLivingBase) && !user.equals(entityLivingBase);
            }
        })) {
            entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 255));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }
}
