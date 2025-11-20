package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataProvider;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxInfo;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.ICreatureBoxData;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftAddCreatureBoxData extends RiftLibMessage<RiftAddCreatureBoxData> {
    private int posX;
    private int posY;
    private int posZ;
    private int playerId;

    public RiftAddCreatureBoxData() {}

    public RiftAddCreatureBoxData(BlockPos pos, EntityPlayer player) {
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeInt(this.playerId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftAddCreatureBoxData message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        ICreatureBoxData creatureBoxData = messagePlayer.world.getCapability(CreatureBoxDataProvider.CREATURE_BOX_DATA_CAPABILITY, null);
        BlockPos blockPos = new BlockPos(message.posX, message.posY, message.posZ);

        if (player != null && creatureBoxData != null) {
            CreatureBoxInfo info = new CreatureBoxInfo(blockPos, player);
            creatureBoxData.getCreatureBoxInformation().add(info);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftAddCreatureBoxData message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
