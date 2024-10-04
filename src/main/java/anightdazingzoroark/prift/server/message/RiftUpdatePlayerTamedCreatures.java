package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftUpdatePlayerTamedCreatures extends AbstractMessage<RiftUpdatePlayerTamedCreatures> {
    private NBTTagCompound nbtTagCompound;
    private int playerId;

    public RiftUpdatePlayerTamedCreatures() {}

    public RiftUpdatePlayerTamedCreatures(NBTBase nbtBase, EntityPlayer player) {
        this.nbtTagCompound = (NBTTagCompound) nbtBase;
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
        buf.writeInt(this.playerId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onClientReceived(Minecraft minecraft, RiftUpdatePlayerTamedCreatures message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().world.getEntityByID(message.playerId);
        if (player != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                IPlayerTamedCreatures capability = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                if (capability != null) PlayerTamedCreaturesProvider.readNBT(capability, null, message.nbtTagCompound);
            });
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftUpdatePlayerTamedCreatures message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
