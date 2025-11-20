package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftGrabbedEntitySetPos extends RiftLibMessage<RiftGrabbedEntitySetPos> {
    private int grabberCreatureId;
    private int grabbedEntityId;

    public RiftGrabbedEntitySetPos() {}

    public RiftGrabbedEntitySetPos(RiftCreature creature, Entity grabbedEntity) {
        this.grabberCreatureId = creature.getEntityId();
        this.grabbedEntityId = grabbedEntity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.grabberCreatureId = buf.readInt();
        this.grabbedEntityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.grabberCreatureId);
        buf.writeInt(this.grabbedEntityId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftGrabbedEntitySetPos message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature grabberCreature = (RiftCreature) messagePlayer.world.getEntityByID(message.grabberCreatureId);
        Entity grabbedEntity = messagePlayer.world.getEntityByID(message.grabbedEntityId);

        if (grabbedEntity != null && grabberCreature != null) {
            grabbedEntity.setPosition(grabberCreature.grabLocation().x, grabberCreature.grabLocation().y, grabberCreature.grabLocation().z);
            grabbedEntity.motionX = 0;
            grabbedEntity.motionY = 0;
            grabbedEntity.motionZ = 0;
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftGrabbedEntitySetPos riftGrabbedEntitySetPos, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
