package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangeDeploymentType implements IMessage {
    private int creatureId;
    private PlayerTamedCreatures.DeploymentType deploymentType;

    public RiftChangeDeploymentType() {}

    public RiftChangeDeploymentType(RiftCreature creature, PlayerTamedCreatures.DeploymentType deploymentType) {
        this.creatureId = creature.getEntityId();
        this.deploymentType = deploymentType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.deploymentType = PlayerTamedCreatures.DeploymentType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.deploymentType.ordinal());
    }

    public static class Handler implements IMessageHandler<RiftChangeDeploymentType, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeDeploymentType message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeDeploymentType message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
                if (creature != null) creature.setDeploymentType(message.deploymentType);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
                if (creature != null) creature.setDeploymentType(message.deploymentType);
            }
        }
    }
}
