package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftIncrementControlUse extends RiftLibMessage<RiftIncrementControlUse> {
    private int entityId;
    private int control;

    public RiftIncrementControlUse() {}

    public RiftIncrementControlUse(EntityLivingBase entity) {
        this(entity, -1);
    }

    public RiftIncrementControlUse(EntityLivingBase entity, int control) {
        this.entityId = entity.getEntityId();
        this.control = control;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.control = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.control);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftIncrementControlUse message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityLivingBase entity = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);

        if (!messagePlayer.world.isRemote) {
            if (entity instanceof RiftCatapult) {
                RiftCatapult catapult = (RiftCatapult) entity;
                catapult.setLeftClickUse(catapult.getLeftClickUse() + 1);
            }
            else if (entity instanceof RiftMortar) {
                RiftMortar mortar = (RiftMortar) entity;
                mortar.setLeftClickUse(mortar.getLeftClickUse() + 1);
            }
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftIncrementControlUse riftIncrementControlUse, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
