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
            if (this.canSwap()) return;

            //first step of swap
            if (this.moveOne == null && this.moveTwo == null && moveForSwap.movePos >= 0) {
                this.moveOne = moveForSwap;
            }
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

            boolean swapSuccessful = false;
            //within learnt moves swap
            if (this.moveOne.moveType == SelectedMoveType.LEARNT && this.moveTwo.moveType == SelectedMoveType.LEARNT) {
                CreatureMove moveToSwap = creature.newGetLearnedMoves().get(this.moveOne.movePos);
                creature.newChangeLearnedMove(this.moveOne.movePos, creature.newGetLearnedMoves().get(this.moveTwo.movePos));
                creature.newChangeLearnedMove(this.moveTwo.movePos, moveToSwap);
                swapSuccessful = true;
            }
            //learnable move x learnt move swap
            else if (this.moveOne.moveType == SelectedMoveType.LEARNABLE && this.moveTwo.moveType == SelectedMoveType.LEARNT) {
                CreatureMove moveToSwap = creature.getLearnableMoves().get(this.moveOne.movePos);
                CreatureMove moveToReplace = creature.newGetLearnedMoves().get(this.moveTwo.movePos);
                if (moveToReplace != null) {
                    creature.changeLearnableMove(this.moveOne.movePos, moveToReplace);
                    creature.newChangeLearnedMove(this.moveTwo.movePos, moveToSwap);
                }
                else {
                    creature.removeLearnableMove(this.moveOne.movePos);
                    creature.newChangeLearnedMove(this.moveTwo.movePos, moveToSwap);
                }
                swapSuccessful = true;
            }
            //learnt move x learnable move swap
            else if (this.moveOne.moveType == SelectedMoveType.LEARNT && this.moveTwo.moveType == SelectedMoveType.LEARNABLE) {
                CreatureMove moveToSwap = creature.newGetLearnedMoves().get(this.moveOne.movePos);
                if (this.moveTwo.movePos >= 0) {
                    creature.newChangeLearnedMove(this.moveOne.movePos, creature.getLearnableMoves().get(this.moveTwo.movePos));
                    creature.changeLearnableMove(this.moveTwo.movePos, moveToSwap);
                }
                else {
                    creature.newRemoveLearnedMove(this.moveOne.movePos);
                    creature.addLearnableMove(moveToSwap);
                }
                swapSuccessful = true;
            }

            if (swapSuccessful) {
                this.moveOne = null;
                this.moveTwo = null;
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
