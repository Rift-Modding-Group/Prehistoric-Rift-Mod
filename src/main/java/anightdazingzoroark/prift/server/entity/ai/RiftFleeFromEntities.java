package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.Vec3d;

public class RiftFleeFromEntities extends EntityAIBase {
    private final RiftCreature creature;
    private final float fleeSpeed;
    private Path path;

    public RiftFleeFromEntities(RiftCreature creature, float fleeSpeed) {
        this.creature = creature;
        this.fleeSpeed = fleeSpeed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        boolean isHerdLeader = this.creature instanceof IHerder ? ((IHerder)this.creature).isHerdLeader() : false;
        boolean isStrayFromHerd = this.creature instanceof IHerder ? !((IHerder)this.creature).isHerdLeader() && !((IHerder)this.creature).hasHerdLeader() : true;
        if (this.creature.isTamed() || !this.creature.fleesFromDanger() || !(isHerdLeader || isStrayFromHerd) || this.creature.getAttackTarget() == null) return false;

        if (this.creature.getRevengeTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getRevengeTarget())) {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, new Vec3d(this.creature.getAttackTarget().posX, this.creature.getAttackTarget().posY, this.creature.getAttackTarget().posZ));

            if (vec3d == null) return false;
            else if (this.creature.getRevengeTarget().getDistanceSq(vec3d.x, vec3d.y, vec3d.z)
                    < this.creature.getRevengeTarget().getDistanceSq(this.creature)) return false;
            else {
                this.path = this.creature.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
                return this.path != null;
            }
        }
        else if (this.creature.getAttackTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getAttackTarget())) {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, new Vec3d(this.creature.getAttackTarget().posX, this.creature.getAttackTarget().posY, this.creature.getAttackTarget().posZ));

            if (vec3d == null) return false;
            else if (this.creature.getAttackTarget().getDistanceSq(vec3d.x, vec3d.y, vec3d.z)
                    < this.creature.getAttackTarget().getDistanceSq(this.creature)) return false;
            else {
                this.path = this.creature.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
                return this.path != null;
            }
        }
        else return false;
    }

    public void startExecuting() {
        this.creature.getNavigator().setPath(this.path, this.fleeSpeed);
        if (this.path.getFinalPathPoint() != null)
            this.creature.getLookHelper().setLookPosition(this.path.getFinalPathPoint().x, this.path.getFinalPathPoint().y, this.path.getFinalPathPoint().z, 30, 30);
    }

    @Override
    public void resetTask() {
        this.creature.setAttackTarget(null);
    }

    public boolean shouldContinueExecuting()
    {
        return !this.creature.getNavigator().noPath();
    }
}
