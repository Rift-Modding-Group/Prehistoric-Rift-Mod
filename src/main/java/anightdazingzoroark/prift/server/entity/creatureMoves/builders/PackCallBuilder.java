package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.ScratchLookUtil;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class PackCallBuilder extends CreatureMoveBuilder {
    public PackCallBuilder() {
        this.setStaminaCost(0.15f)
                .setCooldown(900)
                .setRequireFindTargetToUse()
                .setElement(Element.NEUTRAL)
                .setMoveType(MoveType.STATUS)
                .setOnMoveHitEffect(creature -> {
                    List<ICreature> hitCreatures = getHitCreatures(creature);
                    for (ICreature hitCreature : hitCreatures) {
                        hitCreature.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 600, 2));
                        hitCreature.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600, 2));
                    }
                })
                .setUseLookBasedFacing()
                .setCooldown(900);

    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("pack_call", MoveCategory.STATUS);
            this.setPriorityPredicate((creature, target) -> {
                if (target == null || !target.isEntityAlive()) return false;

                if (creature.isTamed()) {
                    boolean atInterval = creature.getTicksExisted() % 600 == 0;
                    boolean canUse = creature.getEntityWorld().rand.nextInt(4) == 0;
                    return atInterval && canUse && !getHitCreatures(creature).isEmpty();
                }
                else return creature.isHerdLeader() && creature.isInHerd();
            });
        }
    }

    private static List<ICreature> getHitCreatures(ICreature creature) {
        AxisAlignedBB axisAlignedBB = creature.getAreaAroundCreature(6D);
        return creature.getCreaturesInAABB(axisAlignedBB, hitCreature -> {
            if (hitCreature == null) return false;
            if (!creature.getCreatureType().equals(hitCreature.getCreatureType())) return false;

            if (creature.isTamed()) return hitCreature.isTamed();
            else return !hitCreature.isTamed() && hitCreature.getHerdLeader() == creature;
        });
    }
}
