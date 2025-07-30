package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftAddToBox implements IMessage {
    private int playerId;
    private UUID uuid;

    public RiftAddToBox() {}

    public RiftAddToBox(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.uuid = creature.getUniqueID();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.uuid = new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
    }

    public static class Handler implements IMessageHandler<RiftAddToBox, IMessage> {
        @Override
        public IMessage onMessage(RiftAddToBox message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftAddToBox message, MessageContext ctx) {
            /*
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

                boolean creatureInList = playerTamedCreatures.getBoxNBT().stream()
                        .noneMatch(nbt -> nbt.hasKey("UniqueIDMost") && nbt.hasKey("UniqueIDLeast") && nbt.getUniqueId("UniqueID").equals(message.uuid));
                if (creatureInList && creature != null) playerTamedCreatures.addToBoxCreatures(creature);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

                boolean creatureInList = playerTamedCreatures.getBoxNBT().stream()
                        .noneMatch(nbt -> nbt.hasKey("UniqueIDMost") && nbt.hasKey("UniqueIDLeast") && nbt.getUniqueId("UniqueID").equals(message.uuid));
                if (creatureInList && creature != null) playerTamedCreatures.addToBoxCreatures(creature);
            }
             */
        }
    }
}
