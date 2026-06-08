package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.util.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    //phase name has no use yet since no creatures have phases yet
    public List<ImmutablePair<String, CreatureMoveBuilder>> getUsableMoves() {
        if (this.creaturePhase.isEmpty()) return this.creatureType.getMoves();
        else return this.creatureType.getPhaseBuilderMaps().get(this.creaturePhase).getMoves();
    }

    public CreatureMoveBuilder getMoveBuilderCurrentMove() {
        return this.getUsableMoveBuilder(this.creaturePhase, this.currentMove);
    }

    @Nullable
    public CreatureMoveBuilder getUsableMoveBuilder(String moveName) {
        return this.getUsableMoveBuilder("", moveName);
    }

    //phase name has no use yet since no creatures have phases yet
    @Nullable
    public CreatureMoveBuilder getUsableMoveBuilder(String phaseName, String moveName) {
        List<ImmutablePair<String, CreatureMoveBuilder>> moveMap;
        if (phaseName.isEmpty()) moveMap = this.creatureType.getMoves();
        else moveMap = this.creatureType.getPhaseBuilderMaps().get(phaseName).getMoves();

        for (ImmutablePair<String, CreatureMoveBuilder> moveEntry : moveMap) {
            if (!moveEntry.getKey().equals(moveName)) continue;
            return moveEntry.getValue();
        }
        return null;
    }

    //to be used by players when commanding a creature to use a move
    public List<ImmutablePair<String, CreatureMoveBuilder>> getUsableMovesByPlayer() {
        //safety net
        if (this.currentUsableMoves >= 2 || this.currentUsableMoves < 0) this.currentUsableMoves = 0;

        List<ImmutablePair<String, CreatureMoveBuilder>> movesFromCurrentPhase = this.getUsableMoves();
        if (this.currentUsableMoves == 0) {
            return movesFromCurrentPhase.subList(0, Math.min(3, movesFromCurrentPhase.size()));
        }
        else if (this.currentUsableMoves == 1) {
            return movesFromCurrentPhase.subList(3, Math.min(6, movesFromCurrentPhase.size()));
        }
        return null;
    }

    //only matters when player is controlling the creature, allows to swap which moves they
    //can use via mouse
    public void switchUsableMoves() {
        if (this.currentUsableMoves == 0 && this.getUsableMoves().size() >= 3) this.currentUsableMoves = 1;
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
