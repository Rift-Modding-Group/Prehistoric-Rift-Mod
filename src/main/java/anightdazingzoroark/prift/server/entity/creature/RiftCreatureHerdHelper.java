package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * everything relating to herding is done here
 */
public class RiftCreatureHerdHelper {
    private static final double HERD_DISCOVERY_RANGE = 12D;
    private static final double MAXIMUM_JOIN_DISTANCE_SQ = HERD_DISCOVERY_RANGE * HERD_DISCOVERY_RANGE;
    private static final double MAXIMUM_LEADER_DISTANCE_SQ = 16D * 16D;
    private static final double MAXIMUM_RETREAT_LEADER_DISTANCE_SQ = 32D * 32D;
    private static final double FORMATION_DIRECTION_UPDATE_DISTANCE_SQ = 1D;
    private static final double MINIMUM_FORMATION_SPACING = 2D;
    private static final double FORMATION_MEMBER_PADDING = 2D;
    private static final int HERD_DISCOVERY_INTERVAL = 20;

    @NotNull
    private HerdState state;

    public RiftCreatureHerdHelper(@NotNull RiftCreature initialMember) {
        this.state = new HerdState(initialMember);
    }

    public void onUpdate() {
        HerdState updatingState = this.state;
        long worldTime = updatingState.world.getTotalWorldTime();
        this.validateMembers(updatingState);
        if (updatingState.leader == null) return;

        List<RiftCreature> memberSnapshot = new ArrayList<>(updatingState.members);
        memberSnapshot.sort((first, second) -> this.compareLeadership(second, first));

        for (RiftCreature member : memberSnapshot) {
            if (member == updatingState.leader) continue;

            double maximumLeaderDistanceSq = updatingState.retreating
                    ? MAXIMUM_RETREAT_LEADER_DISTANCE_SQ : MAXIMUM_LEADER_DISTANCE_SQ;
            RiftCreatureHerdHelper memberHerd = member.getHerd();
            Vec3d followPosition = memberHerd == null ? null : memberHerd.getFollowPosition(member);
            if (followPosition != null) {
                double formationDistance = Math.sqrt(updatingState.leader.getDistanceSq(
                        followPosition.x, followPosition.y, followPosition.z
                ));
                double followStartDistance = Math.max(2D, member.width);
                double maximumLeaderDistance = formationDistance + Math.max(4D, followStartDistance + 2D);
                maximumLeaderDistanceSq = Math.max(maximumLeaderDistanceSq, maximumLeaderDistance * maximumLeaderDistance);
            }
            if (member.getDistanceSq(updatingState.leader) <= maximumLeaderDistanceSq) continue;

            this.removeMember(updatingState, member);
        }

        if (updatingState.leader == null) return;

        if (updatingState.lastDiscoveryUpdateWorldTime != worldTime) {
            updatingState.lastDiscoveryUpdateWorldTime = worldTime;
            List<RiftCreature> discoveryMembers = new ArrayList<>();
            List<List<RiftCreature>> nearbyCreaturesByMember = new ArrayList<>();

            for (RiftCreature member : memberSnapshot) {
                RiftCreatureHerdHelper memberHerd = member.getHerd();
                if (memberHerd == null || memberHerd.state != updatingState) continue;
                if (Math.floorMod(worldTime + member.getEntityId(), (long) HERD_DISCOVERY_INTERVAL) != 0L) continue;

                List<RiftCreature> nearbyCreatures = updatingState.world.getEntitiesWithinAABB(
                        RiftCreature.class,
                        member.getEntityBoundingBox().grow(HERD_DISCOVERY_RANGE),
                        candidate -> candidate != null
                                && candidate != member
                                && candidate.isAddedToWorld()
                                && candidate.isEntityAlive()
                                && candidate.canDoHerding()
                                && candidate.getCreatureType() == updatingState.creatureType
                                && member.getDistanceSq(candidate) <= MAXIMUM_JOIN_DISTANCE_SQ
                );
                nearbyCreatures.sort((first, second) -> this.compareLeadership(second, first));
                discoveryMembers.add(member);
                nearbyCreaturesByMember.add(nearbyCreatures);
            }

            for (int memberIndex = 0; memberIndex < discoveryMembers.size(); memberIndex++) {
                RiftCreature member = discoveryMembers.get(memberIndex);
                List<RiftCreature> nearbyCreatures = nearbyCreaturesByMember.get(memberIndex);
                for (RiftCreature nearbyCreature : nearbyCreatures) {
                    RiftCreatureHerdHelper currentHerd = member.getHerd();
                    RiftCreatureHerdHelper nearbyHerd = nearbyCreature.getHerd();
                    if (currentHerd == null || nearbyHerd == null || currentHerd.state == nearbyHerd.state) continue;

                    currentHerd.mergeWith(nearbyHerd);
                }
            }
        }

        if (updatingState.leader != null) {
            this.setAllAttackTargets(updatingState, updatingState.leader.getAttackTarget());
        }
    }

