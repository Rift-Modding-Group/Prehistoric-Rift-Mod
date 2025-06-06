package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.world.World;

public class RiftFollowOwner extends EntityAIFollowOwner {
    World world;
    private final RiftCreature tameable;

    public RiftFollowOwner(RiftCreature tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {
        super(tameableIn, followSpeedIn, minDistIn, maxDistIn);
        this.tameable = tameableIn;
        this.world = tameableIn.world;
    }

    public boolean shouldExecute() {
        if (this.tameable.isSleeping()) return false;
        else if (this.tameable.isSitting()) return false;
        else if (this.tameable.getDeploymentType() != PlayerTamedCreatures.DeploymentType.PARTY) return false;
        else if (this.tameable.busyAtWork()) return false;
        return super.shouldExecute() && this.tameable.getEnergy() > 0;
    }

    public boolean shouldContinueExecuting() {
        return !this.tameable.isSleeping()
                && !this.tameable.isSitting()
                && this.tameable.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY
                && !this.tameable.busyAtWork()
                && this.tameable.getEnergy() > 0
                && super.shouldContinueExecuting();
    }
}
