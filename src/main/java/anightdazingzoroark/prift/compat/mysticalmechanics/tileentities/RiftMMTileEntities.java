package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RiftMMTileEntities {
    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityLeadPoweredCrank.class, new ResourceLocation(RiftInitialize.MODID, "lead_powered_crank"));
        GameRegistry.registerTileEntity(TileEntityBlowPoweredTurbine.class, new ResourceLocation(RiftInitialize.MODID, "blow_powered_turbine"));
        GameRegistry.registerTileEntity(TileEntityHandCrank.class, new ResourceLocation(RiftInitialize.MODID, "hand_crank"));
    }
}
