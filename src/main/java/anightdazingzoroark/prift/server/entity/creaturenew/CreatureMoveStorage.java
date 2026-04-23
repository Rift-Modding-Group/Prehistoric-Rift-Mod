package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
* A creature's moves are to be stored here
* */
public class CreatureMoveStorage {
    private static final int usableMoveCount = 3;
    //list down all moves that the creature can learn
    private final List<LearnableMoveHolder> allLearnableMoves = new ArrayList<>();
    //list down usable moves based on the creature's phase
    //note that a creature can have one or two sets of usable moves
    private final Map<String, ImmutablePair<FixedSizeList<String>, FixedSizeList<String>>> usableMovesByPhase = new HashMap<>();

    //-----learnable move related stuff-----
    public void initLearnableMoves(LearnableMoveHolder... moveHolders) {
        for (LearnableMoveHolder moveHolderToAdd : moveHolders) {
            //safety
            if (moveHolderToAdd == null) continue;
            //check invalid move name
            else if (!CreatureMoveRegistry.moveExists(moveHolderToAdd.moveName)) {
                RiftInitialize.logger.warn("No builder exists for {}! Skipping...", moveHolderToAdd.moveName);
                continue;
            }
            this.allLearnableMoves.add(moveHolderToAdd);
        }
    }

    public List<LearnableMoveHolder> getLearnableMoves() {
        return Collections.unmodifiableList(this.allLearnableMoves);
    }

    //check if the move can be used at a certain phase
    public boolean moveIsValidForPhase(String moveName, String phaseName) {
        if (this.allLearnableMoves.isEmpty()) {
            RiftInitialize.logger.warn("No learnable moves list has been defined...");
            return false;
        }
        for (LearnableMoveHolder moveHolder : this.allLearnableMoves) {
            if (!moveHolder.moveName.equals(moveName)) continue;
            if (moveHolder.associatedPhases == null || moveHolder.associatedPhases.length == 0) {
                return phaseName.isEmpty();
            }
            List<String> phaseList = Arrays.asList(moveHolder.associatedPhases);
            return phaseList.contains(phaseName);
        }
        return false;
    }

    //get animation name for a move
    public String getAnimationNameForMove(String moveName) {
        if (this.allLearnableMoves.isEmpty()) {
            RiftInitialize.logger.warn("No learnable moves list has been defined...");
            return "";
        }
        for (LearnableMoveHolder moveHolder : this.allLearnableMoves) {
            if (!moveHolder.moveName.equals(moveName)) continue;
            return moveHolder.moveAnimationName();
        }
        return "";
    }

    //-----usable move related stuff-----
    //these are the usable moves that a creature will always spawn with
    public void initUsableMovesPerPhase(Map<String, List<String>> movesPerPhase) {
        for (Map.Entry<String, List<String>> movesPerPhaseEntry : movesPerPhase.entrySet()) {
            String phaseName = movesPerPhaseEntry.getKey();
            List<String> moves = movesPerPhaseEntry.getValue();

            //check list size first
            if (moves.size() > 6) {
                RiftInitialize.logger.warn("Too many learnable moves! Skipping...");
                return;
            }

            //define movesets
            FixedSizeList<String> movesetOne = new FixedSizeList<>(usableMoveCount);
            FixedSizeList<String> movesetTwo = new FixedSizeList<>(usableMoveCount);

            //add from moves to the movesets
            for (int index = 0; index < moves.size(); index++) {
                String moveToAdd = moves.get(index);
                if (!CreatureMoveRegistry.moveExists(moveToAdd)) {
                    RiftInitialize.logger.warn("No builder exists for {}! Skipping...", moveToAdd);
                    continue;
                }
                else if (!this.moveIsValidForPhase(moveToAdd, phaseName)) {
                    RiftInitialize.logger.warn("{} is not learnable by creature! Skipping...", moveToAdd);
                    continue;
                }

                //add to the movesets
                int indexToAddTo = index % usableMoveCount;
                if (index / usableMoveCount == 0) {
                    movesetOne.set(indexToAddTo, moveToAdd);
                }
                else if (index / usableMoveCount == 1) {
                    movesetTwo.set(indexToAddTo, moveToAdd);
                }
            }

            //add the init moveset
            this.usableMovesByPhase.put(phaseName, new ImmutablePair<>(movesetOne, movesetTwo));
        }
    }

