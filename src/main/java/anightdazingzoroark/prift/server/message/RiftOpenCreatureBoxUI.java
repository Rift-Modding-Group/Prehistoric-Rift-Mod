package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.BlockPosUtil;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import com.cleanroommc.modularui.factory.GuiFactories;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenCreatureBoxUI extends RiftLibMessage<RiftOpenCreatureBoxUI> {
    private int playerId;
    private BlockPos creatureBoxPos;

    public RiftOpenCreatureBoxUI() {}

    public RiftOpenCreatureBoxUI(EntityPlayer player, BlockPos creatureBoxPos) {
        this.playerId = player.getEntityId();
        this.creatureBoxPos = creatureBoxPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureBoxPos = BlockPosUtil.getBlockPosFromNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, BlockPosUtil.getBlockPosAsNBT(this.creatureBoxPos));
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftOpenCreatureBoxUI message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;
        GuiFactories.tileEntity().open(player, message.creatureBoxPos);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenCreatureBoxUI message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
