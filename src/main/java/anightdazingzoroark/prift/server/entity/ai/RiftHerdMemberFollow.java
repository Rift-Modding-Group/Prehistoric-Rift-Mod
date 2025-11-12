package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;

//todo: make version of this for aquatic creatures
//that is more like fish schooling in post 1.13
public class RiftHerdMemberFollow extends EntityAIBase {
    private final RiftCreature creature;

    public RiftHerdMemberFollow(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.hasHerdLeader() && this.creature.getDistance(this.creature.getHerdLeader()) >= (int) Math.ceil(this.creature.getHerdLeader().width) + 2;
    }

    public void resetTask() {
        this.creature.getNavigator().clearPath();
    }

    public void updateTask() {
        if (this.creature.getNavigator().noPath()) {
            this.creature.getNavigator().tryMoveToEntityLiving(this.creature.getHerdLeader(), 1D);
        }
    }
}
