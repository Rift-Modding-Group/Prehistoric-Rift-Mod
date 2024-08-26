package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.IEntityMultiPart;

public interface IRiftMultipart extends IEntityMultiPart {
    RiftCreature getPartParent();
}
