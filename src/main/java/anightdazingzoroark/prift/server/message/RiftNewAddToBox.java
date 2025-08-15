package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftNewAddToBox implements IMessage {
    private int playerId;
    private int creatureId;

    public RiftNewAddToBox() {}

    public RiftNewAddToBox(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureId);
    }

    public static class Handler implements IMessageHandler<RiftNewAddToBox, IMessage> {
        @Override
        public IMessage onMessage(RiftNewAddToBox message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftNewAddToBox message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
                if (playerTamedCreatures != null && creature != null) {
                    //turn creature into nbt
                    playerTamedCreatures.addToBoxNBT(new CreatureNBT(creature));

                    //remove creature
                    creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                }
            }
        }
    }
}
