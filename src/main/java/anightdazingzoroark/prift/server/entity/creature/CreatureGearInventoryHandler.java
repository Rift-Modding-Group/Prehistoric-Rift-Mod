package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import anightdazingzoroark.prift.server.item.RiftItems;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import org.jetbrains.annotations.NotNull;

public class CreatureGearInventoryHandler extends RiftLibInventoryHandler {
    public static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> PLATFORM_EQUIPPED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);

    @NotNull
    private final RiftCreature creature;

    public CreatureGearInventoryHandler(@NotNull RiftCreature creature) {
        super(creature.supportsPlatformSaddle() ? 2 : 1);
        this.creature = creature;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case 0 -> this.creature.canBeRidden() && stack.getItem() == Items.SADDLE;
            case 1 -> this.creature.supportsPlatformSaddle() && Loader.isModLoaded("valkyrienskies")
                    && stack.getItem() == RiftItems.CREATURE_PLATFORM;
            default -> false;
        };
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
        this.creature.setPlatformEquipped(this.getSlots() > 1 && this.getStackInSlot(1).getItem() == RiftItems.CREATURE_PLATFORM);
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        NBTTagCompound gear = nbt.copy();
        gear.setInteger("Size", this.creature.supportsPlatformSaddle() ? 2 : 1);
        super.deserializeNBT(gear);
        this.onContentsChanged(0);
    }
}
