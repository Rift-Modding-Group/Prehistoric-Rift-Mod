package anightdazingzoroark.prift.client.newui.holder;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.screens.synced.RiftCreatureScreen;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.nbt.NBTTagCompound;

//this helper class is for sending creature information to UIs and no less
public class SelectedCreatureInfo implements IGuiHolder<CreatureGuiData> {
    //common
    public final SelectedPosType selectedPosType;
    private final int index;
    private MenuOpenedFrom menuOpenedFrom;

    //box only
    private boolean boxIndexIsDynamic;
    private int boxIndex;
    private IntValue.Dynamic boxIndexDynamic;

    public static SelectedCreatureInfo createFromNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.isEmpty()) return null;

        //constructor stuff
        SelectedPosType selectedPosType = SelectedPosType.values()[nbtTagCompound.getByte("SelectedPosType")];
        int index = nbtTagCompound.getInteger("Index");
        SelectedCreatureInfo toReturn = new SelectedCreatureInfo(selectedPosType, index);

        //additional stuff
        toReturn.menuOpenedFrom = (nbtTagCompound.hasKey("MenuOpenedFrom") && nbtTagCompound.getInteger("MenuOpenedFrom") >= 0) ?
                MenuOpenedFrom.values()[nbtTagCompound.getInteger("MenuOpenedFrom")] : null;
        toReturn.boxIndex = nbtTagCompound.getInteger("BoxIndex");

        return toReturn;
    }

    public static SelectedCreatureInfo partySelectedInfo(int pos) {
        return new SelectedCreatureInfo(SelectedPosType.PARTY, pos);
    }

    public static SelectedCreatureInfo boxSelectedInfo(int boxIndex, int pos) {
        SelectedCreatureInfo toReturn = new SelectedCreatureInfo(SelectedPosType.BOX, pos);
        toReturn.boxIndexIsDynamic = false;
        toReturn.boxIndex = boxIndex;
        return toReturn;
    }

    public static SelectedCreatureInfo boxSelectedInfoDynamic(IntValue.Dynamic boxIndexDynamic, int pos) {
        SelectedCreatureInfo toReturn = new SelectedCreatureInfo(SelectedPosType.BOX, pos);
        toReturn.boxIndexIsDynamic = true;
        toReturn.boxIndexDynamic = boxIndexDynamic;
        return toReturn;
    }

    public static SelectedCreatureInfo boxDeployedInfo(int pos) {
        return new SelectedCreatureInfo(SelectedPosType.BOX_DEPLOYED, pos);
    }

    private SelectedCreatureInfo(SelectedPosType selectedPosType, int index) {
        this.selectedPosType = selectedPosType;
        this.index = index;
    }

    public void setMenuOpenedFrom(MenuOpenedFrom value) {
        this.menuOpenedFrom = value;
    }

    public int getIndex() {
        return this.index;
    }

    public int getBoxIndex() {
        if (this.boxIndexIsDynamic) return this.boxIndexDynamic.getIntValue();
        return this.boxIndex;
    }

    //this is mainly for use in packets
    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setByte("SelectedPosType", (byte) this.selectedPosType.ordinal());
        toReturn.setInteger("Index", this.index);
        toReturn.setInteger("MenuOpenedFrom", this.menuOpenedFrom != null ? this.menuOpenedFrom.ordinal() : -1);
        toReturn.setInteger("BoxIndex", this.boxIndexIsDynamic ? this.boxIndexDynamic.getValue() : this.boxIndex);

        return toReturn;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof SelectedCreatureInfo infoToTest)) return false;

        //normal equality check
        boolean check = infoToTest.index == this.index && infoToTest.selectedPosType == this.selectedPosType;

        //special equality check for box
        if (this.selectedPosType == SelectedPosType.BOX && infoToTest.selectedPosType == SelectedPosType.BOX) {
            if (this.boxIndexIsDynamic && infoToTest.boxIndexIsDynamic) {
                return check && this.boxIndexDynamic.getIntValue() == infoToTest.boxIndexDynamic.getIntValue();
            }
            else if (this.boxIndexIsDynamic && !infoToTest.boxIndexIsDynamic) {
                return check && this.boxIndexDynamic.getIntValue() == infoToTest.boxIndex;
            }
            else if (!this.boxIndexIsDynamic && infoToTest.boxIndexIsDynamic) {
                return check && this.boxIndex == infoToTest.boxIndexDynamic.getIntValue();
            }
            else if (!this.boxIndexIsDynamic && !infoToTest.boxIndexIsDynamic) {
                return check && this.boxIndex == infoToTest.boxIndex;
            }
        }

        return check;
    }

    @Override
    public ModularScreen createScreen(CreatureGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(RiftInitialize.MODID, mainPanel);
    }

    @Override
    public ModularPanel buildUI(CreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureScreen.buildCreatureUI(data, syncManager, settings);
    }

    public enum SelectedPosType {
        PARTY,
        BOX,
        BOX_DEPLOYED
    }

    public enum MenuOpenedFrom {
        PARTY,
        BOX
    }

    public static class SwapInfo {
        private SelectedCreatureInfo creatureOne;
        private SelectedCreatureInfo creatureTwo;

        public SwapInfo() {}

        public SwapInfo(NBTTagCompound nbtTagCompound) {
            if (nbtTagCompound.hasKey("CreatureOne")) {
                this.creatureOne = SelectedCreatureInfo.createFromNBT(nbtTagCompound.getCompoundTag("CreatureOne"));
            }

            if (nbtTagCompound.hasKey("CreatureTwo")) {
                this.creatureTwo = SelectedCreatureInfo.createFromNBT(nbtTagCompound.getCompoundTag("CreatureTwo"));
            }
        }

        public SelectedCreatureInfo getCreatureOne() {
            return this.creatureOne;
        }

        public SelectedCreatureInfo getCreatureTwo() {
            return this.creatureTwo;
        }

        public void setCreature(SelectedCreatureInfo creatureForSwap) {
            if (creatureForSwap == null) return;
            if (this.canSwap()) return;

            //first step of swap
            if (this.creatureOne == null && this.creatureTwo == null) {
                this.creatureOne = creatureForSwap;
            }
            //second step of swap
            else if (this.creatureOne != null && this.creatureTwo == null) {
                this.creatureTwo = creatureForSwap;
            }
        }

        public boolean canSwapHalfway() {
            return this.creatureOne != null && this.creatureTwo == null;
        }

        public boolean canSwap() {
            return this.creatureOne != null && this.creatureTwo != null;
        }

        public void clear() {
            this.creatureOne = null;
            this.creatureTwo = null;
        }

        public NBTTagCompound getNBT() {
            NBTTagCompound toReturn = new NBTTagCompound();

            if (this.creatureOne != null) toReturn.setTag("CreatureOne", this.creatureOne.getNBT());
            if (this.creatureTwo != null) toReturn.setTag("CreatureTwo", this.creatureTwo.getNBT());

            return toReturn;
        }
    }
}
