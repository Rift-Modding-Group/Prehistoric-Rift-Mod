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
    protected PathNavigate pathfinder;
    private BlockPos targetPos;

    public RiftParasaurolophusAlertHerd(Parasaurolophus parasaur) {
        this.parasaur = parasaur;
        this.pathfinder = parasaur.getNavigator();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.parasaur.getAttackTarget();

        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else if (this.parasaur.isTamed()) return false;
        else return true;
    }

    @Override
    public void startExecuting() {
        this.target = this.parasaur.getAttackTarget();
        this.hornAnimTime = 0;
        this.targetPos = null;
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.parasaur.setCanUseHorn(true);
        System.out.println("end");
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.parasaur.getDistance(this.target) <= 16D && this.target.isEntityAlive() && this.target != null;
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
            else {
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
                    }
                }
                else this.parasaur.resetSpeed();
            }
        }
        if (!this.parasaur.canUseHorn() && !this.parasaur.isUsingHorn()) {
            if (this.targetPos == null) this.targetPos = this.getValidSpot();
            if (this.targetPos != null) {
                this.pathfinder.tryMoveToXYZ(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 2.25);
            }
        }
    }

    private BlockPos getValidSpot() {
        Vec3d targetPos = this.target.getPositionVector();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI; // Random angle
            double xOffset = 16 * Math.cos(angle);
            double zOffset = 16 * Math.sin(angle);

            BlockPos newPos = new BlockPos(targetPos.x + xOffset, targetPos.y, targetPos.z + zOffset);

            // Check if newPos is a valid position (not inside a block, etc.)
            if (this.isPositionValid(newPos)) {
                return newPos;
            }
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
        for (int i = 1; i <= height; i++) {
            BlockPos abovePos = pos.up(i);
            IBlockState aboveState = world.getBlockState(abovePos);
            if (aboveState.getMaterial().isSolid()) {
                return false;
            }
        }
        return true;
    }
}
