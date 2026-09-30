package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything relating to managing a creature's sprint happens here.
 */
public class RiftCreatureSprintHelper {
    private static final int MAXIMUM_SPRINT_TICKS = 60;

    @NotNull
    private final RiftCreature creature;
    @NotNull
    private final List<EntityLivingBase> hitEntities = new ArrayList<>();
    private int sprintTicks;
    private int cooldown;
    private boolean riddenSprint;

    public RiftCreatureSprintHelper(@NotNull RiftCreature creature) {
        this.creature = creature;
    }

    public void beginSprint(boolean sprinting, boolean riddenSprint) {
        this.sprintTicks = 0;
        this.hitEntities.clear();
        this.riddenSprint = sprinting && riddenSprint;
        this.creature.setSprinting(sprinting);
    }

    public boolean updateSprint() {
        this.sprintTicks++;
        List<EntityLivingBase> newlyHitEntities = new ArrayList<>();
        List<AnimatedBoundingBox> frontZones = this.creature.getAnimationData().getAnimatedBoundingBoxesByTag().get("frontZone");
        if (frontZones == null) return false;

        //find entities in front zone to hit
        for (AnimatedBoundingBox frontZone : frontZones) {
            AxisAlignedBB frontBounds = this.creature.getAnimationData().getWorldSpaceAABB(frontZone.getName());
            if (frontBounds == null) continue;

            for (EntityLivingBase hitEntity : this.creature.world.getEntitiesWithinAABB(EntityLivingBase.class, frontBounds)) {
                if (hitEntity == this.creature
                        || newlyHitEntities.contains(hitEntity)
                        || this.hitEntities.contains(hitEntity)
                        || !hitEntity.isEntityAlive()
                        || this.creature.isRelatedToEntity(hitEntity)
                ) {
                    continue;
                }
                newlyHitEntities.add(hitEntity);
            }
        }

        //deal damage to and knock back aforementioned hit entities
        for (Entity hitEntity : newlyHitEntities) {
            this.creature.attackEntityFromSprint(hitEntity);

            double displacementX = hitEntity.posX - this.creature.posX;
            double displacementZ = hitEntity.posZ - this.creature.posZ;
            double horizontalDisplacement = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
            if (horizontalDisplacement <= 1E-5D) {
                double yawRadians = Math.toRadians(this.creature.rotationYaw);
                displacementX = -Math.sin(yawRadians);
                displacementZ = Math.cos(yawRadians);
                horizontalDisplacement = 1D;
            }
            hitEntity.addVelocity(
                    displacementX / horizontalDisplacement * 2D,
                    0.5D,
                    displacementZ / horizontalDisplacement * 2D
            );
            hitEntity.velocityChanged = true;
        }
        this.hitEntities.addAll(newlyHitEntities);
        return !newlyHitEntities.isEmpty();
    }

    public boolean canContinueSprinting() {
        return this.creature.isSprinting()
                && this.sprintTicks < MAXIMUM_SPRINT_TICKS
                && !this.creature.collidedHorizontally;
    }

    public void endSprint(boolean startCooldown) {
        if (startCooldown) this.resetCooldown();
        this.creature.setSprinting(false);
        this.sprintTicks = 0;
        this.hitEntities.clear();
        this.riddenSprint = false;
    }

    public boolean isRiddenSprint() {
        return this.riddenSprint;
    }

    public boolean hasSprintTicks() {
        return this.sprintTicks > 0;
    }

    public boolean canSprint() {
        return this.cooldown == 0;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(0, cooldown);
    }

    public void removeCooldown() {
        this.setCooldown(0);
    }

    public void resetCooldown() {
        this.setCooldown(this.creature.getCreatureType().getSprintCooldown());
    }
}
