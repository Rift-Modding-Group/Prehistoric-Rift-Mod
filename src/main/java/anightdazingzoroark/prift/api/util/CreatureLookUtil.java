package anightdazingzoroark.prift.api.util;

import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry.MoveDefinition;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.AnimationHitboxTimeline;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

public class CreatureLookUtil {
    //head pitch min and max bounds
    //todo: replace w something more flexible as not all creatures will use these...
    public static final float HEAD_PITCH_MIN = -15f;
    public static final float HEAD_PITCH_MAX = 15f;

    /**
     * Predict a locator's world-space ray from a sampled pose without changing the live animation.
     * Uses the same model-to-world conversion as RiftLibrary's ray emission.
     * */
    @NotNull
    public static RiftLibRay.RayPose getRayPose(
            @NotNull AnimationDataEntityLiving data, @NotNull AnimatedLocator locator,
            @NotNull Map<String, BoneSnapshot> pose
    ) {
        Map<IBone, BoneSnapshot> previousPose = new HashMap<>();
        pose.forEach((name, snapshot) -> {
            IBone bone = data.getBoneSnapshotCollection().get(name).getLeft();
            previousPose.put(bone, new BoneSnapshot(bone));
            bone.getRotation().set(snapshot.getRotation());
            bone.getPosition().set(snapshot.getPosition());
            bone.getScale().set(snapshot.getScale());
        });
        Vec3d mouth = locator.getModelSpacePosition();
        Quaternion orientation = locator.getModelSpaceYXZQuaternion();
        Vec3d forward = VectorUtils.rotateVectorWithQuaternion(new Vec3d(0D, 0D, -1D), orientation);
        Vec3d up = VectorUtils.rotateVectorWithQuaternion(new Vec3d(0D, 1D, 0D), orientation);
        previousPose.forEach((bone, snapshot) -> {
            bone.getRotation().set(snapshot.getRotation());
            bone.getPosition().set(snapshot.getPosition());
            bone.getScale().set(snapshot.getScale());
        });

        // Model coordinates are in pixels, with X and Z opposite to entity coordinates.
        Quaternion yaw = QuaternionUtils.createYXZQuaternion(0D, -Math.toRadians(data.getModelYaw()), 0D);
        Vec3d origin = data.getHolder().getPositionVector().add(VectorUtils.rotateVectorWithQuaternion(
                new Vec3d(-mouth.x, mouth.y, -mouth.z).scale(data.getScale() / 16D), yaw));
        Vec3d direction = VectorUtils.rotateVectorWithQuaternion(new Vec3d(-forward.x, forward.y, -forward.z), yaw);
        Vec3d worldUp = VectorUtils.rotateVectorWithQuaternion(new Vec3d(-up.x, up.y, -up.z), yaw);
        return new RiftLibRay.RayPose(origin, direction, worldUp);
    }

