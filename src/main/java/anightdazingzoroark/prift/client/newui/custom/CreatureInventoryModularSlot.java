package anightdazingzoroark.prift.client.newui.custom;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.CreatureInventoryHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class CreatureInventoryModularSlot extends ModularSlot {
    private final SelectedCreatureInfo selectedCreatureInfo;

    public CreatureInventoryModularSlot(CreatureInventoryHandler creatureInventoryHandler, int index) {
        this(null, creatureInventoryHandler, index);
    }

    public CreatureInventoryModularSlot(SelectedCreatureInfo selectedCreatureInfo, CreatureInventoryHandler creatureInventoryHandler, int index) {
        super(creatureInventoryHandler, index);
        this.selectedCreatureInfo = selectedCreatureInfo;
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
        if (this.selectedCreatureInfo == null) return;
        CreatureInventoryHandler creatureInventoryHandler = (CreatureInventoryHandler) this.getItemHandler();
        NBTTagCompound paramArg = CreatureNBTKeyword.INVENTORY.setValue(creatureInventoryHandler.serializeNBT());
        PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), paramArg, this.selectedCreatureInfo);
    }
}
