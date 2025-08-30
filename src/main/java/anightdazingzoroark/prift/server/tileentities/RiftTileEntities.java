package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.RiftMMTileEntities;
import anightdazingzoroark.prift.config.GeneralConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RiftTileEntities {
    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(RiftTileEntityFeedingTrough.class, new ResourceLocation(RiftInitialize.MODID, "feeding_trough"));
        GameRegistry.registerTileEntity(RiftTileEntityCreatureBox.class, new ResourceLocation(RiftInitialize.MODID, "creature_box"));
        if (GeneralConfig.canUseMM()) RiftMMTileEntities.registerTileEntities();
    }
}
