package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;

public class RiftPyroberryBush extends RiftBerryBush {
    @Override
    public Item berryItem() {
        return RiftItems.PYROBERRY;
    }
}
