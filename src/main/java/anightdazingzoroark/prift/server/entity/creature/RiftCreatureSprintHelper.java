package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything relating to managing a creature's sprint happens here.
 */
public class RiftCreatureSprintHelper {
    public static final int MAXIMUM_SPRINT_TICKS = 60;

    @NotNull
    private final RiftCreature creature;
    @NotNull
    private final List<EntityLivingBase> hitEntities = new ArrayList<>();
    private int sprintTicks;
    private boolean riddenSprint;

    public RiftCreatureSprintHelper(@NotNull RiftCreature creature) {
        this.creature = creature;
    }

    public void setSprintingFromRider(@NotNull EntityPlayer rider, boolean sprinting) {
        if (this.creature.world.isRemote || this.creature.getControllingPassenger() != rider) return;
        boolean shouldSprint = sprinting && this.creature.riderCanControl() && !this.creature.isLeaping()
                && this.creature.getStamina() > 0f && this.canSprint();
        if (shouldSprint) {
            if (!this.creature.isSprinting()) this.beginSprint(true, true);
        }
        else this.endSprint();
    }

    /**
     * Advances either controller's sprint through the same hit detection and duration tracking.
     */
    public void updateSprint(@NotNull UpdatePhase phase) {
        if (phase == UpdatePhase.TRAVEL) {
            if (this.creature.isSprinting() && this.creature.isBeingRidden() && this.creature.riderCanControl()
                    && this.creature.getControllingPassenger() instanceof EntityPlayer) {
                this.creature.rotationYaw = this.creature.renderYawOffset;
            }
            return;
        }
        if (this.creature.world.isRemote) return;
        if (this.creature.getSprintCooldown() > 0) {
            this.setCooldown(this.creature.getSprintCooldown() - 1);
        }
        if (this.riddenSprint && (!this.creature.isBeingRidden() || !this.creature.isSprinting()
                || !this.creature.riderCanControl() || this.creature.isLeaping() || this.creature.getStamina() <= 0f)) {
            this.endSprint();
            return;
        }
        if (!this.creature.isSprinting()) return;

        this.sprintTicks++;
        List<EntityLivingBase> newlyHitEntities = new ArrayList<>();
        List<AnimatedBoundingBox> frontZones = this.creature.getAnimationData().getAnimatedBoundingBoxesByTag().get("frontZone");

        //find entities in front zone to hit
        if (frontZones != null) {
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

        if (this.riddenSprint) {
            boolean hitBreakableBlocks = this.creature.hasBreakableBlocksInFront();
            if (hitBreakableBlocks) this.creature.breakBlocksInFrontInPathing();
            if (!newlyHitEntities.isEmpty() || hitBreakableBlocks || !this.canContinueSprinting()) {
                this.endSprint();
            }
        }
    }

    public void beginSprint(boolean sprinting, boolean riddenSprint) {
        if (sprinting && this.creature.isParalyzed()) {
            this.endSprint();
            return;
        }
        this.sprintTicks = 0;
        this.hitEntities.clear();
        this.riddenSprint = sprinting && riddenSprint;
        if (this.riddenSprint) this.creature.rotationYaw = this.creature.renderYawOffset;
        this.creature.setSprinting(sprinting);
    }

    public boolean canContinueSprinting() {
        return this.creature.isSprinting() && !this.creature.isParalyzed()
                && this.sprintTicks < MAXIMUM_SPRINT_TICKS && !this.creature.collidedHorizontally;
    }

    /**
     * Interruptions charge a cooldown for an advanced rider sprint; AI cooldowns belong to its move ticker.
     * */
    public void endSprint() {
        this.endSprint(this.riddenSprint && this.sprintTicks > 0);
    }

    public void endSprint(boolean startCooldown) {
        if (startCooldown) this.resetCooldown();
        this.creature.setSprinting(false);
        this.sprintTicks = 0;
        this.hitEntities.clear();
        this.riddenSprint = false;
    }

    public boolean canSprint() {
        return this.creature.getSprintCooldown() == 0 && !this.creature.isParalyzed();
    }

    public void setCooldown(int cooldown) {
        this.creature.setSprintCooldown(cooldown);
    }

    public void removeCooldown() {
        this.setCooldown(0);
    }

    public void resetCooldown() {
        this.setCooldown(this.creature.getCreatureType().getSprintCooldown());
    }

    /**
     * Selects which creature update hook is calling the helper, keeping sprint progression from advancing twice per tick.
     * */
    public enum UpdatePhase {
        //called from onLivingUpdate for server cooldowns, AI or rider sprint progression, and hit detection.
        TICK,
        //called from travel to align ridden sprint direction on either side without advancing its lifetime.
        TRAVEL
    }
}
