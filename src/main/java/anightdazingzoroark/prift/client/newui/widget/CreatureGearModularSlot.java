package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.helper.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.entity.inventory.CreatureGearHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class CreatureGearModularSlot extends ModularSlot {
    private final CreatureGuiData guiData;

    public CreatureGearModularSlot(CreatureGuiData guiData, int index) {
        super(guiData.getCreatureGear(), index);
        this.filter(guiData.getCreatureGear()::itemStackUsableAsGear);
        this.accessibility(guiData.gearSlotChangeable(), guiData.gearSlotChangeable());
        this.changeListener((newItem, onlyAmountChanged, client, init) -> {
            if (!client) {
                guiData.setSaddled(guiData.getCreatureGear().hasSaddle());
                guiData.setLargeWeapon(guiData.getCreatureGear().getLargeWeapon());
            }
        });
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
        CreatureGearHandler creatureGearHandler = (CreatureGearHandler) this.getItemHandler();

        CreatureNBT creatureNBT = this.guiData.getSyncedNBT().getValue();
        NBTTagCompound creatureNBTCompound = creatureNBT.getCreatureNBT();
        CreatureNBTKeyword.mergeResult(creatureNBTCompound, CreatureNBTKeyword.GEAR, creatureGearHandler.serializeNBT());
        CreatureNBTKeyword.mergeResult(creatureNBTCompound, CreatureNBTKeyword.SADDLED, creatureGearHandler.hasSaddle());
        CreatureNBTKeyword.mergeResult(creatureNBTCompound, CreatureNBTKeyword.LARGE_WEAPON_TYPE, (byte) creatureGearHandler.getLargeWeapon().ordinal());
        CreatureNBT newCreatureNBT = new CreatureNBT(creatureNBTCompound);

        this.guiData.getSyncedNBT().setCreatureNBT(newCreatureNBT);
        this.guiData.getSyncedNBT().notifyUpdate();
    }
}