    //get usable moves
    public ImmutablePair<FixedSizeList<String>, FixedSizeList<String>> getUsableMovesByPhase(String phase) {
        return this.usableMovesByPhase.get(phase);
    }

    //-----utility method to test if this storage is valid-----
    public boolean isValid() {
        return !this.allLearnableMoves.isEmpty() && !this.usableMovesByPhase.isEmpty();
    }

    //-----for nbt related stuff-----
    public NBTTagCompound getAsNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //write learnable moves
        NBTTagList learnableMovesTagList = new NBTTagList();
        for (LearnableMoveHolder learnableMoveHolder : this.allLearnableMoves) {
            NBTTagCompound learnableMoveNBT = new NBTTagCompound();

            learnableMoveNBT.setString("Move", learnableMoveHolder.moveName());
            learnableMoveNBT.setString("MoveAnimName", learnableMoveHolder.moveAnimationName());

            NBTTagList validPhasesNBTList = new NBTTagList();
            for (String phase : learnableMoveHolder.associatedPhases()) {
                NBTTagCompound phaseNBT = new NBTTagCompound();
                phaseNBT.setString("Phase", phase);
                validPhasesNBTList.appendTag(phaseNBT);
            }
            learnableMoveNBT.setTag("MovePhases", validPhasesNBTList);
            learnableMovesTagList.appendTag(learnableMoveNBT);
        }
        toReturn.setTag("LearnableMoves", learnableMovesTagList);

        //write usable moves
        NBTTagList usableMovesTagList = new NBTTagList();
        for (Map.Entry<String, ImmutablePair<FixedSizeList<String>, FixedSizeList<String>>> movesPerPhaseEntry : this.usableMovesByPhase.entrySet()) {
            NBTTagCompound phaseNBT = new NBTTagCompound();

            //write phase name nbt
            phaseNBT.setString("Phase", movesPerPhaseEntry.getKey());

            //write moveset one nbt
            FixedSizeList<String> movesetOne = movesPerPhaseEntry.getValue().getLeft();
            NBTTagList movesetOneNBTList = new NBTTagList();
            for (int index = 0; index < movesetOne.size(); index++) {
                String move = movesetOne.get(index);
                if (move == null || move.isEmpty()) continue;

                NBTTagCompound moveAsNBT = new NBTTagCompound();
                moveAsNBT.setInteger("Index", index);
                moveAsNBT.setString("Move", move);
                movesetOneNBTList.appendTag(moveAsNBT);
            }
            phaseNBT.setTag("MovesetOne", movesetOneNBTList);

            //write moveset two nbt
            FixedSizeList<String> movesetTwo = movesPerPhaseEntry.getValue().getRight();
            NBTTagList movesetTwoNBTList = new NBTTagList();
            for (int index = 0; index < movesetTwo.size(); index++) {
                String move = movesetTwo.get(index);
                if (move == null || move.isEmpty()) continue;

                NBTTagCompound moveAsNBT = new NBTTagCompound();
                moveAsNBT.setInteger("Index", index);
                moveAsNBT.setString("Move", move);
                movesetTwoNBTList.appendTag(moveAsNBT);
            }
            phaseNBT.setTag("MovesetTwo", movesetTwoNBTList);

            usableMovesTagList.appendTag(phaseNBT);
        }
        toReturn.setTag("UsableMoves", usableMovesTagList);