    /**
     * Moves as many high-ranking members as capacity allows into the herd whose
     * leader wins the combined leadership comparison.
     */
    public void mergeWith(@NotNull RiftCreatureHerdHelper otherHerd) {
        HerdState firstState = this.state;
        HerdState secondState = otherHerd.state;
        if (firstState == secondState) return;

        this.validateMembers(firstState);
        this.validateMembers(secondState);
        firstState = this.state;
        secondState = otherHerd.state;
        if (firstState == secondState || firstState.leader == null || secondState.leader == null
                || firstState.world != secondState.world
                || firstState.creatureType != secondState.creatureType
                || firstState.retreating || secondState.retreating
        ) {
            return;
        }

        HerdState receivingState;
        HerdState donatingState;
        if (this.compareLeadership(firstState.leader, secondState.leader) >= 0) {
            receivingState = firstState;
            donatingState = secondState;
        }
        else {
            receivingState = secondState;
            donatingState = firstState;
        }

        int availableSlots = receivingState.maximumSize - receivingState.members.size();
        if (availableSlots <= 0) return;

        RiftCreature receivingLeader = receivingState.leader;
        List<RiftCreature> candidates = new ArrayList<>(donatingState.members);
        candidates.removeIf(member -> member.getDistanceSq(receivingLeader) > MAXIMUM_JOIN_DISTANCE_SQ);
        candidates.sort((first, second) -> this.compareLeadership(second, first));
        int transferredMembers = 0;
        for (RiftCreature member : candidates) {
            if (transferredMembers >= availableSlots) break;

            RiftCreatureHerdHelper memberHerd = member.getHerd();
            if (memberHerd == null || memberHerd.state != donatingState || !donatingState.members.remove(member)) continue;

            receivingState.members.add(member);
            memberHerd.state = receivingState;
            transferredMembers++;
        }

        this.updateLeadership(receivingState);
        this.updateLeadership(donatingState);
    }

    public void removeMember(@NotNull RiftCreature member) {
        this.removeMember(this.state, member);
    }

    @Nullable
    public RiftCreature getLeader() {
        return this.state.leader;
    }

    public int getSize() {
        return this.state.members.size();
    }

    public boolean isRetreating() {
        return this.state.retreating;
    }

    @Nullable
    public Vec3d getRetreatThreatPosition() {
        return this.state.retreatThreatPosition;
    }

    @Nullable
    public Vec3d getRetreatOriginPosition() {
        return this.state.retreatOriginPosition;
    }

    @Nullable
    public Vec3d getRetreatDestination() {
        return this.state.retreatDestination;
    }

    public void beginRetreat(@NotNull RiftCreature source, @NotNull Vec3d threatPosition) {
        HerdState currentState = this.state;
        if (source != currentState.leader || !currentState.members.contains(source) || currentState.retreating) return;

        currentState.retreating = true;
        currentState.retreatThreatPosition = threatPosition;
        currentState.retreatOriginPosition = source.getPositionVector();
        currentState.retreatDestination = null;
        this.setAllAttackTargets(currentState, null);
    }

    public void setRetreatDestination(@NotNull RiftCreature source, @NotNull Vec3d destination) {
        HerdState currentState = this.state;
        if (source == currentState.leader && currentState.retreating) currentState.retreatDestination = destination;
    }

    public void clearRetreatDestination(@NotNull RiftCreature source) {
        HerdState currentState = this.state;
        if (source == currentState.leader && currentState.retreating) currentState.retreatDestination = null;
    }

