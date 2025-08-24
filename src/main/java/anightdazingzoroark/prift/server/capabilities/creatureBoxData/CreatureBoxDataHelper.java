package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.server.capabilities.CapabilityHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CreatureBoxDataHelper {
    public static ICreatureBoxData getCreatureBoxData(World world) {
        if (world == null) return null;
        return world.getCapability(CreatureBoxDataProvider.CREATURE_BOX_DATA_CAPABILITY, null);
    }
}
