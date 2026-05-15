package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.SharedMonsterAttributes;
import org.jetbrains.annotations.NotNull;

public class CreatureMoveHelper {
    public enum MoveType {
        PHYSICAL, //uses physical attack
        ELEMENTAL, //uses elemental attack
        STATUS //does non-damaging effects instead
    }

    //calculate the damage a move will do to hit targets
    public static double calculateDamage(@NotNull RiftCreatureNew attackingCreature) {
        //get move
        CreatureMoveBuilder moveBuilder = attackingCreature.getCreatureMoves().getMoveBuilderCurrentMove();
        if (moveBuilder == null || !moveBuilder.isValid()) return 0D;

        double statValueToUse = 0D;
        if (moveBuilder.getMoveType() == CreatureMoveHelper.MoveType.PHYSICAL) {
            statValueToUse = attackingCreature.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        }
        else if (moveBuilder.getMoveType() == CreatureMoveHelper.MoveType.ELEMENTAL) {
            statValueToUse = attackingCreature.getEntityAttribute(RiftCreatureNew.ELEMENTAL_DAMAGE_ATTRIBUTE).getAttributeValue();
        }

        return statValueToUse * moveBuilder.getBasePower() * 0.005;
    }
}
