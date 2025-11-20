package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftAddToBox extends RiftLibMessage<RiftAddToBox> {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftAddToBox message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
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

    @Override
    public void executeOnClient(Minecraft minecraft, RiftAddToBox message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
