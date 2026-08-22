package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.api.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
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
    @Nullable
    private ChargeupPhase currentMoveChargeupPhase;
    private int currentMoveTicks;
    private int currentMoveChargeUpTicks;
    private int currentMoveBuildup;
    private boolean currentMoveHitEffectFired;
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

        CreatureMoveSelectorBuilder.MoveRule blockBreakMoveRule = this.getBlockBreakMoveRule(creature, target);
        for (CreatureMoveSelectorBuilder.MoveRule moveRule : this.creatureType.getMoveSelector().getMoveRules()) {
            MoveRuleBuilder moveRuleBuilder = moveRule.moveRuleBuilder();

            boolean useDueToFrustration = moveRule.moveResult() == CreatureMoveResult.USE_MOVE
                    && target != null
                    && target.isEntityAlive()
                    && creature.atFrustrationThreshold()
                    && moveRuleBuilder.getUseWhenFrustrated();
            int index = useDueToFrustration ? 0 : moveRuleBuilder.getPriorityPredicate().apply(creature, target);

            //block-breaking moves r prioritized over attack leaps, ranged
            //fallbacks, and ordinary attacks until it has opened the shorter corridor
            if (blockBreakMoveRule != null && !moveRule.equals(blockBreakMoveRule)) index = -1;

            //let ordinary move pathing take a survivable route down instead of repeatedly
            //selecting a leap attack across the opening.
            if (index >= 0 && moveRule.moveResult() == CreatureMoveResult.LEAP
                    && target != null
                    && target.isEntityAlive()
                    && creature.getCreaturePathNavigate().hasSafeDownwardWalkingPathTo(target)) {
                index = -1;
            }

            //being on cooldown forcibly changes the index to -1
            if (this.moveCurrentCooldown(moveRuleBuilder.getMoveName()) > 0) {
                index = -1;
            }

            this.prioritizedUsableMoves.remove(moveRule);
            //positive indexes can be added
            if (index >= 0) this.prioritizedUsableMoves.add(index, moveRule);
        }
    }

    @Nullable
    private CreatureMoveSelectorBuilder.MoveRule getBlockBreakMoveRule(
            @NotNull RiftCreature creature,
            @Nullable EntityLivingBase target
    ) {
        if (target == null || !target.isEntityAlive() || !creature.hasBlockBreakZone()) return null;

        CreatureMoveSelectorBuilder.MoveRule bestRule = null;
        int bestPriority = Integer.MAX_VALUE;
        for (CreatureMoveSelectorBuilder.MoveRule moveRule : this.creatureType.getMoveSelector().getMoveRules()) {
            if (moveRule.moveResult() != CreatureMoveResult.USE_MOVE) continue;

            MoveRuleBuilder ruleBuilder = moveRule.moveRuleBuilder();
            if (!ruleBuilder.getUseBlockBreak()
                    || ruleBuilder.getDontPathToTarget()
                    || this.moveCurrentCooldown(ruleBuilder.getMoveName()) > 0) {
                continue;
            }

            CreatureMoveBuilder moveBuilder = this.getUsableMoveBuilder(this.creaturePhase, ruleBuilder.getMoveName());
            if (moveBuilder == null
                    || !moveBuilder.getRequireFindTargetToUse()
                    || moveBuilder.getMoveType() == CreatureMoveBuilder.MoveType.STATUS
                    || !moveBuilder.getMakesContact()) {
                continue;
            }

            int priority = creature.atFrustrationThreshold() && ruleBuilder.getUseWhenFrustrated()
                    ? 0
                    : ruleBuilder.getPriorityPredicate().apply(creature, target);
            if (priority >= 0 && priority < bestPriority) {
                bestRule = moveRule;
                bestPriority = priority;
            }
        }

        return bestRule != null && creature.getCreaturePathNavigate().shouldUseBlockBreakPath(target)
                ? bestRule
                : null;
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
    //---general moves---
    @NotNull
    public String getCurrentMove() {
        return this.currentMove;
    }

    public boolean currentMoveMatches(@NotNull String moveName) {
        return this.currentMoveMatches(moveName, null);
    }

    public boolean currentMoveMatches(@NotNull String moveName, @Nullable ChargeupPhase phase) {
        return this.currentMove.equals(moveName) && this.currentMoveChargeupPhase == phase;
    }

    /**
     * Set the move that the creature holding this class will use
     * */
    public void setCurrentMove(@NotNull String moveName) {
        if (moveName.isEmpty()) this.clearCurrentMove();
        else {
            this.currentMove = moveName;
            this.currentMoveTicks = 0;
            this.currentMoveChargeUpTicks = 0;
            this.currentMoveBuildup = 0;
            this.currentMoveHitEffectFired = false;
            this.currentMoveEndEffectFired = false;

            CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder == null ? null : currentMoveBuilder.getMoveChargeupBuilder();
            this.currentMoveChargeupPhase = chargeupBuilder == null ? null : ChargeupPhase.PREWINDUP;
        }
    }

    /**
     * Used to update the move as it's being used
     * */
    public void tickCurrentMove(@NotNull RiftCreature creature, @Nullable EntityLivingBase target) {
        if (this.currentMove.isEmpty()) return;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) this.clearCurrentMove();
        else {
            this.currentMoveTicks++;

            //well
            if (currentMoveBuilder.getWhileMoveUseEffect() != null) currentMoveBuilder.getWhileMoveUseEffect().accept(creature, target);

            //chargeup stuff
            CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
            if (chargeupBuilder == null || this.currentMoveChargeupPhase == null) return;

            if (this.currentMoveChargeupPhase == ChargeupPhase.RELEASING && chargeupBuilder.getReleaseDuringUseEffect() != null) {
                chargeupBuilder.getReleaseDuringUseEffect().accept(creature, creature.getAttackTarget());
            }

            if (chargeupBuilder.getChargeUpThenRelease() && this.currentMoveChargeupPhase == ChargeupPhase.WINDUP) {
                this.addCurrentMoveBuildup(chargeupBuilder);
                if (this.currentMoveBuildup >= chargeupBuilder.getMaxChargeUp()) {
                    this.finishCurrentMoveChargeupPhase(creature);
                }
            }
            else if (chargeupBuilder.getChargeUpWhileUse() && this.currentMoveChargeupPhase == ChargeupPhase.RELEASING) {
                this.addCurrentMoveBuildup(chargeupBuilder);
                if (this.currentMoveBuildup >= chargeupBuilder.getMaxChargeUp()) {
                    this.finishCurrentMoveUse(creature);
                }
            }
        }
    }

    public boolean shouldCancelCurrentMoveForMissingTarget(@NotNull RiftCreature creature) {
        if (this.currentMove.isEmpty()) return false;
        if (this.currentMoveChargeupPhase == ChargeupPhase.FINISHING) return false;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) return true;
        if (!currentMoveBuilder.getRequireFindTargetToUse()) return false;

        EntityLivingBase target = creature.getAttackTarget();
        return target == null || !target.isEntityAlive();
    }

    public void finishCurrentMoveUse(@NotNull RiftCreature creature) {
        if (this.currentMove.isEmpty()) return;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) this.clearCurrentMove();
        else {
            CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
            if (chargeupBuilder == null || this.currentMoveChargeupPhase == null) return;
            if (this.currentMoveChargeupPhase == ChargeupPhase.FINISHING) return;

            if (this.currentMoveChargeupPhase == ChargeupPhase.RELEASING) {
                this.runChargeupPhaseEndEffect(creature, chargeupBuilder, ChargeupPhase.RELEASING);
            }
            this.currentMoveChargeupPhase = ChargeupPhase.FINISHING;
        }
    }

    //---specifically for chargeup moves---
    public void finishCurrentMoveChargeupPhase(@NotNull RiftCreature creature) {
        if (this.currentMove.isEmpty() || this.currentMoveChargeupPhase == null) return;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder == null ? null : currentMoveBuilder.getMoveChargeupBuilder();
        if (currentMoveBuilder == null || chargeupBuilder == null) this.clearCurrentMove();
        else {
            ChargeupPhase finishedPhase = this.currentMoveChargeupPhase;
            this.runChargeupPhaseEndEffect(creature, chargeupBuilder, finishedPhase);

            if (finishedPhase == ChargeupPhase.PREWINDUP) this.currentMoveChargeupPhase = ChargeupPhase.WINDUP;
            else if (finishedPhase == ChargeupPhase.WINDUP) this.currentMoveChargeupPhase = ChargeupPhase.PRERELEASING;
            else if (finishedPhase == ChargeupPhase.PRERELEASING) {
                this.runCurrentMoveHitEffect(creature, currentMoveBuilder);
                this.currentMoveChargeupPhase = ChargeupPhase.RELEASING;
            }
            else if (finishedPhase == ChargeupPhase.RELEASING) this.currentMoveChargeupPhase = ChargeupPhase.FINISHING;
        }
    }

    public void finishCurrentMoveChargeupPhase(@NotNull RiftCreature creature, @NotNull ChargeupPhase expectedPhase) {
        if (this.currentMoveChargeupPhase != expectedPhase) return;
        this.finishCurrentMoveChargeupPhase(creature);
    }

    private void runChargeupPhaseEndEffect(
            @NotNull RiftCreature creature,
            @NotNull CreatureMoveChargeupBuilder chargeupBuilder,
            @NotNull ChargeupPhase finishedPhase
    ) {
        if (finishedPhase == ChargeupPhase.WINDUP && chargeupBuilder.getWindupEndEffect() != null) {
            chargeupBuilder.getWindupEndEffect().accept(creature);
        }
        else if (finishedPhase == ChargeupPhase.PRERELEASING && chargeupBuilder.getPrereleaseEndEffect() != null) {
            chargeupBuilder.getPrereleaseEndEffect().accept(creature);
        }
        else if (finishedPhase == ChargeupPhase.RELEASING && chargeupBuilder.getReleaseEndEffect() != null) {
            chargeupBuilder.getReleaseEndEffect().accept(creature);
        }
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

        return this.currentMoveChargeupPhase == ChargeupPhase.PRERELEASING
                || this.currentMoveChargeupPhase == ChargeupPhase.RELEASING;
    }

    public void runCurrentMoveHitEffect(@NotNull RiftCreature creature) {
        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null) return;
        this.runCurrentMoveHitEffect(creature, currentMoveBuilder);
    }

    private void runCurrentMoveHitEffect(@NotNull RiftCreature creature, @NotNull CreatureMoveBuilder currentMoveBuilder) {
        if (this.currentMoveHitEffectFired) return;

        this.currentMoveHitEffectFired = true;
        if (creature.getUseBlockBreak()) {
            creature.recordBlockBreakEffectAttempt();
            creature.breakBlocksInFrontInPathing();
        }
        else if (currentMoveBuilder.getOnMoveHitEffect() != null) currentMoveBuilder.getOnMoveHitEffect().accept(creature);
    }

    public void resetCurrentMove(@NotNull RiftCreature creature) {
        //put on cooldown first
        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder != null) {
            //calculate the cooldown first
            int cooldown = this.calculateCurrentMoveCooldown(creature, currentMoveBuilder);
            if (cooldown > 0) this.moveCooldowns.put(this.currentMove, cooldown);
        }

        //now reset
        this.clearCurrentMove();
    }

    private void addCurrentMoveBuildup(@NotNull CreatureMoveChargeupBuilder chargeupBuilder) {
        this.currentMoveChargeUpTicks++;
        this.currentMoveBuildup = Math.min(chargeupBuilder.getMaxChargeUp(), this.currentMoveBuildup + 1);
    }

    private int calculateCurrentMoveCooldown(@NotNull RiftCreature creature, @NotNull CreatureMoveBuilder currentMoveBuilder) {
        CreatureMoveChargeupBuilder chargeupBuilder = currentMoveBuilder.getMoveChargeupBuilder();
        if (chargeupBuilder != null) {
            double cooldownMultiplier = Math.max(0D, chargeupBuilder.getCooldownMultiplier().apply(creature));
            return (int) Math.ceil(this.currentMoveChargeUpTicks * cooldownMultiplier);
        }
        return Math.max(0, currentMoveBuilder.getMoveCooldown());
    }

    private void clearCurrentMove() {
        this.currentMove = "";
        this.currentMoveChargeupPhase = null;
        this.currentMoveTicks = 0;
        this.currentMoveChargeUpTicks = 0;
        this.currentMoveBuildup = 0;
        this.currentMoveHitEffectFired = false;
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
        toReturn.setString("CurrentMoveChargeupPhase", this.currentMoveChargeupPhase == null ? "" : this.currentMoveChargeupPhase.name());
        toReturn.setInteger("CurrentMoveTicks", this.currentMoveTicks);
        toReturn.setInteger("CurrentMoveChargeUpTicks", this.currentMoveChargeUpTicks);
        toReturn.setInteger("CurrentMoveBuildup", this.currentMoveBuildup);
        toReturn.setBoolean("CurrentMoveHitEffectFired", this.currentMoveHitEffectFired);
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
        this.currentMoveChargeupPhase = this.readCurrentMoveChargeupPhase(nbtTagCompound);
        this.currentMoveTicks = Math.max(0, nbtTagCompound.getInteger("CurrentMoveTicks"));
        this.currentMoveChargeUpTicks = Math.max(0, nbtTagCompound.getInteger("CurrentMoveChargeUpTicks"));
        this.currentMoveBuildup = Math.max(0, nbtTagCompound.getInteger("CurrentMoveBuildup"));
        this.currentMoveHitEffectFired = nbtTagCompound.getBoolean("CurrentMoveHitEffectFired");
        this.currentMoveEndEffectFired = nbtTagCompound.getBoolean("CurrentMoveEndEffectFired");

        //-----for move cooldowns-----
    }

    @Nullable
    private ChargeupPhase readCurrentMoveChargeupPhase(@NotNull NBTTagCompound nbtTagCompound) {
        if (this.currentMove.isEmpty()) return null;

        CreatureMoveBuilder currentMoveBuilder = this.getMoveBuilderCurrentMove();
        if (currentMoveBuilder == null || currentMoveBuilder.getMoveChargeupBuilder() == null) return null;

        String phaseName = nbtTagCompound.getString("CurrentMoveChargeupPhase");
        if (phaseName.isEmpty()) phaseName = nbtTagCompound.getString("CurrentMovePhase");
        if (phaseName.isEmpty()) return ChargeupPhase.PREWINDUP;
        try {
            return ChargeupPhase.valueOf(phaseName);
        }
        catch (IllegalArgumentException ignored) {
            return ChargeupPhase.PREWINDUP;
        }
    }
}
