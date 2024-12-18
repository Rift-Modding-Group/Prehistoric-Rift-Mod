package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftChangeCreatureFromMenu extends AbstractMessage<RiftChangeCreatureFromMenu> {
    private TameBehaviorType tameBehavior;
    private int creatureId;

    public RiftChangeCreatureFromMenu() {}

    public RiftChangeCreatureFromMenu(RiftCreature creature, TameBehaviorType tameBehavior) {
        this.creatureId = creature.getEntityId();
        this.tameBehavior = tameBehavior;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.tameBehavior = TameBehaviorType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.tameBehavior.ordinal());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onClientReceived(Minecraft client, RiftChangeCreatureFromMenu message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);
        interacted.setTameBehavior(message.tameBehavior);
        interacted.getNavigator().clearPath();
        interacted.setAttackTarget((EntityLivingBase)null);
    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftChangeCreatureFromMenu message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);

        if (!interacted.getTameBehavior().equals(message.tameBehavior)) this.sendTameBehaviorMessage(message.tameBehavior, interacted);
        interacted.setTameBehavior(message.tameBehavior);

        interacted.getNavigator().clearPath();
        interacted.setAttackTarget((EntityLivingBase)null);
    }

    private void sendTameBehaviorMessage(TameBehaviorType tameBehavior, RiftCreature creature) {
        String tameBehaviorName = "tamebehavior."+tameBehavior.name().toLowerCase();
        ITextComponent itextcomponent = new TextComponentString(creature.getName());
        if (creature.getOwner() instanceof EntityPlayer) {
            ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(tameBehaviorName, itextcomponent), false);
        }
    }
}
