package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageBlockBreakControl extends RiftLibMessage<RiftManageBlockBreakControl> {
    private int creatureId;
    private boolean value;

    public RiftManageBlockBreakControl() {}

    public RiftManageBlockBreakControl(RiftCreature creature, boolean value) {
        this.creatureId = creature.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.value);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftManageBlockBreakControl message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);

        if (creature != null) {
            if (message.value && creature.canSetBlockBreakMode()) {
                creature.setBlockBreakMode(!creature.inBlockBreakMode());
                creature.setCanSetBlockBreakMode(false);
            }
            else if (!message.value && !creature.canSetBlockBreakMode()) creature.setCanSetBlockBreakMode(true);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftManageBlockBreakControl message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
