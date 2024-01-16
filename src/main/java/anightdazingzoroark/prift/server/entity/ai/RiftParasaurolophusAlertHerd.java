package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class RiftParasaurolophusAlertHerd extends EntityAIBase {
    protected Parasaurolophus parasaur;
    private final int hornAnimLength = 20;
    protected int hornAnimTime;
    protected EntityLivingBase target;
    private BlockPos targetPos;
    private boolean flag;

    public RiftParasaurolophusAlertHerd(Parasaurolophus parasaur) {
        this.parasaur = parasaur;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.parasaur.getAttackTarget();

        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else return !this.parasaur.isTamed() && this.parasaur.getDistance(entitylivingbase) <= 16D;
    }

    @Override
    public void startExecuting() {
        this.target = this.parasaur.getAttackTarget();
        this.hornAnimTime = 0;
        this.targetPos = null;
        this.parasaur.getNavigator().clearPath();
        this.flag = true;
    }

    @Override
    public void resetTask() {
        this.parasaur.setCanUseHorn(true);
        this.parasaur.setAttackTarget(null);
        if (this.parasaur.isHerdLeader()) {
            for (RiftCreature herdMem : this.parasaur.getHerdMembers(true)) {
                ((Parasaurolophus)herdMem).setFleeing(false);
            }
        }
        else if (this.parasaur.getHerdLeader().equals(this.parasaur)) this.parasaur.setFleeing(false);
        System.out.println("end");
    }

    @Override
    public boolean shouldContinueExecuting() {
        return flag;
    }

    @Override
    public void updateTask() {
        System.out.println(this.target);
        System.out.println(this.targetPos);
        if (this.parasaur.canUseHorn() && !this.parasaur.isUsingHorn()) {
            if (this.parasaur.isHerdLeader()) {
                this.parasaur.setUsingHorn(true);
                for (RiftCreature herdMem : this.parasaur.getHerdMembers(true)) {
                    herdMem.removeSpeed();
                }
            }
            else if (this.parasaur.getHerdLeader().equals(this.parasaur)) {
                this.parasaur.setUsingHorn(true);
                this.parasaur.removeSpeed();
            }
        }
        if (this.parasaur.isUsingHorn()) {
            this.hornAnimTime++;
            if (this.hornAnimTime >= this.hornAnimLength) {
                this.parasaur.setUsingHorn(false);
                this.parasaur.setCanUseHorn(false);
                if (this.parasaur.isHerdLeader()) {
                    for (RiftCreature herdMem : this.parasaur.getHerdMembers(true)) {
                        herdMem.resetSpeed();
                        ((Parasaurolophus)herdMem).setFleeing(true);
                    }
                }
                else if (this.parasaur.getHerdLeader().equals(this.parasaur)) {
                    this.parasaur.resetSpeed();
                    this.parasaur.setFleeing(true);
                }
            }
        }
        if (!this.parasaur.canUseHorn() && !this.parasaur.isUsingHorn()) {
            if (this.targetPos == null) this.targetPos = this.getValidSpot();
            if (this.targetPos != null && this.parasaur.getNavigator().noPath()) {
                if (this.parasaur.isHerdLeader()) {
                    for (RiftCreature herdMem : this.parasaur.getHerdMembers(true)) {
                        for (int x = 0; x < 15; x++) {
                            herdMem.getNavigator().tryMoveToXYZ(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 2.25);
                            if (!herdMem.getNavigator().noPath()) break;
                        }
                    }
                }
                else if (this.parasaur.getHerdLeader().equals(this.parasaur)) {
                    for (int x = 0; x < 15; x++) {
                        this.parasaur.getNavigator().tryMoveToXYZ(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 2.25);
                        if (!this.parasaur.getNavigator().noPath()) break;
                    }
                }
            }
            if (this.targetPos != null && this.parasaur.getNavigator().noPath()) this.flag = false;
        }
    }

    private BlockPos getValidSpot() {
        Vec3d targetPos = this.target.getPositionVector();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double xOffset = 16 * Math.cos(angle);
            double zOffset = 16 * Math.sin(angle);

            BlockPos newPos = new BlockPos(targetPos.x + xOffset, targetPos.y, targetPos.z + zOffset);

            if (this.isPositionValid(newPos)) return newPos;
        }
        return null;
    }

    private boolean isPositionValid(BlockPos pos) {
        World world = this.parasaur.world;
        IBlockState state = world.getBlockState(pos);
        if (state.getMaterial().isSolid()) {
            return false;
        }
        int height = Math.round(this.parasaur.height);
        int width = Math.round(this.parasaur.width);
        for (int x = (int)(width/2f - 1f); x <= (int)(width/2f + 1f); x++) {
            for (int z = (int)(width/2f - 1f); z <= (int)(width/2f + 1f); z++) {
                for (int y = 0; y <= height; y++) {
                    if (world.getBlockState(pos.add(x, y, z)).getMaterial().isSolid()) return false;
                }
            }
        }
        return true;
    }
}
