package anightdazingzoroark.prift.server.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import java.util.Random;

public class RiftRiftedStone extends Block {
    public RiftRiftedStone() {
        super(Material.ROCK, MapColor.PURPLE);
        this.setHardness(7.5F);
        this.setResistance(10.0F);
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(RiftBlocks.RIFTED_COBBLESTONE);
    }
}
