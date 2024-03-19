package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageUtilizingControl extends AbstractMessage<RiftManageUtilizingControl> {
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
    public void onClientReceived(Minecraft client, RiftManageUtilizingControl message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageUtilizingControl message, EntityPlayer player, MessageContext messageContext) {
        EntityLivingBase entity = (EntityLivingBase) player.world.getEntityByID(message.entityId);
        if (entity instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) entity;
            switch (message.control) {
                case 0:
                    creature.setUsingLeftClick(message.isUsing);
                    break;
                case 1:
                    creature.setUsingRightClick(message.isUsing);
                    break;
                case 2:
                    creature.setUsingSpacebar(message.isUsing);
                    break;
            }
        }
        else if (entity instanceof RiftLargeWeapon) {
            RiftLargeWeapon weapon = (RiftLargeWeapon) entity;
            weapon.setUsingLeftClick(message.isUsing);
        }
    }
}
