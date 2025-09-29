package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftAddToBox implements IMessage {
    private int playerId;
    private NBTTagCompound creatureNBTTagCompound;

    public RiftAddToBox() {}

    public RiftAddToBox(EntityPlayer player, CreatureNBT creatureNBTTagCompound) {
        this.playerId = player.getEntityId();
        this.creatureNBTTagCompound = creatureNBTTagCompound.getCreatureNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureNBTTagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.creatureNBTTagCompound);
    }

    public static class Handler implements IMessageHandler<RiftAddToBox, IMessage> {
        @Override
        public IMessage onMessage(RiftAddToBox message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftAddToBox message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                CreatureNBT creatureNBT = new CreatureNBT(message.creatureNBTTagCompound);

                if (playerTamedCreatures != null) {
                    //turn creature into nbt
                    playerTamedCreatures.addToBoxNBT(creatureNBT);

                    //test if the creature exists in the world
                    //if it does, remove the creature
                    RiftCreature creature = creatureNBT.findCorrespondingCreature(messagePlayer.world);
                    if (creature != null) creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                }
            }
        }
    }
}
