package anightdazingzoroark.prift.server.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class RiftRiftedCobblestone extends Block {
    public RiftRiftedCobblestone() {
        super(Material.ROCK, MapColor.PURPLE);
        this.setHardness(7.5F);
        this.setResistance(10.0F);
        this.setHarvestLevel("pickaxe", 1);
    }
}
