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
    protected int navigateTimer;

    private double minDist;
    private double minDistDoubled;
    private double maxDist;
    private double maxDistDoubled;
    private double speedMod;
    private RiftCreature herdLeader;
//    private final PathNavigate pathfinder;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public RiftHerdMemberFollow(RiftCreature creature, double minDist, double maxDist, double speedMod) {
        this.creature = creature;
//        this.minDist = minDist;
//        this.minDistDoubled = minDist * minDist;
//        this.maxDist = maxDist;
//        this.maxDistDoubled = maxDist * maxDist;
//        this.speedMod = speedMod;
//        this.pathfinder = creature.getNavigator();
        this.setMutexBits(3);
    }

    protected int getInitialFollowDelay() { return 200 + this.creature.getRNG().nextInt(200) % 20; }

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

    public void startExecuting() {
        this.navigateTimer = 0;
    }

    public void resetTask() {
        this.creature.separateFromHerdLeader();
    }

    public void updateTask() {
        if (--this.navigateTimer < 0) {
            this.navigateTimer = -Math.floorDiv(10, 2);
            this.creature.followLeader();
        }
    }
}
