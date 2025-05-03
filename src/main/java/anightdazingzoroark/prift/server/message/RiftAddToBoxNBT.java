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

public class RiftAddToBoxNBT implements IMessage {
    private int playerId;
    private NBTTagCompound tagCompound;

    public RiftAddToBoxNBT() {}

    public RiftAddToBoxNBT(EntityPlayer player, NBTTagCompound tagCompound) {
        this.playerId = player.getEntityId();
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    public static class Handler implements IMessageHandler<RiftAddToBoxNBT, IMessage> {
        @Override
        public IMessage onMessage(RiftAddToBoxNBT message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftAddToBoxNBT message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                playerTamedCreatures.addToBoxNBT(message.tagCompound);
            }
            if (ctx.side == Side.CLIENT) {
                System.out.println("test");
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                playerTamedCreatures.addToBoxNBT(message.tagCompound);
            }
        }
    }
}
