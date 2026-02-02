package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.movesScreen.RiftMovesScreen;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
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

    public static class SwapInfo {
        private SelectedMoveInfo moveOne;
        private SelectedMoveInfo moveTwo;

        public SwapInfo() {}

        public SwapInfo(NBTTagCompound nbtTagCompound) {
            if (nbtTagCompound == null) return;

            if (nbtTagCompound.hasKey("MoveOne")) {
                this.moveOne = new SelectedMoveInfo(nbtTagCompound.getCompoundTag("MoveOne"));
            }

            if (nbtTagCompound.hasKey("MoveTwo")) {
                this.moveTwo = new SelectedMoveInfo(nbtTagCompound.getCompoundTag("MoveTwo"));
            }
        }

        public void setMove(SelectedMoveInfo moveForSwap) {
            if (moveForSwap == null) return;
            if (this.moveOne != null && this.moveTwo != null) return;

            //first step of swap
            if (this.moveOne == null && this.moveTwo == null) this.moveOne = moveForSwap;
            //second step of swap
            else if (this.moveOne != null && this.moveTwo == null) {
                //if moveForSwap and moveOne are LEARNABLE, just override moveOne
                if (this.moveOne.moveType == SelectedMoveType.LEARNABLE
                        && moveForSwap.moveType == SelectedMoveType.LEARNABLE
                ) this.moveOne = moveForSwap;
                //otherwise, moveTwo is to be set
                else this.moveTwo = moveForSwap;
            }
        }

        public void applySwap(RiftCreature creature) {
            if (!this.canSwap()) return;

            if (this.moveOne.moveType == SelectedMoveType.LEARNT && this.moveTwo.moveType == SelectedMoveType.LEARNT) {
                CreatureMove moveToSwap = creature.getLearnedMoves().get(this.moveOne.movePos);
                creature.changeLearnedMove(this.moveOne.movePos, creature.getLearnedMoves().get(this.moveTwo.movePos));
                creature.changeLearnedMove(this.moveTwo.movePos, moveToSwap);
            }
        }

        public boolean canSwap() {
            return this.moveOne != null && this.moveTwo != null;
        }

        public void setMoveOne(SelectedMoveInfo moveOne) {
            this.moveOne = moveOne;
        }

        public SelectedMoveInfo getMoveOne() {
            return this.moveOne;
        }

        public void setMoveTwo(SelectedMoveInfo moveTwo) {
            this.moveTwo = moveTwo;
        }

        public SelectedMoveInfo getMoveTwo() {
            return this.moveTwo;
        }

        public void reset() {
            this.moveOne = null;
            this.moveTwo = null;
        }

        //this is mainly for use in packets
        public NBTTagCompound getNBT() {
            NBTTagCompound toReturn = new NBTTagCompound();

            //for move one
            if (this.moveOne != null) toReturn.setTag("MoveOne", this.moveOne.getNBT());

            //for move two
            if (this.moveTwo != null) toReturn.setTag("MoveTwo", this.moveTwo.getNBT());

            return toReturn;
        }
    }
}
