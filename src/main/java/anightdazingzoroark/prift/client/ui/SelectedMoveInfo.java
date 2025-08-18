package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.movesScreen.RiftMovesScreen;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.nbt.NBTTagCompound;

//this helper class is for sending move information to UIs and
//to some helper functions
public class SelectedMoveInfo {
    public final SelectedMoveType moveType;
    public final int movePos;

    public SelectedMoveInfo(SelectedMoveType moveType, int movePos) {
        this.moveType = moveType;
        this.movePos = movePos;
    }

    public SelectedMoveInfo(NBTTagCompound nbtTagCompound) {
        this.moveType = SelectedMoveType.values()[nbtTagCompound.getByte("SelectedMoveType")];
        this.movePos = nbtTagCompound.getInteger("Position");
    }

    public CreatureMove getMoveUsingNBT(CreatureNBT creatureNBT) {
        if (this.moveType == SelectedMoveType.LEARNT) {
            return creatureNBT.getMovesList().get(this.movePos);
        }
        else if (this.moveType == SelectedMoveType.LEARNABLE) {
            return creatureNBT.getLearnableMovesList().get(this.movePos);
        }
        return null;
    }

    //this is mainly for use in packets
    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setByte("SelectedMoveType", (byte) this.moveType.ordinal());
        toReturn.setInteger("Position", this.movePos);
        return toReturn;
    }

    public enum SelectedMoveType {
        LEARNT,
        LEARNABLE
    }
}
