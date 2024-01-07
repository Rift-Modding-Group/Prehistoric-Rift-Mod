package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageCanUseControl extends AbstractMessage<RiftManageCanUseControl> {
    private int creatureId;
    private int control; //0 for left click, 1 for right click, 2 for spacebar
    private boolean canUseControl;

    public RiftManageCanUseControl() {}

    public RiftManageCanUseControl(RiftCreature creature, int control, boolean canUseControl) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.canUseControl = canUseControl;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.canUseControl = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.canUseControl);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftManageCanUseControl message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageCanUseControl message, EntityPlayer player, MessageContext messageContext) {
        World world = player.world;
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);

        if (!world.isRemote) {
            switch (message.control) {
                case 0:
                    interacted.setCanUseLeftClick(message.canUseControl);
                    break;
                case 1:
                    interacted.setCanUseRightClick(message.canUseControl);
                    break;
                case 2:
                    interacted.setCanUseSpacebar(message.canUseControl);
                    break;
            }
        }
    }
}
