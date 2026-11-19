package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.entity.RiftVineCage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VineCageBuilder extends CreatureMoveBuilder {
    public VineCageBuilder() {
        this.setBasePower(10)
                .setElement(Element.GRASS)
                .setMoveType(MoveType.PHYSICAL)
                .setStaminaCost(0.16f)
                .setRequireFindTargetToUse()
                .setUseLookBasedFacing()
                .setCanUse(creature -> {
                    if (creature.getControllingPassenger() == null || creature.getRiddenAimPosition() == null) return true;
                    RayTraceResult hit = creature.getCrosshairTargets();
                    return hit != null && hit.typeOfHit != RayTraceResult.Type.MISS && isWithinRange(creature, hit.hitVec);
                })
                .setCooldown(400)
                .setOnMoveHitEffect(RiftVineCage::spawn)
                .setRiddenAimingType(RiddenAimingType.TARGETED_MELEE)
                .setRiddenHorizontalRange(8D)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(RiddenCrosshairType.POINT);
    }

    public static boolean isWithinRange(@NotNull ICreature creature, @NotNull Vec3d destination) {
        Vec3d position = creature.getPositionVector();
        double reach = creature.getSize()[0] * 0.5D + 8D;
        return Math.pow(destination.x - position.x, 2D) + Math.pow(destination.z - position.z, 2D) <= reach * reach + 1E-6D;
    }

    @Nullable
    public static Vec3d getStrikeFloor(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        if (target != null && target.isEntityAlive()) {
            Vec3d feet = new Vec3d(target.posX, target.getEntityBoundingBox().minY, target.posZ);
            return findFloor(creature, feet, feet.y - 1D, feet.y);
        }

        RayTraceResult hit = creature.getCrosshairTargets();
        if (hit != null) {
            if (hit.typeOfHit != RayTraceResult.Type.BLOCK) return null;
            BlockPos block = hit.getBlockPos();
            List<AxisAlignedBB> surfaces = new ArrayList<>();
            creature.getEntityWorld().getBlockState(block).addCollisionBoxToList(
                    creature.getEntityWorld(), block, new AxisAlignedBB(block).grow(1D), surfaces, null, false
            );
            return findFloor(hit.hitVec, surfaces, block.getY(), block.getY() + 2D);
        }
        double yaw = Math.toRadians(creature.getAnimationData().getModelYaw());
        double distance = creature.getSize()[0] * 0.5D + 1D;
        Vec3d front = creature.getPositionVector().add(-Math.sin(yaw) * distance, 0D, Math.cos(yaw) * distance);
        return findFloor(creature, front, front.y - 1D, front.y + 1D);
    }

    @Nullable
    private static Vec3d findFloor(@NotNull ICreature creature, @NotNull Vec3d destination, double minimumY, double maximumY) {
        List<AxisAlignedBB> surfaces = creature.getEntityWorld().getCollisionBoxes(null, new AxisAlignedBB(
                destination.x - 0.001D, minimumY - 0.001D, destination.z - 0.001D,
                destination.x + 0.001D, maximumY + 0.001D, destination.z + 0.001D
        ));
        return findFloor(destination, surfaces, minimumY, maximumY);
    }

    @Nullable
    private static Vec3d findFloor(@NotNull Vec3d destination, @NotNull List<AxisAlignedBB> surfaces, double minimumY, double maximumY) {
        Vec3d floor = null;
        for (AxisAlignedBB surface : surfaces) {
            if (destination.x < surface.minX || destination.x > surface.maxX
                    || destination.z < surface.minZ || destination.z > surface.maxZ
                    || surface.maxY < minimumY - 1E-6D || surface.maxY > maximumY + 1E-6D
            ) {
                continue;
            }
            if (floor == null || surface.maxY > floor.y) floor = new Vec3d(destination.x, surface.maxY, destination.z);
        }
        return floor;
    }

    public static boolean canTarget(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        if (target == null || !target.isEntityAlive() || !isWithinRange(creature, target.getPositionVector())) return false;
        Vec3d feet = new Vec3d(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        return findFloor(creature, feet, feet.y - 1D, feet.y) != null;
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("vine_cage", MoveCategory.RANGED);
            this.setPriorityPredicate(VineCageBuilder::canTarget);
        }
    }
}
