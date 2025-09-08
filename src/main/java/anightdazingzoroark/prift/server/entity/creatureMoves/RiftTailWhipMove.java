package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public class RiftTailWhipMove extends RiftCreatureMove {
    public RiftTailWhipMove() {
        super(CreatureMove.TAIL_WHIP);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && this.targetNearEntity(user, target, true);
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
        if (target == null) return;
        AxisAlignedBB area = target.getEntityBoundingBox().grow(4D, 4D, 4D);
        List<Entity> hitEntitiesList = user.world.getEntitiesWithinAABB(Entity.class, area, this.generalEntityPredicate(user));

        for (Entity entity : hitEntitiesList) {
            double d0 = user.posX - entity.posX;
            double d1 = user.posZ - entity.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            if (entity instanceof EntityLivingBase) ((EntityLivingBase) entity).knockBack(user, 1, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
            entity.attackEntityFrom(DamageSource.causeMobDamage(user), 1f);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
