package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
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
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && this.targetNearEntity(user, target, false);
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
        AxisAlignedBB area = user.getEntityBoundingBox().grow(4D, 0, 4D);
        List<Entity> hitEntitiesList = user.world.getEntitiesWithinAABB(Entity.class, area, this.generalEntityPredicate(user));

        for (Entity entity : hitEntitiesList) {
            user.attackEntityAsMobWithMultiplier(entity, 0.125f);
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {

    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
