package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class RiftForceSyncPartyNBT implements IMessage {
    private int playerId;
    private List<NBTTagCompound> tagCompounds;

    public RiftForceSyncPartyNBT() {}

    public RiftForceSyncPartyNBT(EntityPlayer player) {
        this(player, new ArrayList<>());
    }

    public RiftForceSyncPartyNBT(EntityPlayer player, List<NBTTagCompound> tagCompounds) {
        this.playerId = player.getEntityId();
        this.tagCompounds = tagCompounds;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.tagCompounds = this.setNBTTagListToNBTList(compound.getTagList("List", 10));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        NBTTagCompound compound = new NBTTagCompound();
        if (this.tagCompounds.isEmpty()) compound.setTag("List", new NBTTagList());
        else compound.setTag("List", this.setNBTListToNBTTagList(this.tagCompounds));
        ByteBufUtils.writeTag(buf, compound);
    }

    private NBTTagList setNBTListToNBTTagList(List<NBTTagCompound> tagCompounds) {
        NBTTagList tagList = new NBTTagList();
        for (NBTTagCompound tagCompound : tagCompounds) tagList.appendTag(tagCompound);
        return tagList;
    }

    private List<NBTTagCompound> setNBTTagListToNBTList(NBTTagList tagList) {
        List<NBTTagCompound> compoundList = new ArrayList<>();
        for (NBTBase nbtBase : tagList) compoundList.add((NBTTagCompound) nbtBase);
        return compoundList;
    }

    public static class Handler implements IMessageHandler<RiftForceSyncPartyNBT, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncPartyNBT message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncPartyNBT message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.tagCompounds.isEmpty()) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyNBT(player, playerTamedCreatures.getPartyNBT()));
                else playerTamedCreatures.setPartyNBT(message.tagCompounds);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.tagCompounds.isEmpty()) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyNBT(player, playerTamedCreatures.getPartyNBT()));
                else playerTamedCreatures.setPartyNBT(message.tagCompounds);
            }
        }
    }
}
