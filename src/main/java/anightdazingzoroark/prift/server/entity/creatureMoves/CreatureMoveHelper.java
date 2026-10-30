package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CreatureMoveHelper {
    public static boolean hasSameElementAttackBonus(@NotNull CreatureMoveBuilder move, @NotNull Element[] userElements) {
        return move.getMoveType() != CreatureMoveBuilder.MoveType.STATUS && move.getBasePower() > 0
                && Arrays.stream(userElements).anyMatch(element -> element == move.getElement());
    }

    public static double getBasePowerWithElementBonus(@NotNull CreatureMoveBuilder move, @NotNull Element[] userElements) {
        return move.getBasePower() * (hasSameElementAttackBonus(move, userElements) ? 1.5D : 1D);
    }

    //calculate the damage a move will do to hit targets
    public static double calculateDamage(@NotNull RiftCreature attackingCreature) {
        return calculateDamage(attackingCreature, attackingCreature.getCreatureMoves().getMoveBuilderCurrentMove());
    }

    public static double calculateDamage(@NotNull RiftCreature attackingCreature, @Nullable CreatureMoveBuilder moveBuilder) {
        if (moveBuilder == null || !moveBuilder.isValid() || moveBuilder.getBasePower() <= 0) return 0D;

        //get stat value
        double statValueToUse = 0D;
        if (moveBuilder.getMoveType() == CreatureMoveBuilder.MoveType.PHYSICAL) {
            statValueToUse = attackingCreature.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        }
        else if (moveBuilder.getMoveType() == CreatureMoveBuilder.MoveType.SPECIAL) {
            statValueToUse = attackingCreature.getEntityAttribute(RiftCreature.ELEMENTAL_DAMAGE_ATTRIBUTE).getAttributeValue();
        }

        //get base power
        int basePower = moveBuilder.getBasePower();
        double finalBasePower = getBasePowerWithElementBonus(moveBuilder, attackingCreature.getCreatureType().getElement());
        if (moveBuilder.getMoveChargeupBuilder() != null) {
            int chargeUpTicks = attackingCreature.getCreatureMoves().getCurrentMoveChargeUpTicks();
            double basePowerMultiplier = moveBuilder.getMoveChargeupBuilder().getBasePowerMultiplier().apply(
                    attackingCreature, basePower, chargeUpTicks
            );
            finalBasePower *= basePowerMultiplier;
        }
        finalBasePower *= 0.005D; //small multiplier

        return statValueToUse * finalBasePower;
    }

    public static boolean targetMatchesAnyDetectionRule(@NotNull MoveRuleBuilder rule, @NotNull ICreature creature, @NotNull EntityLivingBase target) {
        return rule.getDetectionRules().stream().anyMatch(detection -> detection.targetWithinRange(creature, target));
    }

}
