package anightdazingzoroark.prift.client.newui.holder;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.screens.synced.RiftCreatureScreen;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBoxHelper;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

//this helper class is for sending creature information to UIs and no less
public class SelectedCreatureInfo implements IGuiHolder<CreatureGuiData> {
    public final SelectedPosType selectedPosType;
    public final int[] pos;
    private MenuOpenedFrom menuOpenedFrom;
    private BlockPos creatureBoxOpenedFrom = new BlockPos(0, 0, 0);

    public static SelectedCreatureInfo nullableSelectedCreatureInfo(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.isEmpty()) return null;
        return new SelectedCreatureInfo(nbtTagCompound);
    }

    public SelectedCreatureInfo(SelectedPosType selectedPosType, int[] pos) {
        this(selectedPosType, pos, null);
    }

    public SelectedCreatureInfo(SelectedPosType selectedPosType, int[] pos, MenuOpenedFrom menuOpenedFrom) {
        this.selectedPosType = selectedPosType;
        this.pos = pos;
        this.menuOpenedFrom = menuOpenedFrom;
    }

    public SelectedCreatureInfo(NBTTagCompound nbtTagCompound) {
        this.selectedPosType = SelectedPosType.values()[nbtTagCompound.getByte("SelectedPosType")];
        this.pos = nbtTagCompound.getIntArray("Position");
        this.creatureBoxOpenedFrom = new BlockPos(
                nbtTagCompound.getInteger("OpenedFromX"),
                nbtTagCompound.getInteger("OpenedFromY"),
                nbtTagCompound.getInteger("OpenedFromZ")
        );
        this.menuOpenedFrom = (nbtTagCompound.hasKey("MenuOpenedFrom") && nbtTagCompound.getInteger("MenuOpenedFrom") >= 0) ?
                MenuOpenedFrom.values()[nbtTagCompound.getInteger("MenuOpenedFrom")] : null;
    }

    public void setMenuOpenedFrom(MenuOpenedFrom value) {
        this.menuOpenedFrom = value;
    }

    public void setCreatureBoxOpenedFrom(BlockPos pos) {
        this.creatureBoxOpenedFrom = pos;
    }

    public BlockPos getCreatureBoxOpenedFrom() {
        return this.creatureBoxOpenedFrom;
    }

    public boolean cbNotOpenedFromZero() {
        return !this.creatureBoxOpenedFrom.equals(new BlockPos(0, 0, 0));
    }

    public MenuOpenedFrom getMenuOpenedFrom() {
        return this.menuOpenedFrom;
    }

    @Deprecated
    public CreatureNBT getCreatureNBT(EntityPlayer player) {
        if (this.selectedPosType == SelectedPosType.PARTY) return PlayerTamedCreaturesHelper.getPlayerPartyNBT(player).get(this.pos[0]);
        else if (this.selectedPosType == SelectedPosType.BOX) return PlayerTamedCreaturesHelper.getCreatureBoxStorage(player).getBoxContents(this.pos[0]).get(this.pos[1]);
        else if (this.selectedPosType == SelectedPosType.BOX_DEPLOYED) {
            TileEntity tileEntity = player.world.getTileEntity(this.getCreatureBoxOpenedFrom());
            if (!(tileEntity instanceof RiftTileEntityCreatureBox)) return new CreatureNBT();
            RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;
            RiftTileEntityCreatureBoxHelper.forceSyncCreatureBoxDeployed(player, this.getCreatureBoxOpenedFrom());
            return teCreatureBox.getDeployedCreatures().get(this.pos[0]);
        }
        return new CreatureNBT();
    }

    //this is mainly for use in packets
    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setByte("SelectedPosType", (byte) this.selectedPosType.ordinal());
        toReturn.setIntArray("Position", this.pos);
        toReturn.setInteger("OpenedFromX", this.creatureBoxOpenedFrom.getX());
        toReturn.setInteger("OpenedFromY", this.creatureBoxOpenedFrom.getY());
        toReturn.setInteger("OpenedFromZ", this.creatureBoxOpenedFrom.getZ());
        toReturn.setInteger("MenuOpenedFrom", this.menuOpenedFrom != null ? this.menuOpenedFrom.ordinal() : -1);
        return toReturn;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof SelectedCreatureInfo infoToTest) || this.creatureBoxOpenedFrom == null) return false;
        return infoToTest.selectedPosType == this.selectedPosType
                && Arrays.equals(infoToTest.pos, this.pos)
                && infoToTest.creatureBoxOpenedFrom.equals(this.creatureBoxOpenedFrom);
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
                this.creatureOne = new SelectedCreatureInfo(nbtTagCompound.getCompoundTag("CreatureOne"));
            }

            if (nbtTagCompound.hasKey("CreatureTwo")) {
                this.creatureTwo = new SelectedCreatureInfo(nbtTagCompound.getCompoundTag("CreatureTwo"));
            }
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

        public boolean applySwap(IPlayerParty playerParty) {
            if (!this.canSwap() || playerParty == null) return false;

            boolean swapSuccess = false;
            if (this.creatureOne.selectedPosType == SelectedPosType.PARTY && this.creatureTwo.selectedPosType == SelectedPosType.PARTY) {
                CreatureNBT nbtOne = playerParty.getPartyMember(this.creatureOne.pos[0]);
                CreatureNBT nbtTwo = playerParty.getPartyMember(this.creatureTwo.pos[0]);

                playerParty.setPartyMember(this.creatureOne.pos[0], nbtTwo);
                playerParty.setPartyMember(this.creatureTwo.pos[0], nbtOne);

                swapSuccess = true;
            }

            this.clear();

            return swapSuccess;
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
