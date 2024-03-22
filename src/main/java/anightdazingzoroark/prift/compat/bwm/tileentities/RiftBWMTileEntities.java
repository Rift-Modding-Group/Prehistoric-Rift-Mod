package anightdazingzoroark.prift.compat.bwm.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RiftBWMTileEntities {
    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityLeadPoweredCrank.class, new ResourceLocation(RiftInitialize.MODID, "lead_powered_crank"));
    }
}
