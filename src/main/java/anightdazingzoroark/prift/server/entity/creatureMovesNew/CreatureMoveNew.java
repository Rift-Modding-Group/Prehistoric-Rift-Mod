package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.SharedMonsterAttributes;
import org.jetbrains.annotations.NotNull;

//store names of all move builders here
public class CreatureMoveNew {
    public static final String BITE = "bite";
    public static final String THAGOMIZE = "thagomize";

    //calculate the damage a move will do to hit targets
    public static double calculateDamage(@NotNull RiftCreatureNew attackingCreature) {
        //get move
        String moveName = attackingCreature.getCurrentMove();
        if (moveName.isEmpty()) return 0D;
        CreatureMoveBuilder moveBuilder = CreatureMoveRegistry.getCreatureMove(moveName);
        if (moveBuilder == null) return 0D;

        double statValueToUse = 0D;
        if (moveBuilder.getMoveType() == CreatureMoveEnums.MoveType.PHYSICAL) {
            statValueToUse = attackingCreature.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        }
        else if (moveBuilder.getMoveType() == CreatureMoveEnums.MoveType.ELEMENTAL) {
            statValueToUse = attackingCreature.getEntityAttribute(RiftCreatureNew.ELEMENTAL_DAMAGE_ATTRIBUTE).getAttributeValue();
        }

        return statValueToUse * moveBuilder.getBasePower() * 0.005;
    }
}
