package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.entity.RiftMoveExplosion;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class BubbleBombBuilder extends CreatureMoveBuilder {
    public BubbleBombBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(80)
                .setRequireFindTargetToUse()
                .setElement(Element.WATER)
                .setMoveType(MoveType.SPECIAL)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setOnMoveHitEffect(creature -> {
                    EntityLivingBase target = creature.getAttackTarget();
                    if (creature.getControllingPassenger() == null && (target == null || !target.isEntityAlive())) return;

                    ProjectileBuilder bubbleBombProjectile = new ProjectileBuilder().setName("bubble_bomb")
                            .setStraightFlight()
                            .setProjectileModel(ProjectileBuilder.ProjectileModel.CUBE)
                            .setImpactSoundEvent(RiftSounds.getSound("generic_water.hit"))
                            .setOnImpactFromCreatureEffect((iCreature, projectile, hitEntity, hitPos) ->
                                    RiftMoveExplosion.create(projectile, hitEntity, hitPos, 3f)
                            );

                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_water.use"), SoundCategory.NEUTRAL, 1f, 1f
                    );
                    if (creature.getControllingPassenger() != null) {
                        creature.launchProjectile(bubbleBombProjectile, 0.5f, 0f);
                    }
                    else creature.launchProjectile(bubbleBombProjectile, target, 0.5f, 0f);
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(RiddenCrosshairType.POINT)
                .setCooldown(600);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("bubble_bomb", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D))
                    .setDontPathToTarget();
        }
    }
}
