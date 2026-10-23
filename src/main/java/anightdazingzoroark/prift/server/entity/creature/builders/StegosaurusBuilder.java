package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCommon;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.List;

public class StegosaurusBuilder extends RiftCreatureBuilder {
    public StegosaurusBuilder() {
        this.setName("stegosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setTributeItemPartName("plate")
                .setStats(5.5, 5, 2, 4, 2)
                .setScaleRangeForAge(0.3f, 2.125f)
                .setSpawnEggColors(1731840, 16743424)
                .setMainHitboxSize(2.125f, 2.5f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(3)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.NORMAL)
                        .setControllerRideLocator("controllerPos")
                )
                .setHitboxInformation(Map.of(
                        "leg", creature -> 0.25D,
                        "body", creature -> 0.75D,
                        "head", creature -> 1.25D,
                        "tail", creature -> 0.25D
                ))
                .setIsHerder(8)
                .setCanRetreat()
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f && !creature.isInHerd()
                                    && (RiftUtil.entityInTargetGroup(threat, "human")
                                    || RiftUtil.entityInTargetGroup(threat, "carnivoreCreature"));
                        }
                )
                .addBlockBreakLevel("axe", 2)
                .registerIntegerValue("ChargeUpLevel", 0)
                //---moves---
                .addMove("tail_stab", CreatureMoveCommon.standardMeleeMove.copy()
                        .setBasePower(50)
                        .setOnMoveBeginEffect(this::captureTailStabLook)
                        .setUseLookBasedFacing(true)
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setAnimNames("tail_stab")
                )
                .addMove("tail_slam", new CreatureMoveBuilder()
                        .setMakesContact()
                        .setPhysical()
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.15f)
                        .setBasePower(70)
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setAnimNames("tail_slam")
                        .setCooldown(400)
                )
                .addMove("thagomize", new CreatureMoveBuilder()
                        .setMakesContact()
                        .setPhysical()
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.08f)
                        .setStaminaDrainPerSecond(0.02f)
                        .setBasePower(50)
                        .setPreventMovement(true)
                        .setUseLookBasedFacing(true)
                        .setOnMoveBeginEffect(this::captureThagomizeLook)
                        .setOnMoveEndEffect(creature -> creature.setProperty("ChargeUpLevel", 0))
                        .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                .setChargeUpThenRelease()
                                .setCanRotateWhileCharging(true)
                                .setWindupEndEffect(creature -> this.captureThagomizeLook(creature, creature.getAttackTarget()))
                                .setMaxChargeUp(100)
                                .setCooldownMultiplier(6D)
                                .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                                    int chargeIntMultiplier = (int) Math.floor(chargeUp / 20D) + 1;
                                    return MathUtil.slopeResult(chargeIntMultiplier, true, 1, 6, 1D, 2D);
                                })
                                .setWindupEffect((creature, chargeUp) -> {
                                    int chargeUpLevel = (int) creature.getProperty("ChargeUpLevel").getValue();
                                    if (chargeUpLevel >= 5 || chargeUp <= 0 || chargeUp > 100 || chargeUp % 20D != 0) return;

                                    //set chargeup level
                                    chargeUpLevel++;
                                    creature.setProperty("ChargeUpLevel", chargeUpLevel);

                                    //show particles
                                    AxisAlignedBB creatureAABB = creature.getEntityBoundingBox();
                                    double width = creatureAABB.maxX - creatureAABB.minX;
                                    double height = creatureAABB.maxY - creatureAABB.minY;
                                    RiftLibParticleHelper.createParticle(
                                            "prift:charge_level_up",
                                            creature.getPositionVector().x, creature.getPositionVector().y + height / 2D, creature.getPositionVector().z,
                                            0, 0,
                                            "variable.emission_radius", Double.toString(width * 0.005D)
                                    );

                                    //play sound
                                    if (chargeUpLevel == 1 || chargeUpLevel == 2) {
                                        creature.getEntityWorld().playSound(
                                                null,
                                                creature.getPositionVector().x,
                                                creature.getPositionVector().y,
                                                creature.getPositionVector().z,
                                                RiftSounds.getSound("generic_move.chargeup_weak"),
                                                SoundCategory.NEUTRAL,
                                                1f, 1f
                                        );
                                    }
                                    else if (chargeUpLevel == 3 || chargeUpLevel == 4) {
                                        creature.getEntityWorld().playSound(
                                                null,
                                                creature.getPositionVector().x,
                                                creature.getPositionVector().y,
                                                creature.getPositionVector().z,
                                                RiftSounds.getSound("generic_move.chargeup_medium"),
                                                SoundCategory.NEUTRAL,
                                                1f, 1f
                                        );
                                    }
                                    else if (chargeUpLevel == 5) {
                                        creature.getEntityWorld().playSound(
                                                null,
                                                creature.getPositionVector().x,
                                                creature.getPositionVector().y,
                                                creature.getPositionVector().z,
                                                RiftSounds.getSound("generic_move.chargeup_strong"),
                                                SoundCategory.NEUTRAL,
                                                1f, 1f
                                        );
                                    }
                                })
                                .setOnHitEntityDuringRelease((creature, chargeUp, hitEntity) -> {
                                    if (!(hitEntity instanceof EntityLivingBase hitEntityLiving)) return;
                                    OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(hitEntityLiving);
                                    if (otherEntityProperties == null) return;
                                    int bleedStrength = (int) MathUtil.slopeResult(chargeUp, true, 0, 100, 0, 4);
                                    otherEntityProperties.setBleeding(bleedStrength, 100);
                                })
                        )
                )
                .addMove("plate_fling", new CreatureMoveBuilder()
                        .setStaminaCost(0.08f)
                        .setPhysical()
                        .setBasePower(30)
                        .setRequireFindTargetToUse()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setOnMoveHitEffect(creature -> {
                            //when ridden, shoot forward
                            if (creature.getControllingPassenger() != null) {
                                creature.launchProjectile(
                                        new ProjectileBuilder().setName("thrown_stegosaurus_plate").setRotateAlongPitch(),
                                        3f, 0f
                                );
                            }
                            //otherwise, hit target
                            else {
                                EntityLivingBase target = creature.getAttackTarget();
                                if (target == null || !target.isEntityAlive()) return;

                                creature.launchProjectile(
                                        new ProjectileBuilder().setName("thrown_stegosaurus_plate").setRotateAlongPitch(),
                                        target, 3f, 0f
                                );
                            }
                        })
                        .setAnimNames("plate_fling")
                )
                //---attack ai---
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                        .setMoveRule(
                                new MoveRuleBuilder("tail_stab")
                                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                        .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", this::captureTailStabLook))
                                        .setUseBlockBreak()
                                )
                        .setMoveRule(
                                new MoveRuleBuilder("tail_slam")
                                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                        .addDetectionRule(new BoundingBoxDetectionRule("frontZone", true))
                                        .setUseBlockBreak()
                        )
                        .setMoveRule(
                                new MoveRuleBuilder("thagomize")
                                        .setPriorityPredicate((creature, target) -> {
                                            return target != null && target.isEntityAlive() && creature.atRageThreshold() ? 0 : -1;
                                        })
                                        .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", this::captureThagomizeLook))
                                )
                        .setMoveRule(
                                new MoveRuleBuilder("plate_fling")
                                        .setPriorityPredicate((creature, target) -> {
                                            return (target != null && target.isEntityAlive()) ? 0 : -1;
                                        })
                                        .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                                        .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                                        .setDontPathToTarget()
                        )
                )
                //---targeting---
                .addDefaultFoodItemWhitelistEntry("fruit")
                .addDefaultFoodItemWhitelistEntry("vegetable")
                .addDefaultFoodItemWhitelistEntry("grass")
                .addDefaultFoodItemWhitelistEntry("leaves")
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }

    //-----helpers for moves-----
    /**
     * Preserve the last visible neck pose and fit tail stab's downward strike to its target.
     * */
    private void captureTailStabLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getLookPose(creature);
        data.setVariable("tail_attack_pitch", look.pitch());
        data.setVariable("tail_attack_yaw", look.yaw());
        data.setVariable("tail_attack_strike_pitch", -25.0991D - look.pitch());

        Vec3d targetPosition = this.getTailAttackTargetPosition(creature, target);
        if (targetPosition == null) return;
        TailStrikePose strike = this.createTailStrikePose(data, "animation.stegosaurus.tail_stab", 12.5D);
        if (strike == null) return;
        if (target != null && target.isEntityAlive() && strike.crossesGroundTargetHeight(target)) return;

        Vec3d eyes = data.getHolder().getPositionEyes(1f);
        Vec3d offset = targetPosition.subtract(eyes);
        double distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        double pitch = Math.clamp(Math.toDegrees(Math.atan2(-offset.y, distance)), -15D, 15D);
        strike.fitPitchToHeight(eyes.y - Math.tan(Math.toRadians(pitch)) * distance);
    }

    /**
     * Prediction and windup completion share this capture; the release keeps these variables frozen.
     * */
    private void captureThagomizeLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getLookPose(creature);
        if (!creature.getCurrentMove().isEmpty()) {
            // The windup has its own authored pose; capture the physical head aim, not that pose.
            look = new CreatureLookUtil.LookPose(Math.clamp(data.getHeadPitch(1f), -15f, 15f), Math.clamp(data.getHeadYaw(1f), -15f, 15f));
        }
        data.setVariable("tail_attack_pitch", look.pitch());
        data.setVariable("tail_attack_yaw", look.yaw());
        data.setVariable("tail_attack_strike_pitch", -22.3499D - look.pitch());

        TailStrikePose strike = this.createTailStrikePose(data, "animation.stegosaurus.thagomize_releasing", 5D);
        if (strike == null) return;
        if (target != null && target.isEntityAlive() && Math.abs(look.pitch()) < 1E-3f && strike.crossesGroundTargetHeight(target)) return;

        Vec3d eyes = data.getHolder().getPositionEyes(1f);
        Vec3d targetPosition = this.getTailAttackTargetPosition(creature, target);
        // Without a target, aim along the rider's head ray at the tail's natural reach.
        double distance = 3D * data.getScale();
        if (targetPosition != null) {
            Vec3d offset = targetPosition.subtract(eyes);
            distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        }
        strike.fitPitchToHeight(eyes.y - Math.tan(Math.toRadians(look.pitch())) * distance);
    }

    @Nullable
    private Vec3d getTailAttackTargetPosition(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        if (target != null && target.isEntityAlive()) return target.getEntityBoundingBox().getCenter();
        return creature.getRiddenAimPosition();
    }

    @Nullable
    private TailStrikePose createTailStrikePose(@NotNull AnimationDataEntityLiving data, @NotNull String animationName, double tick) {
        if (!(data.getHolder() instanceof IAnimatable<?> animatable)) return null;

        List<AnimatedBoundingBox> hitboxes = data.getAnimatedBoundingBoxesByTag().get("thagomizer");
        if (hitboxes == null || hitboxes.isEmpty() || data.getBoneSnapshotCollection().isEmpty()) return null;

        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return null;

        Animation animation = model.getAnimations(animationName, animatable);
        if (animation == null) return null;

        return new TailStrikePose(data, animation, hitboxes, tick);
    }

    /**
     * Geometry utilities shared by both attacks, with no move-selection logic.
     * */
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