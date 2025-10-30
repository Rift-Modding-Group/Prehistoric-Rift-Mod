package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class RiftCreatureProjectile {
    public static List<RiftCreatureProjectileBuilder> CREATURE_PROJECTILE_BUILDERS = new ArrayList<>();

    public static RiftCreatureProjectileEntity createCreatureProjectile(Enum creatureProjectileEnum, RiftCreature creature) {
        //find name first
        RiftCreatureProjectileBuilder builder = CREATURE_PROJECTILE_BUILDERS.stream()
                .filter(b -> b.projectileEnum == creatureProjectileEnum)
                .findFirst().get();
        RiftCreatureProjectileEntity projectileEntity = new RiftCreatureProjectileEntity(creature);
        projectileEntity.setProjectileBuilder(builder);
        if (builder.getHasVariants()) projectileEntity.setVariant(creature.getVariant());
        if (builder.getHasDelayedEffectOnImpact()) projectileEntity.setCountdown(builder.getDelayedEffectOnImpactCountdown());
        return projectileEntity;
    }

    public static void initCreatureProjectileBuilders() {
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(Enum.THROWN_STEGOSAURUS_PLATE)
                .setHasVariants()
                .setSelfDestruct()
                .setImpactSoundEvent(SoundEvents.ENTITY_ARROW_HIT)
                .setDamageCalculator((projectile) -> {
                    double levelBasedIncrement = 0;
                    if (projectile.shootingEntity instanceof RiftCreature) {
                        RiftCreature creature = (RiftCreature) projectile.shootingEntity;
                        levelBasedIncrement = creature.getLevel() / 10D;
                    }
                    return 4D + levelBasedIncrement;
                })
        );
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(Enum.POISON_SPIT)
                .setHasFlatModel()
                .setSelfDestruct()
                .setNoVerticalRotation()
                .setImpactSoundEvent(SoundEvents.BLOCK_SLIME_HIT)
                .setOnHitEffect((projectile, hitEntity) -> {
                    if (hitEntity != null) {
                        hitEntity.addPotionEffect(new PotionEffect(MobEffects.POISON, 10 * 20));
                        hitEntity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20));
                    }
                })
                .setDamageCalculator((projectile) -> {
                    double levelBasedIncrement = 0;
                    if (projectile.shootingEntity instanceof RiftCreature) {
                        RiftCreature creature = (RiftCreature) projectile.shootingEntity;
                        levelBasedIncrement = creature.getLevel() / 10D;
                    }
                    return 2D + levelBasedIncrement;
                })
        );
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(Enum.VENOM_BOMB)
                .setImpactSoundEvent(SoundEvents.BLOCK_SLIME_HIT)
                .setDelayedEffectOnImpact(100, (projectile, hitEntity) -> {
                    projectile.world.createExplosion(projectile, projectile.posX, projectile.posY, projectile.posZ, 2f, false);
                    projectile.setDead();
                })
                .setAnimation((projectile) -> {
                    return new AnimationController(projectile, "default", 0, new AnimationController.IAnimationPredicate() {
                        @Override
                        public PlayState test(AnimationEvent event) {
                            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.venom_bomb.default", true));
                            return PlayState.CONTINUE;
                        }
                    });
                })
        );
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(Enum.MUDBALL)
                .setHasFlatModel()
                .setSelfDestruct()
                .setNoVerticalRotation()
                .setImpactSoundEvent(SoundEvents.BLOCK_SLIME_HIT)
                .setOnHitEffect((projectile, hitEntity) -> {
                    if (hitEntity != null) {
                        hitEntity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20));
                    }
                })
                .setDamageCalculator((projectile) -> {
                    double levelBasedIncrement = 0;
                    if (projectile.shootingEntity instanceof RiftCreature) {
                        RiftCreature creature = (RiftCreature) projectile.shootingEntity;
                        levelBasedIncrement = creature.getLevel() / 10D;
                    }
                    return 4D + levelBasedIncrement;
                })
        );
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(Enum.POWER_BLOW)
                .setHasNoModel()
                .setSelfDestruct()
                .setUsePower(2f, 8f)
                .setDamageCalculator((projectile) -> {
                    return 2D;
                })
                .setOnHitEffect((projectile, hitEntity) -> {
                    if (hitEntity != null) {
                        //knock back the entity
                        double d0 = projectile.posX - hitEntity.posX;
                        double d1 = projectile.posZ - hitEntity.posZ;
                        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
                        hitEntity.knockBack(projectile, projectile.getPower(), d0 / d2 * 8.0D, d1 / d2 * 8.0D);
                    }
                })
        );
    }

    public enum Enum {
        THROWN_STEGOSAURUS_PLATE,
        POISON_SPIT,
        VENOM_BOMB,
        MUDBALL,
        POWER_BLOW
    }
}