    /**
     * Capture a straight ray's aim from its live locator to the AI target, or from the rider's look.
     * Animations distribute ray_release_pitch and ray_aim_yaw across their own bones.
     */
    public static void captureRayLook(@NotNull ICreature creature, @Nullable Entity target, @NotNull String locatorName) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        Entity rider = creature.getControllingPassenger();
        double pitch;
        double yaw;
        if (rider != null) {
            // Living entities' getLook() uses head yaw on the server, which can lag steering packets.
            // The crosshair and mounted model both follow the rider's actual input angles.
            pitch = rider.rotationPitch;
            yaw = MathHelper.wrapDegrees(rider.rotationYaw - data.getModelYaw());
        }
        else {
            AnimatedLocator locator = data.getAnimatedLocator(locatorName);
            if (locator == null || target == null || !target.isEntityAlive()) return;
            Vec3d origin = getRayPose(data, locator, Map.of()).origin();
            Vec3d direction = target.getPositionVector().add(0D, target.height * 0.5D, 0D).subtract(origin);
            if (direction.lengthSquared() <= 1E-12D) return;
            pitch = Math.toDegrees(Math.atan2(-direction.y, Math.hypot(direction.x, direction.z)));
            yaw = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(-direction.x, direction.z)) - data.getModelYaw());
        }
        double swing = MathUtil.slopeResult(data.getHeadPitch(1f), true, HEAD_PITCH_MIN, 0D, -1D, 1D);
        data.setVariable("ray_body_aim", rider == null ? 1D : 0D);
        data.setVariable("ray_aim_swing", swing);
        data.setVariable("ray_aim_yaw", yaw);
        data.setVariable("ray_release_pitch", pitch);
        double windupPitch;
        if (pitch < 0D) windupPitch = Math.max(10D, pitch + 30D);
        else windupPitch = pitch - 30D;
        data.setVariable("ray_windup_pitch", windupPitch);
    }

    /**
     * Clamp input as well as interpolation history so excess mouse movement cannot accumulate.
     * */
    public static void clampRiddenAimPitch(
            @NotNull EntityPlayer rider, @Nullable ICreature creature, @Nullable String moveName
    ) {
        float[] bounds = getRiddenAimPitchBounds(creature, moveName);
        rider.rotationPitch = Math.clamp(rider.rotationPitch, bounds[0], bounds[1]);
        rider.prevRotationPitch = Math.clamp(rider.prevRotationPitch, bounds[0], bounds[1]);
    }

    public static float @NotNull [] getRiddenAimPitchBounds(@Nullable ICreature creature, @Nullable String moveName) {
        CreatureMoveBuilder move = getRiddenAimMove(creature, moveName);
        return move == null ? new float[]{HEAD_PITCH_MIN, HEAD_PITCH_MAX} : move.getRiddenPitchBounds();
    }

    @Nullable
    public static CreatureMoveBuilder getRiddenAimMove(@Nullable ICreature creature, @Nullable String moveName) {
        if (creature == null || moveName == null) return null;
        AbstractCreatureBuilder<?> configuration = creature.getCreatureType();
        if (!creature.getPhase().isEmpty()) configuration = creature.getCreatureType().getPhaseBuilderMaps().get(creature.getPhase());
        if (configuration == null) return null;
        MoveDefinition definition = configuration.getMoveDefinitions().get(moveName);
        return definition == null ? null : definition.moveBuilder();
    }

    /**
     * Discover head-driven attack hitboxes from the move's animation effects and aim across
     * their usable height. Attacks unaffected by head pitch retain the normal camera focus.
     * */
    @Nullable
    @SuppressWarnings("SuspiciousNameCombination")
    public static Vec3d getRiddenAimHitboxPosition(
            @NotNull ICreature creature, @NotNull String moveName, float headPitch, float bodyYaw, double partialTicks
    ) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        double previousPitch = data.getVariable("head_attack_pitch");
        double previousYaw = data.getVariable("head_attack_yaw");
        double previousSwing = data.getVariable("head_attack_swing");
        double previousUpward = data.getVariable("head_attack_upward");
        headPitch = Math.clamp(headPitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
        data.setVariable("head_attack_pitch", headPitch);
        data.setVariable("head_attack_yaw", 0D);
        data.setVariable("head_attack_swing", MathUtil.slopeResult(headPitch, true, HEAD_PITCH_MIN, 0D, -1D, 1D));
        data.setVariable("head_attack_upward", headPitch < 0f ? 1D : 0D);
        HeadStrikePose strike = getHeadStrikePose(creature, moveName);
        Vec3d result = null;
        if (strike != null) {
            // Tail attacks whose hitboxes do not follow head pitch keep the normal camera focus.
            boolean followsHeadPitch = !strike.sampleBounds(HEAD_PITCH_MIN, 0f, Vec3d.ZERO)
                    .equals(strike.sampleBounds(HEAD_PITCH_MAX, 0f, Vec3d.ZERO));
            if (followsHeadPitch) {
                EntityLivingBase holder = data.getHolder();
                Vec3d position = new Vec3d(
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, holder.prevPosX, holder.posX),
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, holder.prevPosY, holder.posY),
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, holder.prevPosZ, holder.posZ)
                );
                result = getHeadAttackFocus(strike.sampleBounds(headPitch, bodyYaw, position), headPitch);
            }
        }
        data.setVariable("head_attack_pitch", previousPitch);
        data.setVariable("head_attack_yaw", previousYaw);
        data.setVariable("head_attack_swing", previousSwing);
        data.setVariable("head_attack_upward", previousUpward);
        return result;
    }

    /**
     * Shared rider target for pose capture, including attacks released after charging.
     * */
    @Nullable
    public static Vec3d getRiddenAttackPosition(@NotNull ICreature creature) {
        if (!(creature.getControllingPassenger() instanceof EntityPlayer rider)) return null;
        Vec3d aim = creature.getRiddenAimPosition();
        if (aim != null) return aim;

        double yaw = Math.toRadians(rider.rotationYaw);
        double reach = 3D * creature.getAnimationData().getScale();
        return creature.getPositionVector().add(-Math.sin(yaw) * reach, 0D, Math.cos(yaw) * reach);
    }

    @NotNull
    public static LookPose getAttackLookPose(@NotNull ICreature creature) {
        Vec3d target = getRiddenAttackPosition(creature);
        if (target == null) return getLookPose(creature);

        AnimationDataEntityLiving data = creature.getAnimationData();
        Vec3d offset = target.subtract(data.getHolder().getPositionEyes(1f));
        double horizontalDistance = Math.hypot(offset.x, offset.z);
        float pitch = (float) Math.toDegrees(Math.atan2(-offset.y, horizontalDistance));
        float yaw = 0f;
        if (horizontalDistance > 1E-6D) {
            yaw = MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(-offset.x, offset.z)) - data.getModelYaw());
        }
        return new LookPose(Math.clamp(pitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX), Math.clamp(yaw, -15f, 15f));
    }

    /**
     * The last evaluated look pose, shared by animation capture and attack facing.
     * */
    @NotNull
    public static LookPose getLookPose(@NotNull ICreature creature) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        float pitch = data.getHeadPitch(1f);
        float yaw = data.getHeadYaw(1f);
        // Steering may already have changed the head fields; prefer the last animated neck pose.
        if (data.getBoneSnapshotCollection().containsKey("neck")) {
            BoneSnapshot neck = data.getBoneSnapshotCollection().get("neck").getRight();
            BoneSnapshot rest = data.getBoneSnapshotCollection().get("neck").getLeft().getInitialSnapshot();
            pitch = (float) -Math.toDegrees(neck.getRotation().x - rest.getRotation().x);
            yaw = (float) -Math.toDegrees(neck.getRotation().y - rest.getRotation().y);
        }
        return new LookPose(Math.clamp(pitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX), Math.clamp(yaw, -15f, 15f));
    }

    /**
     * Face the target while retaining the local yaw already saved into the attack animation.
     * */
    public static float getLookBasedAttackYaw(@NotNull ICreature creature, @NotNull EntityLivingBase target) {
        return getLookBasedAttackYaw(creature, target, creature.getPositionVector());
    }

    /**
     * Also used to predict facing at a prospective attack position.
     * */
    public static float getLookBasedAttackYaw(@NotNull ICreature creature, @NotNull EntityLivingBase target, @NotNull Vec3d position) {
        float bodyYaw = creature.getAnimationData().getModelYaw();
        Vec3d offset = target.getPositionVector().subtract(position);
        if (offset.x * offset.x + offset.z * offset.z < 1E-4D) return bodyYaw;

        float targetYaw = (float) Math.toDegrees(Math.atan2(-offset.x, offset.z));
        float lookYaw = getLookPose(creature).yaw();
        return bodyYaw + MathHelper.wrapDegrees(targetYaw - lookYaw - bodyYaw);
    }


    /**
     * Captures head aim using the owning move's animation bindings and active hitbox intervals.
     * Animations without offense hitboxes retain the captured look direction.
     */
    public static void captureHeadAttackLook(@NotNull ICreature creature, @NotNull String moveName) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        LookPose look = getAttackLookPose(creature);
        data.setVariable("head_attack_pitch", look.pitch());
        data.setVariable("head_attack_yaw", look.yaw());
        data.setVariable("head_attack_swing", MathUtil.slopeResult(look.pitch(), true, HEAD_PITCH_MIN, 0D, -1D, 1D));
        data.setVariable("head_attack_upward", look.pitch() < 0f ? 1D : 0D);

        Vec3d target = getRiddenAttackPosition(creature);
        if (target == null) return;
        HeadStrikePose strike = getHeadStrikePose(creature, moveName);
        if (strike == null) return;

        float pitch = fitHeadAttackPitch(target, look.pitch(), value -> strike.sampleBounds(
                (float) value, data.getModelYaw(), data.getHolder().getPositionVector()));
        data.setVariable("head_attack_pitch", pitch);
        data.setVariable("head_attack_swing", MathUtil.slopeResult(pitch, true, HEAD_PITCH_MIN, 0D, -1D, 1D));
        data.setVariable("head_attack_upward", pitch < 0f ? 1D : 0D);
    }

    /** Keep the focus inside an actual attack pose, using the full vertical reach of the sweep. */
    @Nullable
    public static Vec3d getHeadAttackFocus(@NotNull List<AxisAlignedBB> bounds, float pitch) {
        if (bounds.isEmpty()) return null;
        double minimum = bounds.stream().mapToDouble(box -> box.minY).min().orElseThrow();
        double maximum = bounds.stream().mapToDouble(box -> box.maxY).max().orElseThrow();
        double fraction = MathUtil.slopeResult(pitch, true, HEAD_PITCH_MIN, HEAD_PITCH_MAX, 0.95D, 0.05D);
        double height = MathUtil.slopeResult(fraction, true, 0D, 1D, minimum, maximum);
        Vec3d reference = new Vec3d(
                bounds.stream().mapToDouble(box -> box.getCenter().x).average().orElseThrow(), height,
                bounds.stream().mapToDouble(box -> box.getCenter().z).average().orElseThrow());
        Vec3d closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (AxisAlignedBB box : bounds) {
            Vec3d point = new Vec3d(Math.clamp(reference.x, box.minX, box.maxX),
                    Math.clamp(reference.y, box.minY, box.maxY), Math.clamp(reference.z, box.minZ, box.maxZ));
            double distance = point.squareDistanceTo(reference);
            if (distance < closestDistance) {
                closest = point;
                closestDistance = distance;
            }
        }
        return closest;
    }

    /** Fit contact anywhere in the damage sweep, rather than fitting an averaged hitbox center. */
    public static float fitHeadAttackPitch(
            @NotNull Vec3d target, float preferredPitch, @NotNull DoubleFunction<List<AxisAlignedBB>> sampleBounds
    ) {
        DoubleUnaryOperator errorAtPitch = pitch -> {
            double error = Double.MAX_VALUE;
            for (AxisAlignedBB box : sampleBounds.apply(pitch)) {
                Vec3d closest = new Vec3d(Math.clamp(target.x, box.minX, box.maxX),
                        Math.clamp(target.y, box.minY, box.maxY), Math.clamp(target.z, box.minZ, box.maxZ));
                error = Math.min(error, closest.squareDistanceTo(target));
            }
            return error;
        };
        float bestPitch = Math.clamp(preferredPitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
        double bestError = errorAtPitch.applyAsDouble(bestPitch);
        for (int step = 0; step <= 30 && bestError > 1E-8D; step++) {
            float pitch = (float) MathUtil.slopeResult(step, true, 0D, 30D, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
            double error = errorAtPitch.applyAsDouble(pitch);
            if (error < bestError) {
                bestPitch = pitch;
                bestError = error;
            }
        }
        double radius = 0.5D;
        for (int step = 0; step < 10 && bestError > 1E-8D; step++) {
            float lower = (float) Math.clamp(bestPitch - radius, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
            float upper = (float) Math.clamp(bestPitch + radius, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
            double lowerError = errorAtPitch.applyAsDouble(lower);
            double upperError = errorAtPitch.applyAsDouble(upper);
            if (lowerError < bestError) {
                bestPitch = lower;
                bestError = lowerError;
            }
            if (upperError < bestError) {
                bestPitch = upper;
                bestError = upperError;
            }
            radius *= 0.5D;
        }
        return bestPitch;
    }

    @Nullable
    private static HeadStrikePose getHeadStrikePose(@NotNull ICreature creature, @NotNull String moveName) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        if (!(data.getHolder() instanceof IAnimatable<?> animatable) || data.getBoneSnapshotCollection().isEmpty()) return null;
        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return null;
        CreatureMoveBuilder move = getRiddenAimMove(creature, moveName);
        if (move == null) return null;
        List<Animation> animations = new ArrayList<>();
        for (String name : CreatureMoveUtil.getPreviewAnimationNames(creature, moveName)) {
            Animation animation = model.getAnimations(name, animatable);
            if (animation == null) return null;
            animations.add(animation);
        }
        List<AnimatedBoundingBox> hitboxes = new ArrayList<>(data.getAnimatedBoundingBoxes().values());
        List<HeadAttackSample> samples = getHeadAttackSamples(CreatureMoveUtil.getHitboxWindows(creature, move, animations, hitboxes, data));
        return samples.isEmpty() ? null : new HeadStrikePose(data, samples);
    }

    /** Sample authored damage intervals and animation hit instants without running move effects. */
    @NotNull
    public static List<HeadAttackSample> getHeadAttackSamples(@NotNull List<AnimationHitboxTimeline.HitboxWindow> windows) {
        List<HeadAttackSample> samples = new ArrayList<>();
        for (Animation animation : windows.stream().map(AnimationHitboxTimeline.HitboxWindow::animation).distinct().toList()) {
            List<HeadAttackSample> animationSamples = new ArrayList<>();
            List<AnimationHitboxTimeline.HitboxWindow> instants = windows.stream()
                    .filter(window -> window.animation() == animation && window.startTick() == window.endTick()).toList();
            for (double tick : instants.stream().map(AnimationHitboxTimeline.HitboxWindow::startTick).distinct().sorted().toList()) {
                List<AnimatedBoundingBox> active = instants.stream().filter(window -> window.startTick() == tick)
                        .map(AnimationHitboxTimeline.HitboxWindow::hitbox).distinct().toList();
                animationSamples.add(new HeadAttackSample(animation, active, tick));
            }
            List<AnimationHitboxTimeline.HitboxWindow> animationWindows = windows.stream()
                    .filter(window -> window.animation() == animation && window.endTick() > window.startTick()).toList();
            List<Double> boundaries = animationWindows.stream()
                    .flatMap(window -> Stream.of(window.startTick(), window.endTick()))
                    .distinct().sorted().toList();
            for (int interval = 1; interval < boundaries.size(); interval++) {
                double start = boundaries.get(interval - 1);
                double end = boundaries.get(interval);
                List<AnimatedBoundingBox> active = animationWindows.stream()
                        .filter(window -> window.startTick() <= start && window.endTick() >= end)
                        .map(AnimationHitboxTimeline.HitboxWindow::hitbox).distinct().toList();
                if (active.isEmpty()) continue;
                int steps = Math.max(1, (int) Math.ceil(end - start));
                for (int step = 0; step <= steps; step++) {
                    double tick = MathUtil.slopeResult(step, true, 0D, steps, start, end);
                    animationSamples.add(new HeadAttackSample(animation, active, tick));
                }
            }
            animationSamples.sort(Comparator.comparingDouble(HeadAttackSample::tick));
            samples.addAll(animationSamples);
        }
        return List.copyOf(samples);
    }

    public record HeadAttackSample(
            @NotNull Animation animation, @NotNull List<AnimatedBoundingBox> hitboxes, double tick
    ) {}

    private record HeadStrikePose(@NotNull AnimationDataEntityLiving data, @NotNull List<HeadAttackSample> samples) {
        @NotNull
        private List<AxisAlignedBB> sampleBounds(float pitch, float yaw, @NotNull Vec3d position) {
            this.data.setVariable("head_attack_pitch", pitch);
            this.data.setVariable("head_attack_swing", MathUtil.slopeResult(pitch, true, HEAD_PITCH_MIN, 0D, -1D, 1D));
            this.data.setVariable("head_attack_upward", pitch < 0f ? 1D : 0D);
            List<AxisAlignedBB> bounds = new ArrayList<>();
            for (HeadAttackSample sample : this.samples) {
                Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(sample.animation(), this.data, sample.tick());
                for (AnimatedBoundingBox hitbox : sample.hitboxes()) {
                    bounds.add(hitbox.getWorldSpaceBounds(this.data.getScale(), yaw, position, pose));
                }
            }
            return bounds;
        }

    }

    public record LookPose(float pitch, float yaw) {}
}
