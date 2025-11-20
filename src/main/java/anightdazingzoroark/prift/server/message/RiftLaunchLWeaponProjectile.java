package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLaunchLWeaponProjectile extends RiftLibMessage<RiftLaunchLWeaponProjectile> {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftLaunchLWeaponProjectile message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftLargeWeapon weapon = (RiftLargeWeapon) messagePlayer.world.getEntityByID(message.weaponId);
        if (weapon != null && weapon.getLeftClickCooldown() <= 0) weapon.launchProjectile(messagePlayer, Math.min(message.charge, 100));
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftLaunchLWeaponProjectile riftLaunchLWeaponProjectile, EntityPlayer entityPlayer, MessageContext messageContext) {}

}
