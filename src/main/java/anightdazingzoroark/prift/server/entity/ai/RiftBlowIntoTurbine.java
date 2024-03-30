package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi.TileCombustionWorkerStoneBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;

public class RiftBlowIntoTurbine extends EntityAIBase {
    private final RiftCreature creature;
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
        TileEntity te = this.creature.world.getTileEntity(this.creature.getWorkstationPos());
        if (te != null) {
            if (Loader.isModLoaded(RiftInitialize.MYSTICAL_MECHANICS_MOD_ID)) return te instanceof TileEntityBlowPoweredTurbine && this.creature.isUsingWorkstation() && this.creature instanceof IWorkstationUser;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isUsingWorkstation() && !this.destroyedFlag;
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
                        this.creature.setEnergy(this.creature.getEnergy() - 3);
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
        if (!this.user.isWorkstation(this.workstationPos)) this.destroyedFlag = true;
    }
}
