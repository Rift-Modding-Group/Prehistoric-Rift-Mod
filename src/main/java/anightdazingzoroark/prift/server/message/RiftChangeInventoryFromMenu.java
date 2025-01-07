package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangeInventoryFromMenu implements IMessage {
    private int creatureId;
    private RiftCreatureInvData inventory;
    private PlayerInvData playerInventory;

    public RiftChangeInventoryFromMenu() {}

    public RiftChangeInventoryFromMenu(RiftCreature creature, EntityPlayer player) {
        this.creatureId = creature.getEntityId();
        this.inventory = RiftCreatureInvData.fromRiftInventory(creature.creatureInventory);
        this.playerInventory = PlayerInvData.fromPlayerInventory(player.inventory);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.inventory = RiftCreatureInvData.fromByteBuf(buf);
        this.playerInventory = PlayerInvData.fromByteBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        RiftCreatureInvData.writeToByteBuf(buf);
        PlayerInvData.writeToByteBuf(buf);
    }

    public static class Handler implements IMessageHandler<RiftChangeInventoryFromMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeInventoryFromMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeInventoryFromMenu message, MessageContext ctx) {
            EntityPlayer messagePlayer = ctx.getServerHandler().player;

            RiftCreature interacted = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);

            interacted.creatureInventory.setInventoryFromData(message.inventory);
            PlayerInvData.applyInventoryData(messagePlayer, message.playerInventory);
        }
    }

    public static class RiftCreatureInvData {
        private static ItemStack[] inventoryContents;

        public RiftCreatureInvData(ItemStack[] inventoryContents) {
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

        public static RiftCreatureInvData fromByteBuf(ByteBuf buf) {
            int size = buf.readInt();
            ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                contents[i] = ByteBufUtils.readItemStack(buf);
            }
            return new RiftCreatureInvData(contents);
        }

        public static RiftCreatureInvData fromRiftInventory(RiftCreature.RiftCreatureInventory container) {
            ItemStack[] contents = new ItemStack[container.getSizeInventory()];
            for (int i = 0; i < contents.length; i++) {
                contents[i] = container.getStackInSlot(i);
            }
            return new RiftCreatureInvData(contents);
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
