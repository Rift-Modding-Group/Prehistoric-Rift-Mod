package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RootSpikeBuilder extends CreatureMoveBuilder {
    public RootSpikeBuilder() {
        this.setBasePower(50)
                .setElement(Element.GRASS)
                .setMoveType(MoveType.SPECIAL)
                .setStaminaCost(0.08f)
                .setRequireFindTargetToUse()
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    if (creature.getEntityWorld().isRemote) return;
                    RayTraceResult hit = creature.getCrosshairTargets();
                    EntityLivingBase target = null;
                    if (creature.getControllingPassenger() == null) target = creature.getAttackTarget();
                    else if (hit != null && hit.entityHit instanceof EntityLivingBase living) target = living;

                    Vec3d floor = getStrikeFloor(creature, target, hit);
                    if (floor == null || !isWithinRange(creature, floor)) return;

                    creature.createMoveGraphic(
                            new CreatureMoveGraphic("root_spike", "animation.move_graphic.root_spike", null, CreatureMoveGraphic.Placement.TARGET_GROUND),
                            target
                    );
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("root_stab.use"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    if (target == null) target = findTargetAtFloor(creature, floor);
                    if (target != null && target.isEntityAlive() && !creature.isRelatedToEntity(target)
                            && isWithinRange(creature, target.getPositionVector())
                    ) {
                        creature.attackEntityAsMob(target);

                        creature.getEntityWorld().playSound(
                                null, target.posX, target.posY, target.posZ,
                                RiftSounds.getSound("root_stab.use"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                    }
                })
                .setCooldown(200)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenHorizontalRange(8D)
                .setRiddenPitchBounds(-45f, 90f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT);
    }

    private static boolean isWithinRange(@NotNull ICreature creature, @NotNull Vec3d destination) {
        Vec3d position = creature.getPositionVector();
        double reach = creature.getSize()[0] * 0.5D + 8D;
        return Math.pow(destination.x - position.x, 2D) + Math.pow(destination.z - position.z, 2D) <= reach * reach + 1E-6D;
    }

    @Nullable
    private static Vec3d getStrikeFloor(@NotNull ICreature creature, @Nullable EntityLivingBase target, @Nullable RayTraceResult hit) {
        if (target != null && target.isEntityAlive()) {
            Vec3d feet = new Vec3d(target.posX, target.getEntityBoundingBox().minY, target.posZ);
            return findFloor(creature, feet, feet.y - 1D, feet.y);
        }
        if (creature.getRiddenAimPosition() != null) {
            if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return null;
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
                    || surface.maxY < minimumY - 1E-6D || surface.maxY > maximumY + 1E-6D) continue;
            if (floor == null || surface.maxY > floor.y) floor = new Vec3d(destination.x, surface.maxY, destination.z);
        }
        return floor;
    }

    @Nullable
    private static EntityLivingBase findTargetAtFloor(@NotNull ICreature creature, @NotNull Vec3d floor) {
        return creature.getEntityWorld().getEntitiesWithinAABB(
                EntityLivingBase.class,
                new AxisAlignedBB(floor, floor.add(0D, 1D, 0D)).grow(1D, 0.001D, 1D),
                living -> living != null && living != creature && living != creature.getControllingPassenger()
                        && living.isEntityAlive() && !creature.isRelatedToEntity(living)
                        && living.getEntityBoundingBox().minY >= floor.y - 1E-6D
                        && living.getEntityBoundingBox().minY <= floor.y + 1D + 1E-6D
                        && isWithinRange(creature, living.getPositionVector())
        ).stream().min(Comparator.comparingDouble(living -> living.getDistanceSq(floor.x, floor.y, floor.z))).orElse(null);
    }

    private static boolean canTarget(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        if (target == null || !target.isEntityAlive() || !isWithinRange(creature, target.getPositionVector())) return false;
        Vec3d feet = new Vec3d(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        return findFloor(creature, feet, feet.y - 1D, feet.y) != null;
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("root_spike", MoveCategory.RANGED);
            this.setPriorityPredicate(RootSpikeBuilder::canTarget).setUseWhenFrustrated();
        }
    }
}
