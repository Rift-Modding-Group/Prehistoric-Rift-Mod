package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import org.jetbrains.annotations.NotNull;

public class CreatureMoveHelper {
    //calculate the damage a move will do to hit targets
    public static double calculateDamage(@NotNull RiftCreature attackingCreature) {
        //get move
        CreatureMoveBuilder moveBuilder = attackingCreature.getCreatureMoves().getMoveBuilderCurrentMove();
        if (moveBuilder == null || !moveBuilder.isValid()) return 0D;

        double statValueToUse = 0D;
        if (moveBuilder.getMoveType() == CreatureMoveBuilder.MoveType.PHYSICAL) {
            statValueToUse = attackingCreature.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        }
        else if (moveBuilder.getMoveType() == CreatureMoveBuilder.MoveType.ELEMENTAL) {
            statValueToUse = attackingCreature.getEntityAttribute(RiftCreature.ELEMENTAL_DAMAGE_ATTRIBUTE).getAttributeValue();
        }

        return statValueToUse * moveBuilder.getBasePower() * 0.005;
    }
}
