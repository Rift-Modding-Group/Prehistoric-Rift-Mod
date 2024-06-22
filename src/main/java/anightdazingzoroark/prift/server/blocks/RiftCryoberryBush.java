package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;

public class RiftCryoberryBush extends RiftBerryBush {
    @Override
    public Item berryItem() {
        return RiftItems.CRYOBERRY;
    }
}
