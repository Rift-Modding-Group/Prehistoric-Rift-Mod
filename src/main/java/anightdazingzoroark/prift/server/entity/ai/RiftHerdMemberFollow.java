package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;

public class RiftHerdMemberFollow extends EntityAIBase {
    private RiftCreature creature;
    private double minDist;
    private double minDistDoubled;
    private double maxDist;
    private double maxDistDoubled;
    private double speedMod;
    private RiftCreature herdLeader;
    private final PathNavigate pathfinder;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public RiftHerdMemberFollow(RiftCreature creature, double minDist, double maxDist, double speedMod) {
        this.creature = creature;
        this.minDist = minDist;
        this.minDistDoubled = minDist * minDist;
        this.maxDist = maxDist;
        this.maxDistDoubled = maxDist * maxDist;
        this.speedMod = speedMod;
        this.pathfinder = creature.getNavigator();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.canDoHerding()) {
            return false;
        }
        else if (this.creature.isHerdLeader()) {
            return false;
        }
        else if (this.creature.getHerdLeader() != null) {
            if (this.creature.getDistanceSq(this.creature.getHerdLeader()) <= this.minDistDoubled) {
                return false;
            }
            else {
                this.herdLeader = this.creature.getHerdLeader();
                return true;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.isHerdLeader() && !this.pathfinder.noPath() && this.creature.getDistance(this.creature.getHerdLeader()) <= this.minDistDoubled;
    }

    public void startExecuting() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.creature.getPathPriority(PathNodeType.WATER);
        this.creature.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    public void resetTask() {
        this.herdLeader = null;
        this.pathfinder.clearPath();
        this.creature.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
    }

    public void updateTask() {
        this.creature.getLookHelper().setLookPositionWithEntity(this.herdLeader, (float) this.creature.getHerdDist(), (float)this.creature.getVerticalFaceSpeed());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            this.pathfinder.tryMoveToEntityLiving(this.herdLeader, this.speedMod);
        }
    }
}
