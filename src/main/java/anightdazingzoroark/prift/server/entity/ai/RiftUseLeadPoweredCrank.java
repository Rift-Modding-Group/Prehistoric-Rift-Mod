package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RiftUseLeadPoweredCrank extends EntityAIBase {
    private final RiftCreature creature;
    private final double radius;
    private int angle;
    private boolean restFlag;

    public RiftUseLeadPoweredCrank(RiftCreature creature) {
        this.creature = creature;
        this.radius = 3 + creature.width;
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        TileEntity te = this.creature.world.getTileEntity(this.creature.getWorkstationPos());
        if (te != null) {
            return te instanceof TileEntityLeadPoweredCrank && this.creature.isUsingWorkstation();
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isUsingWorkstation() && this.creature.world.getBlockState(this.creature.getWorkstationPos()).getMaterial().isSolid();
    }

    @Override
    public void startExecuting() {
        this.restFlag = false;
        this.angle = this.distFromCrankDeg();
    }

    @Override
    public void updateTask() {
        if (this.creature.getEnergy() > 6 && !this.restFlag) {
//            System.out.println(distFromCrank());
            if (this.withinTolerance(this.distFromCrank(), this.radius, 0.25D)) {
//                System.out.println("in tolerance");
                if (this.angle >= 360) this.angle = 0;
                else this.angle += 1;

                double radian = Math.toRadians(this.angle);
                double offsetX = Math.cos(radian) * this.radius;
                double offsetZ = Math.sin(radian) * this.radius;
                BlockPos targetPos = this.creature.getWorkstationPos().add(offsetX, 0, offsetZ);

                this.creature.getMoveHelper().setMoveTo(targetPos.getX(), this.creature.posY, targetPos.getZ(), 1);
            }
            else if (this.distFromCrank() > this.radius + this.radius * 0.25D) {
//                System.out.println("outside");
                double workXPos = this.creature.getWorkstationPos().getX();
                double workZPos = this.creature.getWorkstationPos().getZ();
                this.creature.getMoveHelper().setMoveTo(workXPos, this.creature.posY, workZPos, 1);
            }
            else if (this.distFromCrank() < this.radius - this.radius * 0.25D) {
                double xDist = Math.cos(Math.toRadians(this.distFromCrankDeg())) * this.radius;
                double zDist = Math.sin(Math.toRadians(this.distFromCrankDeg())) * this.radius;
                BlockPos newPos = this.creature.getWorkstationPos().add(xDist, 0, zDist);
                this.creature.getMoveHelper().setMoveTo(newPos.getX(), 0 ,newPos.getZ(), 1);
            }
        }
        else if (this.creature.getEnergy() <= 6 && !this.restFlag) this.restFlag = true;
        else if (this.creature.getEnergy() == 20) this.restFlag = false;
    }

    private double distFromCrank() {
        double xDist = this.creature.getWorkstationPos().getX() - this.creature.posX;
        double zDist = this.creature.getWorkstationPos().getZ() - this.creature.posZ;
        return Math.sqrt(xDist * xDist + zDist * zDist);
    }

    private int distFromCrankDeg() { //angle in degrees
        double xWorkDist = this.creature.posX - this.creature.getWorkstationPos().getX();
        double zWorkDist = this.creature.posZ - this.creature.getWorkstationPos().getZ();
        return (int)Math.toDegrees(Math.atan2(zWorkDist, xWorkDist));
    }

    private boolean withinTolerance(double inp, double value, double tolerance) {
        double lowerVal = value - (value * tolerance);
        double upperVal = value + (value * tolerance);
        return inp >= lowerVal && inp <= upperVal;
    }
}
