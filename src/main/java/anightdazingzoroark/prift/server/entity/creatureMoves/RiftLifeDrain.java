package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;

public class RiftLifeDrain extends RiftCreatureMove {
    public RiftLifeDrain() {
        super(CreatureMove.LIFE_DRAIN);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.getHealth()/user.getMaxHealth() <= 0.5f) {
            return MovePriority.HIGH;
        }
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setGrabVictim(null);
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {
        if (user.getGrabVictim() != null && user.getGrabVictim().isEntityAlive()) {
            user.attackEntityAsMobWithMultiplier(user.getGrabVictim(), 0.25f);
            user.heal((float) user.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 0.25f);
        }
        else this.forceStopFlag = true;
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        user.setGrabVictim(target);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setGrabVictim(null);
        user.resetSpeed();
    }
}
