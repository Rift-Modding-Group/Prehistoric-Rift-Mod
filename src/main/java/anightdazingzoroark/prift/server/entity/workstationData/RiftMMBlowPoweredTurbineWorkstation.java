package anightdazingzoroark.prift.server.entity.workstationData;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RiftMMBlowPoweredTurbineWorkstation extends RiftWorkstationData {
    @Override
    public boolean canUseWorkstation(RiftCreature user, BlockPos workstationPos) {
        if (!GeneralConfig.canUseMM()) return false;

        TileEntityBlowPoweredTurbine tileBlowPoweredTurbine = tileBlowPoweredTurbine(user, workstationPos);
        if (tileBlowPoweredTurbine == null) return false;

        return super.canUseWorkstation(user, workstationPos);
    }

    @Override
    public void onStartWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    @Override
    public void onHitWorkstation(RiftCreature user, BlockPos workstationPos) {
        TileEntityBlowPoweredTurbine tileBlowPoweredTurbine = tileBlowPoweredTurbine(user, workstationPos);
        tileBlowPoweredTurbine.setPower(30f);
    }

    @Override
    public void onEndWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    private TileEntityBlowPoweredTurbine tileBlowPoweredTurbine(RiftCreature creature, BlockPos workstationPos) {
        TileEntity tileBlowPoweredTurbine = creature.world.getTileEntity(workstationPos);
        if (tileBlowPoweredTurbine instanceof TileEntityBlowPoweredTurbine) return (TileEntityBlowPoweredTurbine) tileBlowPoweredTurbine;
        return null;
    }
}
