package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.RiftMMTileEntities;
import net.minecraftforge.fml.common.Loader;

public class RiftTileEntities {
    public static void registerTileEntities() {
        if (Loader.isModLoaded("mysticalmechanics")) RiftMMTileEntities.registerTileEntities();
    }
}
