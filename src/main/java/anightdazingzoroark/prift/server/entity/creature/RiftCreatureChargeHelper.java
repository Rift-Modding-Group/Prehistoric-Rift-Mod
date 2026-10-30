package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Moves a released charge along its captured heading and resolves the first front-zone impact.
 */
public class RiftCreatureChargeHelper {
    @NotNull
    private final RiftCreature creature;
    private boolean active;
    private float heading;
    private int ticks;
    private int maximumTicks;
    @Nullable
    private RushState preparedState;

    public RiftCreatureChargeHelper(@NotNull RiftCreature creature) {
        this.creature = creature;
    }

    /**
     * Travel applies movement on both sides; the living tick advances the server's duration once.
     */
    public boolean updateCharge(@NotNull UpdatePhase phase) {
        CreatureMoveBuilder move = this.creature.getCreatureMoves().getMoveBuilderCurrentMove();
        CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
        RushState rushState = this.creature.getRushState();
        if (charge == null || rushState.move().isEmpty() || !rushState.move().equals(this.creature.getCurrentMove())) {
            if (!rushState.move().isEmpty() && !this.creature.world.isRemote) this.creature.setRushState(RushState.EMPTY);
            this.preparedState = null;
            if (this.active) {
                this.active = false;
                this.creature.motionX = 0D;
                this.creature.motionZ = 0D;
            }
            return false;
        }
        if (this.preparedState != rushState) {
            this.preparedState = rushState;
            this.active = false;
        }

        this.creature.getNavigator().clearPath();
        this.creature.setAIMoveSpeed(0f);
        this.creature.setMoveForward(0f);
        this.creature.setMoveStrafing(0f);
        this.creature.rotationPitch = this.creature.prevRotationPitch = 0f;
        boolean releasing = this.creature.getCreatureMoves().currentMoveMatches(this.creature.getCurrentMove(), ChargeupPhase.RELEASING);
        if (!releasing || !this.creature.isEntityAlive() || this.creature.getIsSleeping()
                || this.creature.isStaggered() || this.creature.isBoxSitting() || this.creature.isLeaping()
        ) {
            this.active = false;
            this.creature.motionX = 0D;
            this.creature.motionZ = 0D;
            if (releasing && !this.creature.world.isRemote) this.creature.getCreatureMoves().finishCurrentMoveUse(this.creature);
            return true;
        }

        boolean justStarted = !this.active;
        if (justStarted) {
            this.active = true;
            this.ticks = 0;
            this.heading = rushState.yaw();
            this.maximumTicks = rushState.maxTicks();
        }
        this.creature.rotationYaw = this.creature.prevRotationYaw = this.heading;
        this.creature.renderYawOffset = this.creature.prevRenderYawOffset = this.heading;
        this.creature.rotationYawHead = this.creature.prevRotationYawHead = this.heading;

        if (phase == UpdatePhase.TICK) {
            if (!this.creature.world.isRemote && ((!justStarted && ++this.ticks >= this.maximumTicks)
                    || this.creature.collidedHorizontally)) {
                this.creature.getCreatureMoves().finishCurrentMoveUse(this.creature);
                this.active = false;
                this.creature.motionX = 0D;
                this.creature.motionZ = 0D;
            }
            return true;
        }

        double speed = this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * rushState.speedMultiplier();
        double radians = Math.toRadians(this.heading);
        Vec3d movement = new Vec3d(-Math.sin(radians) * speed, 0D, Math.cos(radians) * speed);
        Map<Entity, Double> entities = new HashMap<>();
        Map<BlockPos, Double> blocks = new HashMap<>();
        double firstContact = 2D;
        List<AnimatedBoundingBox> zones = this.creature.getAnimationData().getAnimatedBoundingBoxesByTag().get("frontZone");
        if (!this.creature.world.isRemote && zones != null) {
            for (AnimatedBoundingBox zone : zones) {
                AxisAlignedBB bounds = this.creature.getAnimationData().getWorldSpaceAABB(zone.getName());
                if (bounds == null) continue;
                AxisAlignedBB sweep = bounds.expand(movement.x, 0D, movement.z);
                for (Entity entity : this.creature.world.getEntitiesWithinAABB(Entity.class, sweep)) {
                    if (!entity.isEntityAlive() || !entity.canBeCollidedWith()
                            || entity.getLowestRidingEntity() == this.creature.getLowestRidingEntity()
                            || entity instanceof MultiPartEntityPart part && part.parent == this.creature
                    ) {
                        continue;
                    }
                    double contact = this.contactProgress(bounds, entity.getEntityBoundingBox(), movement);
                    if (contact > 1D) continue;
                    entities.merge(entity, contact, Math::min);
                    firstContact = Math.min(firstContact, contact);
                }
                for (BlockPos position : BlockPos.getAllInBoxMutable(new BlockPos(sweep.minX, sweep.minY, sweep.minZ),
                        new BlockPos(Math.ceil(sweep.maxX) - 1D, Math.ceil(sweep.maxY) - 1D, Math.ceil(sweep.maxZ) - 1D))
                ) {
                    IBlockState state = this.creature.world.getBlockState(position);
                    AxisAlignedBB collision = state.getCollisionBoundingBox(this.creature.world, position);
                    if (collision == null) continue;
                    double contact = this.contactProgress(bounds, collision.offset(position), movement);
                    if (contact > 1D) continue;
                    blocks.merge(position.toImmutable(), contact, Math::min);
                    firstContact = Math.min(firstContact, contact);
                }
            }
        }

        if (!this.creature.world.isRemote && (firstContact <= 1D || !this.creature.isWithinDeploymentRange(
                this.creature.posX + movement.x, this.creature.posY, this.creature.posZ + movement.z))
        ) {
            if (firstContact <= 1D) {
                this.creature.move(MoverType.SELF, movement.x * firstContact, 0D, movement.z * firstContact);
                Set<Integer> hitVictims = new HashSet<>();
                //attack hit entities
                for (Map.Entry<Entity, Double> hit : entities.entrySet()) {
                    if (hit.getValue() > firstContact + 1E-6D) continue;
                    Entity entity = hit.getKey();
                    Entity victim = entity instanceof MultiPartEntityPart part ? (Entity) part.parent : entity;
                    if (this.creature.isRelatedToEntity(entity) || !hitVictims.add(victim.getEntityId())) continue;
                    if (this.creature.attackEntityWithMove(entity) && rushState.includeKnockback()) {
                        victim.addVelocity(-Math.sin(radians) * 2D, 0.5D, Math.cos(radians) * 2D);
                        victim.velocityChanged = true;
                    }
                }
                //break hit blocks
                if (ForgeEventFactory.getMobGriefingEvent(this.creature.world, this.creature)) {
                    for (Map.Entry<BlockPos, Double> hit : blocks.entrySet()) {
                        BlockPos position = hit.getKey();
                        if (hit.getValue() > firstContact + 1E-6D || !this.creature.canBreakBlock(position)
                                || this.creature.getCreaturePathNavigate().isBlockBreakTemporarilyDenied(position)
                        ) {
                            continue;
                        }
                        IBlockState state = this.creature.world.getBlockState(position);
                        if (ForgeEventFactory.onEntityDestroyBlock(this.creature, position, state)) {
                            this.creature.world.destroyBlock(position, true);
                        }
                        else this.creature.getCreaturePathNavigate().markBlockBreakDenied(position);
                    }
                    this.creature.getCreaturePathNavigate().invalidateBlockBreakPathCache();
                }
            }
            this.creature.getCreatureMoves().finishCurrentMoveUse(this.creature);
            this.active = false;
            this.creature.motionX = 0D;
            this.creature.motionZ = 0D;
        }
        else {
            this.creature.motionX = movement.x;
            this.creature.motionZ = movement.z;
        }
        return true;
    }

    private double contactProgress(@NotNull AxisAlignedBB zone, @NotNull AxisAlignedBB collider, @NotNull Vec3d movement) {
        if (!zone.expand(movement.x, movement.y, movement.z).intersects(collider)) return 2D;
        if (zone.intersects(collider)) return 0D;
        Vec3d center = zone.getCenter();
        AxisAlignedBB expanded = collider.grow((zone.maxX - zone.minX) * 0.5D,
                (zone.maxY - zone.minY) * 0.5D, (zone.maxZ - zone.minZ) * 0.5D);
        RayTraceResult contact = expanded.calculateIntercept(center, center.add(movement));
        if (contact == null || movement.lengthSquared() <= 1E-8D) return 2D;
        return Math.clamp(center.distanceTo(contact.hitVec) / Math.sqrt(movement.lengthSquared()), 0D, 1D);
    }

    public enum UpdatePhase {
        TICK,
        TRAVEL
    }

    //transient data between server and client
    public record RushState(@NotNull String move, double speedMultiplier, int maxTicks, float yaw, boolean includeKnockback) {
        @NotNull
        public static final RushState EMPTY = new RushState("", 0D, 0, 0f, false);
    }
}