        //final append
        return toReturn;
    }

    public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
        this.allLearnableMoves.clear();
        this.usableMovesByPhase.clear();

        //parse learnable moves
        NBTTagList learnableMovesTagList = nbtTagCompound.getTagList("LearnableMoves", 10);
        for (int index = 0; index < learnableMovesTagList.tagCount(); index++) {
            NBTTagCompound learnableMoveNBT = learnableMovesTagList.getCompoundTagAt(index);
            String moveName = learnableMoveNBT.getString("Move");
            String moveAnimName = learnableMoveNBT.getString("MoveAnimName");

            NBTTagList validPhasesNBTList = learnableMoveNBT.getTagList("MovePhases", 10);
            String[] validPhases = new String[validPhasesNBTList.tagCount()];
            for (int phaseIndex = 0; phaseIndex < validPhasesNBTList.tagCount(); phaseIndex++) {
                validPhases[phaseIndex] = validPhasesNBTList.getCompoundTagAt(phaseIndex).getString("Phase");
            }

            if (!CreatureMoveRegistry.moveExists(moveName)) {
                RiftInitialize.logger.warn("No builder exists for {}! Skipping...", moveName);
                continue;
            }

            this.allLearnableMoves.add(new LearnableMoveHolder(moveName, moveAnimName, validPhases));
        }

        //parse usable moves
        NBTTagList usableMovesTagList = nbtTagCompound.getTagList("UsableMoves", 10);
        for (int index = 0; index < usableMovesTagList.tagCount(); index++) {
            NBTTagCompound phaseNBT = usableMovesTagList.getCompoundTagAt(index);
            String phaseName = phaseNBT.getString("Phase");

            FixedSizeList<String> movesetOne = new FixedSizeList<>(usableMoveCount);
            FixedSizeList<String> movesetTwo = new FixedSizeList<>(usableMoveCount);

            NBTTagList movesetOneNBTList = phaseNBT.getTagList("MovesetOne", 10);
            for (int moveIndex = 0; moveIndex < movesetOneNBTList.tagCount(); moveIndex++) {
                NBTTagCompound moveAsNBT = movesetOneNBTList.getCompoundTagAt(moveIndex);
                int slot = moveAsNBT.getInteger("Index");
                String moveName = moveAsNBT.getString("Move");

                if (slot < 0 || slot >= usableMoveCount) continue;
                if (!CreatureMoveRegistry.moveExists(moveName)) {
                    RiftInitialize.logger.warn("No builder exists for {}! Skipping...", moveName);
                    continue;
                }
                if (!this.moveIsValidForPhase(moveName, phaseName)) {
                    RiftInitialize.logger.warn("{} is not learnable by creature! Skipping...", moveName);
                    continue;
                }

                movesetOne.set(slot, moveName);
            }

            NBTTagList movesetTwoNBTList = phaseNBT.getTagList("MovesetTwo", 10);
            for (int moveIndex = 0; moveIndex < movesetTwoNBTList.tagCount(); moveIndex++) {
                NBTTagCompound moveAsNBT = movesetTwoNBTList.getCompoundTagAt(moveIndex);
                int slot = moveAsNBT.getInteger("Index");
                String moveName = moveAsNBT.getString("Move");

                if (slot < 0 || slot >= usableMoveCount) continue;
                if (!CreatureMoveRegistry.moveExists(moveName)) {
                    RiftInitialize.logger.warn("No builder exists for {}! Skipping...", moveName);
                    continue;
                }
                if (!this.moveIsValidForPhase(moveName, phaseName)) {
                    RiftInitialize.logger.warn("{} is not learnable by creature! Skipping...", moveName);
                    continue;
                }

                movesetTwo.set(slot, moveName);
            }

            this.usableMovesByPhase.put(phaseName, new ImmutablePair<>(movesetOne, movesetTwo));
        }
    }

    //helper class for adding moves, associated animations, and associated phases
    //note that if associatedPhases is omitted, that means it applies to main phase
    public record LearnableMoveHolder(String moveName, String moveAnimationName, String... associatedPhases) {}
}
