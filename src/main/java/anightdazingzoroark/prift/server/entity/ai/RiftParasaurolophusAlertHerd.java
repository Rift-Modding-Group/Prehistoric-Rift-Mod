package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class RiftParasaurolophusAlertHerd extends EntityAIBase {
    protected Parasaurolophus parasaur;
    private final int hornAnimLength = 20;
    protected int hornAnimTime;
    protected EntityLivingBase target;
    private BlockPos targetPos;
    private boolean flag;
    private boolean moveFlag;

    public RiftParasaurolophusAlertHerd(Parasaurolophus parasaur) {
        this.parasaur = parasaur;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.parasaur.getAttackTarget();

        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else return !this.parasaur.isTamed() && this.parasaur.getDistance(entitylivingbase) <= 12D;
    }

    @Override
    public void startExecuting() {
        this.target = this.parasaur.getAttackTarget();
        this.hornAnimTime = 0;
        this.targetPos = null;
        this.parasaur.getNavigator().clearPath();
        this.flag = true;
        this.moveFlag = true;
    }

    @Override
    public void resetTask() {
        System.out.println("endin");
        this.parasaur.setCanUseHorn(true);
        this.parasaur.setAttackTarget(null);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.flag;
    }

    @Override
    public void updateTask() {
        if (this.parasaur.canUseHorn() && !this.parasaur.isUsingHorn()) {
            System.out.println("honk");
            //for herd leaders
            if (this.parasaur.isHerdLeader()) {
                System.out.println("leader");
                this.parasaur.setUsingHorn(true);
                for (RiftCreature herdMem : this.parasaur.getHerdMembers(true)) {
                    herdMem.removeSpeed();
                }
            }
            //for single ones
            else if (this.parasaur.getHerdMembers(false).isEmpty()) {
                System.out.println("single");
                this.parasaur.setUsingHorn(true);
                this.parasaur.removeSpeed();
            }
        }
        if (this.parasaur.canUseHorn() && this.parasaur.isUsingHorn()) {
            this.hornAnimTime++;
            if (this.hornAnimTime > this.hornAnimLength) {
                //for herd leaders
                if (this.parasaur.isHerdLeader()) {
                    this.parasaur.setCanUseHorn(false);
                    this.parasaur.setUsingHorn(false);
                    for (RiftCreature herdMem : this.parasaur.getHerdMembers(true)) {
                        herdMem.resetSpeed();
                    }
                }
                //for single ones
                else if (this.parasaur.getHerdMembers(false).isEmpty()) {
                    this.parasaur.setCanUseHorn(false);
                    this.parasaur.setUsingHorn(false);
                    this.parasaur.resetSpeed();
                }
            }
        }
        if (!this.parasaur.canUseHorn() && !this.parasaur.isUsingHorn()) {
            if (this.parasaur.isHerdLeader()) {
                System.out.println("moving as herd");
            }
            //for single ones
            else if (this.parasaur.getHerdMembers(false).isEmpty()) {
                if (this.targetPos == null) this.targetPos = this.getValidSpot();
                if (this.targetPos != null) {
                    System.out.println(this.targetPos);
                    this.parasaur.getMoveHelper().setMoveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 2.25D);
//                    this.moveFlag = false;
                    if (RiftUtil.entityAtLocation(this.parasaur, targetPos, 2)) this.flag = false;
                    System.out.println("moving as single");
                }
            }
        }
    }

    private BlockPos getValidSpot() {
        Vec3d targetPos = this.target.getPositionVector();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double xOffset = 48 * Math.cos(angle);
            double zOffset = 48 * Math.sin(angle);

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
                for (int y = -height; y <= height; y++) {
                    if (RiftUtil.blockExposedToSky(world, pos.add(x, y, z))) return true;
                }
            }
        }
        return false;
    }
}