    public void finishRetreat(@NotNull RiftCreature source) {
        HerdState currentState = this.state;
        if (source != currentState.leader || !currentState.retreating) return;

        currentState.retreating = false;
        currentState.retreatThreatPosition = null;
        currentState.retreatOriginPosition = null;
        currentState.retreatDestination = null;
        this.setAllAttackTargets(currentState, null);
    }

    public boolean isAssembledForRetreat() {
        HerdState currentState = this.state;
        RiftCreature currentLeader = currentState.leader;
        if (currentLeader == null) return true;

        for (RiftCreature member : currentState.members) {
            if (member != currentLeader && member.getDistanceSq(currentLeader) > MAXIMUM_LEADER_DISTANCE_SQ) return false;
        }
        return true;
    }

    /**
     * Returns this member's width-scaled position in the rows trailing the leader.
     */
    @Nullable
    public Vec3d getFollowPosition(@NotNull RiftCreature member) {
        HerdState currentState = this.state;
        RiftCreature currentLeader = currentState.leader;
        RiftCreatureHerdHelper memberHerd = member.getHerd();
        if (currentLeader == null || member == currentLeader || memberHerd == null
                || memberHerd.state != currentState || !currentState.members.contains(member)
        ) {
            return null;
        }

        int followerIndex = 0;
        double maximumMemberWidth = currentLeader.width;
        for (RiftCreature candidate : currentState.members) {
            maximumMemberWidth = Math.max(maximumMemberWidth, candidate.width);
            if (candidate != currentLeader && candidate.getEntityId() < member.getEntityId()) followerIndex++;
        }

        int followerCount = currentState.members.size() - 1;
        int row = 1;
        int precedingFollowers = 0;
        int rowCapacity = 2;
        while (followerIndex >= precedingFollowers + rowCapacity) {
            precedingFollowers += rowCapacity;
            row++;
            rowCapacity++;
        }

        int followersInRow = Math.min(rowCapacity, followerCount - precedingFollowers);
        int positionInRow = followerIndex - precedingFollowers;
        double spacing = Math.max(MINIMUM_FORMATION_SPACING, maximumMemberWidth + FORMATION_MEMBER_PADDING);
        double backwardOffset = row * spacing;
        double lateralOffset = (positionInRow - (followersInRow - 1) * 0.5D) * spacing;

        if (currentState.formationLeader != currentLeader) {
            currentState.formationLeader = currentLeader;
            currentState.formationLeaderX = currentLeader.posX;
            currentState.formationLeaderZ = currentLeader.posZ;
            currentState.formationYaw = currentLeader.rotationYaw;
        }
        else {
            double leaderDisplacementX = currentLeader.posX - currentState.formationLeaderX;
            double leaderDisplacementZ = currentLeader.posZ - currentState.formationLeaderZ;
            if (leaderDisplacementX * leaderDisplacementX + leaderDisplacementZ * leaderDisplacementZ
                    >= FORMATION_DIRECTION_UPDATE_DISTANCE_SQ
            ) {
                currentState.formationLeaderX = currentLeader.posX;
                currentState.formationLeaderZ = currentLeader.posZ;
                currentState.formationYaw = Math.toDegrees(Math.atan2(-leaderDisplacementX, leaderDisplacementZ));
            }
        }
        double leaderYaw = Math.toRadians(currentState.formationYaw);

        return new Vec3d(
                currentLeader.posX + Math.sin(leaderYaw) * backwardOffset + Math.cos(leaderYaw) * lateralOffset,
                currentLeader.posY,
                currentLeader.posZ - Math.cos(leaderYaw) * backwardOffset + Math.sin(leaderYaw) * lateralOffset
        );
    }

    public boolean contains(@Nullable RiftCreature creature) {
        if (creature == null) return false;

        RiftCreatureHerdHelper creatureHerd = creature.getHerd();
        return creatureHerd != null && creatureHerd.state == this.state && this.state.members.contains(creature);
    }

    /**
     * Propagates a target chosen by the current leader.
     */
    public void updateLeaderTarget(@NotNull RiftCreature source, @Nullable EntityLivingBase target) {
        HerdState currentState = this.state;
        if (currentState.settingAttackTargets || source != currentState.leader || !currentState.members.contains(source)) return;

        this.setAllAttackTargets(currentState, target);
    }

