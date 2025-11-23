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

    public static final String THROWN_STEGOSAURUS_PLATE = "thrown_stegosaurus_plate";
    public static final String POISON_SPIT = "poison_spit";
    public static final String VENOM_BOMB = "venom_bomb";
    public static final String MUDBALL = "mudball";
    public static final String POWER_BLOW = "power_blow";

    public static RiftCreatureProjectileEntity createCreatureProjectile(String creatureProjectileName, RiftCreature creature) {
        //find name first
        RiftCreatureProjectileBuilder builder = getBuilderByName(creatureProjectileName);
        if (builder != null) {
            RiftCreatureProjectileEntity projectileEntity = new RiftCreatureProjectileEntity(creature);
            projectileEntity.setProjectileBuilder(builder);
            if (builder.getHasVariants()) projectileEntity.setVariant(creature.getVariant());
            if (builder.getHasDelayedEffectOnImpact())
                projectileEntity.setCountdown(builder.getDelayedEffectOnImpactCountdown());
            return projectileEntity;
        }
        return null;
    }

    public static RiftCreatureProjectileBuilder getBuilderByName(String name) {
        boolean builderIsPresent = CREATURE_PROJECTILE_BUILDERS.stream()
                .anyMatch(b -> b.projectileName.equals(name));
        if (builderIsPresent) return CREATURE_PROJECTILE_BUILDERS.stream()
                .filter(b -> b.projectileName.equals(name))
                .findFirst().get();
        return null;
    }

    public static void initCreatureProjectileBuilders() {
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(THROWN_STEGOSAURUS_PLATE)
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
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(POISON_SPIT)
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
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(VENOM_BOMB)
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
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(MUDBALL)
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
        CREATURE_PROJECTILE_BUILDERS.add(new RiftCreatureProjectileBuilder(POWER_BLOW)
                .setHasNoModel()
                .setSelfDestruct()
                .setUsePower(2f, 8f)
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
}
