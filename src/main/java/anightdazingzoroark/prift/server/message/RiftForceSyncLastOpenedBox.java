package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftForceSyncLastOpenedBox extends RiftLibMessage<RiftForceSyncLastOpenedBox> {
    private int playerId;
    private int lastOpenedBox;

    public RiftForceSyncLastOpenedBox() {}

    public RiftForceSyncLastOpenedBox(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncLastOpenedBox(EntityPlayer player, int lastOpenedBox) {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftForceSyncLastOpenedBox message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (playerTamedCreatures != null) {
            int value = playerTamedCreatures.getLastOpenedBox();
            RiftMessages.WRAPPER.sendTo(new RiftForceSyncLastOpenedBox(player, value), (EntityPlayerMP) player);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftForceSyncLastOpenedBox message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (playerTamedCreatures != null) playerTamedCreatures.setLastOpenedBox(message.lastOpenedBox);
    }
}
