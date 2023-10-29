package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RiftMoveToHomePos extends EntityAIBase {
    private final RiftCreature creature;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
    private final double speed;

    public RiftMoveToHomePos(RiftCreature creature, double speedIn) {
        this.creature = creature;
        this.speed = speedIn;
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isTamed()) {
            if (creature.getHasHomePos() && creature.getTameStatus() == TameStatusType.WANDER) {
                if (this.creature.getIdleTime() >= 100) return false;
                if (this.creature.getRNG().nextInt(120) != 0) return false;

                BlockPos blockpos = this.creature.getHomePos();
                Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.creature, 16, 7, new Vec3d((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()));

                if (vec3d == null) return false;
                else {
                    this.movePosX = vec3d.x;
                    this.movePosY = vec3d.y;
                    this.movePosZ = vec3d.z;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.speed);
    }
}
