package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeTurretTargetingFromMenu implements IMessage {
    private int creatureId;
    private TurretModeTargeting turretModeTargeting;

    public RiftChangeTurretTargetingFromMenu() {}

    public RiftChangeTurretTargetingFromMenu(RiftCreature creature, TurretModeTargeting turretModeTargeting) {
        this.creatureId = creature.getEntityId();
        this.turretModeTargeting = turretModeTargeting;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.turretModeTargeting = TurretModeTargeting.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.turretModeTargeting.ordinal());
    }

    public static class Handler implements IMessageHandler<RiftChangeTurretTargetingFromMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeTurretTargetingFromMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeTurretTargetingFromMenu message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            RiftCreature interacted = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);
            ITurretModeUser turretModeUser = (ITurretModeUser) interacted;
            if (!turretModeUser.getTurretTargeting().equals(message.turretModeTargeting)) this.sendTurretTargetingMessage(message.turretModeTargeting, interacted);
            turretModeUser.setTurretModeTargeting(message.turretModeTargeting);
        }

        private void sendTurretTargetingMessage(TurretModeTargeting turretModeTargeting, RiftCreature creature) {
            String turretTargetingName = "turrettargeting."+turretModeTargeting.name().toLowerCase();
            ITextComponent itextcomponent = new TextComponentString(creature.getName());
            if (creature.getOwner() instanceof EntityPlayer) {
                ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(turretTargetingName, itextcomponent), false);
            }
        }
    }
}
