package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.compat.bwm.tileentities.RiftBWMTileEntities;
import net.minecraftforge.fml.common.Loader;

public class RiftTileEntities {
    public static void registerTileEntities() {
        if (Loader.isModLoaded("betterwithmods")) RiftBWMTileEntities.registerTileEntities();
    }
}
