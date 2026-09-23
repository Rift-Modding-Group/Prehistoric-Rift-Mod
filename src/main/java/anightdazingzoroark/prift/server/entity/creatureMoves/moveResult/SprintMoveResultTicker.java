package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder.SprintMoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SprintMoveResultTicker extends AbstractMoveResultTicker {
    private static final int MAX_SPRINT_TICKS = 100;

    private final double destinationX;
    private final double destinationY;
    private final double destinationZ;
    private final boolean hasDestination;
    @NotNull
    private final SprintMoveRuleBuilder sprintMoveRuleBuilder;
    @NotNull
    private final List<EntityLivingBase> hitEntitiesWhileSprinting = new ArrayList<>();
    private int sprintTicks;

    public SprintMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        if (!(moveRuleBuilder instanceof SprintMoveRuleBuilder sprintMoveRuleBuilder)) {
            throw new IllegalArgumentException("Sprint move results require a sprint move rule builder!");
        }
        this.sprintMoveRuleBuilder = sprintMoveRuleBuilder;

        EntityLivingBase target = creature.getAttackTarget();
        this.hasDestination = target != null
                && target.isEntityAlive()
                && creature.canUseStamina(MoveResult.SPRINT.staminaConsumption())
                && creature.getCreaturePathNavigate().hasStraightWalkingPathTo(target);
        this.destinationX = this.hasDestination ? target.posX : creature.posX;
        this.destinationY = this.hasDestination ? target.posY : creature.posY;
        this.destinationZ = this.hasDestination ? target.posZ : creature.posZ;
        if (this.hasDestination && creature.atFrustrationThreshold()) {
            creature.removeSprintToAttackCooldown();
            creature.resetFrustration();
        }
        creature.getCreaturePathNavigate().clearPath();
        creature.setSprinting(this.hasDestination);
    }

    @Override
    public boolean canContinueTicking() {
        float staminaDrain = MoveResult.SPRINT.staminaConsumption() / 20f;
        return this.hasDestination
                && this.creature.isSprinting()
                && this.creature.canUseStamina(staminaDrain)
                && this.sprintTicks < MAX_SPRINT_TICKS
                && !this.creature.collidedHorizontally
                && !this.hasReachedDestination();
    }

    @Override
    public void onUpdate() {
        this.sprintTicks++;
        if (this.creature.getMoveHelper() instanceof RiftCreatureMoveHelperBase moveHelper) {
            moveHelper.setChargeTo(this.destinationX, this.destinationY, this.destinationZ, 1D);
        }

        //check for anything inside front AABBs
        List<AnimatedBoundingBox> frontZoneAnimatedBBs = this.creature.getAnimationData().getAnimatedBoundingBoxesByTag().get("frontZone");
        if (frontZoneAnimatedBBs != null) {
            //fill up list of hit entities
            List<EntityLivingBase> allHitEntities = new ArrayList<>();
            for (AnimatedBoundingBox frontZoneAnimatedBB : frontZoneAnimatedBBs) {
                AxisAlignedBB aabb = this.creature.getAnimationData().getWorldSpaceAABB(frontZoneAnimatedBB.getName());
                List<EntityLivingBase> hitEntities = this.creature.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)
                        .stream().filter(entity -> {
                            return entity != null && !allHitEntities.contains(entity) && !this.hitEntitiesWhileSprinting.contains(entity)
                                    && !this.creature.equals(entity) && !this.creature.isRelatedToEntity(entity);
                        }).toList();
                allHitEntities.addAll(hitEntities);
            }

            //apply damage and knockback once to each entity without ending the sprint
            if (!allHitEntities.isEmpty()) {
                for (Entity hitEntity : allHitEntities) {
                    this.creature.attackEntityFromSprint(hitEntity, this.sprintMoveRuleBuilder.getBasePower());

                    double displacementX = hitEntity.posX - this.creature.posX;
                    double displacementZ = hitEntity.posZ - this.creature.posZ;
                    double horizontalDisplacement = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
                    if (horizontalDisplacement <= 1E-5D) {
                        double rotationYawRadians = Math.toRadians(this.creature.rotationYaw);
                        displacementX = -Math.sin(rotationYawRadians);
                        displacementZ = Math.cos(rotationYawRadians);
                        horizontalDisplacement = 1D;
                    }
                    hitEntity.addVelocity(
                            displacementX / horizontalDisplacement * 2D,
                            0.5D,
                            displacementZ / horizontalDisplacement * 2D
                    );
                    //for players
                    hitEntity.velocityChanged = true;
                }
                this.hitEntitiesWhileSprinting.addAll(allHitEntities);
            }
        }
    }

    @Override
    public void onEndTicker() {
        if (this.hasDestination) this.creature.resetSprintToAttackCooldown();
        this.creature.setSprinting(false);
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
