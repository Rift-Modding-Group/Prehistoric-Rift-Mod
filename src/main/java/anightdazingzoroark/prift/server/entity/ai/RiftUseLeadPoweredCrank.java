package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class RiftUseLeadPoweredCrank extends EntityAIBase {
    private final RiftCreature creature;
    private ILeadWorkstationUser leadWorkstationUser;
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
        if (this.creature instanceof ILeadWorkstationUser) {
            this.leadWorkstationUser = (ILeadWorkstationUser) this.creature;
            TileEntity te = this.creature.world.getTileEntity(this.leadWorkstationUser.getLeadWorkPos());
            if (te != null) return te instanceof TileEntityLeadPoweredCrank && this.creature.busyAtWorkWithNoTargets();
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.busyAtWorkWithNoTargets() && this.creature.world.getBlockState(this.leadWorkstationUser.getLeadWorkPos()).getMaterial().isSolid();
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
            if (this.hasMoveSpace() && !this.pathingOutsideCreatureBoxBounds()) {
                double markAngle = Math.toRadians((this.posMark * 45) - 90);
                double xPos = (this.radius * Math.cos(markAngle)) + this.leadWorkstationUser.getLeadWorkPos().getX();
                double zPos = (this.radius * Math.sin(markAngle)) + this.leadWorkstationUser.getLeadWorkPos().getZ();
                BlockPos newPos = new BlockPos(xPos, this.yRotPos, zPos);

                this.creature.getNavigator().tryMoveToXYZ(newPos.getX(), newPos.getY(), newPos.getZ(), 1);

                if (RiftUtil.entityAtLocation(this.creature, newPos, 1)) {
                    this.posMark++;
                    this.creature.setXP(this.creature.getXP() + 1);
                    if (this.posMark > 7) this.posMark = 0;
                }
            }
            else {
                if (!this.hasMoveSpace()) {
                    //send message
                    if (!this.creature.getNavigator().noPath()) {
                        EntityPlayer player = (EntityPlayer)this.creature.getOwner();
                        player.sendStatusMessage(new TextComponentTranslation("reminder.crank_blocked", this.creature.getName(false)), false);
                    }
                }
                if (this.pathingOutsideCreatureBoxBounds()) {
                    if (!this.creature.getNavigator().noPath()) {
                        EntityPlayer player = (EntityPlayer)this.creature.getOwner();
                        player.sendStatusMessage(new TextComponentTranslation("reminder.crank_path_outside_creature_box_bounds", this.creature.getName(false)), false);
                    }
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
            double xPos = (this.radius * Math.cos(markAngle)) + this.leadWorkstationUser.getLeadWorkPos().getX();
            double zPos = (this.radius * Math.sin(markAngle)) + this.leadWorkstationUser.getLeadWorkPos().getZ();
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
        final int xzBound = 3 + (int)Math.ceil(this.creature.width);
        final int lowerY = (int)(this.creature.posY - this.leadWorkstationUser.getLeadWorkPos().getY());
        final int boundY = 3 + (int)(this.creature.height + Math.abs(this.yRotPos - this.leadWorkstationUser.getLeadWorkPos().getY()));

        for (double x = -xzBound; x <= xzBound; x++) {
            for (int y = lowerY; y <= boundY; y++) {
                for (double z = -xzBound; z <= xzBound; z++) {
                    BlockPos spacePos = this.leadWorkstationUser.getLeadWorkPos().add(x, y, z);
                    if (x == 0 && z == 0) continue;
                    if (this.creature.world.getBlockState(spacePos).getMaterial() != Material.AIR) return false;
                }
            }
        }

        return true;
    }

    private boolean pathingOutsideCreatureBoxBounds() {
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.creature.world.getTileEntity(this.creature.getHomePos());
        if (creatureBox == null) return false;
        final int xzBound = 3 + (int)Math.ceil(this.creature.width);
        final int lowerY = (int)(this.creature.posY - this.leadWorkstationUser.getLeadWorkPos().getY());
        final int boundY = 3 + (int)(this.creature.height + Math.abs(this.yRotPos - this.leadWorkstationUser.getLeadWorkPos().getY()));

        for (double x = -xzBound; x <= xzBound; x++) {
            for (int y = lowerY; y <= boundY; y++) {
                for (double z = -xzBound; z <= xzBound; z++) {
                    BlockPos spacePos = this.leadWorkstationUser.getLeadWorkPos().add(x, y, z);
                    if (x == 0 && z == 0) continue;
                    if (spacePos.distanceSqToCenter(this.creature.getHomePos().getX(), this.creature.getHomePos().getY(), this.creature.getHomePos().getZ()) >=
                            Math.pow(creatureBox.getWanderRange(), 2)) return true;
                }
            }
        }
        return false;

    }
}