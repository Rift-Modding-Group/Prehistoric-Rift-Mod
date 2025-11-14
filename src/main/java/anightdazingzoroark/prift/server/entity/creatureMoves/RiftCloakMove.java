package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftCloakMove extends RiftCreatureMove {
    public RiftCloakMove() {
        super(CreatureMove.CLOAK);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.isTamed()) {
            return super.canBeExecutedUnmounted(user, target)
                    && ((!user.isCloaked()
                    && user.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE)
                    || (user.isCloaked() && user.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY))
                    && user.getGrabVictim() == null;
        }
        else return super.canBeExecutedUnmounted(user, target) && !user.isCloaked() && user.getGrabVictim() == null;
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
        user.setCloaked(!user.isCloaked());
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
