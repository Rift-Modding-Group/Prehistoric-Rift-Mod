package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftIncrementControlUse extends AbstractMessage<RiftIncrementControlUse> {
    private int entityId;
    private int control;

    public RiftIncrementControlUse() {}

    public RiftIncrementControlUse(EntityLivingBase entity) {
        this(entity, -1);
    }

    public RiftIncrementControlUse(EntityLivingBase entity, int control) {
        this.entityId = entity.getEntityId();
        this.control = control;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.control = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.control);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftIncrementControlUse message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftIncrementControlUse message, EntityPlayer player, MessageContext messageContext) {
        EntityLivingBase entity = (EntityLivingBase) player.world.getEntityByID(message.entityId);

        if (entity instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) entity;
            switch (message.control) {
                case 0:
                    creature.setLeftClickUse(creature.getLeftClickUse() + 1);
                    break;
                case 1:
                    if (creature.getEnergy() > 6) creature.setRightClickUse(creature.getRightClickUse() + 1);
                    break;
                case 2:
                    if (creature.getEnergy() > 6) creature.setSpacebarUse(creature.getSpacebarUse() + 1);
                    break;
            }
        }
        else if (entity instanceof RiftCatapult) {
            RiftCatapult catapult = (RiftCatapult) entity;
            catapult.setLeftClickUse(catapult.getLeftClickUse() + 1);
        }
    }
}
