package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeTurretTargetingFromMenu extends AbstractMessage<RiftChangeTurretTargetingFromMenu> {
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
    public void onClientReceived(Minecraft minecraft, RiftChangeTurretTargetingFromMenu message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftChangeTurretTargetingFromMenu message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);
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
