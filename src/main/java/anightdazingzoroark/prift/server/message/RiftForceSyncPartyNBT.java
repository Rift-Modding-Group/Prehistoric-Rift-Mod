package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
    private int listSize;
    private FixedSizeList<CreatureNBT> tagCompounds;

    public RiftForceSyncPartyNBT() {}

    public RiftForceSyncPartyNBT(EntityPlayer player) {
        this(player, new FixedSizeList<>(0));
    }

    public RiftForceSyncPartyNBT(EntityPlayer player, FixedSizeList<CreatureNBT> tagCompounds) {
        this.playerId = player.getEntityId();
        this.listSize = tagCompounds.size();
        this.tagCompounds = tagCompounds;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.listSize = buf.readInt();

        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.tagCompounds = this.setNBTTagListToNBTList(compound.getTagList("List", 10), this.listSize);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.listSize);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("List", this.setNBTListToNBTTagList(this.tagCompounds));
        ByteBufUtils.writeTag(buf, compound);
    }

    private NBTTagList setNBTListToNBTTagList(FixedSizeList<CreatureNBT> tagCompounds) {
        NBTTagList tagList = new NBTTagList();
        for (CreatureNBT tagCompound : tagCompounds.getList()) tagList.appendTag(tagCompound.getCreatureNBT());
        return tagList;
    }

    private FixedSizeList<CreatureNBT> setNBTTagListToNBTList(NBTTagList tagList, int size) {
        FixedSizeList<CreatureNBT> compoundList = new FixedSizeList<>(size, new CreatureNBT());
        for (int x = 0; x < tagList.tagCount(); x++) {
            CreatureNBT tagCompound = new CreatureNBT((NBTTagCompound) tagList.get(x));
            compoundList.set(x, tagCompound);
        }
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
                RiftMessages.WRAPPER.sendTo(new RiftForceSyncPartyNBT(player, playerTamedCreatures.getPartyNBT()), (EntityPlayerMP) player);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                playerTamedCreatures.setPartyNBT(message.tagCompounds);
            }
        }
    }
}
