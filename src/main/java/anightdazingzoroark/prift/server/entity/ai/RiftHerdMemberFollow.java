package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;

import javax.annotation.Nullable;
import java.util.List;

public class RiftHerdMemberFollow extends EntityAIBase {
    private final RiftCreature creature;

    public RiftHerdMemberFollow(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.canDoHerding()) {
            List<RiftCreature> nearbyCreatures = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.creature.herdBoundingBox(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature toHerd) {
                    return toHerd != null && (toHerd.canAddToHerd() || !toHerd.hasHerdLeader());
                }
            });
            RiftCreature herdLeader = nearbyCreatures.stream()
                    .filter(RiftCreature::canAddToHerd).findAny()
                    .orElse(this.creature);
            herdLeader.addCreatureToHerd(nearbyCreatures.stream()
                    .filter(testCreature -> !testCreature.hasHerdLeader()));
            return this.creature.hasHerdLeader();
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.hasHerdLeader() && this.creature.isNearHerdLeader();
    }

    public void resetTask() {
        this.creature.separateFromHerdLeader();
    }

    public void updateTask() {
        if (!this.creature.isHerdLeader() && this.creature.hasHerdLeader()) this.creature.herderFollowLeader();
    }
}
