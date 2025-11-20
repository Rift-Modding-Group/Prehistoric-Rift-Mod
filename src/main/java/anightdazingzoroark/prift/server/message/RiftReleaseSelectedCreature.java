package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
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

public class RiftReleaseSelectedCreature extends RiftLibMessage<RiftReleaseSelectedCreature> {
    private int playerId;
    private NBTTagCompound selectedCreatureInfoNBT;

    public RiftReleaseSelectedCreature() {}

    public RiftReleaseSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedCreatureInfoNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftReleaseSelectedCreature message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (playerTamedCreatures != null) {
            SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);

            if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                //automatically undeploy creature first
                PlayerTamedCreaturesHelper.deployCreatureFromParty(player, selectedCreatureInfo.pos[0], false);

                //now remove creature
                playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], new CreatureNBT());
            }
            else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                playerTamedCreatures.getBoxNBT().setBoxCreature(selectedCreatureInfo.pos[0], selectedCreatureInfo.pos[1], new CreatureNBT());
            }
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftReleaseSelectedCreature message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
