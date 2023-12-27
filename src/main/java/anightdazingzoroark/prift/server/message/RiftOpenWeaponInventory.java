package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenWeaponInventory extends AbstractMessage<RiftOpenWeaponInventory> {
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

    @Override
    public void onClientReceived(Minecraft client, RiftOpenWeaponInventory message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftOpenWeaponInventory message, EntityPlayer player, MessageContext messageContext) {
        World world = player.getEntityWorld();
        player.openGui(RiftInitialize.instance, ServerProxy.GUI_WEAPON_INVENTORY, world, message.weaponId, 0, 0);
    }
}
