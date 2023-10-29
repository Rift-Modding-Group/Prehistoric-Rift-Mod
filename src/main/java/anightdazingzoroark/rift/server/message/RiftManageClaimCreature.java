package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageClaimCreature extends AbstractMessage<RiftManageClaimCreature> {
    private int creatureId;
    private boolean claim;

    public RiftManageClaimCreature() {}

    public RiftManageClaimCreature(RiftCreature creature, boolean claim) {
        this.creatureId = creature.getEntityId();
        this.claim = claim;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.claim = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.claim);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftManageClaimCreature message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageClaimCreature message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        if (message.claim) {
            creature.setOwnerId(player.getUniqueID());
            player.sendStatusMessage(new TextComponentTranslation("reminder.claim_creature", creature.getName()), false);
        }
        else {
            creature.setTameStatus(TameStatusType.SIT);
            creature.setTameBehavior(TameBehaviorType.PASSIVE);
            creature.setOwnerId(null);
            player.sendStatusMessage(new TextComponentTranslation("reminder.unclaim_creature", creature.getName()), false);
        }
    }
}