    /**
     * The attacked member is allowed to make the entire herd retaliate.
     */
    public void retaliate(@NotNull RiftCreature attackedMember, @Nullable EntityLivingBase attacker) {
        HerdState currentState = this.state;
        if (!this.contains(attackedMember) || attacker == null || !attacker.isEntityAlive()) return;
        if (attacker instanceof RiftCreature attackerCreature && this.contains(attackerCreature)) return;

        this.setAllAttackTargets(currentState, attacker);
    }

    private void validateMembers(@NotNull HerdState validatingState) {
        Iterator<RiftCreature> memberIterator = validatingState.members.iterator();
        while (memberIterator.hasNext()) {
            RiftCreature member = memberIterator.next();
            RiftCreatureHerdHelper memberHerd = member.getHerd();
            if (memberHerd != null && memberHerd.state == validatingState
                    && member.world == validatingState.world && member.isAddedToWorld()
                    && member.isEntityAlive() && member.canDoHerding()
                    && member.getCreatureType() == validatingState.creatureType
            ) {
                continue;
            }

            memberIterator.remove();
            if (memberHerd != null && memberHerd.state == validatingState) {
                memberHerd.state = new HerdState(member);
            }
        }
        this.updateLeadership(validatingState);
    }

    private void removeMember(@NotNull HerdState previousState, @NotNull RiftCreature member) {
        RiftCreatureHerdHelper memberHerd = member.getHerd();
        if (memberHerd == null || memberHerd.state != previousState || !previousState.members.remove(member)) return;

        memberHerd.state = new HerdState(member);
        this.updateLeadership(previousState);
    }

    /**
     * A positive result means the first creature is the better leader.
     */
    private int compareLeadership(@NotNull RiftCreature first, @NotNull RiftCreature second) {
        int levelComparison = Integer.compare(first.getLevel(), second.getLevel());
        if (levelComparison != 0) return levelComparison;
        return Integer.compare(first.getEntityId(), second.getEntityId());
    }

    private void updateLeadership(@NotNull HerdState updatedState) {
        if (updatedState.members.isEmpty()) {
            updatedState.leader = null;
            return;
        }

        RiftCreature electedLeader = null;
        for (RiftCreature member : updatedState.members) {
            if (electedLeader == null || this.compareLeadership(member, electedLeader) > 0) {
                electedLeader = member;
            }
        }
        updatedState.leader = electedLeader;
        this.setAllAttackTargets(updatedState, updatedState.leader.getAttackTarget());
    }

    private void setAllAttackTargets(@NotNull HerdState targetState, @Nullable EntityLivingBase target) {
        EntityLivingBase resolvedTarget = !targetState.retreating && target != null && target.world == targetState.world
                && target.isAddedToWorld() && target.isEntityAlive()
                && (!(target instanceof RiftCreature targetCreature) || !targetState.members.contains(targetCreature))
                ? target : null;

        targetState.settingAttackTargets = true;
        for (RiftCreature member : targetState.members) {
            member.setAttackTarget(resolvedTarget);
        }
        targetState.settingAttackTargets = false;
    }

    private static class HerdState {
        @NotNull
        private final World world;
        @NotNull
        private final RiftCreatureBuilder creatureType;
        private final int maximumSize;
        @NotNull
        private final Set<RiftCreature> members = new HashSet<>();
        @Nullable
        private RiftCreature leader;
        @Nullable
        private RiftCreature formationLeader;
        private double formationLeaderX;
        private double formationLeaderZ;
        private double formationYaw;
        private long lastDiscoveryUpdateWorldTime = Long.MIN_VALUE;
        private boolean settingAttackTargets;
        private boolean retreating;
        @Nullable
        private Vec3d retreatThreatPosition;
        @Nullable
        private Vec3d retreatOriginPosition;
        @Nullable
        private Vec3d retreatDestination;

        private HerdState(@NotNull RiftCreature initialMember) {
            this.world = initialMember.world;
            this.creatureType = initialMember.getCreatureType();
            this.maximumSize = this.creatureType.getMaxHerdSize();
            this.members.add(initialMember);
            this.leader = initialMember;
        }
    }
}
