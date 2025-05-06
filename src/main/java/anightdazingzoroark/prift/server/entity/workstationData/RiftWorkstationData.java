package anightdazingzoroark.prift.server.entity.workstationData;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import net.minecraft.util.math.BlockPos;

public abstract class RiftWorkstationData {
    public boolean canUseWorkstation(RiftCreature user, BlockPos workstationPos) {
        if (!(user instanceof IWorkstationUser)) return false;
        return ((IWorkstationUser)user).isWorkstation(user.world, workstationPos);
    }

    public abstract void onStartWorkstationUse(RiftCreature user, BlockPos workstationPos);

    public abstract void onHitWorkstation(RiftCreature user, BlockPos workstationPos);

    public abstract void onEndWorkstationUse(RiftCreature user, BlockPos workstationPos);
}
