package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangeSelectedCreatureName implements IMessage {
    private int playerId;
    private NBTTagCompound selectedCreatureInfoNBT;
    private String newName;

    public RiftChangeSelectedCreatureName() {}

    public RiftChangeSelectedCreatureName(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo, String newName) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(buf);
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedCreatureInfoNBT);
        ByteBufUtils.writeUTF8String(buf, this.newName);
    }

    public static class Handler implements IMessageHandler<RiftChangeSelectedCreatureName, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeSelectedCreatureName message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeSelectedCreatureName message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
                    CreatureNBT creatureNBT = selectedCreatureInfo.getCreatureNBT(player);
                    creatureNBT.setCustomName(message.newName);

                    if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                        playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], creatureNBT);
                    }
                    else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                        playerTamedCreatures.getBoxNBT().setBoxCreature(selectedCreatureInfo.pos[0], selectedCreatureInfo.pos[1], creatureNBT);
                    }
                }
            }
        }
    }
}
