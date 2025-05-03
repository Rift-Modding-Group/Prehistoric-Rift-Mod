package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class RiftHerdMemberFollow extends EntityAIBase {
    private final RiftCreature creature;
    private IHerder herder;

    public RiftHerdMemberFollow(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature instanceof IHerder) {
            this.herder = (IHerder) this.creature;
            if (this.herder.canDoHerding() && !this.creature.isTamed()) {
                List<RiftCreature> nearbyCreatures = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.herder.herdBoundingBox(), new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature creature) {
                        if (creature instanceof IHerder) {
                            IHerder testHerder = (IHerder) creature;
                            return testHerder.canAddToHerd() || !testHerder.hasHerdLeader();
                        }
                        return false;
                    }
                });
                RiftCreature herdLeader = nearbyCreatures.stream()
                        .filter(testCreature -> ((IHerder)testCreature).canAddToHerd()).findAny()
                        .orElse(this.creature);
                ((IHerder)herdLeader).addCreatureToHerd(nearbyCreatures.stream()
                        .filter(testCreature -> !((IHerder)testCreature).hasHerdLeader()));
                return this.herder.hasHerdLeader() && !this.creature.isTamed();
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.herder.hasHerdLeader() && this.herder.isNearHerdLeader();
    }

    public void resetTask() {
        this.herder.separateFromHerdLeader();
    }

    public void updateTask() {
        if (!this.herder.isHerdLeader() && this.herder.hasHerdLeader()) this.herder.followLeader();
    }
}
