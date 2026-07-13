package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.CreatureMoveSelector;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import anightdazingzoroark.prift.util.PriorityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

/**
* A creature's moves are to be stored and managed here
* */
public class CreatureMoveStorage {
    //creature builder will be used to determine the moves
    private RiftCreatureBuilder creatureType;
    //cooldowns of the moves that are used
    private final Map<String, Integer> moveCooldowns = new HashMap<>();
    //dynamically updated priority list for usable moves
    private final PriorityList<CreatureMoveSelector.MoveRule> prioritizedUsableMoves = new PriorityList<>();
    //the phase of the creature that has this
    @NotNull
    private String creaturePhase = "";
    //the name of the move that is currently being used
    @NotNull
    private String currentMove = "";
    //flag for the current usable moves to use, from usableMovesByPhase. 0 is left, 1 is right
    //this only matters in player controlling creatures
    private byte currentUsableMoves = 0;

    //-----initialization stuff starts here-----
    /**
     * to be used upon initialization
     * */
    public void setCreatureUser(RiftCreatureBuilder creatureUser) {
        this.creatureType = creatureUser;
    }

    /**
     * validate initialization
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isInitialized() {
        return this.creatureType != null;
    }
    //-----initialization stuff ends here-----

    //-----manage currently usable moves-----
    /**
     * Update list of usable moves. To be ticked on creature it is attached to
     * */
    public void updateUsableMoves(@NotNull RiftCreature creature, @Nullable EntityLivingBase target) {
        if (!this.isInitialized()) return;

        for (CreatureMoveSelector.MoveRule moveRule : this.creatureType.getMoveSelector().getMoveRules()) {
            MoveRuleBuilder moveRuleBuilder = moveRule.moveRuleBuilder();

            boolean useDueToFrustration = moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE
                    && target != null
                    && target.isEntityAlive()
                    && creature.atFrustrationThreshold()
                    && moveRuleBuilder.getUseWhenFrustrated();
            int index = useDueToFrustration ? 0 : moveRuleBuilder.getPriorityPredicate().apply(creature, target);
            //being on cooldown forcibly changes the index to -1
            if (!useDueToFrustration && this.moveCooldowns.containsKey(moveRuleBuilder.getMoveName()) && this.moveCooldowns.get(moveRuleBuilder.getMoveName()) > 0) {
                index = -1;
            }

            this.prioritizedUsableMoves.remove(moveRule);
            //positive indexes can be added
            if (index >= 0) this.prioritizedUsableMoves.add(index, moveRule);
        }
    }

    @Nullable
    public CreatureMoveSelector.MoveRule getBestMoveRuleUnmounted() {
        return this.prioritizedUsableMoves.next();
    }


    /**
     * This is to be used by creatures when on their own
     * note that phase name has no use yet since no creatures have phases yet
     * */
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

    /**
     * note that phase name has no use yet since no creatures have phases yet
     * */
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
        //put on cooldown first
        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder != null && currentMoveBuilder.getMoveCooldown() > 0) {
            this.moveCooldowns.put(this.currentMove, currentMoveBuilder.getMoveCooldown());
        }

        //now reset
        this.currentMove = "";
    }

    //-------move cooldown management-------
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
