package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftRangedAttack extends EntityAIBase {
    protected RiftCreature creature;
    protected int shootAnimLength;
    protected int shootAnimTime;
    protected int animTime;
    double speedTowardsTarget;

    public RiftRangedAttack(RiftCreature creature, double speedIn, float shootAnimLength, float shootAnimTime) {
        this.creature = creature;
        this.speedTowardsTarget = speedIn;
        this.shootAnimLength = (int)(shootAnimLength * 20);
        this.shootAnimTime = (int)(shootAnimTime * 20);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
