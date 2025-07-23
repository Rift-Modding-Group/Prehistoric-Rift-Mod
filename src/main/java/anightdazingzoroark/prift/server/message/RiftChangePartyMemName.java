package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
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

import java.util.UUID;

public class RiftChangePartyMemName implements IMessage {
    private int playerId;
    private int partyMemPos;
    private String newName;

    public RiftChangePartyMemName() {}

    public RiftChangePartyMemName(EntityPlayer player, int partyMemPos, String newName) {
        this.playerId = player.getEntityId();
        this.partyMemPos = partyMemPos;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partyMemPos = buf.readInt();
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyMemPos);
        ByteBufUtils.writeUTF8String(buf, this.newName);
    }

    public static class Handler implements IMessageHandler<RiftChangePartyMemName, IMessage> {
        @Override
        public IMessage onMessage(RiftChangePartyMemName message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangePartyMemName message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    NBTTagCompound partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.partyMemPos);

                    //test if creature is deployed
                    PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];
                    if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY) {
                        UUID creatureUUID = partyMemNBT.getUniqueId("UniqueID");
                        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);
                        if (creature != null) creature.setCustomNameTag(message.newName);
                    }
                    else {
                        partyMemNBT.setString("CustomName", message.newName);
                        playerTamedCreatures.setPartyMemNBT(message.partyMemPos, partyMemNBT);
                    }
                }
            }
        }
    }
}
