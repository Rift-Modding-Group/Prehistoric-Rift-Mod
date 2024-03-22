package anightdazingzoroark.prift.compat.bwm.blocks;

import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.Block;

public class RiftBWMBlocks {
    public static Block LEAD_POWERED_CRANK;

    public static void registerBWMBlocks() {
        LEAD_POWERED_CRANK = RiftBlocks.registerBlock(new BlockLeadPoweredCrank(), "lead_powered_crank");
    }
}
