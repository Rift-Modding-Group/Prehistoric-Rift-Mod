package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.CreatureMoveSelectorBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import anightdazingzoroark.prift.util.PriorityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* A creature's moves are to be stored and managed here
* */
public class CreatureMoveStorage {
    //creature builder will be used to determine the moves
    private RiftCreatureBuilder creatureType;
    //cooldowns of the moves that are used
    private final Map<String, Integer> moveCooldowns = new HashMap<>();
    //dynamically updated priority list for usable moves
    private final PriorityList<CreatureMoveSelectorBuilder.MoveRule> prioritizedUsableMoves = new PriorityList<>();
    //the phase of the creature that has this
    @NotNull
    private String creaturePhase = "";
    //the name of the move that is currently being used
    @NotNull
    private String currentMove = "";
    @NotNull
    private MoveUsePhase currentMovePhase = MoveUsePhase.NONE;
    private int currentMoveTicks;
    private int currentMoveChargeUpTicks;
    private int currentMoveBuildup;
    private boolean currentMoveHitEffectFired;
    private boolean currentMoveUseEndEffectFired;
    private boolean currentMoveEndEffectFired;
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

        for (CreatureMoveSelectorBuilder.MoveRule moveRule : this.creatureType.getMoveSelector().getMoveRules()) {
            MoveRuleBuilder moveRuleBuilder = moveRule.moveRuleBuilder();

            boolean useDueToFrustration = moveRule.moveResult() == MoveResult.USE_MOVE
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
    public ImmutablePair<CreatureMoveSelectorBuilder.MoveRule, Integer> getBestMoveRuleUnmounted() {
        return this.prioritizedUsableMoves.nextWithPriority();
    }


    /**
     * This is to be used by creatures when on their own
     * note that phase name has no use yet since no creatures have phases yet
     * */
    public List<ImmutablePair<String, CreatureMoveBuilder>> getUsableMoves() {
        if (!this.isInitialized()) return Collections.emptyList();
        if (this.creaturePhase.isEmpty()) return this.creatureType.getMoves();

        CreaturePhaseBuilder phaseBuilder = this.creatureType.getPhaseBuilderMaps().get(this.creaturePhase);
        return phaseBuilder == null ? Collections.emptyList() : phaseBuilder.getMoves();
    }

    @Nullable
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
        if (!this.isInitialized()) return null;

        List<ImmutablePair<String, CreatureMoveBuilder>> moveMap;
        if (phaseName.isEmpty()) moveMap = this.creatureType.getMoves();
        else {
            CreaturePhaseBuilder phaseBuilder = this.creatureType.getPhaseBuilderMaps().get(phaseName);
            if (phaseBuilder == null) return null;
            moveMap = phaseBuilder.getMoves();
        }

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
        return Collections.emptyList();
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

    @NotNull
    public MoveUsePhase getCurrentMovePhase() {
        return this.currentMovePhase;
    }

    public int getCurrentMoveTicks() {
        return this.currentMoveTicks;
    }

    public int getCurrentMoveChargeUpTicks() {
        return this.currentMoveChargeUpTicks;
    }

    public int getCurrentMoveBuildup() {
        return this.currentMoveBuildup;
    }

    public boolean currentMoveMatches(@NotNull String moveName) {
        return this.currentMove.equals(moveName);
    }

    public boolean currentMoveMatches(@NotNull String moveName, @NotNull MoveUsePhase phase) {
        return this.currentMoveMatches(moveName) && this.currentMovePhase == phase;
    }

    public boolean currentMovePastWindup(@NotNull String moveName) {
        return this.currentMoveMatches(moveName) && this.currentMovePhase != MoveUsePhase.WINDUP && this.currentMovePhase != MoveUsePhase.NONE;
    }

    public void setCurrentMove(@NotNull String moveName) {
        if (moveName.isEmpty()) {
            this.clearCurrentMove();
            return;
        }

        this.currentMove = moveName;
        this.currentMoveTicks = 0;
        this.currentMoveChargeUpTicks = 0;
        this.currentMoveBuildup = 0;
        this.currentMoveHitEffectFired = false;
        this.currentMoveUseEndEffectFired = false;
        this.currentMoveEndEffectFired = false;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder == null ? null : currentMoveBuilder.getMoveChargeupBuilder();
        this.currentMovePhase = chargeupBuilder == null ? MoveUsePhase.ACTIVE : MoveUsePhase.WINDUP;
    }

    public void tickCurrentMove(@NotNull RiftCreature creature) {
        if (this.currentMove.isEmpty()) return;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) {
            this.clearCurrentMove();
            return;
        }

        this.currentMoveTicks++;

        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
        if (chargeupBuilder == null) return;

        if (chargeupBuilder.getChargeUpThenRelease()) {
            if (this.currentMovePhase == MoveUsePhase.WINDUP) {
                this.addCurrentMoveBuildup(creature, chargeupBuilder);
                if (this.currentMoveBuildup >= chargeupBuilder.getMaxChargeUp()) {
                    this.requestCurrentMoveRelease();
                }
            }
        }
        else if (chargeupBuilder.getChargeUpWhileUse()) {
            if (this.currentMovePhase == MoveUsePhase.RELEASING) {
                this.addCurrentMoveBuildup(creature, chargeupBuilder);
                if (this.currentMoveBuildup >= chargeupBuilder.getMaxChargeUp()) {
                    this.finishCurrentMoveUse();
                }
            }
            else if (this.currentMovePhase == MoveUsePhase.WINDUP && this.currentMoveTicks >= chargeupBuilder.getMaxChargeUp()) {
                this.finishCurrentMoveUse();
            }
        }
    }

