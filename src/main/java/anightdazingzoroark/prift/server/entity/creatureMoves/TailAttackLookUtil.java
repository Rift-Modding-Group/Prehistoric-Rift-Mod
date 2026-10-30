package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.util.CreatureMoveUtil;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Shared look capture and strike fitting for tail attacks. */
public class TailAttackLookUtil {
    /**
     * Preserve the last visible neck pose and fit tail stab's downward strike to its target.
     *
     */
    public static void captureTailStabLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getAttackLookPose(creature);
        data.setVariable("tail_attack_pitch", look.pitch());
        data.setVariable("tail_attack_yaw", look.yaw());
        data.setVariable("tail_attack_strike_pitch", -25.0991D - look.pitch());

        Vec3d targetPosition = getTailAttackTargetPosition(creature, target);
        if (targetPosition == null) return;
        TailStrikePose strike = createTailStrikePose(creature, "tail_stab", null, 12.5D);
        if (strike == null) return;
        if (target != null && target.isEntityAlive() && strike.crossesGroundTargetHeight(target)) return;

        Vec3d eyes = data.getHolder().getPositionEyes(1f);
        Vec3d offset = targetPosition.subtract(eyes);
        double distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        double pitch = Math.clamp(Math.toDegrees(Math.atan2(-offset.y, distance)), CreatureLookUtil.HEAD_PITCH_MIN, CreatureLookUtil.HEAD_PITCH_MAX);
        if (CreatureLookUtil.getRiddenAttackPosition(creature) != null) strike.fitPitchToHeight(targetPosition.y);
        else strike.fitPitchToHeight(eyes.y - Math.tan(Math.toRadians(pitch)) * distance);
    }

    /**
     * Prediction and windup completion share this capture; the release keeps these variables frozen.
     *
     */
    public static void captureThagomizeLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getAttackLookPose(creature);
        if (!creature.getCurrentMove().isEmpty() && creature.getControllingPassenger() == null) {
            // The windup has its own authored pose; capture the physical head aim, not that pose.
            look = new CreatureLookUtil.LookPose(Math.clamp(data.getHeadPitch(1f), CreatureLookUtil.HEAD_PITCH_MIN, CreatureLookUtil.HEAD_PITCH_MAX), Math.clamp(data.getHeadYaw(1f), -15f, 15f));
        }
        data.setVariable("tail_attack_pitch", look.pitch());
        data.setVariable("tail_attack_yaw", look.yaw());
        data.setVariable("tail_attack_strike_pitch", -22.3499D - look.pitch());

        TailStrikePose strike = createTailStrikePose(creature, "thagomize", ChargeupPhase.RELEASING, 5D);
        if (strike == null) return;
        if (target != null && target.isEntityAlive() && Math.abs(look.pitch()) < 1E-3f && strike.crossesGroundTargetHeight(target))
            return;

        Vec3d eyes = data.getHolder().getPositionEyes(1f);
        Vec3d targetPosition = getTailAttackTargetPosition(creature, target);
        // AI keeps its head-based reach; riders supply an explicit strike height.
        double distance = 3D * data.getScale();
        if (targetPosition != null) {
            Vec3d offset = targetPosition.subtract(eyes);
            distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        }
        if (CreatureLookUtil.getRiddenAttackPosition(creature) != null && targetPosition != null)
            strike.fitPitchToHeight(targetPosition.y);
        else strike.fitPitchToHeight(eyes.y - Math.tan(Math.toRadians(look.pitch())) * distance);
    }

    @Nullable
    private static Vec3d getTailAttackTargetPosition(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        if (target != null && target.isEntityAlive()) return target.getEntityBoundingBox().getCenter();
        return CreatureLookUtil.getRiddenAttackPosition(creature);
    }

    @Nullable
    private static TailStrikePose createTailStrikePose(
            @NotNull ICreature creature, @NotNull String moveName,
            @Nullable ChargeupPhase phase, double tick
    ) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        if (!(data.getHolder() instanceof IAnimatable<?> animatable)) return null;

        List<AnimatedBoundingBox> hitboxes = data.getAnimatedBoundingBoxesByTag().get("thagomizer");
        if (hitboxes == null || hitboxes.isEmpty() || data.getBoneSnapshotCollection().isEmpty()) return null;

        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return null;

        List<String> names = CreatureMoveUtil.getAnimationNames(creature, creature.getPhase(), moveName, phase);
        if (names.isEmpty()) return null;
        Animation animation = model.getAnimations(names.getFirst(), animatable);
        if (animation == null) return null;

        return new TailStrikePose(data, animation, hitboxes, tick);
    }

    /**
     * Geometry utilities shared by both attacks, with no move-selection logic.
     *
     */
    private record TailStrikePose(
            @NotNull AnimationDataEntityLiving data, @NotNull Animation animation,
            @NotNull List<AnimatedBoundingBox> hitboxes, double tick
    ) {
        private boolean crossesGroundTargetHeight(@NotNull EntityLivingBase target) {
            if (Math.abs(target.getEntityBoundingBox().minY - this.data.getHolder().getEntityBoundingBox().minY) > this.data.getHolder().stepHeight) {
                return false;
            }
            // Keep an authored downstroke if it already reaches a ground target, including short creatures.
            Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(this.animation, this.data, this.tick);
            AxisAlignedBB targetBounds = target.getEntityBoundingBox();
            return this.hitboxes.stream().anyMatch(hitbox -> {
                AxisAlignedBB bounds = hitbox.getWorldSpaceBounds(
                        this.data.getScale(), this.data.getModelYaw(),
                        this.data.getHolder().getPositionVector(), pose
                );
                return bounds.minY < targetBounds.maxY && bounds.maxY > targetBounds.minY;
            });
        }

        private void fitPitchToHeight(double targetHeight) {
            // Fit the tail joint itself; the body's turn makes copying head pitch insufficient.
            float lowerPitch = -60f;
            float upperPitch = 60f;
            double lowerHeight = this.sampleHeight(lowerPitch);
            double upperHeight = this.sampleHeight(upperPitch);
            targetHeight = Math.clamp(targetHeight, Math.min(lowerHeight, upperHeight), Math.max(lowerHeight, upperHeight));
            for (int step = 0; step < 10; step++) {
                float pitch = (lowerPitch + upperPitch) / 2f;
                double height = this.sampleHeight(pitch);
                if ((height < targetHeight) == (upperHeight > lowerHeight)) lowerPitch = pitch;
                else upperPitch = pitch;
            }
            this.data.setVariable("tail_attack_strike_pitch", (lowerPitch + upperPitch) / 2f);
        }

        private double sampleHeight(float pitch) {
            this.data.setVariable("tail_attack_strike_pitch", pitch);
            Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(this.animation, this.data, this.tick);
            return this.hitboxes.stream().mapToDouble(hitbox -> {
                AxisAlignedBB foundHitbox = hitbox.getWorldSpaceBounds(this.data.getScale(), this.data.getModelYaw(), this.data.getHolder().getPositionVector(), pose);
                return foundHitbox.getCenter().y;
            }).average().orElseThrow();
        }
    }
}
