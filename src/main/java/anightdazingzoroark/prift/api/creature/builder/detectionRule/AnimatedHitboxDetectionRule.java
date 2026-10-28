package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.riftlib.model.AnimationHitboxTimeline;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

/**
 * Predicts contact with tagged hitboxes during an animation's damage window.
 * */
public class AnimatedHitboxDetectionRule extends DetectionRule {
    private static final int SWEEP_SAMPLE_INTERVALS = 4;
    @NotNull
    private final String hitboxTag;
    @NotNull
    private final BiConsumer<ICreature, EntityLivingBase> preparePose;

    /**
     * Animation names and hitbox lifetimes come from the owning move and its animation effects.
     * preparePose sets only prediction variables and must not start the move or its effects.
     */
    public AnimatedHitboxDetectionRule(@NotNull String hitboxTag, @NotNull Consumer<ICreature> preparePose) {
        this(hitboxTag, (creature, target) -> preparePose.accept(creature));
    }

    public AnimatedHitboxDetectionRule(@NotNull String hitboxTag, @NotNull BiConsumer<ICreature, EntityLivingBase> preparePose) {
        this.hitboxTag = hitboxTag;
        this.preparePose = preparePose;
    }

    @Override
    public boolean targetWithinRange(@NotNull ICreature creature, @NotNull EntityLivingBase target) {
        if (!target.isEntityAlive()) return false;

        AttackPreview preview = this.createPreview(creature, target);
        if (preview == null) return false;
        float modelYaw = creature.getAnimationData().getModelYaw();
        if (creature.getCurrentMove().isEmpty()) modelYaw = CreatureLookUtil.getLookBasedAttackYaw(creature, target);
        List<AxisAlignedBB> targets = getTargetBounds(target);
        return preview.bounds(creature.getPositionVector(), modelYaw).stream()
                .anyMatch(bounds -> targets.stream().anyMatch(bounds::intersects));
    }

    @Nullable
    public AttackPreview createPreview(@NotNull ICreature user, @Nullable EntityLivingBase target) {
        AbstractCreatureBuilder<?> configuration = user.getCreatureType();
        if (!user.getPhase().isEmpty()) {
            configuration = user.getCreatureType().getPhaseBuilderMaps().get(user.getPhase());
        }
        if (configuration == null) return null;

        AbstractCreatureBuilder.MoveDefinition definition = configuration.getDetectionOwners().get(this);
        if (definition == null) return null;
        CreatureMoveBuilder move = definition.moveBuilder();

        AnimationDataEntityLiving data = user.getAnimationData();
        List<String> animationSuffixes;
        if (move.getMoveChargeupBuilder() != null) {
            animationSuffixes = Arrays.stream(ChargeupPhase.values())
                    .map(phase -> definition.name() + "_" + phase.name().toLowerCase()).toList();
        }
        else {
            String[] names = move.getAnimNames();
            if (names == null || names.length == 0) return null;
            int index = 0;
            if (user.getCurrentMove().equals(definition.name()) && names.length > 1) {
                index = Math.clamp((int) data.getVariable("chosenMove"), 0, names.length - 1);
            }
            animationSuffixes = List.of(names[index]);
        }

        //once the move starts, prediction must keep its captured aim.
        if (user.getCurrentMove().isEmpty()) this.preparePose.accept(user, target);
        List<AnimatedBoundingBox> hitboxes = data.getAnimatedBoundingBoxesByTag().get(this.hitboxTag);
        if (hitboxes == null || hitboxes.isEmpty() || data.getBoneSnapshotCollection().isEmpty()) return null;

        if (!(data.getHolder() instanceof IAnimatable<?> animatable)) return null;
        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return null;
        List<Animation> animations = new ArrayList<>();
        for (String suffix : animationSuffixes) {
            Animation animation = model.getAnimations("animation." + user.getCreatureType().getName() + "." + suffix, animatable);
            if (animation == null) return null;
            animations.add(animation);
        }

        Map<AnimatedBoundingBox, List<Map<String, BoneSnapshot>>> poses = new HashMap<>();
        for (AnimationHitboxTimeline.HitboxWindow window : AnimationHitboxTimeline.getHitboxWindows(animations, hitboxes, data)) {
            List<Map<String, BoneSnapshot>> hitboxPoses = poses.computeIfAbsent(window.hitbox(), hitbox -> new ArrayList<>());
            Map<String, BoneSnapshot> previousPose = null;
            for (double tick = Math.ceil(window.startTick()); tick <= Math.ceil(window.endTick()); tick++) {
                Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(window.animation(), data, tick);
                if (previousPose == null) hitboxPoses.add(pose);
                else {
                    for (int step = 1; step <= SWEEP_SAMPLE_INTERVALS; step++) {
                        float progress = (float) MathUtil.slopeResult(step, false, 0D, SWEEP_SAMPLE_INTERVALS, 0D, 1D);
                        hitboxPoses.add(AnimationPoseSampler.interpolate(previousPose, pose, progress));
                    }
                }
                previousPose = pose;
            }
        }
        if (poses.isEmpty()) return null;
        return new AttackPreview(poses, data.getScale());
    }

    @NotNull
    public static List<AxisAlignedBB> getTargetBounds(@NotNull EntityLivingBase target) {
        List<AxisAlignedBB> targetBounds = new ArrayList<>();
        if (target instanceof IEntityMultiPart) {
            Entity[] parts = target.getParts();
            if (parts != null) {
                for (Entity part : parts) {
                    if (part instanceof MultiPartEntityPart) targetBounds.add(part.getEntityBoundingBox());
                }
            }
        }
        if (targetBounds.isEmpty()) targetBounds.add(target.getEntityBoundingBox());

        return targetBounds;
    }

    public record AttackPreview(@NotNull Map<AnimatedBoundingBox, List<Map<String, BoneSnapshot>>> poses, float scale) {
        @NotNull
        public List<AxisAlignedBB> bounds(@NotNull Vec3d position, float yaw) {
            List<AxisAlignedBB> bounds = new ArrayList<>();
            for (Map.Entry<AnimatedBoundingBox, List<Map<String, BoneSnapshot>>> hitbox : this.poses.entrySet()) {
                for (Map<String, BoneSnapshot> pose : hitbox.getValue()) {
                    bounds.add(hitbox.getKey().getWorldSpaceBounds(this.scale, yaw, position, pose));
                }
            }
            return bounds;
        }
    }
}