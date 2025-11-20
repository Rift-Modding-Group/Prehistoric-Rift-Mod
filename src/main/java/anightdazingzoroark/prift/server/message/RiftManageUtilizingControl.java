package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
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

public class RiftManageUtilizingControl extends RiftLibMessage<RiftManageUtilizingControl> {
    private int entityId;
    private int control; //0 is left click, 1 is right click, 2 is spacebar, 3 is middle click
    private boolean isUsing;

    public RiftManageUtilizingControl() {}

    public RiftManageUtilizingControl(EntityLivingBase entity, boolean isUsing) {
        this(entity, -1, isUsing);
    }

    public RiftManageUtilizingControl(EntityLivingBase entity, int control, boolean isUsing) {
        this.entityId = entity.getEntityId();
        this.control = control;
        this.isUsing = isUsing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.control = buf.readInt();
        this.isUsing = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.isUsing);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftManageUtilizingControl message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityLivingBase entity = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);
        if (entity instanceof RiftLargeWeapon) {
            RiftLargeWeapon weapon = (RiftLargeWeapon) entity;
            weapon.setUsingLeftClick(message.isUsing);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftManageUtilizingControl riftManageUtilizingControl, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
