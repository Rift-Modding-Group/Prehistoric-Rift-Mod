package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeCreatureFromMenu implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftChangeCreatureFromMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeCreatureFromMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeCreatureFromMenu message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
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
}
