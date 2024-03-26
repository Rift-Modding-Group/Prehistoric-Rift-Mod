package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.RiftMMTileEntities;
import anightdazingzoroark.prift.config.GeneralConfig;
import net.minecraftforge.fml.common.Loader;

public class RiftTileEntities {
    public static void registerTileEntities() {
        if (Loader.isModLoaded(RiftInitialize.MYSTICAL_MECHANICS_MOD_ID) && GeneralConfig.mmIntegration) RiftMMTileEntities.registerTileEntities();
    }
}
