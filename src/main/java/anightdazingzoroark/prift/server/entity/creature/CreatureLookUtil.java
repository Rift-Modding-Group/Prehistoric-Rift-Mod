package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.AnimationHitboxTimeline;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class CreatureLookUtil {
    //head pitch min and max bounds
    //todo: replace w something more flexible as not all creatures will use these...
    public static final float HEAD_PITCH_MIN = -15f;
    public static final float HEAD_PITCH_MAX = 15f;

    /**
     * Clamp input as well as interpolation history so excess mouse movement cannot accumulate.
     * */
    public static void clampRiddenAimPitch(@NotNull EntityPlayer rider) {
        rider.rotationPitch = Math.clamp(rider.rotationPitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
        rider.prevRotationPitch = Math.clamp(rider.prevRotationPitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
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
        AbstractCreatureBuilder<?> configuration = creature.getCreatureType();
        if (!creature.getPhase().isEmpty()) configuration = creature.getCreatureType().getPhaseBuilderMaps().get(creature.getPhase());
        if (configuration == null) return null;
        AbstractCreatureBuilder.MoveDefinition definition = configuration.getMoveDefinitions().get(moveName);
        if (definition == null) return null;
        CreatureMoveBuilder move = definition.moveBuilder();

        AnimationDataEntityLiving data = creature.getAnimationData();
        if (!(data.getHolder() instanceof IAnimatable<?> animatable) || data.getBoneSnapshotCollection().isEmpty()) return null;
        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return null;
        List<AnimatedBoundingBox> hitboxes = new ArrayList<>(data.getAnimatedBoundingBoxes().values());
        if (hitboxes.isEmpty()) return null;

        // Match the move controller's animation selection, including charge-up phases.
        List<String> animationSuffixes;
        if (move.getMoveChargeupBuilder() != null) {
            animationSuffixes = Arrays.stream(ChargeupPhase.values())
                    .map(phase -> moveName + "_" + phase.name().toLowerCase()).toList();
        }
        else {
            String[] names = move.getAnimNames();
            if (names == null || names.length == 0) return null;
            int index = 0;
            if (creature.getCurrentMove().equals(moveName) && names.length > 1) {
                index = Math.clamp((int) data.getVariable("chosenMove"), 0, names.length - 1);
            }
            animationSuffixes = List.of(names[index]);
        }
        List<Animation> animations = new ArrayList<>();
        for (String suffix : animationSuffixes) {
            Animation animation = model.getAnimations("animation." + creature.getCreatureType().getName() + "." + suffix, animatable);
            if (animation == null) return null;
            animations.add(animation);
        }

        double previousPitch = data.getVariable("head_attack_pitch");
        double previousYaw = data.getVariable("head_attack_yaw");
        double previousSwing = data.getVariable("head_attack_swing");
        headPitch = Math.clamp(headPitch, HEAD_PITCH_MIN, HEAD_PITCH_MAX);
        data.setVariable("head_attack_pitch", headPitch);
        data.setVariable("head_attack_yaw", 0D);
        data.setVariable("head_attack_swing", MathUtil.slopeResult(headPitch, true, HEAD_PITCH_MIN, 0D, -1D, 1D));
        List<AnimationHitboxTimeline.HitboxWindow> windows = AnimationHitboxTimeline.getHitboxWindows(animations, hitboxes, data);
        Vec3d result = null;
        if (!windows.isEmpty()) {
            Animation firstAnimation = windows.getFirst().animation();
            AnimationHitboxTimeline.HitboxWindow firstStrike = windows.stream()
                    .filter(window -> window.animation() == firstAnimation)
                    .min(Comparator.comparingDouble(AnimationHitboxTimeline.HitboxWindow::startTick)).orElseThrow();
            double strikeTick = MathUtil.slopeResult(0.5D, true, 0D, 1D, firstStrike.startTick(), firstStrike.endTick());
            List<AnimatedBoundingBox> activeHitboxes = windows.stream()
                    .filter(window -> window.animation() == firstAnimation && window.startTick() <= strikeTick && window.endTick() >= strikeTick)
                    .map(AnimationHitboxTimeline.HitboxWindow::hitbox).distinct().toList();
            // Let the authored animation determine whether this attack needs head-based
            // camera focus. Tail attacks keep their existing forward-facing camera behavior.
            Map<String, BoneSnapshot> upperPose = sampleHeadAttackPose(firstAnimation, data, HEAD_PITCH_MIN, strikeTick);
            Map<String, BoneSnapshot> lowerPose = sampleHeadAttackPose(firstAnimation, data, HEAD_PITCH_MAX, strikeTick);
            boolean followsHeadPitch = activeHitboxes.stream().anyMatch(hitbox ->
                    !hitbox.getWorldSpaceBounds(data.getScale(), 0f, Vec3d.ZERO, upperPose)
                            .equals(hitbox.getWorldSpaceBounds(data.getScale(), 0f, Vec3d.ZERO, lowerPose)));
            if (followsHeadPitch) {
                Map<String, BoneSnapshot> pose = sampleHeadAttackPose(firstAnimation, data, headPitch, strikeTick);
                EntityLivingBase holder = data.getHolder();
                Vec3d position = new Vec3d(
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, holder.prevPosX, holder.posX),
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, holder.prevPosY, holder.posY),
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, holder.prevPosZ, holder.posZ)
                );
                Vec3d center = Vec3d.ZERO;
                double verticalFraction = MathUtil.slopeResult(headPitch, true, HEAD_PITCH_MIN, HEAD_PITCH_MAX, 0.95D, 0.05D);
                for (AnimatedBoundingBox hitbox : activeHitboxes) {
                    AxisAlignedBB bounds = hitbox.getWorldSpaceBounds(data.getScale(), bodyYaw, position, pose);
                    Vec3d hitboxCenter = bounds.getCenter();
                    double aimHeight = MathUtil.slopeResult(verticalFraction, true, 0D, 1D, bounds.minY, bounds.maxY);
                    center = center.add(hitboxCenter.x, aimHeight, hitboxCenter.z);
                }
                result = center.scale(1D / activeHitboxes.size());
            }
        }
        data.setVariable("head_attack_pitch", previousPitch);
        data.setVariable("head_attack_yaw", previousYaw);
        data.setVariable("head_attack_swing", previousSwing);
        return result;
    }

    @NotNull
    private static Map<String, BoneSnapshot> sampleHeadAttackPose(
            @NotNull Animation animation, @NotNull AnimationDataEntityLiving data, float headPitch, double tick
    ) {
        data.setVariable("head_attack_pitch", headPitch);
        data.setVariable("head_attack_swing", MathUtil.slopeResult(headPitch, true, HEAD_PITCH_MIN, 0D, -1D, 1D));
        return AnimationPoseSampler.sample(animation, data, tick);
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

    public record LookPose(float pitch, float yaw) {}
}