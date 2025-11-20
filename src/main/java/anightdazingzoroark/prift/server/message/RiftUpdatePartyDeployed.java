package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class RiftUpdatePartyDeployed extends RiftLibMessage<RiftUpdatePartyDeployed> {
    private int playerId;
    private int index;
    private CreatureNBT tagCompound;

    public RiftUpdatePartyDeployed() {}

    public RiftUpdatePartyDeployed(EntityPlayer player) {
        this(player, -1, new CreatureNBT());
    }

    public RiftUpdatePartyDeployed(EntityPlayer player, int index, CreatureNBT tagCompound) {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdatePartyDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
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

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdatePartyDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) playerTamedCreatures.setPartyMemNBT(message.index, message.tagCompound);
    }
}
