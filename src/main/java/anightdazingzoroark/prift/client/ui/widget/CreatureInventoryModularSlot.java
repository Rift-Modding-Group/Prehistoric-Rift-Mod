package anightdazingzoroark.prift.client.ui.widget;

import anightdazingzoroark.prift.client.ui.data.CreatureGuiData;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.helper.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.entity.inventory.CreatureInventoryHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class CreatureInventoryModularSlot extends ModularSlot {
    private final CreatureGuiData guiData;

    public CreatureInventoryModularSlot(CreatureGuiData guiData, int index) {
        super(guiData.getCreatureInventory(), index);
        this.guiData = guiData;
    }

    @Override
    public void onSlotChanged() {
        this.updateSelectedCreatureNBT();
    }

    @Override
    public void onSlotChangedReal(ItemStack itemStack, boolean onlyChangedAmount, boolean client, boolean init) {
        super.onSlotChangedReal(itemStack, onlyChangedAmount, client, init);
        this.updateSelectedCreatureNBT();
    }

    @Override
    public void onCraftShiftClick(EntityPlayer player, ItemStack stack) {
        this.updateSelectedCreatureNBT();
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        super.putStack(stack);
        this.updateSelectedCreatureNBT();
    }

    private void updateSelectedCreatureNBT() {
        if (this.guiData.dataType == CreatureGuiData.DataType.CREATURE) return;
        CreatureInventoryHandler creatureInventoryHandler = (CreatureInventoryHandler) this.getItemHandler();

        CreatureNBT creatureNBT = this.guiData.getSyncedNBT().getValue();
        NBTTagCompound creatureNBTCompound = creatureNBT.getCreatureNBT();
        CreatureNBTKeyword.mergeResult(creatureNBTCompound, CreatureNBTKeyword.INVENTORY, creatureInventoryHandler.serializeNBT());
        CreatureNBT newCreatureNBT = new CreatureNBT(creatureNBTCompound);

        this.guiData.getSyncedNBT().setCreatureNBT(newCreatureNBT);
        this.guiData.getSyncedNBT().notifyUpdate();
    }
}
