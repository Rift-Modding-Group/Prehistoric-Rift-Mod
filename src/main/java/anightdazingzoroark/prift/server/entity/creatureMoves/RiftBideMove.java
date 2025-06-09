package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public class RiftBideMove extends RiftCreatureMove {
    private float accumulatedDamage = 0;

    public RiftBideMove() {
        super(CreatureMove.BIDE);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {
        this.accumulatedDamage += user.getRecentlyHitDamage();
    }

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        float damageToRelease = this.accumulatedDamage * 2f;
        AxisAlignedBB area = user.getEntityBoundingBox().grow(4D, 4D, 4D);
        List<Entity> hitEntitiesList = user.world.getEntitiesWithinAABB(Entity.class, area, this.generalEntityPredicate(user));

        for (Entity entity : hitEntitiesList) {
            entity.attackEntityFrom(DamageSource.causeMobDamage(user), damageToRelease);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
