package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public class RiftShellSpinMove extends RiftCreatureMove {
    public RiftShellSpinMove() {
        super(CreatureMove.SHELL_SPIN);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.getDistance(target) > user.attackWidth() + 1 && user.getDistance(target) <= user.rangedWidth()) {
            return MovePriority.NONE;
        }
        return MovePriority.HIGH;
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
        AxisAlignedBB area = user.getEntityBoundingBox().grow(4D, 0, 4D);
        List<Entity> hitEntitiesList = user.world.getEntitiesWithinAABB(Entity.class, area, new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity instanceof EntityLivingBase) {
                    return RiftUtil.checkForNoAssociations(user, entity) && !user.equals(entity);
                }
                return true;
            }
        });

        for (Entity entity : hitEntitiesList) {
            user.attackEntityAsMobWithMultiplier(entity, 0.125f);
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {

    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }
}
