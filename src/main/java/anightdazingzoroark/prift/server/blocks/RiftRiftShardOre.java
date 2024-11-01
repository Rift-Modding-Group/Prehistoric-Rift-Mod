package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class RiftRiftShardOre extends Block {
    public RiftRiftShardOre() {
        super(Material.ROCK, MapColor.PURPLE);
        this.setHardness(7.5F);
        this.setResistance(10.0F);
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return RiftItems.RIFT_SHARD;
    }

    public int quantityDropped(Random random) {
        return RiftUtil.randomInRange(2, 5);
    }

    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped((IBlockState)this.getBlockState().getValidStates().iterator().next(), random, fortune)) {
            int i = random.nextInt(fortune + 2) - 1;

            if (i < 0) i = 0;

            return this.quantityDropped(random) * (i + 1);
        }
        else return this.quantityDropped(random);
    }

    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : new Random();
        if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
            return RiftUtil.randomInRange(0, 2);
        }
        return 0;
    }
}
