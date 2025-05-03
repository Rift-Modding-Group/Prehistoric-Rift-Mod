package anightdazingzoroark.prift.client.renderer;


import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class ItemRenderer {
    public static void registerItemRenderer() {
        for (Item item : RiftItems.ITEMS) {
            register(item);
        }
        for (ItemBlock itemBlock : RiftBlocks.ITEM_BLOCKS) {
            register(itemBlock);
        }
    }

    private static void register(final Item item) {
        final String resName = item.getRegistryName().toString();
        final ModelResourceLocation res = new ModelResourceLocation(resName, "inventory");
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, res);
    }
}
