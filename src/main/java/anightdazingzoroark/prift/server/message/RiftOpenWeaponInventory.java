package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenWeaponInventory extends RiftLibMessage<RiftOpenWeaponInventory> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftOpenWeaponInventory message, EntityPlayer messagePlayer, MessageContext messageContext) {
        World world = messagePlayer.getEntityWorld();
        messagePlayer.openGui(RiftInitialize.instance, RiftGui.GUI_WEAPON_INVENTORY, world, message.weaponId, 0, 0);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenWeaponInventory riftOpenWeaponInventory, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
