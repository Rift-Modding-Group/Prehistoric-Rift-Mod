package anightdazingzoroark.prift.propertySystem.networking;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PropertiesNetworking {
    public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(RiftInitialize.MODID+"_channel");
    private static int id = 0;

    public static void register() {
        channel.registerMessage(SPacketPropsFull.Handler.class,  SPacketPropsFull.class,  id++, Side.CLIENT);
        channel.registerMessage(SPacketPropsDelta.Handler.class, SPacketPropsDelta.class, id++, Side.CLIENT);
    }

    //send full set state (server to clients tracking entity)
    public static void sendFull(Entity entity, String setKey, NBTTagCompound setNbt) {
        if (!(entity.world instanceof WorldServer)) return;
        SPacketPropsFull packet = new SPacketPropsFull(entity.getEntityId(), setKey, setNbt);
        sendToTrackers(entity, packet);
    }

    //send one-property delta (server to clients tracking entity)
    public static void sendDelta(Entity entity, String setKey, String propKey, NBTTagCompound propNbt) {
        if (!(entity.world instanceof WorldServer)) return;
        SPacketPropsDelta packet = new SPacketPropsDelta(entity.getEntityId(), setKey, propKey, propNbt);
        sendToTrackers(entity, packet);
    }

    private static void sendToTrackers(Entity entity, IMessage packet) {
        channel.sendToAllTracking(packet, entity);

        // Ensure player gets their own updates too
        if (entity instanceof EntityPlayerMP playerMP) channel.sendTo(packet, playerMP);
    }
}
