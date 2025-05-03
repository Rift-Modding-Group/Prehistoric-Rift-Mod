package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLaunchLWeaponProjectile implements IMessage {
    private int weaponId;
    private int charge;

    public RiftLaunchLWeaponProjectile() {}

    public RiftLaunchLWeaponProjectile(RiftLargeWeapon weapon) {
        this(weapon, 0);
    }

    public RiftLaunchLWeaponProjectile(RiftLargeWeapon weapon, int charge) {
        this.weaponId = weapon.getEntityId();
        this.charge = charge;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.weaponId = buf.readInt();
        this.charge = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.weaponId);
        buf.writeInt(this.charge);
    }

    public static class Handler implements IMessageHandler<RiftLaunchLWeaponProjectile, IMessage> {
        @Override
        public IMessage onMessage(RiftLaunchLWeaponProjectile message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftLaunchLWeaponProjectile message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftLargeWeapon weapon = (RiftLargeWeapon)playerEntity.world.getEntityByID(message.weaponId);
            if (weapon.getLeftClickCooldown() <= 0) weapon.launchProjectile(playerEntity, Math.min(message.charge, 100));
        }
    }
}
