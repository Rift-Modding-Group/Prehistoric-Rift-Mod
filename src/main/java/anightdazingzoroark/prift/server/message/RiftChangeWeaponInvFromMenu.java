package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeWeaponInvFromMenu extends AbstractMessage<RiftChangeWeaponInvFromMenu> {
    private int creatureId;
    private RiftWeaponInvData inventory;
    private PlayerInvData playerInventory;
    private RiftLargeWeapon weapon;
    private EntityPlayer player;

    public RiftChangeWeaponInvFromMenu() {}

    public RiftChangeWeaponInvFromMenu(RiftLargeWeapon weapon, EntityPlayer player) {
        this.weapon = weapon;
        this.player = player;
        this.creatureId = weapon.getEntityId();
        this.inventory = RiftWeaponInvData.fromRiftInventory(weapon.weaponInventory);
        this.playerInventory = PlayerInvData.fromPlayerInventory(player.inventory);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.inventory = RiftWeaponInvData.fromByteBuf(buf);
        this.playerInventory = PlayerInvData.fromByteBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        RiftWeaponInvData.writeToByteBuf(buf);
        PlayerInvData.writeToByteBuf(buf);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftChangeWeaponInvFromMenu message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftChangeWeaponInvFromMenu message, EntityPlayer player, MessageContext messageContext) {
        RiftLargeWeapon interacted = (RiftLargeWeapon) player.world.getEntityByID(message.creatureId);

        interacted.weaponInventory.setInventoryFromData(message.inventory);
        PlayerInvData.applyInventoryData(player, message.playerInventory);
    }

    public static class RiftWeaponInvData {
        private static ItemStack[] inventoryContents;

        public RiftWeaponInvData(ItemStack[] inventoryContents) {
            this.inventoryContents = inventoryContents;
        }

        public ItemStack[] getInventoryContents() {
            return inventoryContents;
        }

        public static void writeToByteBuf(ByteBuf buf) {
            buf.writeInt(inventoryContents.length);
            for (ItemStack stack : inventoryContents) {
                ByteBufUtils.writeItemStack(buf, stack);
            }
        }

        public static RiftWeaponInvData fromByteBuf(ByteBuf buf) {
            int size = buf.readInt();
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                contents[i] = ByteBufUtils.readItemStack(buf);
            }
            return new RiftWeaponInvData(contents);
        }

        public static RiftWeaponInvData fromRiftInventory(RiftLargeWeapon.RiftLargeWeaponInventory container) {
            ItemStack[] contents = new ItemStack[container.getSizeInventory()];
            for (int i = 0; i < contents.length; i++) {
                contents[i] = container.getStackInSlot(i);
            }
            return new RiftWeaponInvData(contents);
        }
    }

    public static class PlayerInvData {
        private static ItemStack[] inventoryContents;

        public PlayerInvData(ItemStack[] inventoryContents) {
            this.inventoryContents = inventoryContents;
        }

        public ItemStack[] getInventoryContents() {
            return inventoryContents;
        }

        public static void writeToByteBuf(ByteBuf buf) {
            buf.writeInt(inventoryContents.length);
            for (ItemStack stack : inventoryContents) {
                ByteBufUtils.writeItemStack(buf, stack);
            }
        }

        public static PlayerInvData fromByteBuf(ByteBuf buf) {
            int size = buf.readInt();
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                contents[i] = ByteBufUtils.readItemStack(buf);
            }
            return new PlayerInvData(contents);
        }

        public static PlayerInvData fromPlayerInventory(InventoryPlayer container) {
            ItemStack[] contents = new ItemStack[container.getSizeInventory()];
            for (int i = 0; i < contents.length; i++) {
                contents[i] = container.getStackInSlot(i);
            }
            return new PlayerInvData(contents);
        }

        public static void applyInventoryData(EntityPlayer player, PlayerInvData inventoryData) {
            InventoryPlayer playerInventory = player.inventory;

            ItemStack[] serializedInventory = inventoryData.getInventoryContents();

            for (int slot = 0; slot < serializedInventory.length; slot++) {
                ItemStack newItemStack = serializedInventory[slot];

                playerInventory.setInventorySlotContents(slot, newItemStack);
            }
        }
    }
}
