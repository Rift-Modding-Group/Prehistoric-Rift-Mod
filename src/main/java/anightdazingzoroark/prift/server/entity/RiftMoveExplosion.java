package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An explosion powered by the move retained by its projectile.
 * */
public class RiftMoveExplosion extends Explosion {
    @NotNull
    private final World world;
    @NotNull
    private final RiftProjectile projectile;
    @Nullable
    private final EntityLivingBase directHit;
    @NotNull
    private final Vec3d origin;
    private final float strength;
    private final boolean damagesTerrain;
    private final double moveDamage;
    @NotNull
    private final DamageSource damageSource;

    public RiftMoveExplosion(
            @NotNull RiftProjectile projectile, @Nullable EntityLivingBase directHit,
            @NotNull Vec3d origin, float strength, boolean damagesTerrain
    ) {
        super(projectile.world, projectile.shootingEntity, origin.x, origin.y, origin.z, strength, false, damagesTerrain);
        this.world = projectile.world;
        this.projectile = projectile;
        this.directHit = directHit;
        this.origin = origin;
        this.strength = strength;
        this.damagesTerrain = damagesTerrain;
        this.moveDamage = projectile.getDamage();
        if (projectile.getDamageSource() instanceof RiftDamage.RiftElementDamageSource source) {
            this.damageSource = RiftDamage.getElementDamageSource(
                    "explosion.player", projectile, projectile.shootingEntity, source.element
            ).setExplosion();
        }
        else this.damageSource = DamageSource.causeExplosionDamage(this);
    }

    public static void create(
            @NotNull IProjectile projectile, @Nullable EntityLivingBase directHit,
            @NotNull Vec3d origin, float strength
    ) {
        if (!(projectile instanceof RiftProjectile source) || !(source.world instanceof WorldServer world) || strength <= 0f) return;
        // RiftLibrary supplies the projectile's pre-impact position, so recover the contacted surface.
        Vec3d movement = projectile.getMotionVector();
        Vec3d end = origin.add(movement).add(movement.normalize().scale(0.1D));
        RayTraceResult contact;
        if (directHit == null) contact = world.rayTraceBlocks(origin, end, false, true, false);
        else contact = directHit.getEntityBoundingBox().grow(0.3D).calculateIntercept(origin, end);
        if (contact != null) origin = contact.hitVec;
        boolean damagesTerrain = ForgeEventFactory.getMobGriefingEvent(world, source.shootingEntity);
        RiftMoveExplosion explosion = new RiftMoveExplosion(source, directHit, origin, strength, damagesTerrain);
        if (ForgeEventFactory.onExplosionStart(world, explosion)) return;
        explosion.doExplosionA();
        explosion.doExplosionB(false);
        if (!damagesTerrain) explosion.clearAffectedBlockPositions();
        for (EntityPlayer player : world.playerEntities) {
            if (player instanceof EntityPlayerMP serverPlayer && player.getDistanceSq(origin.x, origin.y, origin.z) < 4096D) {
                serverPlayer.connection.sendPacket(new SPacketExplosion(
                        origin.x, origin.y, origin.z, strength,
                        explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(player)
                ));
            }
        }
    }

    @Override
    public void doExplosionA() {
        // Preserve vanilla block resistance and blast propagation without its fixed damage formula.
        if (this.damagesTerrain) {
            Set<BlockPos> affectedBlocks = new HashSet<>();
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (x != 0 && x != 15 && y != 0 && y != 15 && z != 0 && z != 15) continue;
                        Vec3d direction = new Vec3d(
                                MathUtil.slopeResult(x, true, 0D, 15D, -1D, 1D),
                                MathUtil.slopeResult(y, true, 0D, 15D, -1D, 1D),
                                MathUtil.slopeResult(z, true, 0D, 15D, -1D, 1D)
                        ).normalize();
                        float energy = this.strength * (0.7f + this.world.rand.nextFloat() * 0.6f);
                        Vec3d point = this.origin;
                        while (energy > 0f) {
                            BlockPos position = new BlockPos(point);
                            IBlockState state = this.world.getBlockState(position);
                            if (state.getMaterial() != Material.AIR) {
                                float resistance;
                                if (this.projectile.shootingEntity != null) {
                                    resistance = this.projectile.shootingEntity.getExplosionResistance(this, this.world, position, state);
                                }
                                else resistance = state.getBlock().getExplosionResistance(this.world, position, null, this);
                                energy -= (resistance + 0.3f) * 0.3f;
                            }
                            if (energy > 0f && (this.projectile.shootingEntity == null
                                    || this.projectile.shootingEntity.canExplosionDestroyBlock(this, this.world, position, state, energy))) {
                                affectedBlocks.add(position);
                            }
                            point = point.add(direction.scale(0.3D));
                            energy -= 0.22500001f;
                        }
                    }
                }
            }
            this.getAffectedBlockPositions().addAll(affectedBlocks);
        }

        double radius = this.strength * 2D;
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(
                this.projectile.shootingEntity,
                new AxisAlignedBB(this.origin, this.origin).grow(radius + 1D)
        );
        ForgeEventFactory.onExplosionDetonate(this.world, this, entities, radius);
        Set<Entity> affectedEntities = new HashSet<>();
        for (Entity candidate : entities) {
            Entity entity = candidate instanceof MultiPartEntityPart part ? (Entity) part.parent : candidate;
            if (entity == this.projectile || entity == this.projectile.shootingEntity || entity.isImmuneToExplosions()
                    || !affectedEntities.add(entity)) continue;
            if (this.projectile.shootingEntity != null && this.projectile.shootingEntity.isPassenger(entity)) continue;
            double distance = entity.getDistance(this.origin.x, this.origin.y, this.origin.z) / radius;
            if (distance >= 1D) continue;
            double exposure = this.world.getBlockDensity(this.origin, entity.getEntityBoundingBox());
            double impact = MathUtil.slopeResult(distance, true, 0D, 1D, 1D, 0D) * exposure;
            if (impact <= 0D) continue;

            // RiftLibProjectile applies the direct target's normal move damage after this callback.
            if (entity != this.directHit) entity.attackEntityFrom(this.damageSource, (float) (this.moveDamage * impact));
            Vec3d direction = entity.getPositionVector().add(0D, entity.getEyeHeight(), 0D).subtract(this.origin).normalize();
            double knockback = impact;
            if (entity instanceof EntityLivingBase living) knockback = EnchantmentProtection.getBlastDamageReduction(living, impact);
            entity.motionX += direction.x * knockback;
            entity.motionY += direction.y * knockback;
            entity.motionZ += direction.z * knockback;
            if (entity instanceof EntityPlayer player && !player.isSpectator()
                    && (!player.isCreative() || !player.capabilities.isFlying)) {
                this.getPlayerKnockbackMap().put(player, direction.scale(impact));
            }
        }
    }
}
