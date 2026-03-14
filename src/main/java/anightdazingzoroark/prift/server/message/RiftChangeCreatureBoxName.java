package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeCreatureBoxName extends RiftLibMessage<RiftChangeCreatureBoxName> {
    private int playerId;
    private int boxPos;
    private String newName;

    public RiftChangeCreatureBoxName() {}

    public RiftChangeCreatureBoxName(EntityPlayer player, int boxPos, String newName) {
        this.playerId = player.getEntityId();
        this.boxPos = boxPos;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.boxPos = buf.readInt();
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.boxPos);
        ByteBufUtils.writeUTF8String(buf, this.newName);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftChangeCreatureBoxName message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;

        PlayerCreatureBoxProperties creatureBoxProperties = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);
        if (creatureBoxProperties == null) return;

        CreatureBoxStorage creatureBoxStorage = creatureBoxProperties.getCreatureBoxStorage();
        creatureBoxStorage.setBoxName(message.boxPos, message.newName);
        creatureBoxProperties.setCreatureBoxStorage(creatureBoxStorage);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftChangeCreatureBoxName message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
