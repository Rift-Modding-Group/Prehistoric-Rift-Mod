package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.util.CreatureMoveUtil;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Captures Scratch's vertical arm aim for playback and AI prediction. */
public class ScratchLookUtil {
    public static void captureScratchLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        // The existing melee camera samples this input; only the scratching arm consumes it.
        CreatureLookUtil.captureHeadAttackLook(creature);
        if (creature.getControllingPassenger() != null || target == null || !target.isEntityAlive()) return;

        AnimationDataEntityLiving data = creature.getAnimationData();
        if (data.getBoneSnapshotCollection().isEmpty()) return;
        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(data.getAnimatable());
        if (model == null) return;
        List<String> names = CreatureMoveUtil.getPreviewAnimationNames(creature, "scratch");
        if (names.isEmpty()) return;
        Animation animation = model.getAnimations(names.getFirst(), data.getAnimatable());
        if (animation == null) return;

        // Fit the chosen arm's visual pose independently of the fixed forward attack zone.
        boolean usesLeftArm = animation.boneAnimations.stream().anyMatch(channel -> channel.boneName.equals("leftArm"));
        String locatorName = usesLeftArm ? "leftClawAim" : "rightClawAim";
        AnimatedLocator locator = data.getAnimatedLocator(locatorName);
        AnimatedBoundingBox hitbox = data.getAnimatedBoundingBoxes().get("clawHitZone");
        if (locator == null || hitbox == null) return;
        List<CreatureMoveUtil.HitboxSample> samples = CreatureMoveUtil.getHitEffectSamples(List.of(animation), List.of(hitbox));
        if (samples.isEmpty()) return;

        ScratchStrikePose strike = new ScratchStrikePose(data, animation, locator, samples.getFirst().tick());
        float lowerPitch = CreatureLookUtil.HEAD_PITCH_MIN;
        float upperPitch = CreatureLookUtil.HEAD_PITCH_MAX;
        double lowerHeight = strike.sampleHeight(lowerPitch);
        double upperHeight = strike.sampleHeight(upperPitch);
        double targetHeight = Math.clamp(target.getEntityBoundingBox().getCenter().y,
                Math.min(lowerHeight, upperHeight), Math.max(lowerHeight, upperHeight));
        for (int step = 0; step < 10; step++) {
            float pitch = (lowerPitch + upperPitch) / 2f;
            double height = strike.sampleHeight(pitch);
            if ((height < targetHeight) == (upperHeight > lowerHeight)) lowerPitch = pitch;
            else upperPitch = pitch;
        }
        data.setVariable("head_attack_pitch", (lowerPitch + upperPitch) / 2f);
    }

    private record ScratchStrikePose(
            @NotNull AnimationDataEntityLiving data, @NotNull Animation animation,
            @NotNull AnimatedLocator locator, double tick
    ) {
        private double sampleHeight(float pitch) {
            this.data.setVariable("head_attack_pitch", pitch);
            return CreatureLookUtil.getRayPose(this.data, this.locator,
                    AnimationPoseSampler.sample(this.animation, this.data, this.tick)).origin().y;
        }
    }
}
