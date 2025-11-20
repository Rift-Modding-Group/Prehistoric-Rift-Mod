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

public class RiftCreatureBoxSetLastOpenedTime extends RiftLibMessage<RiftCreatureBoxSetLastOpenedTime> {
    private int playerId;
    private int lastOpenedTime;

    public RiftCreatureBoxSetLastOpenedTime() {}

    public RiftCreatureBoxSetLastOpenedTime(EntityPlayer player, int lastOpenedTime) {
        this.playerId = player.getEntityId();
        this.lastOpenedTime = lastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastOpenedTime);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftCreatureBoxSetLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (playerTamedCreatures != null) {
            int timeToSubtract = message.lastOpenedTime - playerTamedCreatures.getBoxLastOpenedTime();

            //deal with countdown for creatures
            playerTamedCreatures.getBoxNBT().countdownCreatureRevival(timeToSubtract);

            //now set the time
            playerTamedCreatures.setBoxLastOpenedTime(message.lastOpenedTime);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftCreatureBoxSetLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
