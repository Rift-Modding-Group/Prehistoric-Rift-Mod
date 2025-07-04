package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;

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
        List<Entity> targets = user.getAllTargetsInFront(false);
        for (Entity entity : targets) {
            if (entity instanceof MultiPartEntityPart
                    && ((MultiPartEntityPart)entity).parent instanceof EntityLivingBase)
                user.attackEntityAsMob((EntityLivingBase) ((MultiPartEntityPart)entity).parent);
            else if (entity instanceof EntityLivingBase) user.attackEntityAsMob(entity);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
