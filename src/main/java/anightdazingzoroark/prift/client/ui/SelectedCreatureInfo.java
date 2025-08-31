package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen.RiftCreatureBoxInfoScreen;
import anightdazingzoroark.prift.client.ui.partyScreen.RiftPartyScreen;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBoxHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

//this helper class is for sending creature information to UIs and no less
public class SelectedCreatureInfo {
    public final SelectedPosType selectedPosType;
    public final int[] pos;
    private MenuOpenedFrom menuOpenedFrom;
    private BlockPos creatureBoxOpenedFrom = new BlockPos(0, 0, 0);

    public SelectedCreatureInfo(SelectedPosType selectedPosType, int[] pos) {
        this.selectedPosType = selectedPosType;
        this.pos = pos;
    }

    public SelectedCreatureInfo(NBTTagCompound nbtTagCompound) {
        this.selectedPosType = SelectedPosType.values()[nbtTagCompound.getByte("SelectedPosType")];
        this.pos = nbtTagCompound.getIntArray("Position");
        this.creatureBoxOpenedFrom = new BlockPos(
                nbtTagCompound.getInteger("OpenedFromX"),
                nbtTagCompound.getInteger("OpenedFromY"),
                nbtTagCompound.getInteger("OpenedFromZ")
        );
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

    public void exitToLastMenu(Minecraft minecraft) {
        if (this.menuOpenedFrom == MenuOpenedFrom.PARTY) {
            RiftLibUIHelper.showUI(minecraft.player, new RiftPartyScreen(this, true));
        }
        else if (this.menuOpenedFrom == MenuOpenedFrom.BOX) {
            RiftLibUIHelper.showUI(minecraft.player, new RiftCreatureBoxInfoScreen(this.creatureBoxOpenedFrom, this, true));
        }
    }

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
        return toReturn;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof SelectedCreatureInfo) || this.creatureBoxOpenedFrom == null) return false;
        SelectedCreatureInfo infoToTest = (SelectedCreatureInfo) object;
        return infoToTest.selectedPosType == this.selectedPosType
                && Arrays.equals(infoToTest.pos, this.pos)
                && infoToTest.creatureBoxOpenedFrom.equals(this.creatureBoxOpenedFrom);
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
}
