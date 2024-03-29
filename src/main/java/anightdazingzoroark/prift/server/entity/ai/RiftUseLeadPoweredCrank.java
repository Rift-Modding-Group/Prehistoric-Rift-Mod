package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;

public class RiftUseLeadPoweredCrank extends EntityAIBase {
    private final RiftCreature creature;
    private final double radius;
    private int posMark; //is values from 0 to 7
    private boolean restFlag;
    private double yRotPos;

    public RiftUseLeadPoweredCrank(RiftCreature creature) {
        this.creature = creature;
        this.radius = 3 + creature.width * 2;
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
        this.posMark = this.initPostMark();
        this.yRotPos = this.creature.posY;
    }

    @Override
    public void resetTask() {
        this.creature.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (this.creature.getEnergy() > 6 && !this.restFlag) {
            if (this.hasMoveSpace()) {
                double markAngle = Math.toRadians((this.posMark * 45) - 90);
                double xPos = (this.radius * Math.cos(markAngle)) + this.creature.getWorkstationPos().getX();
                double zPos = (this.radius * Math.sin(markAngle)) + this.creature.getWorkstationPos().getZ();
                BlockPos newPos = new BlockPos(xPos, this.yRotPos, zPos);

                this.creature.getNavigator().tryMoveToXYZ(newPos.getX(), newPos.getY(), newPos.getZ(), 1);

                if (RiftUtil.entityAtLocation(this.creature, newPos, 1)) {
                    this.posMark++;
                    if (this.posMark > 7) this.posMark = 0;
                }
            }
            else {
                //send message
                if (!this.creature.getNavigator().noPath()) {
                    EntityPlayer player = (EntityPlayer)this.creature.getOwner();
                    player.sendStatusMessage(new TextComponentTranslation("reminder.crank_blocked", this.creature.getName()), false);
                }

                //clear path
                this.creature.getNavigator().clearPath();
            }
        }
        else if (this.creature.getEnergy() <= 6 && !this.restFlag) {
            this.creature.getNavigator().clearPath();
            this.restFlag = true;
        }
        else if (this.creature.getEnergy() == 20) this.restFlag = false;
    }

    private int initPostMark() {
        double minDist = Double.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < 8; i++) {
            double markAngle = Math.toRadians((i * 45) - 90);
            double xPos = (this.radius * Math.cos(markAngle)) + this.creature.getWorkstationPos().getX();
            double zPos = (this.radius * Math.sin(markAngle)) + this.creature.getWorkstationPos().getZ();
            double xDist = xPos - this.creature.posX;
            double zDist = zPos - this.creature.posZ;
            double dist = Math.sqrt(xDist * xDist + zDist * zDist);
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private boolean hasMoveSpace() {
        final int xzBound = 3 + Math.round(this.creature.width);
        final int lowerY = (int)(this.creature.posY - this.creature.getWorkstationPos().getY());
        final int boundY = 3 + (int)(this.creature.height + Math.abs(this.yRotPos - this.creature.getWorkstationPos().getY()));

        for (double x = -xzBound; x <= xzBound; x++) {
            for (int y = lowerY; y <= boundY; y++) {
                for (double z = -xzBound; z <= xzBound; z++) {
                    BlockPos spacePos = this.creature.getWorkstationPos().add(x, y, z);
                    if (x == 0 && z == 0) continue;
                    if (this.creature.world.getBlockState(spacePos).getMaterial() != Material.AIR) return false;
                }
            }
        }

        return true;
    }
}