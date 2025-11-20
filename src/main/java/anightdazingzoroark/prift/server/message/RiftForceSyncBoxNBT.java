package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureBoxStorage;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftForceSyncBoxNBT extends RiftLibMessage<RiftForceSyncBoxNBT> {
    private int playerId;
    private boolean canCountdownRevival;
    private CreatureBoxStorage creatureBoxStorage;

    public RiftForceSyncBoxNBT() {}

    public RiftForceSyncBoxNBT(EntityPlayer player) {
        this(player, false, new CreatureBoxStorage());
    }


    public RiftForceSyncBoxNBT(EntityPlayer player, boolean canCountdownRevival) {
        this(player, canCountdownRevival, new CreatureBoxStorage());
    }

    public RiftForceSyncBoxNBT(EntityPlayer player, boolean canCountdownRevival, CreatureBoxStorage creatureBoxStorage) {
        this.playerId = player.getEntityId();
        this.canCountdownRevival = canCountdownRevival;
        this.creatureBoxStorage = creatureBoxStorage;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.canCountdownRevival = buf.readBoolean();

        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.creatureBoxStorage = new CreatureBoxStorage(compound.getTagList("CreatureBoxStorage", 10));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.canCountdownRevival);
        NBTTagCompound compound = new NBTTagCompound();
        if (this.creatureBoxStorage != null) {
            compound.setTag("CreatureBoxStorage", this.creatureBoxStorage.writeNBTList());
            ByteBufUtils.writeTag(buf, compound);
        }
        else {
            compound.setTag("CreatureBoxStorage", new CreatureBoxStorage().writeNBTList());
            ByteBufUtils.writeTag(buf, compound);
        }
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftForceSyncBoxNBT message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity potentialPlayer = messagePlayer.world.getEntityByID(message.playerId);
        if (!(potentialPlayer instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) potentialPlayer;

        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) {
            //counting down for revival also happens here
            if (message.canCountdownRevival) playerTamedCreatures.getBoxNBT().countdownCreatureRevival(1);
            RiftMessages.WRAPPER.sendTo(new RiftForceSyncBoxNBT(player, false, playerTamedCreatures.getBoxNBT()), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftForceSyncBoxNBT message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity potentialPlayer = messagePlayer.world.getEntityByID(message.playerId);
        if (!(potentialPlayer instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) potentialPlayer;

        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) {
            playerTamedCreatures.setBoxNBT(message.creatureBoxStorage);
        }
    }
}
