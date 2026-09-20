package anightdazingzoroark.prift.server.block;

import anightdazingzoroark.prift.server.item.RiftItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RiftBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>();
    private static boolean blocksRegistered;
    public static BlockCrops SMOKENUT_BUSH;

    //-----registry stuff-----
    public static void registerBlocks() {
        if (blocksRegistered) throw new IllegalStateException("Blocks have already been registered!");

        SMOKENUT_BUSH = registerBlock(new SmokenutBush(), "smokenut_bush", false, false);

        blocksRegistered = true;
    }

    //---registry stuff---
    private static <T extends Block> T registerBlock(T block, String registryName, boolean canBeItem, boolean canBeInCreative) {
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
