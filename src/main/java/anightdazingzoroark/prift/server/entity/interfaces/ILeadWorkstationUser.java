package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.util.math.BlockPos;

public interface ILeadWorkstationUser {
    boolean canBeAttachedForWork();
    boolean isAttachableForWork(BlockPos pos);
    int pullPower();
}
