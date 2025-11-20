package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftUpdateIndividualBoxDeployedCreatureServer extends RiftLibMessage<RiftUpdateIndividualBoxDeployedCreatureServer> {
    private int playerId;
    private int creatureId;

    public RiftUpdateIndividualBoxDeployedCreatureServer() {}

    public RiftUpdateIndividualBoxDeployedCreatureServer(EntityPlayer player, RiftCreature creature) {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdateIndividualBoxDeployedCreatureServer message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
        if (player != null && creature != null) {
            //find creature box
            TileEntity te = messagePlayer.world.getTileEntity(creature.getHomePos());
            if (!(te instanceof RiftTileEntityCreatureBox)) return;
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) te;

            //first of all, check if creature is deployed from the box
            int pos = -1;
            UUID creatureUUID = creature.getUniqueID();
            for (int x = 0; x < teCreatureBox.getDeployedCreatures().size(); x++) {
                CreatureNBT deployedMemNBT = teCreatureBox.getDeployedCreatures().get(x);
                if (!deployedMemNBT.nbtIsEmpty()
                        && deployedMemNBT.getUniqueID() != null
                        && deployedMemNBT.getUniqueID().equals(creatureUUID)) {
                    pos = x;
                    break;
                }
            }
            if (pos < 0) return;

            //if creature is in the party, go on and update
            CreatureNBT creatureNBT = new CreatureNBT(creature);
            teCreatureBox.setCreatureInPos(pos, creatureNBT);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateIndividualBoxDeployedCreatureServer message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
