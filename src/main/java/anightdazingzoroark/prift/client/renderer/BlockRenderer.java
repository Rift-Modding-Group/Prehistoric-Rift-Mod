package anightdazingzoroark.prift.client.renderer;

import anightdazingzoroark.prift.client.renderer.block.BlowPoweredTurbineRenderer;
import anightdazingzoroark.prift.client.renderer.block.FeedingTroughRenderer;
import anightdazingzoroark.prift.client.renderer.block.LeadPoweredCrankRenderer;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class BlockRenderer {
    public static void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(RiftTileEntityFeedingTrough.class, new FeedingTroughRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLeadPoweredCrank.class, new LeadPoweredCrankRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlowPoweredTurbine.class, new BlowPoweredTurbineRenderer());
    }
}
