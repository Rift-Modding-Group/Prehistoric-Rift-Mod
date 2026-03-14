package anightdazingzoroark.prift.server.properties.playerCreatureBox;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.server.message.RiftChangeCreatureBoxName;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.properties.RiftPropertyRegistry;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerCreatureBoxHelper {
    public static PlayerCreatureBoxProperties getPlayerCreatureBox(EntityPlayer player) {
        if (player == null) return null;
        return Property.getProperty(RiftPropertyRegistry.PLAYER_CREATURE_BOX, player);
    }

    public static void changeBoxNameClient(EntityPlayer player, int index, String newBoxName) {
        if (!player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureBoxName(player, index, newBoxName));
    }
}
