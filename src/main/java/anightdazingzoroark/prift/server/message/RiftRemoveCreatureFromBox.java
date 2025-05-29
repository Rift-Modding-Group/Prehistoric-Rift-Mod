package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftRemoveCreatureFromBox implements IMessage {
    private UUID creatureUUID;

    public RiftRemoveCreatureFromBox() {}

    public RiftRemoveCreatureFromBox(UUID uuid) {
        this.creatureUUID = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());
    }

    public static class Handler implements IMessageHandler<RiftRemoveCreatureFromBox, IMessage> {
        @Override
        public IMessage onMessage(RiftRemoveCreatureFromBox message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftRemoveCreatureFromBox message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                //remove from world
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.creatureUUID);
                if (creature != null) {
                    messagePlayer.sendStatusMessage(new TextComponentTranslation("reminder.creature_released", creature.getName(false), creature.getName(false)), false);
                    creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.NONE);

                    //for removing hitboxes
                    RiftUtil.removeCreature(creature);
                }

                //remove from party and box
                IPlayerTamedCreatures playerTamedCreatures = messagePlayer.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                playerTamedCreatures.removeCreature(message.creatureUUID);
                playerTamedCreatures.removeCreatureFromBoxDeployed(messagePlayer.world, ClientProxy.creatureBoxBlockPos, message.creatureUUID);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                //remove from world
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.creatureUUID);
                if (creature != null) {
                    creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.NONE);
                    RiftUtil.removeCreature(creature);
                }
            }
        }
    }
}