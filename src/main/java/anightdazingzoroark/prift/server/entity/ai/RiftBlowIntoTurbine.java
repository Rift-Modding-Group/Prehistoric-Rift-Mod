package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RiftBlowIntoTurbine extends EntityAIBase {
    private final RiftCreature creature;
    private IWorkstationUser workstationUser;
    private final float blowPower;
    private final int animLength;
    private final int animBlowTime;
    private IWorkstationUser user;
    private int animTime;
    private boolean destroyedFlag;
    private BlockPos workstationPos;

    public RiftBlowIntoTurbine(RiftCreature creature, float blowPower, float animLength, float animBlowTime) {
        this.creature = creature;
        this.blowPower = blowPower;
        this.animLength = (int)(animLength * 20f);
        this.animBlowTime = (int)(animBlowTime * 20f);
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature instanceof IWorkstationUser) {
            this.workstationUser = (IWorkstationUser) this.creature;
            TileEntity te = this.creature.world.getTileEntity(this.workstationUser.getWorkstationPos());
            if (te != null) {
                if (GeneralConfig.canUseMM()) return te instanceof TileEntityBlowPoweredTurbine && this.workstationUser.isUsingWorkstation();
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.workstationUser.isUsingWorkstation() && !this.destroyedFlag;
    }

    @Override
    public void startExecuting() {
        this.animTime = 0;
        this.destroyedFlag = false;
        this.workstationPos = this.workstationUser.getWorkstationPos();
        this.user = (IWorkstationUser)this.creature;
        this.user.setUsingWorkAnim(false);
    }

    @Override
    public void resetTask() {
        this.user.setUsingWorkAnim(false);
        if (this.destroyedFlag) this.workstationUser.clearWorkstation(true);
    }

    @Override
    public void updateTask() {
        if (this.user.workstationUseFromPos() != null) {
            this.creature.getLookHelper().setLookPosition(this.workstationUser.getWorkstationPos().getX(), this.workstationUser.getWorkstationPos().getY(), this.workstationUser.getWorkstationPos().getZ(), 30, 30);
            if (RiftUtil.entityAtLocation(this.creature, this.user.workstationUseFromPos(), 0.5)) {
                //use workstation
                TileEntity tileEntity = this.creature.world.getTileEntity(this.workstationUser.getWorkstationPos());
                if (tileEntity != null) {
                    if (tileEntity instanceof TileEntityBlowPoweredTurbine) {
                        TileEntityBlowPoweredTurbine turbine = (TileEntityBlowPoweredTurbine) tileEntity;
                        if (this.creature.getEnergy() > 6) {
                            if (this.animTime == 0) {
                                this.user.setUsingWorkAnim(true);
                                this.creature.playSound(this.user.useAnimSound(), 2, 1);
                            }
                            if (this.animTime == this.animBlowTime) turbine.setPower(this.blowPower);
                        }
                    }
                    if (this.animTime == this.animLength) {
                        this.user.setUsingWorkAnim(false);
                        this.creature.energyActionMod++;
                        this.creature.setXP(this.creature.getXP() + 5);
                    }
                    if (this.animTime > 120) {
                        this.animTime = -1;
                        this.user.setUsingWorkAnim(false);
                    }
                }
                this.animTime++;
            }
            else {
                //move to front of workstation
                this.creature.getMoveHelper().setMoveTo(this.user.workstationUseFromPos().getX(), this.user.workstationUseFromPos().getY(), this.user.workstationUseFromPos().getZ(), 1);
            }
        }
        if (!this.user.isWorkstation(this.creature.world, this.workstationPos)) this.destroyedFlag = true;
    }
}
