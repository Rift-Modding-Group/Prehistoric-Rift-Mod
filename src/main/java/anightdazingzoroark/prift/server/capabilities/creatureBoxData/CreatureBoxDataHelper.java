package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.server.message.RiftAddCreatureBoxData;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftRemoveCreatureBoxData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreatureBoxDataHelper {
    public static ICreatureBoxData getCreatureBoxData(World world) {
        if (world == null) return null;
        return world.getCapability(CreatureBoxDataProvider.CREATURE_BOX_DATA_CAPABILITY, null);
    }

    public static void addCreatureBoxPos(BlockPos blockPos, EntityPlayer player) {
        if (blockPos == null || player == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftAddCreatureBoxData(blockPos, player));
    }

    public static void removeCreatureBoxPos(BlockPos blockPos) {
        if (blockPos == null) return;
        RiftMessages.WRAPPER.sendToServer(new RiftRemoveCreatureBoxData(blockPos));
    }
}
