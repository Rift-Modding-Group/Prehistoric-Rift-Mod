package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class LeafDartBuilder extends CreatureMoveBuilder {
    public LeafDartBuilder() {
        this.setStaminaCost(0.08f)
                .setElement(Element.GRASS)
                .setMoveType(MoveType.PHYSICAL)
                .setBasePower(30)
                .setRequireFindTargetToUse()
                .setRiddenAimingType(RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(RiddenCrosshairType.POINT)
                .setCooldown(100)
                .setOnMoveHitEffect(creature -> {
                    //when ridden, shoot forward
                    ProjectileBuilder projectileBuilder = new ProjectileBuilder().setName("thrown_leaf_dart")
                            .setImpactSoundEvent(RiftSounds.getSound("feather_dart.impact"))
                            .setRotateAlongPitch()
                            .setStayAfterImpact()
                            .setProjectileModel(ProjectileBuilder.ProjectileModel.CUSTOM);
                    if (creature.getControllingPassenger() != null) {
                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.projectile"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );

                        for (int x = -2; x <= 2; x++) {
                            creature.launchProjectile(projectileBuilder, 1.5f, 0f, 0, -7.5f * x);
                        }
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.projectile"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                        for (int x = -2; x <= 2; x++) {
                            creature.launchProjectile(projectileBuilder, target, 1.5f, 0f, 0f, -7.5f * x);
                        }
                    }
                });
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("leaf_dart", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .setDontPathToTarget()
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D));
        }
    }
}
