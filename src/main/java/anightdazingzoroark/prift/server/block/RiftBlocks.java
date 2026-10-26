package anightdazingzoroark.prift.server.block;

import anightdazingzoroark.prift.client.RiftCreativeTabs;
import anightdazingzoroark.prift.server.item.RiftItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RiftBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>();
    private static boolean blocksRegistered;

    public static BlockCrops SMOKENUT_BUSH;
    public static Block RIFTED_STONE;
    public static Block RIFTED_COBBLESTONE;
    public static Block RIFT_SHARD_ORE;
    public static Block RIFT_SHARD_BLOCK;
    public static Block CREATURE_BOX;

    //-----registry stuff-----
    public static void registerBlocks() {
        if (blocksRegistered) throw new IllegalStateException("Blocks have already been registered!");

        SMOKENUT_BUSH = registerBlock(new SmokenutBush(), "smokenut_bush", false, false);

        RIFTED_STONE = registerBlock(
                new Block(Material.ROCK, MapColor.PURPLE) {
                    @Override
                    @NotNull
                    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random random, int fortune) {
                        return Item.getItemFromBlock(RIFTED_COBBLESTONE);
                    }
                }.setHardness(7.5F).setResistance(10.0F),
                "rifted_stone", true, true
        );
        RIFTED_STONE.setHarvestLevel("pickaxe", 1);

        RIFTED_COBBLESTONE = registerBlock(
                new Block(Material.ROCK, MapColor.PURPLE).setHardness(7.5F).setResistance(10.0F),
                "rifted_cobblestone", true, true
        );
        RIFTED_COBBLESTONE.setHarvestLevel("pickaxe", 1);

        RIFT_SHARD_ORE = registerBlock(
                new BlockOre(MapColor.PURPLE) {
                    @Override
                    @NotNull
                    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random random, int fortune) {
                        return RiftItems.RIFT_SHARD;
                    }

                    @Override
                    public int quantityDropped(@NotNull Random random) {
                        return random.nextInt(2, 6);
                    }

                    @Override
                    public int getExpDrop(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, int fortune) {
                        Random random = world instanceof World blockWorld ? blockWorld.rand : new Random();
                        return random.nextInt(3);
                    }
                }.setHardness(7.5f).setResistance(10f),
                "rift_shard_ore", true, true
        );
        RIFT_SHARD_ORE.setHarvestLevel("pickaxe", 1);

        RIFT_SHARD_BLOCK = registerBlock(
                new Block(Material.GLASS, MapColor.MAGENTA).setHardness(5f).setSoundType(SoundType.GLASS),
                "rift_shard_block", true, true
        );

        CREATURE_BOX = registerBlock(new CreatureBoxBlock(), "creature_box", true, true);
        CREATURE_BOX.setHarvestLevel("pickaxe", 1);

        blocksRegistered = true;
    }

    //---registry stuff---
    private static <T extends Block> T registerBlock(T block, String registryName, boolean canBeItem, boolean canBeInCreative) {
        if (canBeInCreative) block.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
        block.setRegistryName(registryName);
        block.setTranslationKey(registryName);
        BLOCKS.add(block);
        if (canBeItem) RiftItems.registerBlockItem(block, registryName, canBeInCreative);
        return block;
    }

    @SubscribeEvent
    public void onBlockRegistry(RegistryEvent.Register<Block> e) {
        IForgeRegistry<Block> reg = e.getRegistry();
        reg.registerAll(BLOCKS.toArray(new Block[0]));
    }
}
