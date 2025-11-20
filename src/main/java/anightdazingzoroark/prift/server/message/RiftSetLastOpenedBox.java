package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetLastOpenedBox extends RiftLibMessage<RiftSetLastOpenedBox> {
    private int playerId;
    private int lastOpenedBox;

    public RiftSetLastOpenedBox() {}

    public RiftSetLastOpenedBox(EntityPlayer player, int lastOpenedBox) {
        this.playerId = player.getEntityId();
        this.lastOpenedBox = lastOpenedBox;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastOpenedBox = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastOpenedBox);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetLastOpenedBox message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (playerTamedCreatures != null) playerTamedCreatures.setLastOpenedBox(message.lastOpenedBox);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetLastOpenedBox message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
