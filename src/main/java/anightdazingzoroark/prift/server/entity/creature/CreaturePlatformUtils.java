package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.builder.CreatureDomesticationBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureDomesticationBuilder.PlatformSaddle;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.IShipData;
import valkyrienwarfare.api.RiftedValkyriumAPI;
import valkyrienwarfare.api.ShipBuildOptions;
import valkyrienwarfare.api.ShipEntityDeathBehavior;
import valkyrienwarfare.api.TransformType;

import java.util.UUID;

//helper utils for platform saddle
public class CreaturePlatformUtils {
    public static void tick(@NotNull RiftCreature creature) {
        if (!(creature.world instanceof WorldServer world) || !creature.supportsPlatformSaddle()) return;
        if (!creature.isEntityAlive()) return;

        CreatureDomesticationBuilder domesticationBuilder = creature.getCreatureType().getDomestication();
        if (domesticationBuilder == null) return;

        PlatformSaddle platform = domesticationBuilder.getPlatformSaddle();
        if (platform == null) return;

        NBTTagCompound state = creature.getEntityData().getCompoundTag("PlatformSaddle");
        creature.getEntityData().setTag("PlatformSaddle", state);
        UUID shipId = state.hasUniqueId("Ship") ? state.getUniqueId("Ship") : null;
        if (shipId != null && state.getInteger("Dimension") != creature.dimension) return;

        IShipData ship = null;
        if (shipId != null) ship = RiftedValkyriumAPI.getShipByUUID(world, shipId).orElse(null);
        if (ship == null && shipId != null) {
            // Assembly runs after entity ticking. Never queue duplicates while it is pending.
            if (world.getTotalWorldTime() - state.getLong("Requested") < 100L) return;
            if (state.hasKey("Assembly")) {
                BlockPos seed = BlockPos.fromLong(state.getLong("Assembly"));
                Iterable<BlockPos> blockRange = BlockPos.getAllInBox(
                        seed.add(-platform.initialX() / 2, -platform.initialY() / 2, -platform.initialZ() / 2),
                        seed.add((platform.initialX() - 1) / 2, (platform.initialY() - 1) / 2, (platform.initialZ() - 1) / 2)
                );

                for (BlockPos pos : blockRange) {
                    if (world.getBlockState(pos).getBlock() == RiftBlocks.PLATFORM_FLOOR) world.setBlockToAir(pos);
                }
            }
            state.removeTag("ShipMost");
            state.removeTag("ShipLeast");
            state.removeTag("Assembly");
        }

        if (ship != null && !creature.isPlatformEquipped()) {
            IPhysicsEntity physics = RiftedValkyriumAPI.getPhysicsEntity(world, ship).orElse(null);
            if (physics == null) return;
            AxisAlignedBB bounds = ship.getShipBB();
            AxisAlignedBB localBounds = getLocalBounds(physics, bounds);
            for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(localBounds.minX, localBounds.minY, localBounds.minZ),
                    new BlockPos(localBounds.maxX, localBounds.maxY, localBounds.maxZ))) {
                if (RiftedValkyriumAPI.getShipManagingBlock(world, pos).filter(data -> data.getUuid().equals(shipId)).isEmpty()) continue;
                world.destroyBlock(pos, true);
            }
            state.removeTag("ShipMost");
            state.removeTag("ShipLeast");
            state.removeTag("Assembly");
            return;
        }

        if (!creature.isPlatformEquipped() || !creature.isTamed()) return;
        Vec3d center = CreatureLookUtil.getSourceLocatorPosition(creature, platform.locator(), 1f);
        if (center == null) return;
        Vec3d offset = new Vec3d(
                -(platform.initialX() % 2) / 2D,
                -platform.maxY() / 2D - (platform.initialY() + 1) / 2D,
                -(platform.initialZ() % 2) / 2D
        );
        if (ship != null) {
            RiftedValkyriumAPI.linkShipToEntity(world, ship, creature, offset, ShipEntityDeathBehavior.UNLOAD, platform.locator());
            state.removeTag("Assembly");
            return;
        }

        // A clear staging floor prevents assembly from taking or replacing terrain or another ship's blocks.
        BlockPos seed = new BlockPos(center.subtract(0D, (platform.initialY() + 1) / 2D, 0D));
        BlockPos min = seed.add(-platform.initialX() / 2, -platform.initialY() / 2, -platform.initialZ() / 2);
        BlockPos max = min.add(platform.initialX() - 1, platform.initialY() - 1, platform.initialZ() - 1);
        AxisAlignedBB floorBounds = new AxisAlignedBB(min, max.add(1, 1, 1));
        if (min.getY() < 0 || max.getY() >= world.getHeight() || !world.isAreaLoaded(min, max)) return;
        for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
            if (!world.isAirBlock(pos) || RiftedValkyriumAPI.isPosInShipyard(world, pos)) return;
        }
        for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
            world.setBlockState(pos, RiftBlocks.PLATFORM_FLOOR.getDefaultState(), 2);
        }
        try {
            ship = RiftedValkyriumAPI.createNewShip(
                    world, new Vec3d(seed.getX(), seed.getY(), seed.getZ()),
                    floorBounds,
                    new ShipBuildOptions()
                            .setMaxShipBounds(platform.maxX(), platform.maxY(), platform.maxZ())
                            .setShipBlockBlacklist((candidateWorld, pos, block) -> !floorBounds.contains(
                                    new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D))
                                    || block.getBlock() != RiftBlocks.PLATFORM_FLOOR)
                            .setEntityPeggedTo(creature, offset, ShipEntityDeathBehavior.UNLOAD, platform.locator())
            );
            state.setUniqueId("Ship", ship.getUuid());
            state.setInteger("Dimension", creature.dimension);
            state.setLong("Assembly", seed.toLong());
            state.setLong("Requested", world.getTotalWorldTime());
        }
        catch (RuntimeException exception) {
            for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
                if (world.getBlockState(pos).getBlock() == RiftBlocks.PLATFORM_FLOOR) world.setBlockToAir(pos);
            }
            throw exception;
        }
    }

    public static boolean dismountOntoPlatform(@NotNull RiftCreature creature, @NotNull EntityLivingBase rider) {
        if (!(creature.world instanceof WorldServer world) || rider.world != world || rider.isRiding()
                || !rider.isEntityAlive() || !creature.isEntityAlive() || !creature.isPlatformEquipped()
                || world.getEntityByID(creature.getEntityId()) != creature
                || world.getEntityByID(rider.getEntityId()) != rider
        ) {
            return false;
        }
        CreatureDomesticationBuilder domesticationBuilder = creature.getCreatureType().getDomestication();
        if (domesticationBuilder == null) return false;

        PlatformSaddle platform = domesticationBuilder.getPlatformSaddle();
        if (platform == null) return false;

        NBTTagCompound state = creature.getEntityData().getCompoundTag("PlatformSaddle");
        UUID shipId = state.hasUniqueId("Ship") ? state.getUniqueId("Ship") : null;
        if (shipId == null || state.getInteger("Dimension") != creature.dimension) return false;

        IShipData ship = RiftedValkyriumAPI.getShipByUUID(world, shipId).orElse(null);
        if (ship == null) return false;

        IPhysicsEntity physics = RiftedValkyriumAPI.getPhysicsEntity(world, ship).orElse(null);
        if (physics == null) return false;

        Vec3d up = physics.rotateVector(new Vec3d(0D, 1D, 0D), TransformType.SUBSPACE_TO_GLOBAL);
        if (up.y < 0.5D) return false;

        AxisAlignedBB localBounds = getLocalBounds(physics, ship.getShipBB());
        BlockPos min = new BlockPos(localBounds.minX, localBounds.minY, localBounds.minZ);
        BlockPos max = new BlockPos(localBounds.maxX, localBounds.maxY, localBounds.maxZ);

        Vec3d center = CreatureLookUtil.getSourceLocatorPosition(creature, platform.locator(), 1f);
        if (center == null) return false;

        Vec3d destination = null;
        double closestDistance = Double.MAX_VALUE;
        double halfWidth = rider.width / 2D;
        // An upright rider's entire footprint must clear a tilted deck.
        double clearance = halfWidth * (Math.abs(up.x) + Math.abs(up.z)) / up.y + 0.02D;
        for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
            // Rotated ship bounds can include empty, unloaded shipyard chunks.
            if (!world.isBlockLoaded(pos)) continue;
            IBlockState block = world.getBlockState(pos);
            if (block.getBlock() == Blocks.MAGMA || !block.isSideSolid(world, pos, EnumFacing.UP)) continue;
            if (RiftedValkyriumAPI.getShipManagingBlock(world, pos).filter(data -> data.getUuid().equals(ship.getUuid())).isEmpty()) continue;

            AxisAlignedBB support = block.getCollisionBoundingBox(world, pos);
            if (support == null) continue;

            Vec3d platformTestPos = new Vec3d(pos.getX() + 0.5D, pos.getY() + support.maxY, pos.getZ() + 0.5D);
            Vec3d candidate = physics.transformVector(platformTestPos, TransformType.SUBSPACE_TO_GLOBAL).add(0D, clearance, 0D);
            double distance = candidate.squareDistanceTo(center);
            if (distance >= closestDistance) continue;

            AxisAlignedBB standingBounds = new AxisAlignedBB(
                    candidate.x - halfWidth, candidate.y, candidate.z - halfWidth,
                    candidate.x + halfWidth, candidate.y + rider.height, candidate.z + halfWidth
            );
            AxisAlignedBB localStandingBounds = getLocalBounds(physics, standingBounds);
            if (!world.getWorldBorder().contains(standingBounds)
                    || !world.getCollisionBoxes(rider, standingBounds).isEmpty()
                    || !world.getCollisionBoxes(null, localStandingBounds).isEmpty()
                    || world.containsAnyLiquid(standingBounds) || world.containsAnyLiquid(localStandingBounds)
                    || world.isFlammableWithin(standingBounds) || world.isFlammableWithin(localStandingBounds)
            ) {
                continue;
            }
            // Use current bounds even before a teleported rider's chunk membership updates.
            if (world.loadedEntityList.stream().anyMatch(entity -> entity != rider && entity != creature
                    && entity instanceof EntityLivingBase && entity.isEntityAlive()
                    && EntitySelectors.NOT_SPECTATING.apply(entity)
                    && entity.getEntityBoundingBox().intersects(standingBounds))
            ) {
                continue;
            }
            destination = candidate;
            closestDistance = distance;
        }
        if (destination == null) return false;
        rider.fallDistance = 0f;
        rider.motionX = 0D;
        rider.motionY = 0D;
        rider.motionZ = 0D;
        // Detach on the client before teleporting so its passenger tick cannot restore the seat position.
        if (rider instanceof EntityPlayerMP player) {
            player.connection.sendPacket(new SPacketSetPassengers(creature));
        }
        rider.setPositionAndUpdate(destination.x, destination.y, destination.z);
        return true;
    }

    @NotNull
    public static AxisAlignedBB getLocalBounds(@NotNull IPhysicsEntity physics, @NotNull AxisAlignedBB bounds) {
        Vec3d first = physics.transformVector(new Vec3d(bounds.minX, bounds.minY, bounds.minZ), TransformType.GLOBAL_TO_SUBSPACE);
        AxisAlignedBB localBounds = new AxisAlignedBB(first, first);
        for (int corner = 1; corner < 8; corner++) {
            Vec3d point = physics.transformVector(new Vec3d(
                    (corner & 1) == 0 ? bounds.minX : bounds.maxX,
                    (corner & 2) == 0 ? bounds.minY : bounds.maxY,
                    (corner & 4) == 0 ? bounds.minZ : bounds.maxZ
            ), TransformType.GLOBAL_TO_SUBSPACE);
            localBounds = localBounds.union(new AxisAlignedBB(point, point));
        }
        return localBounds;
    }

}