    public boolean shouldCancelCurrentMoveForMissingTarget(@NotNull RiftCreature creature) {
        if (this.currentMove.isEmpty()) return false;
        if (this.currentMovePhase == MoveUsePhase.FINISHING) return false;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) return true;
        if (!currentMoveBuilder.getRequireFindTargetToUse()) return false;

        EntityLivingBase target = creature.getAttackTarget();
        return target == null || !target.isEntityAlive();
    }

    public void requestCurrentMoveRelease() {
        if (this.currentMovePhase == MoveUsePhase.WINDUP) this.currentMovePhase = MoveUsePhase.RELEASING;
    }

    public void finishCurrentMoveUse() {
        if (!this.currentMove.isEmpty()) this.currentMovePhase = MoveUsePhase.FINISHING;
    }

    public boolean hasCurrentMoveUseEndEffectFired() {
        return this.currentMoveUseEndEffectFired;
    }

    public void markCurrentMoveUseEndEffectFired() {
        this.currentMoveUseEndEffectFired = true;
    }

    public boolean hasCurrentMoveEndEffectFired() {
        return this.currentMoveEndEffectFired;
    }

    public void markCurrentMoveEndEffectFired() {
        this.currentMoveEndEffectFired = true;
    }

    public boolean canRunCurrentMoveHitEffect() {
        if (this.currentMove.isEmpty()) return false;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) return false;

        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
        if (chargeupBuilder == null) return true;
        if (this.currentMoveHitEffectFired) return false;

        if (chargeupBuilder.getChargeUpThenRelease()) {
            return this.currentMovePhase == MoveUsePhase.RELEASING || this.currentMovePhase == MoveUsePhase.ACTIVE;
        }
        else if (chargeupBuilder.getChargeUpWhileUse()) {
            return this.currentMovePhase == MoveUsePhase.WINDUP || this.currentMovePhase == MoveUsePhase.RELEASING;
        }
        return true;
    }

    public void markCurrentMoveHitEffectFired() {
        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder == null ? null : currentMoveBuilder.getMoveChargeupBuilder();

        if (chargeupBuilder != null) this.currentMoveHitEffectFired = true;
        if (chargeupBuilder != null && chargeupBuilder.getChargeUpWhileUse() && this.currentMovePhase == MoveUsePhase.WINDUP) {
            this.currentMovePhase = MoveUsePhase.RELEASING;
        }
        else if (chargeupBuilder != null && chargeupBuilder.getChargeUpThenRelease() && this.currentMovePhase == MoveUsePhase.RELEASING) {
            this.currentMovePhase = MoveUsePhase.ACTIVE;
        }
    }

    public boolean canCurrentMoveAnimationExit() {
        if (this.currentMove.isEmpty()) return true;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) return true;

        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
        if (chargeupBuilder == null) return true;

        if (chargeupBuilder.getChargeUpThenRelease()) {
            return this.currentMovePhase == MoveUsePhase.RELEASING
                    || this.currentMovePhase == MoveUsePhase.ACTIVE
                    || this.currentMovePhase == MoveUsePhase.FINISHING;
        }
        else if (chargeupBuilder.getChargeUpWhileUse()) {
            return this.currentMovePhase == MoveUsePhase.FINISHING;
        }
        return true;
    }

    public void resetCurrentMove(@Nullable RiftCreature creature) {
        //put on cooldown first
        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder != null) {
            int cooldown = this.calculateCurrentMoveCooldown(creature, currentMoveBuilder);
            if (cooldown > 0) this.moveCooldowns.put(this.currentMove, cooldown);
        }

        //now reset
        this.clearCurrentMove();
    }

    public void resetCurrentMove() {
        this.resetCurrentMove(null);
    }

    private void addCurrentMoveBuildup(@NotNull RiftCreature creature, @NotNull CreatureMoveChargeupBuilder chargeupBuilder) {
        Integer buildupToAdd = chargeupBuilder.getBuildup().apply(creature);
        int resolvedBuildupToAdd = buildupToAdd == null ? 0 : Math.max(0, buildupToAdd);
        this.currentMoveChargeUpTicks++;
        this.currentMoveBuildup = Math.min(chargeupBuilder.getMaxChargeUp(), this.currentMoveBuildup + resolvedBuildupToAdd);
    }

    private int calculateCurrentMoveCooldown(@Nullable RiftCreature creature, @NotNull CreatureMoveBuilder currentMoveBuilder) {
        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
        if (creature != null && chargeupBuilder != null) {
            Double cooldownMultiplier = chargeupBuilder.getCooldownMultiplier().apply(creature);
            double resolvedCooldownMultiplier = cooldownMultiplier == null ? 0D : Math.max(0D, cooldownMultiplier);
            return Math.max(0, (int) Math.ceil(this.currentMoveChargeUpTicks * resolvedCooldownMultiplier));
        }
        return Math.max(0, currentMoveBuilder.getMoveCooldown());
    }

    private void clearCurrentMove() {
        this.currentMove = "";
        this.currentMovePhase = MoveUsePhase.NONE;
        this.currentMoveTicks = 0;
        this.currentMoveChargeUpTicks = 0;
        this.currentMoveBuildup = 0;
        this.currentMoveHitEffectFired = false;
        this.currentMoveUseEndEffectFired = false;
        this.currentMoveEndEffectFired = false;
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
        toReturn.setString("MoveUser", this.creatureType == null ? "" : this.creatureType.getName());

        //-----for currently used move-----
        toReturn.setString("CurrentMove", this.currentMove);
        toReturn.setString("CurrentMovePhase", this.currentMovePhase.name());
        toReturn.setInteger("CurrentMoveTicks", this.currentMoveTicks);
        toReturn.setInteger("CurrentMoveChargeUpTicks", this.currentMoveChargeUpTicks);
        toReturn.setInteger("CurrentMoveBuildup", this.currentMoveBuildup);
        toReturn.setBoolean("CurrentMoveHitEffectFired", this.currentMoveHitEffectFired);
        toReturn.setBoolean("CurrentMoveUseEndEffectFired", this.currentMoveUseEndEffectFired);
        toReturn.setBoolean("CurrentMoveEndEffectFired", this.currentMoveEndEffectFired);

        //-----for move cooldowns-----

        //final append
        return toReturn;
    }

    public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
        this.moveCooldowns.clear();

        //-----for creature move user-----
        String creatureTypeName = nbtTagCompound.getString("MoveUser");
        this.creatureType = creatureTypeName.isEmpty() ? null : RiftCreatureRegistry.getCreatureBuilder(creatureTypeName);

        //-----for currently used move-----
        this.currentMove = nbtTagCompound.getString("CurrentMove");
        this.currentMovePhase = this.currentMove.isEmpty() ? MoveUsePhase.NONE : this.readMoveUsePhase(nbtTagCompound.getString("CurrentMovePhase"));
        this.currentMoveTicks = Math.max(0, nbtTagCompound.getInteger("CurrentMoveTicks"));
        this.currentMoveChargeUpTicks = Math.max(0, nbtTagCompound.getInteger("CurrentMoveChargeUpTicks"));
        this.currentMoveBuildup = Math.max(0, nbtTagCompound.getInteger("CurrentMoveBuildup"));
        this.currentMoveHitEffectFired = nbtTagCompound.getBoolean("CurrentMoveHitEffectFired");
        this.currentMoveUseEndEffectFired = nbtTagCompound.getBoolean("CurrentMoveUseEndEffectFired");
        this.currentMoveEndEffectFired = nbtTagCompound.getBoolean("CurrentMoveEndEffectFired");

        //-----for move cooldowns-----
    }

    @NotNull
    private MoveUsePhase readMoveUsePhase(@NotNull String phaseName) {
        if (phaseName.isEmpty()) return MoveUsePhase.ACTIVE;
        try {
            return MoveUsePhase.valueOf(phaseName);
        }
        catch (IllegalArgumentException ignored) {
            return MoveUsePhase.ACTIVE;
        }
    }

    public enum MoveUsePhase {
        NONE, //no move is being used
        WINDUP, //winding up a chargeup move
        RELEASING, //releasing a chargeup move
        FINISHING, //recovering from using a chargeup move
        ACTIVE //only matters for non chargeup moves
    }
}
