package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenWeaponInventory implements IMessage {
    private int weaponId;

    public RiftOpenWeaponInventory() {}

    public RiftOpenWeaponInventory(RiftLargeWeapon weapon) {
        this.weaponId = weapon.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.weaponId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.weaponId);
    }

    public static class Handler implements IMessageHandler<RiftOpenWeaponInventory, IMessage> {
        @Override
        public IMessage onMessage(RiftOpenWeaponInventory message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftOpenWeaponInventory message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            playerEntity.openGui(RiftInitialize.instance, ServerProxy.GUI_WEAPON_INVENTORY, world, message.weaponId, 0, 0);
        }
    }
}
