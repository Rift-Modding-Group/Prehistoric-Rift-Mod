package anightdazingzoroark.prift.client.renderer;


import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.server.items.RiftCreatureSpawnEggItem;
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
        //special case for creature spawn eggs
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                (stack, tintIndex) -> {
                    if (!(stack.getItem() instanceof RiftCreatureSpawnEggItem)) return 0xFFFFFF;

                    AbstractCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(RiftCreatureSpawnEggItem.getCreatureName(stack));
                    if (builder == null) return 0xFFFFFF;

                    int[] colors = builder.getSpawnEggColors();
                    if (colors == null || colors.length < 2) return 0xFFFFFF;

                    return tintIndex == 0 ? colors[0] : colors[1];
                },
                RiftItems.CREATURE_SPAWN_EGG
        );
    }

    private static void register(final Item item) {
        final String resName = item.getRegistryName().toString();
        final ModelResourceLocation res = new ModelResourceLocation(resName, "inventory");
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, res);
    }
}
