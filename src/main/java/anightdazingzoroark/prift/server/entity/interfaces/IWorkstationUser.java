package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.util.math.BlockPos;

public interface IWorkstationUser {
    boolean canUseWorkstation();
    boolean isWorkstation(BlockPos pos);
    BlockPos workstationUseFromPos();
}
