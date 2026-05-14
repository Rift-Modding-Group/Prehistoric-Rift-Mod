package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.IndexedMap;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
* A creature's moves are to be stored here
* */
public class CreatureMoveStorage {
    public static final int usableMoveCount = 6;
    //creature builder will be used to determine the moves
    private RiftCreatureBuilder creatureType;
    //cooldowns of the moves that are used
    private final Map<String, Integer> moveCooldowns = new HashMap<>();
    //the phase of the creature that has this
    @NotNull
    private String creaturePhase = "";
    //the name of the move that is currently being used
    @NotNull
    private String currentMove = "";
    //flag for the current usable moves to use, from usableMovesByPhase. 0 is left, 1 is right
    //this only matters in player controlling creatures
    private byte currentUsableMoves = 0;

    //to be used upon initialization
    public void setCreatureUser(RiftCreatureBuilder creatureUser) {
        this.creatureType = creatureUser;
    }

    //validate initialization
    public boolean isInitialized() {
        return this.creatureType != null;
    }

    //to be used by creatures when on their own
    public FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>> getUsableMoves() {
        return this.creatureType.getUsableMoves().get(this.creaturePhase);
    }

    public CreatureMoveBuilder getMoveBuilderCurrentMove() {
        return this.getUsableMoveBuilder(this.creaturePhase, this.currentMove);
    }

    public CreatureMoveBuilder getUsableMoveBuilder(String moveName) {
        return this.getUsableMoveBuilder("", moveName);
    }

    public CreatureMoveBuilder getUsableMoveBuilder(String phaseName, String moveName) {
        for (Map.Entry<String, FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>>> entry : this.creatureType.getUsableMoves().entrySet()) {
            if (!entry.getKey().equals(phaseName)) continue;

            for (int index = 0; index < entry.getValue().size(); index++) {
                ImmutablePair<String, CreatureMoveBuilder> moveBuilderPair = entry.getValue().get(index);
                if (moveBuilderPair.getLeft().equals(moveName)) return moveBuilderPair.getRight();
            }
        }
        return null;
    }

    //to be used by players when commanding a creature to use a move
    public FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>> getUsableMovesByPlayer() {
        //safety net
        if (this.currentUsableMoves >= 2 || this.currentUsableMoves < 0) this.currentUsableMoves = 0;

        FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>> movesFromCurrentPhase = this.creatureType.getUsableMoves().get(this.creaturePhase);
        if (this.currentUsableMoves == 0) {
            return movesFromCurrentPhase.sublist(0, 3);
        }
        else if (this.currentUsableMoves == 1) {
            return movesFromCurrentPhase.sublist(3, 6);
        }
        return null;
    }

    //only matters when player is controlling the creature, allows to swap which moves they
    //can use via mouse
    public void switchUsableMoves() {
        if (this.currentUsableMoves <= 0) this.currentUsableMoves = 1;
        else this.currentUsableMoves = 0;
    }

    //-----currently used move management-----
    @NotNull
    public String getCurrentMove() {
        return this.currentMove;
    }

    public void setCurrentMove(@NotNull String moveName) {
        this.currentMove = moveName;
    }

    public void resetCurrentMove() {
        this.currentMove = "";
    }

    //-------move cooldown management-------
    public void putMoveOnCooldown(String moveName, int cooldownToSet) {
        this.moveCooldowns.put(moveName, cooldownToSet);
    }

    public int moveCurrentCooldown(String moveName) {
        if (!this.moveCooldowns.containsKey(moveName)) return 0;
        return this.moveCooldowns.get(moveName);
    }

    public void tickCooldowns() {
        List<String> movesToRemoveFromCooldown = new ArrayList<>();
        for (Map.Entry<String, Integer> moveCooldownDef : this.moveCooldowns.entrySet()) {
            int tickedCooldownVal = moveCooldownDef.getValue() - 1;

            if (tickedCooldownVal <= 0) movesToRemoveFromCooldown.add(moveCooldownDef.getKey());
            else this.moveCooldowns.put(moveCooldownDef.getKey(), tickedCooldownVal);
        }

        for (String moveToRemove : movesToRemoveFromCooldown) this.moveCooldowns.remove(moveToRemove);
    }

    //-------for nbt related stuff-------
    public NBTTagCompound getAsNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //-----for creature move user-----
        toReturn.setString("MoveUser", this.creatureType.getName());

        //-----for currently used move-----
        toReturn.setString("CurrentMove", this.currentMove);

        //-----for move cooldowns-----

        //final append
        return toReturn;
    }

    public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
        this.moveCooldowns.clear();

        //-----for creature move user-----
        this.creatureType = RiftCreatureRegistry.getCreatureBuilder(nbtTagCompound.getString("MoveUser"));

        //-----for currently used move-----
        this.currentMove = nbtTagCompound.getString("CurrentMove");

        //-----for move cooldowns-----
    }
}
