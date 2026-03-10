package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftInitDeployedCreatureMap extends RiftLibMessage<RiftInitDeployedCreatureMap> {
    private int playerId;

    public RiftInitDeployedCreatureMap() {}

    public RiftInitDeployedCreatureMap(EntityPlayer player) {
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftInitDeployedCreatureMap message, EntityPlayer entityPlayer, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft minecraft, RiftInitDeployedCreatureMap message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraft.world.getEntityByID(message.playerId);
        if (player == null) return;

        PlayerPartyProperties playerParty = PlayerPartyHelper.getPlayerParty(player);
        if (playerParty == null) return;

        for (int index = 0; index < PlayerPartyHelper.maxSize; index++) {
            CreatureNBT creatureNBT = playerParty.getPartyMember(index);
            if (creatureNBT.nbtIsEmpty()) continue;
            if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                RiftCreature correspondingCreature = creatureNBT.findCorrespondingCreature(minecraft.world);
                if (correspondingCreature == null) {
                    System.out.println("no corresponding creature at index "+index);
                    continue;
                }
                System.out.println("found corresponding creature at index "+index);
                //PlayerPartyHelper.deployedCreatures.put(index, correspondingCreature);
            }
        }
    }
}
