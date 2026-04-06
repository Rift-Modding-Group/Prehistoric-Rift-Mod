package anightdazingzoroark.prift.client.ui.holder;

import anightdazingzoroark.prift.client.ui.data.CreatureGuiData;
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

    public CreatureMove applyMove(CreatureGuiData guiData) {
        if (this.moveType == SelectedMoveType.LEARNT) return guiData.getLearnedMoves().get(this.movePos);
        else if (this.moveType == SelectedMoveType.LEARNABLE) {
            if (this.movePos < 0) return null;
            return guiData.getLearnableMoves().get(this.movePos);
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

        //these are for updating move swap related sections
        private boolean canUpdateLearntMoves;
        private boolean canUpdateLearnableMoves;

        public SwapInfo() {}

        public SwapInfo(NBTTagCompound nbtTagCompound) {
            if (nbtTagCompound.hasKey("MoveOne")) {
                this.moveOne = new SelectedMoveInfo(nbtTagCompound.getCompoundTag("MoveOne"));
            }

            if (nbtTagCompound.hasKey("MoveTwo")) {
                this.moveTwo = new SelectedMoveInfo(nbtTagCompound.getCompoundTag("MoveTwo"));
            }
        }

        public void setMove(SelectedMoveInfo moveForSwap) {
            if (moveForSwap == null) return;
            if (this.canSwap()) return;

            //first step of swap
            if (this.moveOne == null && this.moveTwo == null && moveForSwap.movePos >= 0) {
                this.moveOne = moveForSwap;
            }
            //second step of swap
            else if (this.moveOne != null && this.moveTwo == null) {
                this.moveTwo = moveForSwap;
            }
        }

        public boolean canChooseNextMove() {
            return this.moveOne != null && this.moveTwo == null;
        }

        public boolean getCanUpdate(SelectedMoveType section) {
            if (section == SelectedMoveType.LEARNT) {
                boolean toReturn = this.canUpdateLearntMoves;
                this.canUpdateLearntMoves = false;
                return toReturn;
            }
            else if (section == SelectedMoveType.LEARNABLE) {
                boolean toReturn = this.canUpdateLearnableMoves;
                this.canUpdateLearnableMoves = false;
                return toReturn;
            }
            return false;
        }

        public void applySwap(CreatureGuiData data) {
            if (!this.canSwap()) return;

            boolean swapSuccessful = false;
            boolean tempCanUpdateLearntMoves = false;
            boolean tempCanUpdateLearnableMoves = false;

            //within learnt moves swap
            if (this.moveOne.moveType == SelectedMoveType.LEARNT && this.moveTwo.moveType == SelectedMoveType.LEARNT) {
                CreatureMove moveToSwap = data.getLearnedMoves().get(this.moveOne.movePos);
                data.changeLearnedMove(this.moveOne.movePos, data.getLearnedMoves().get(this.moveTwo.movePos));
                data.changeLearnedMove(this.moveTwo.movePos, moveToSwap);
                swapSuccessful = true;
                tempCanUpdateLearntMoves = true;
            }
            //learnable move x learnt move swap
            else if (this.moveOne.moveType == SelectedMoveType.LEARNABLE && this.moveTwo.moveType == SelectedMoveType.LEARNT) {
                CreatureMove moveToSwap = data.getLearnableMoves().get(this.moveOne.movePos);
                CreatureMove moveToReplace = data.getLearnedMoves().get(this.moveTwo.movePos);
                if (moveToReplace != null) {
                    data.changeLearnableMove(this.moveOne.movePos, moveToReplace);
                    data.changeLearnedMove(this.moveTwo.movePos, moveToSwap);
                }
                else {
                    data.removeLearnableMove(this.moveOne.movePos);
                    data.changeLearnedMove(this.moveTwo.movePos, moveToSwap);
                }
                swapSuccessful = true;
                tempCanUpdateLearntMoves = true;
                tempCanUpdateLearnableMoves = true;
            }
            //learnt move x learnable move swap
            else if (this.moveOne.moveType == SelectedMoveType.LEARNT && this.moveTwo.moveType == SelectedMoveType.LEARNABLE) {
                CreatureMove moveToSwap = data.getLearnedMoves().get(this.moveOne.movePos);
                if (this.moveTwo.movePos >= 0) {
                    data.changeLearnedMove(this.moveOne.movePos, data.getLearnableMoves().get(this.moveTwo.movePos));
                    data.changeLearnableMove(this.moveTwo.movePos, moveToSwap);
                }
                else {
                    data.removeLearnedMove(this.moveOne.movePos);
                    data.addLearnableMove(moveToSwap);
                }
                swapSuccessful = true;
                tempCanUpdateLearntMoves = true;
                tempCanUpdateLearnableMoves = true;
            }
            //learnable move x learnable move, no swap will happen by this point, instead, moveTwo will be cleared
            //at put in moveOne
            else if (this.moveOne.moveType == SelectedMoveType.LEARNABLE && this.moveTwo.moveType == SelectedMoveType.LEARNABLE) {
                if (this.moveOne.movePos == this.moveTwo.movePos) this.clear();
                else {
                    this.moveOne = this.moveTwo;
                    this.moveTwo = null;
                }
            }

            if (swapSuccessful) {
                this.clear();
                this.canUpdateLearntMoves = tempCanUpdateLearntMoves;
                this.canUpdateLearnableMoves = tempCanUpdateLearnableMoves;
            }
        }

        public boolean canSwap() {
            return this.moveOne != null && this.moveTwo != null;
        }

        public void clear() {
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
