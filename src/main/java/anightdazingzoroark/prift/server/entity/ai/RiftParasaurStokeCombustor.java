package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import com.codetaylor.mc.pyrotech.IAirflowConsumerCapability;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi.TileCombustionWorkerStoneBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class RiftParasaurStokeCombustor extends EntityAIBase {
    private Parasaurolophus parasaur;
    private final int animLength;
    private final int animBlowTime;
    private int animTime;
    private boolean destroyedFlag;
    private BlockPos workstationPos;

    public RiftParasaurStokeCombustor(Parasaurolophus parasaur) {
        this.parasaur = parasaur;
        this.animLength = (int)(1.76D * 20D);
        this.animBlowTime = (int)(0.48D * 20D);
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        return this.parasaur.isUsingWorkstation();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.parasaur.isUsingWorkstation() && this.parasaur.world.getBlockState(this.parasaur.getWorkstationPos()).getMaterial().isSolid();
    }

    @Override
    public void startExecuting() {
        this.animTime = 0;
        this.parasaur.setBlowing(false);
        this.destroyedFlag = false;
        this.workstationPos = this.parasaur.getWorkstationPos();
    }

    @Override
    public void resetTask() {
        this.parasaur.setBlowing(false);
        if (this.destroyedFlag) this.parasaur.clearWorkstation(true);
    }

    @Override
    public void updateTask() {
        if (this.parasaur.workstationUseFromPos() != null) {
            this.parasaur.getLookHelper().setLookPosition(this.parasaur.getWorkstationPos().getX(), this.parasaur.getWorkstationPos().getY(), this.parasaur.getWorkstationPos().getZ(), 30, 30);
            if (RiftUtil.entityAtLocation(this.parasaur, this.parasaur.workstationUseFromPos(), 0.5)) {
                //use workstation
                TileEntity tileEntity = this.parasaur.world.getTileEntity(this.parasaur.getWorkstationPos());

                if (tileEntity != null) {
                    if (tileEntity instanceof TileCombustionWorkerStoneBase) {
                        TileCombustionWorkerStoneBase stoked = (TileCombustionWorkerStoneBase) tileEntity;
                        if (this.parasaur.getEnergy() > 6 && stoked.hasFuel() && stoked.workerIsActive() && stoked.hasInput()) {
                            if (this.animTime == 0) {
                                this.parasaur.setBlowing(true);
                                this.parasaur.playSound(RiftSounds.PARASAUROLOPHUS_BLOW, 2, 1);
                            }
                            if (this.animTime == this.animBlowTime) stoked.consumeAirflow(8f, false);
                        }
                    }
                    if (this.animTime == this.animLength) {
                        this.parasaur.setBlowing(false);
                        this.parasaur.setEnergy(this.parasaur.getEnergy() - 3);
                    }
                    if (this.animTime > 120) {
                        this.animTime = -1;
                        this.parasaur.setBlowing(false);
                    }
                }
                this.animTime++;
            }
            else {
                //move to front of workstation
                this.parasaur.getMoveHelper().setMoveTo(this.parasaur.workstationUseFromPos().getX(), this.parasaur.workstationUseFromPos().getY(), this.parasaur.workstationUseFromPos().getZ(), 1);
            }
        }
        if (!this.parasaur.isWorkstation(this.workstationPos)) this.destroyedFlag = true;
    }
}
