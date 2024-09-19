package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.fluids.RiftFluids;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RiftBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final HashMap<String, Block> blocks = new HashMap<String, Block>();
    public static final List<ItemBlock> ITEM_BLOCKS = new ArrayList<>();

    public static Block FEEDING_TROUGH;
    public static Block PYROBERRY_BUSH;
    public static Block CRYOBERRY_BUSH;

    public static Block PYROBERRY_JUICE_FLUID;
    public static Block CRYOBERRY_JUICE_FLUID;

    public static Block CREATURE_BOX;

    public static void registerBlocks() {
        FEEDING_TROUGH = registerBlock(new RiftFeedingTroughBlock(), "feeding_trough", true, false, true);
        PYROBERRY_BUSH = registerBlock(new RiftPyroberryBush(), "pyroberry_bush", false);
        CRYOBERRY_BUSH = registerBlock(new RiftCryoberryBush(), "cryoberry_bush", false);

        //fluid shenanigans
        PYROBERRY_JUICE_FLUID = registerBlock(new RiftFluidBlock(RiftFluids.PYROBERRY_JUICE, Material.WATER), "pyroberry_juice", true, true, false);
        CRYOBERRY_JUICE_FLUID = registerBlock(new RiftFluidBlock(RiftFluids.CRYOBERRY_JUICE, Material.WATER), "cryoberry_juice", true, true, false);

        CREATURE_BOX = registerBlock(new RiftCreatureBox(), "creature_block", true);
        //to add: deployment boxes for ranchery and breeding
        //ranchery is for creatures that may be ranched for their meat or other drops
        //breeding is for creature breeding
        //should probably add gender to creatures too

        if (GeneralConfig.canUseMM()) RiftMMBlocks.registerMMBlocks();
    }

    public static void registerOreDicTags() {
        OreDictionary.registerOre("grass", RiftBlocks.PYROBERRY_BUSH);
        OreDictionary.registerOre("grass", RiftBlocks.CRYOBERRY_BUSH);
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
