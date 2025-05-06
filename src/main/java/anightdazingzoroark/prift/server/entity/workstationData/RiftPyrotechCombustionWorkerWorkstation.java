package anightdazingzoroark.prift.server.entity.workstationData;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi.TileCombustionWorkerStoneBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RiftPyrotechCombustionWorkerWorkstation extends RiftWorkstationData {
    @Override
    public boolean canUseWorkstation(RiftCreature user, BlockPos workstationPos) {
        if (!GeneralConfig.canUsePyrotech()) return false;

        TileCombustionWorkerStoneBase tileCombustionWorker = this.tileCombustionWorker(user, workstationPos);
        if (tileCombustionWorker == null) return false;

        return super.canUseWorkstation(user, workstationPos) && tileCombustionWorker.hasFuel() && tileCombustionWorker.workerIsActive() && tileCombustionWorker.hasInput();
    }

    @Override
    public void onStartWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    @Override
    public void onHitWorkstation(RiftCreature user, BlockPos workstationPos) {
        TileCombustionWorkerStoneBase tileCombustionWorker = this.tileCombustionWorker(user, workstationPos);
        tileCombustionWorker.consumeAirflow(8f, false);
    }

    @Override
    public void onEndWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    private TileCombustionWorkerStoneBase tileCombustionWorker(RiftCreature creature, BlockPos workstationPos) {
        TileEntity tileEntity = creature.world.getTileEntity(workstationPos);
        if (tileEntity instanceof TileCombustionWorkerStoneBase) return (TileCombustionWorkerStoneBase) tileEntity;
        return null;
    }
}
