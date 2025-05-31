package anightdazingzoroark.prift.server.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class RiftRiftShardBlock extends Block {
    public RiftRiftShardBlock() {
        super(Material.ROCK, MapColor.MAGENTA);
        this.setHardness(5F);
        this.setResistance(6F);
        this.setHarvestLevel("pickaxe", 1);
    }
}
