package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftAddToParty extends RiftLibMessage<RiftAddToParty> {
    private int playerId;
    private CreatureNBT creatureNBT;

    public RiftAddToParty() {}

    public RiftAddToParty(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureNBT = new CreatureNBT(creature);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureNBT = new CreatureNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.creatureNBT.getCreatureNBT());
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftAddToParty message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) playerTamedCreatures.addToPartyNBT(message.creatureNBT);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftAddToParty message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
