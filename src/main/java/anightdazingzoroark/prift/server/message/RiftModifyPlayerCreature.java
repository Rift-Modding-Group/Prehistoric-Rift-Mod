package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftModifyPlayerCreature implements IMessage {
    private int playerID;
    private UUID creatureUUID;
    private NBTTagCompound tagCompound;

    public RiftModifyPlayerCreature() {}

    public RiftModifyPlayerCreature(EntityPlayer player, UUID creatureUUID, NBTTagCompound tagCompound) {
        this.playerID = player.getEntityId();
        this.creatureUUID = creatureUUID;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerID = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);

        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerID);

        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());

        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    public static class Handler implements IMessageHandler<RiftModifyPlayerCreature, IMessage> {
        @Override
        public IMessage onMessage(RiftModifyPlayerCreature message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftModifyPlayerCreature message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerID);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                playerTamedCreatures.modifyCreature(message.creatureUUID, message.tagCompound);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerID);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                playerTamedCreatures.modifyCreature(message.creatureUUID, message.tagCompound);
            }
        }
    }
}
