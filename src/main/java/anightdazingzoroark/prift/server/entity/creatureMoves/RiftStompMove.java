package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RiftStompMove extends RiftCreatureMove {
    public RiftStompMove() {
        super(CreatureMove.STOMP);
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
        if (target != null) {
            //attack target first
            user.attackEntityAsMob(target);

            //get mobs surrounding target and deal 1/4 the damage done to the target
            AxisAlignedBB aabb = target.getEntityBoundingBox().grow(4D, 0, 4D);
            for (EntityLivingBase entityLivingBase : user.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb, this.generalEntityPredicate(user))) {
                if (!entityLivingBase.equals(target) && !entityLivingBase.equals(user))
                    user.attackEntityAsMobWithMultiplier(entityLivingBase, 0.25f);
            }
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
