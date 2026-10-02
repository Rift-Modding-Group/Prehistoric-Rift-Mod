package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureSprintHelper;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

public class SprintMoveResultTicker extends AbstractMoveResultTicker {
    private final double destinationX;
    private final double destinationY;
    private final double destinationZ;
    private final boolean hasDestination;
    @NotNull
    private final RiftCreatureSprintHelper sprintHelper;

    public SprintMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        EntityLivingBase target = creature.getAttackTarget();
        this.hasDestination = target != null && target.isEntityAlive() && creature.getCreaturePathNavigate().hasStraightWalkingPathTo(target);
        this.destinationX = this.hasDestination ? target.posX : creature.posX;
        this.destinationY = this.hasDestination ? target.posY : creature.posY;
        this.destinationZ = this.hasDestination ? target.posZ : creature.posZ;
        this.sprintHelper = creature.getSprintHelper();
        if (this.hasDestination && creature.atFrustrationThreshold()) {
            this.sprintHelper.removeCooldown();
            creature.resetFrustration();
        }
        creature.getCreaturePathNavigate().clearPath();
        this.sprintHelper.beginSprint(this.hasDestination, false);
    }

    @Override
    public boolean canContinueTicking() {
        return this.hasDestination && this.sprintHelper.canContinueSprinting()
                && !this.hasReachedDestination();
    }

    @Override
    public void onUpdate() {
        this.sprintHelper.updateSprint();
        if (this.creature.getMoveHelper() instanceof RiftCreatureMoveHelperBase moveHelper) {
            moveHelper.setChargeTo(this.destinationX, this.destinationY, this.destinationZ, 1D);
        }
    }

    @Override
    public void onEndTicker() {
        this.sprintHelper.endSprint(this.hasDestination);
        this.creature.getCreaturePathNavigate().clearPath();

        //preserve last look direction after target is gone
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target == null || !target.isEntityAlive()) this.preserveLastLookDirection();
    }

    @Override
    public boolean isOverridableWhileUsed() {
        return false;
    }

    private boolean hasReachedDestination() {
        double displacementX = this.destinationX - this.creature.posX;
        double displacementZ = this.destinationZ - this.creature.posZ;
        double stoppingDistance = Math.max(0.5D, this.creature.width * 0.5D);
        return displacementX * displacementX + displacementZ * displacementZ <= stoppingDistance * stoppingDistance;
    }
}
