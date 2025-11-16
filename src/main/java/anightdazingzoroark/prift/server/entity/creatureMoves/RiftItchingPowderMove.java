package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RiftItchingPowderMove extends RiftCreatureMove {
    public RiftItchingPowderMove() {
        super(CreatureMove.ITCHING_POWDER);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0 && this.targetNearEntity(user, target, false);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    //itching powder should now damage all affected creatures by half their current hp
    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        AxisAlignedBB area = user.getEntityBoundingBox().grow(3D, 3D, 3D);
        List<Entity> nearbyEntities = user.world.getEntitiesWithinAABB(Entity.class, area, this.generalEntityPredicate(user, false));
        for (Entity entity : nearbyEntities) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                float halfHealth = (float) Math.floor(entityLivingBase.getHealth() / 2f);
                entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(user), halfHealth);
            }
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
