package anightdazingzoroark.prift.propertySystem.networking;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SPacketPropsFull implements IMessage {
    private int entityId;
    private String key;
    private NBTTagCompound nbtTagCompound;

    public SPacketPropsFull() {}

    public SPacketPropsFull(int entityId, String key, NBTTagCompound nbtTagCompound) {
        this.entityId = entityId;
        this.key = key;
        this.nbtTagCompound = nbtTagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.key = ByteBufUtils.readUTF8String(buf);
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.key);
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
    }

    public static class Handler implements IMessageHandler<SPacketPropsFull, IMessage> {
        @Override
        public IMessage onMessage(SPacketPropsFull message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return;

                Entity entity = world.getEntityByID(message.entityId);
                if (entity == null) return;

                AbstractEntityProperties properties = Property.getProperty(message.key, entity);
                if (properties == null) return;

                properties.readAllFromNBT(message.nbtTagCompound);
            });
            return null;
        }
    }
}
