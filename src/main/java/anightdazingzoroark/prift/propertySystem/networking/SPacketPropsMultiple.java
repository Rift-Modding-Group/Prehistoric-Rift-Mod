package anightdazingzoroark.prift.propertySystem.networking;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class SPacketPropsMultiple implements IMessage {
    private int entityId;
    private String setKey;
    private List<String> propertyKeys;
    private NBTTagCompound propertyNbt;

    public SPacketPropsMultiple() {}

    public SPacketPropsMultiple(int entityId, String setKey, List<String> propertyKeys, NBTTagCompound propertyNbt) {
        this.entityId = entityId;
        this.setKey = setKey;
        this.propertyKeys = propertyKeys;
        this.propertyNbt = propertyNbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.setKey = ByteBufUtils.readUTF8String(buf);

        NBTTagCompound propertyKeysNBT = ByteBufUtils.readTag(buf);
        if (propertyKeysNBT == null) return;
        this.propertyKeys = this.getPropertyKeysFromNBT(propertyKeysNBT);

        this.propertyNbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.setKey);
        ByteBufUtils.writeTag(buf, this.getPropertyKeysAsNBT(this.propertyKeys));
        ByteBufUtils.writeTag(buf, this.propertyNbt);
    }

    private NBTTagCompound getPropertyKeysAsNBT(List<String> propertyKeys) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        NBTTagList nbtTagList = new NBTTagList();
        for (String propertyKey : propertyKeys) {
            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setString("PropertyKey", propertyKey);
            nbtTagList.appendTag(toAppend);
        }
        nbtTagCompound.setTag("PropertyKeyList", nbtTagList);
        return nbtTagCompound;
    }

    private List<String> getPropertyKeysFromNBT(NBTTagCompound nbtTagCompound) {
        List<String> toReturn = new ArrayList<>();
        NBTTagList nbtTagList = nbtTagCompound.getTagList("PropertyKeyList", 10);
        for (int index = 0; index < nbtTagList.tagCount(); index++) {
            NBTTagCompound nbtFromList = nbtTagList.getCompoundTagAt(index);
            if (nbtFromList.isEmpty()) continue;

            toReturn.add(nbtFromList.getString("PropertyKey"));
        }
        return toReturn;
    }


    public static class Handler implements IMessageHandler<SPacketPropsMultiple, IMessage> {
        @Override
        public IMessage onMessage(SPacketPropsMultiple message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return;

                Entity entity = world.getEntityByID(message.entityId);
                if (entity == null) return;

                AbstractEntityProperties<?> properties = Property.getProperty(message.setKey, entity);
                if (properties == null) return;

                properties.readMultipleFromNBT(message.propertyNbt, message.propertyKeys);
            });
            return null;
        }
    }
}
