package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.TameBehaviorType;
import anightdazingzoroark.rift.server.entity.TameStatusType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeCreatureFromMenu implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftChangeCreatureFromMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeCreatureFromMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeCreatureFromMenu message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            RiftCreature interacted = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);

            if (world != null) {
                if (!interacted.getTameBehavior().equals(message.tameBehavior)) this.sendTameBehaviorMessage(message.tameBehavior, interacted);
                interacted.setTameBehavior(message.tameBehavior);
                if (!interacted.getTameStatus().equals(message.tameStatus)) this.sendTameStatusMessage(message.tameStatus, interacted);
                interacted.setTameStatus(message.tameStatus);
                interacted.getNavigator().clearPath();
                interacted.setAttackTarget((EntityLivingBase)null);
            }
        }

        private void sendTameStatusMessage(TameStatusType tameStatus, RiftCreature creature) {
            String tameStatusName = "tame_status."+tameStatus.name().toLowerCase();
            ITextComponent itextcomponent = new TextComponentString(creature.getName());
            if (creature.getOwner() instanceof EntityPlayer) {
                ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(tameStatusName, itextcomponent), false);
            }
        }

        private void sendTameBehaviorMessage(TameBehaviorType tameBehavior, RiftCreature creature) {
            String tameStatusName = "tame_status."+tameBehavior.name().toLowerCase();
            ITextComponent itextcomponent = new TextComponentString(creature.getName());
            if (creature.getOwner() instanceof EntityPlayer) {
                ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(tameStatusName, itextcomponent), false);
            }
        }
    }
}
