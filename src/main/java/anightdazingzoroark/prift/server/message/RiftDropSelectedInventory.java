package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftDropSelectedInventory implements IMessage {
    private int playerId;
    private NBTTagCompound selectedCreatureInfoNBT;

    public RiftDropSelectedInventory() {}

    public RiftDropSelectedInventory(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedCreatureInfoNBT);
    }

    public static class Handler implements IMessageHandler<RiftDropSelectedInventory, IMessage> {
        @Override
        public IMessage onMessage(RiftDropSelectedInventory message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftDropSelectedInventory message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
                CreatureNBT creatureNBT = selectedCreatureInfo.getCreatureNBT(player);
                creatureNBT.dropInventory(messagePlayer.world);
            }
        }
    }
}