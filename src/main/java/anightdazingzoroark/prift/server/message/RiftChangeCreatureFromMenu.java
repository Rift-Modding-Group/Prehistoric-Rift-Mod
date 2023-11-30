package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeCreatureFromMenu extends AbstractMessage<RiftChangeCreatureFromMenu> {
    private TameStatusType tameStatus;
    private TameBehaviorType tameBehavior;
    private RiftCreature creature;
    private int creatureId;

    public RiftChangeCreatureFromMenu() {}

    public RiftChangeCreatureFromMenu(RiftCreature creature, TameStatusType tameStatus) {
        this(creature, tameStatus, creature.getTameBehavior());
    }

    public RiftChangeCreatureFromMenu(RiftCreature creature, TameBehaviorType tameBehavior) {
        this(creature, creature.getTameStatus(), tameBehavior);
    }

    public RiftChangeCreatureFromMenu(RiftCreature creature, TameStatusType tameStatus, TameBehaviorType tameBehavior) {
        this.creature = creature;
        this.creatureId = creature.getEntityId();
        this.tameStatus = tameStatus;
        this.tameBehavior = tameBehavior;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.tameStatus = TameStatusType.values()[buf.readInt()];
        this.tameBehavior = TameBehaviorType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.tameStatus.ordinal());
        buf.writeInt(this.tameBehavior.ordinal());
    }

    @Override
    public void onClientReceived(Minecraft client, RiftChangeCreatureFromMenu message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftChangeCreatureFromMenu message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);

        if (!interacted.getTameBehavior().equals(message.tameBehavior)) this.sendTameBehaviorMessage(message.tameBehavior, interacted);
        interacted.setTameBehavior(message.tameBehavior);
        if (!interacted.getTameStatus().equals(message.tameStatus)) this.sendTameStatusMessage(message.tameStatus, interacted);
        interacted.setTameStatus(message.tameStatus);
        interacted.getNavigator().clearPath();
        interacted.setAttackTarget((EntityLivingBase)null);
    }

    private void sendTameStatusMessage(TameStatusType tameStatus, RiftCreature creature) {
        String tameStatusName = "tamestatus."+tameStatus.name().toLowerCase();
        ITextComponent itextcomponent = new TextComponentString(creature.getName());
        if (creature.getOwner() instanceof EntityPlayer) {
            ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(tameStatusName, itextcomponent), false);
        }
    }

    private void sendTameBehaviorMessage(TameBehaviorType tameBehavior, RiftCreature creature) {
        String tameBehaviorName = "tamebehavior."+tameBehavior.name().toLowerCase();
        ITextComponent itextcomponent = new TextComponentString(creature.getName());
        if (creature.getOwner() instanceof EntityPlayer) {
            ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(tameBehaviorName, itextcomponent), false);
        }
    }
}
