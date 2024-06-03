package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import anightdazingzoroark.prift.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RiftBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final HashMap<String, Block> blocks = new HashMap<String, Block>();
    public static final List<ItemBlock> ITEM_BLOCKS = new ArrayList<>();

    public static Block FEEDING_TROUGH;

    public static void registerBlocks() {
        FEEDING_TROUGH = registerBlock(new RiftFeedingTroughBlock(), "feeding_trough", true, false, true);

        if (GeneralConfig.canUseMM()) RiftMMBlocks.registerMMBlocks();
    }

    public static Block registerBlock(Block block, String registryName, boolean includeItem) {
        return registerBlock(block, registryName, includeItem, true, true);
    }

    public static Block registerBlock(Block block, String registryName, boolean includeItem, boolean itemStackable) {
        return registerBlock(block, registryName, includeItem, itemStackable, true);
    }

    public static Block registerBlock(Block block, String registryName, boolean includeItem, boolean itemStackable, boolean showInCreative) {
        if (showInCreative) block.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
        block.setRegistryName(registryName);
        block.setTranslationKey(registryName);
        BLOCKS.add(block);
        if (includeItem) {
            ItemBlock itemBlock = new ItemBlock(block);
            itemBlock.setRegistryName(registryName);
            itemBlock.setTranslationKey(registryName);
            if (!itemStackable) itemBlock.setMaxStackSize(1);
            ITEM_BLOCKS.add(itemBlock);
        }
        return block;
    }

    @SubscribeEvent
    public void onBlockRegistry(RegistryEvent.Register<Block> e) {
        IForgeRegistry<Block> reg = e.getRegistry();
        reg.registerAll(BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent
    public void onItemRegistry(RegistryEvent.Register<Item> e) {
        IForgeRegistry<Item> reg = e.getRegistry();
        reg.registerAll(ITEM_BLOCKS.toArray(new Item[0]));
    }
}
