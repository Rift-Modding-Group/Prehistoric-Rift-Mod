package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.passive.EntityTameable;

public class RiftFollowOwner extends EntityAIFollowOwner {
    private final EntityTameable tameable;

    public RiftFollowOwner(EntityTameable tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {
        super(tameableIn, followSpeedIn, minDistIn, maxDistIn);
        this.tameable = tameableIn;
    }

    public boolean shouldExecute() {
        if (((RiftCreature) this.tameable).getTameStatus() != TameStatusType.STAND) {
            return false;
        }
        return super.shouldExecute();
    }

    public boolean shouldContinueExecuting() {
        return ((RiftCreature) this.tameable).getTameStatus() != TameStatusType.STAND && super.shouldContinueExecuting();
    }
}
