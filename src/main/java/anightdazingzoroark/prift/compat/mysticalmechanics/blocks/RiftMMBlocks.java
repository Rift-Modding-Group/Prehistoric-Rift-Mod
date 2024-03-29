package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.Block;

public class RiftMMBlocks {
    public static Block LEAD_POWERED_CRANK;
    public static Block BLOW_POWERED_TURBINE;

    public static void registerMMBlocks() {
        LEAD_POWERED_CRANK = RiftBlocks.registerBlock(new BlockLeadPoweredCrank(), "lead_powered_crank", true);
        BLOW_POWERED_TURBINE = RiftBlocks.registerBlock(new BlockBlowPoweredTurbine(), "blow_powered_turbine", false);
    }
}
