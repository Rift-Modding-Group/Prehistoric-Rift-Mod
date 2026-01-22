package anightdazingzoroark.prift.server.entity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CreatureGearHandler extends ItemStackHandler {
    private final RiftCreatureType creatureType;

    public CreatureGearHandler(RiftCreatureType creatureType) {
        super(creatureType.gearSlotCount());
        this.creatureType = creatureType;
    }

    public ItemStack getGear(RiftCreatureType.InventoryGearType gearType) {
        return this.stacks.get(this.creatureType.slotIndexForGear(gearType));
    }

    public void setStackInSlot(RiftCreatureType.InventoryGearType gearType, @Nonnull ItemStack stack) {
        int slot = this.creatureType.slotIndexForGear(gearType);

        this.validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        this.onContentsChanged(slot);
    }

    //make setting size do nothing xd
    @Override
    public void setSize(int size) {}

    //same with this xd
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {}
}
