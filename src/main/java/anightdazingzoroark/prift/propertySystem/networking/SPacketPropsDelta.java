package anightdazingzoroark.prift.propertySystem.networking;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketPropsDelta implements IMessage {
    private int entityId;
    private String setKey;
    private String propertyKey;
    private NBTTagCompound propertyNbt;

    public SPacketPropsDelta() {}

    public SPacketPropsDelta(int entityId, String setKey, String propertyKey, NBTTagCompound propertyNbt) {
        this.entityId = entityId;
        this.setKey = setKey;
        this.propertyKey = propertyKey;
        this.propertyNbt = propertyNbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.setKey = ByteBufUtils.readUTF8String(buf);
        this.propertyKey = ByteBufUtils.readUTF8String(buf);
        this.propertyNbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.setKey);
        ByteBufUtils.writeUTF8String(buf, this.propertyKey);
        ByteBufUtils.writeTag(buf, this.propertyNbt);
    }

    public static class Handler implements IMessageHandler<SPacketPropsDelta, IMessage> {
        @Override
        public IMessage onMessage(SPacketPropsDelta message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return;

                Entity entity = world.getEntityByID(message.entityId);
                if (entity == null) return;

                AbstractEntityProperties<?> properties = Property.getProperty(message.setKey, entity);
                if (properties == null) return;

                properties.readOneFromNBT(message.propertyNbt, message.propertyKey);
            });
            return null;
        }
    }
}
