package anightdazingzoroark.prift.server.entity.workstationData;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualBase;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class RiftMMSemiManualMachineWorkstation extends RiftWorkstationData {
    @Override
    public boolean canUseWorkstation(RiftCreature user, BlockPos workstationPos) {
        if (!GeneralConfig.canUseMM()) return false;

        TileEntitySemiManualBase tileSemiManualBase = tileSemiManualBase(user, workstationPos);
        if (tileSemiManualBase == null) return false;

        return super.canUseWorkstation(user, workstationPos) && tileSemiManualBase.getTopTEntity().getMustBeReset() && !tileSemiManualBase.canDoResetAnim();
    }

    @Override
    public void onStartWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    @Override
    public void onHitWorkstation(RiftCreature user, BlockPos workstationPos) {
        TileEntitySemiManualBase tileSemiManualBase = tileSemiManualBase(user, workstationPos);
        tileSemiManualBase.setPlayResetAnim(true);
        tileSemiManualBase.getTopTEntity().setMustBeReset(false);
        user.world.playSound(null, workstationPos.getX(), workstationPos.getY(), workstationPos.getZ(), RiftSounds.SEMI_MANUAL_MACHINE_RESET, SoundCategory.BLOCKS, 0.75f, user.world.rand.nextFloat() * 0.15F + 0.6F);
    }

    @Override
    public void onEndWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    private TileEntitySemiManualBase tileSemiManualBase(RiftCreature creature, BlockPos workstationPos) {
        TileEntity tileSemiManualBase = creature.world.getTileEntity(workstationPos);
        if (tileSemiManualBase instanceof TileEntitySemiManualBase) return (TileEntitySemiManualBase) tileSemiManualBase;
        return null;
    }
}
