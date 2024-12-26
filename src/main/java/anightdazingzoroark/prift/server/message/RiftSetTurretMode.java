package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetTurretMode implements IMessage {
    private int creatureId;
    private boolean value;

    public RiftSetTurretMode() {}

    public RiftSetTurretMode(RiftCreature creature, boolean value) {
        this.creatureId = creature.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.value);
    }

    public static class Handler implements IMessageHandler<RiftSetTurretMode, IMessage> {
        @Override
        public IMessage onMessage(RiftSetTurretMode message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetTurretMode message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            RiftCreature creature = (RiftCreature)playerEntity.world.getEntityByID(message.creatureId);
            ITurretModeUser turretModeUser = (ITurretModeUser) creature;

            //send message
            String messageToSend = message.value ? "action.creature_start_turret_mode" : "action.creature_stop_turret_mode";
            ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(messageToSend), false);

            //set turret mode
            turretModeUser.setTurretMode(message.value);

            //remove or reset speed
            if (message.value) creature.removeSpeed();
            else creature.resetSpeed();
        }
    }
}
