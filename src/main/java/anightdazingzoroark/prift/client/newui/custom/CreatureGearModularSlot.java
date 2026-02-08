package anightdazingzoroark.prift.client.newui.custom;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.CreatureGearHandler;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CreatureGearModularSlot extends ModularSlot {
    private final SelectedCreatureInfo selectedCreatureInfo;

    public CreatureGearModularSlot(CreatureGearHandler creatureInventoryHandler, int index) {
        this(null, creatureInventoryHandler, index);
    }

    public CreatureGearModularSlot(SelectedCreatureInfo selectedCreatureInfo, CreatureGearHandler creatureInventoryHandler, int index) {
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
        CreatureGearHandler creatureGearHandler = (CreatureGearHandler) this.getItemHandler();
        NBTTagCompound paramArg = CreatureNBTKeyword.GEAR.setValue(creatureGearHandler.serializeNBT());
        PlayerTamedCreaturesHelper.setCreatureNBTParam(this.getPlayer(), paramArg, this.selectedCreatureInfo);
    }
}
