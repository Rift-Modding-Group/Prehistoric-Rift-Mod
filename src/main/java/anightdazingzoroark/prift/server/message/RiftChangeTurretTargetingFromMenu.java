package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeTurretTargetingFromMenu extends RiftLibMessage<RiftChangeTurretTargetingFromMenu> {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftChangeTurretTargetingFromMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature interacted = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
        if (interacted != null) {
            if (interacted.getTurretTargeting() != message.turretModeTargeting) this.sendTurretTargetingMessage(message.turretModeTargeting, interacted);
            interacted.setTurretModeTargeting(message.turretModeTargeting);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftChangeTurretTargetingFromMenu message, EntityPlayer entityPlayer, MessageContext messageContext) {}

    private void sendTurretTargetingMessage(TurretModeTargeting turretModeTargeting, RiftCreature creature) {
        String turretTargetingName = "turrettargeting."+turretModeTargeting.name().toLowerCase();
        ITextComponent itextcomponent = new TextComponentString(creature.getName());
        if (creature.getOwner() instanceof EntityPlayer) {
            ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(turretTargetingName, itextcomponent), false);
        }
    }
}
