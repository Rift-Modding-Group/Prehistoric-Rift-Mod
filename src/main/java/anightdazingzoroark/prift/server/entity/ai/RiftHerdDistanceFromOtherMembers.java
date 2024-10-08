package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class RiftHerdDistanceFromOtherMembers extends EntityAIBase {
    private RiftCreature creature;
    private RiftCreature memberToAvoid;
    private double avoidDistance;
    private Path path;
    private final PathNavigate navigation;

    public RiftHerdDistanceFromOtherMembers(RiftCreature creature, double avoidDistance) {
        this.creature = creature;
//        this.avoidDistance = 3D;
        this.avoidDistance = avoidDistance;
        this.navigation = creature.getNavigator();
        this.setMutexBits(1);
    }
    @Override
    public boolean shouldExecute() {
        if (this.creature instanceof IHerder) {
            IHerder herder = (IHerder) this.creature;
            if (!herder.canDoHerding()) return false;
            else if (herder.isHerdLeader()) return false;
            else {
                List<RiftCreature> otherMembers = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.creature.getEntityBoundingBox().grow(this.avoidDistance, this.avoidDistance, this.avoidDistance), new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature input) {
                        return !input.isTamed();
                    }
                });
                Vec3d vec3d;
                for (RiftCreature herdMember : otherMembers) {
                    IHerder herderMember = (IHerder) herdMember;
                    if (!herderMember.isHerdLeader() && herderMember.getHerdLeader() == herder.getHerdLeader() && herdMember != this.creature) {
                        this.memberToAvoid = herdMember;
                        break;
                    }
                }
                if (this.memberToAvoid != null) {
                    vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, (int)this.avoidDistance + 1, (int)this.avoidDistance + 1, new Vec3d(this.memberToAvoid.posX, this.memberToAvoid.posY, this.memberToAvoid.posZ));

                    if (vec3d == null) return false;
                    else if (this.memberToAvoid.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < this.memberToAvoid.getDistanceSq(this.creature)) {
                        return false;
                    }
                    else {
                        this.path = this.navigation.getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
                        return this.path != null;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public boolean shouldContinueExecuting() {
        return !this.navigation.noPath();
    }

    public void startExecuting() {
        this.navigation.setPath(this.path, 1D);
    }

    public void resetTask() {
        this.memberToAvoid = null;
    }
}
