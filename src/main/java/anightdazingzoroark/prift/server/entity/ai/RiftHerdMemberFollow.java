package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;

import java.util.List;
import java.util.Map;

public class RiftHerdMemberFollow extends EntityAIBase {
    private RiftCreature creature;

    public RiftHerdMemberFollow(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.canDoHerding() && !this.creature.isTamed()) {
            final List<RiftCreature> nearbyCreatures = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.creature.herdBoundingBox(), entity -> entity.canAddToHerd() || !entity.hasHerdLeader());
            final RiftCreature herdLeader = nearbyCreatures.stream().filter(RiftCreature::canAddToHerd).findAny().orElse(this.creature);
            herdLeader.addCreatureToHerd(nearbyCreatures.stream().filter(RiftCreature::hasNoHerdLeader));
            return this.creature.hasHerdLeader() && !this.creature.isTamed();
        }
        else return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.hasHerdLeader() && this.creature.isNearHerdLeader();
    }

    public void resetTask() {
        this.creature.separateFromHerdLeader();
    }

    public void updateTask() {
        if (!this.creature.isHerdLeader() && this.creature.hasHerdLeader()) this.creature.followLeader();
    }
}
