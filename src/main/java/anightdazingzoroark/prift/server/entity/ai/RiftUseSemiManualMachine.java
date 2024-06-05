package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualBase;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RiftUseSemiManualMachine extends EntityAIBase {
    private final RiftCreature creature;
    private final int animLength;
    private final int animStompTime;
    private IWorkstationUser user;
    private int animTime;
    private boolean destroyedFlag;
    private BlockPos workstationPos;

    public RiftUseSemiManualMachine(RiftCreature creature, float animLength, float animStompTime) {
        this.creature = creature;
        this.animLength = (int)(animLength * 20);
        this.animStompTime = (int)(animStompTime * 20);
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        TileEntity te = this.creature.world.getTileEntity(this.creature.getWorkstationPos());
        if (te != null) {
            if (GeneralConfig.canUseMM()) return te instanceof TileEntitySemiManualBase && this.creature.isUsingWorkstation();
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.animTime = 0;
        this.destroyedFlag = false;
        this.workstationPos = this.creature.getWorkstationPos();
        this.user = (IWorkstationUser)this.creature;
        this.user.setUsingWorkAnim(false);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isUsingWorkstation() && !this.destroyedFlag;
    }

    @Override
    public void resetTask() {
        this.user.setUsingWorkAnim(false);
        if (this.destroyedFlag) this.creature.clearWorkstation(true);
    }

    @Override
    public void updateTask() {
        if (this.user.workstationUseFromPos() != null) {
            this.creature.getLookHelper().setLookPosition(this.creature.getWorkstationPos().getX(), this.creature.getWorkstationPos().getY(), this.creature.getWorkstationPos().getZ(), 30, 30);
            if (RiftUtil.entityAtLocation(this.creature, this.user.workstationUseFromPos(), 0.5)) {
                //use workstation
                TileEntity tileEntity = this.creature.world.getTileEntity(this.creature.getWorkstationPos());
                if (tileEntity != null) {
                    if (tileEntity instanceof TileEntitySemiManualBase) {
                        TileEntitySemiManualBase semiManualBase = (TileEntitySemiManualBase) tileEntity;
                        if (this.creature.getEnergy() > 6) {
                            if (this.animTime == 0 && semiManualBase.getTopTEntity().getMustBeReset()) {
                                this.user.setUsingWorkAnim(true);
                                this.creature.playSound(this.user.useAnimSound(), 2, 1);
                            }
                            if (this.animTime == this.animStompTime && semiManualBase.getTopTEntity().getMustBeReset()) semiManualBase.getTopTEntity().setMustBeReset(false);
                            if (this.animTime == this.animLength) {
                                this.user.setUsingWorkAnim(false);
                                this.creature.setEnergy(this.creature.getEnergy() - 3);
                                this.creature.setXP(this.creature.getXP() + 5);
                                this.animTime = -1;
                            }
                            if (semiManualBase.getTopTEntity().getMustBeReset() || this.user.isUsingWorkAnim()) this.animTime++;
                        }
                        else this.animTime = -1;
                    }
                }
            }
            else {
                //move to front of workstation
                this.creature.getMoveHelper().setMoveTo(this.user.workstationUseFromPos().getX(), this.user.workstationUseFromPos().getY(), this.user.workstationUseFromPos().getZ(), 1);
            }
        }
        if (!this.user.isWorkstation(this.workstationPos)) this.destroyedFlag = true;
    }
}
