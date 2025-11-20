package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
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

public class RiftHoverChangeControl extends RiftLibMessage<RiftHoverChangeControl> {
    private int creatureId;
    private boolean ascend;
    private boolean descend;

    public RiftHoverChangeControl() {}

    public RiftHoverChangeControl(RiftCreature creature, boolean ascend, boolean descend) {
        this.creatureId = creature.getEntityId();
        this.ascend = ascend;
        this.descend = descend;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.ascend = buf.readBoolean();
        this.descend = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.ascend);
        buf.writeBoolean(this.descend);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftHoverChangeControl message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftWaterCreature creature = (RiftWaterCreature) messagePlayer.world.getEntityByID(message.creatureId);
        if (creature != null && creature.isInWater()) {
            if (message.ascend && !message.descend) {
                creature.setIsAscending(true);
                creature.setIsDescending(false);
            }
            else if (!message.ascend && message.descend) {
                creature.setIsAscending(false);
                creature.setIsDescending(true);
            }
            else {
                creature.setIsAscending(false);
                creature.setIsDescending(false);
            }
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftHoverChangeControl riftHoverChangeControl, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
