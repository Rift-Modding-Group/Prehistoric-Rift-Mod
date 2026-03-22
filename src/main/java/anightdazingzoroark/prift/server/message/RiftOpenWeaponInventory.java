package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import com.cleanroommc.modularui.factory.EntityGuiFactory;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenWeaponInventory extends RiftLibMessage<RiftOpenWeaponInventory> {
    private int playerId;
    private int weaponId;

    public RiftOpenWeaponInventory() {}

    public RiftOpenWeaponInventory(EntityPlayer player, RiftLargeWeapon weapon) {
        this.playerId = player.getEntityId();
        this.weaponId = weapon.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.weaponId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.weaponId);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftOpenWeaponInventory message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        RiftLargeWeapon largeWeapon = (RiftLargeWeapon) server.getEntityWorld().getEntityByID(message.weaponId);
        if (largeWeapon == null) return;

        EntityGuiFactory.INSTANCE.open(player, largeWeapon);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenWeaponInventory riftOpenWeaponInventory, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
