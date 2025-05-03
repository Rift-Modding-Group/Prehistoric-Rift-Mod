package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftManualUseMove implements IMessage {
    private int creatureId;
    private int control; //0 is left click, 1 is right click, 2 is middle click, -1 disables all
    private CreatureMove creatureMove;

    public RiftManualUseMove() {}

    public RiftManualUseMove(RiftCreature creature, int control, CreatureMove creatureMove) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.creatureMove = creatureMove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        int tempCreatureMoveInt = buf.readInt();
        this.creatureMove = tempCreatureMoveInt >= 0 ? CreatureMove.values()[tempCreatureMoveInt] : null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        if (this.creatureMove != null) buf.writeInt(this.creatureMove.ordinal());
        else buf.writeInt(-1);
    }

    public static class Handler implements IMessageHandler<RiftManualUseMove, IMessage> {
        @Override
        public IMessage onMessage(RiftManualUseMove message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftManualUseMove message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);

                if (creature != null) {
                    switch (message.control) {
                        case -1:
                            creature.setUsingMoveOne(false);
                            creature.setUsingMoveTwo(false);
                            creature.setUsingMoveThree(false);
                            break;
                        case 0:
                            creature.setUsingMoveOne(true);
                            if (message.creatureMove.chargeType.requiresCharge()) creature.setMoveOneUse(Math.min(creature.getMoveOneUse() + 1, message.creatureMove.maxUse));
                            break;
                        case 1:
                            creature.setUsingMoveTwo(true);
                            if (message.creatureMove.chargeType.requiresCharge()) creature.setMoveTwoUse(Math.min(creature.getMoveTwoUse() + 1, message.creatureMove.maxUse));
                            break;
                        case 2:
                            creature.setUsingMoveThree(true);
                            if (message.creatureMove.chargeType.requiresCharge()) creature.setMoveThreeUse(Math.min(creature.getMoveThreeUse() + 1, message.creatureMove.maxUse));
                            break;
                    }
                }
            }
        }
    }
}
