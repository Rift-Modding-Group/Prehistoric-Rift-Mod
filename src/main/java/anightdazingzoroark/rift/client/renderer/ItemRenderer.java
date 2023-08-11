package anightdazingzoroark.rift.client.renderer;


import anightdazingzoroark.rift.server.items.RiftItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

public class ItemRenderer {
    public static void registerItemRenderer() {
        for (Item item : RiftItems.ITEMS) {
            register(item);
        }
    }

    private static void register(final Item item) {
        final String resName = item.getRegistryName().toString();
        final ModelResourceLocation res = new ModelResourceLocation(resName, "inventory");
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, res);
    }
}
