package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RiftParalyzingPowderMove extends RiftCreatureMove {
    public RiftParalyzingPowderMove() {
        super(CreatureMove.PARALYZING_POWDER);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0 && !RiftUtil.hasPotionEffect(target, RiftEffects.PARALYSIS);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        AxisAlignedBB area = user.getEntityBoundingBox().grow(3D, 3D, 3D);
        List<Entity> nearbyEntities = user.world.getEntitiesWithinAABB(Entity.class, area, this.generalEntityPredicate(user, false));
        for (Entity entity : nearbyEntities) RiftUtil.addPotionEffect(entity, new PotionEffect(RiftEffects.PARALYSIS, 100));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
