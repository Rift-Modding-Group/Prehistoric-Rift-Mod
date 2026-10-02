package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import org.jetbrains.annotations.NotNull;

public class CreatureGearInventoryHandler extends RiftLibInventoryHandler {
    public static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    @NotNull
    private final RiftCreature creature;

    public CreatureGearInventoryHandler(@NotNull RiftCreature creature) {
        super(1, stack -> stack.getItem() == Items.SADDLE);
        this.creature = creature;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0 && this.creature.canBeRidden() && stack.getItem() == Items.SADDLE;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot == 0 && stack.isEmpty() && this.creature.isBeingRidden() && !this.getStackInSlot(slot).isEmpty()) return;
        super.setStackInSlot(slot, stack);
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0 && this.creature.isBeingRidden()) return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        this.creature.setSaddled(this.creature.canBeRidden() && this.getStackInSlot(0).getItem() == Items.SADDLE);
    }
}
