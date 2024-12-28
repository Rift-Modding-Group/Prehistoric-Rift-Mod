package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftUpdatePlayerTamedCreatures implements IMessage {
    private NBTTagCompound nbtTagCompound;
    private int playerId;

    public RiftUpdatePlayerTamedCreatures() {}

    public RiftUpdatePlayerTamedCreatures(NBTBase nbtBase, EntityPlayer player) {
        this.nbtTagCompound = (NBTTagCompound) nbtBase;
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
        buf.writeInt(this.playerId);
    }

    public static class Handler implements IMessageHandler<RiftUpdatePlayerTamedCreatures, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdatePlayerTamedCreatures message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdatePlayerTamedCreatures message, MessageContext ctx) {
            EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().world.getEntityByID(message.playerId);
            if (player != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    IPlayerTamedCreatures capability = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                    if (capability != null) PlayerTamedCreaturesProvider.readNBT(capability, null, message.nbtTagCompound);
                });
            }
        }
    }
}
