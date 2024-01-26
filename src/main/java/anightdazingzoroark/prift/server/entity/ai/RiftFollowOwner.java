package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
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
        if (this.tameable.getTameStatus() != TameStatusType.STAND) {
            return false;
        }
        else if (this.tameable.isUtilizingCharging()) {
            return false;
        }
        return super.shouldExecute() && this.tameable.getEnergy() > 0;
    }

    public boolean shouldContinueExecuting() {
        return this.tameable.getTameStatus() != TameStatusType.STAND && !this.tameable.isUtilizingCharging() && !this.tameable.isUsingWorkstation() && this.tameable.getEnergy() > 0 && super.shouldContinueExecuting();
    }
}
