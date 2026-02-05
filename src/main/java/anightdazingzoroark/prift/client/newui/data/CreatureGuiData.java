package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class CreatureGuiData extends GuiData {
    //data type stuff
    public final DataType dataType;
    private RiftCreature creature;
    private SelectedCreatureInfo selectedCreatureInfo;

    //ui related stuff
    public int currentSelectedMoveUI = -1;
    public boolean selectedMoveFromRightUI = false;
    public boolean isMoveSwitchingUI = false;
    public SelectedMoveInfo.SwapInfo moveSwapInfoUI = new SelectedMoveInfo.SwapInfo();

    public CreatureGuiData(EntityPlayer player, RiftCreature creature) {
        super(player);
        this.creature = creature;
        this.dataType = DataType.CREATURE;
    }

    public CreatureGuiData(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        super(player);
        this.selectedCreatureInfo = selectedCreatureInfo;
        this.dataType = DataType.SELECTION;
    }

    public CreatureGuiData(EntityPlayer player, NBTTagCompound nbtTagCompound) {
        super(player);
        this.dataType = DataType.values()[nbtTagCompound.getInteger("DataType")];
        if (this.dataType == DataType.CREATURE) {
            this.creature = (RiftCreature) player.world.getEntityByID(nbtTagCompound.getInteger("CreatureId"));
        }
        else if (this.dataType == DataType.SELECTION) {
            this.selectedCreatureInfo = new SelectedCreatureInfo(nbtTagCompound.getCompoundTag("SelectionInfo"));
        }
    }

    public Object getGuiHolder() {
        if (this.dataType == DataType.CREATURE) return this.creature;
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo;
        return null;
    }

    public CreatureNBT getCreatureNBT() {
        if (this.dataType == DataType.CREATURE) return new CreatureNBT(this.creature);
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer());
        return new CreatureNBT();
    }

    public String getName(boolean includeLevel) {
        if (this.dataType == DataType.CREATURE) return this.creature.getName(includeLevel);
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getCreatureName(includeLevel);
        return "";
    }

    public int getLevel() {
        if (this.dataType == DataType.CREATURE) return this.creature.getLevel();
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getCreatureLevel();
        return -1;
    }

    public RiftCreatureType getCreatureType() {
        if (this.dataType == DataType.CREATURE) return this.creature.creatureType;
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getCreatureType();
        return null;
    }

    public float[] getHealth() {
        if (this.dataType == DataType.CREATURE) {
            return new float[]{this.creature.getHealth(), this.creature.getMaxHealth()};
        }
        else if (this.dataType == DataType.SELECTION) {
            return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getCreatureHealth();
        }
        return new float[]{0f, 0f};
    }

    public int[] getEnergy() {
        if (this.dataType == DataType.CREATURE) {
            return new int[]{this.creature.getEnergy(), this.creature.getMaxEnergy()};
        }
        else if (this.dataType == DataType.SELECTION) {
            return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getCreatureEnergy();
        }
        return new int[]{0, 0};
    }

    public int[] getXP() {
        if (this.dataType == DataType.CREATURE) {
            return new int[]{this.creature.getXP(), this.creature.getMaxXP()};
        }
        else if (this.dataType == DataType.SELECTION) {
            return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getCreatureXP();
        }
        return new int[]{0, 0};
    }

    public int getAgeInDays() {
        if (this.dataType == DataType.CREATURE) return this.creature.getAgeInDays();
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getAgeInDays();
        return 0;
    }

    public String getAcquisitionInfoString() {
        if (this.dataType == DataType.CREATURE) return this.creature.getAcquisitionInfo().acquisitionInfoString();
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getAcquisitionInfoString();
        return "";
    }

    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        toReturn.setInteger("DataType", this.dataType.ordinal());
        if (this.dataType == DataType.CREATURE) toReturn.setInteger("CreatureId", this.creature.getEntityId());
        else if (this.dataType == DataType.SELECTION) toReturn.setTag("SelectionInfo", this.selectedCreatureInfo.getNBT());

        return toReturn;
    }

    //-----move related stuff starts here-----
    public FixedSizeList<CreatureMove> getLearnedMoves() {
        if (this.dataType == DataType.CREATURE) return this.creature.getLearnedMoves();
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getLearnedMoves();
        return new FixedSizeList<>(3);
    }

    public void changeLearnedMove(int pos, CreatureMove move) {
        if (this.dataType == DataType.CREATURE) this.creature.changeLearnedMove(pos, move);
        else if (this.dataType == DataType.SELECTION) this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).changeLearnedMove(pos, move);
    }

    public void removeLearnedMove(int pos) {
        if (this.dataType == DataType.CREATURE) this.creature.removeLearnedMove(pos);
        else if (this.dataType == DataType.SELECTION) this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).removeLearnedMove(pos);
    }

    public List<CreatureMove> getLearnableMoves() {
        if (this.dataType == DataType.CREATURE) return this.creature.getLearnableMoves();
        else if (this.dataType == DataType.SELECTION) return this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).getLearnableMoves();
        return new ArrayList<>();
    }

    public void changeLearnableMove(int pos, CreatureMove move) {
        if (this.dataType == DataType.CREATURE) this.creature.changeLearnableMove(pos, move);
        else if (this.dataType == DataType.SELECTION) this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).changeLearnableMove(pos, move);
    }

    public void addLearnableMove(CreatureMove move) {
        if (this.dataType == DataType.CREATURE) this.creature.addLearnableMove(move);
        else if (this.dataType == DataType.SELECTION) this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).addLearnableMove(move);
    }

    public void removeLearnableMove(int pos) {
        if (this.dataType == DataType.CREATURE) this.creature.removeLearnableMove(pos);
        else if (this.dataType == DataType.SELECTION) this.selectedCreatureInfo.getCreatureNBT(this.getPlayer()).removeLearnableMove(pos);
    }
    //-----move related stuff ends here-----

    public enum DataType {
        CREATURE,
        SELECTION
    }
}
