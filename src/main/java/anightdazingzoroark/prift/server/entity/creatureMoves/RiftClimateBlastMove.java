package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import net.minecraft.entity.Entity;

import java.util.List;

public class RiftClimateBlastMove extends RiftCreatureMove {
    public RiftClimateBlastMove() {
        super(CreatureMove.CLIMATE_BLAST);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && (user instanceof Dimetrodon) && ((Dimetrodon)user).getTemperature() != EggTemperature.NEUTRAL;
    }

    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user instanceof Dimetrodon && ((Dimetrodon)user).getTemperature() != EggTemperature.NEUTRAL;
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
        List<Entity> entitiesToHit = user.world.getEntitiesWithinAABB(Entity.class, user.getEntityBoundingBox().grow(3D), this.generalEntityPredicate(user));

        //supposing only dimetrodons can use this move, attackEntityAsMobWithMultiplier
        //will do just fine
        for (Entity entity : entitiesToHit) {
            user.attackEntityAsMobWithMultiplier(entity, 1.25f);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
