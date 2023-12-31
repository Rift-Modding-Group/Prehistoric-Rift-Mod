package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLaunchLWeaponProjectile extends AbstractMessage<RiftLaunchLWeaponProjectile> {
    private int weaponId;

    public RiftLaunchLWeaponProjectile() {}

    public RiftLaunchLWeaponProjectile(RiftLargeWeapon weapon) {
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

    @Override
    public void onClientReceived(Minecraft client, RiftLaunchLWeaponProjectile message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftLaunchLWeaponProjectile message, EntityPlayer player, MessageContext messageContext) {
        RiftLargeWeapon weapon = (RiftLargeWeapon)player.world.getEntityByID(message.weaponId);
        weapon.launchProjectile(player);
    }
}
