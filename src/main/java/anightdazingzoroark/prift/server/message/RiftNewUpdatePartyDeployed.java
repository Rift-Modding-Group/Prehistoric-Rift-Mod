package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.Sys;

import java.util.List;
import java.util.UUID;

public class RiftNewUpdatePartyDeployed implements IMessage {
    private int playerId;
    private int index;
    private CreatureNBT tagCompound;

    public RiftNewUpdatePartyDeployed() {}

    public RiftNewUpdatePartyDeployed(EntityPlayer player) {
        this(player, -1, new CreatureNBT());
    }

    public RiftNewUpdatePartyDeployed(EntityPlayer player, int index, CreatureNBT tagCompound) {
        this.playerId = player.getEntityId();
        this.index = index;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.index = buf.readInt();
        this.tagCompound = new CreatureNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.index);
        ByteBufUtils.writeTag(buf, this.tagCompound.getCreatureNBT());
    }

    public static class Handler implements IMessageHandler<RiftNewUpdatePartyDeployed, IMessage> {
        @Override
        public IMessage onMessage(RiftNewUpdatePartyDeployed message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftNewUpdatePartyDeployed message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                if (playerTamedCreatures != null) {
                    List<CreatureNBT> partyNBT = playerTamedCreatures.getPartyNBT().getList();
                    for (int x = 0; x < partyNBT.size(); x++) {
                        if (partyNBT.get(x).nbtIsEmpty()) continue;

                        //get uuid
                        UUID memberUUID = partyNBT.get(x).getUniqueID();
                        if (memberUUID == null || memberUUID.equals(RiftUtil.nilUUID)) continue;

                        //get creature from uuid
                        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, memberUUID);
                        if (creature == null) continue;

                        //get nbt from creature
                        CreatureNBT partyMemNBT = new CreatureNBT(creature);
                        if (partyMemNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                            playerTamedCreatures.setPartyMemNBT(x, partyMemNBT);
                        }
                    }
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                if (playerTamedCreatures != null) {
                    playerTamedCreatures.setPartyMemNBT(message.index, message.tagCompound);
                }
            }
        }
    }
}
