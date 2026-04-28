package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;

//store names of all move builders here
public class CreatureMoveNew {
    public static final String BITE = "bite";
    public static final String STOMP = "stomp";
    public static final String THAGOMIZE = "thagomize";

    //some common raisePriorityPredicate instances for use
    public static final BiFunction<RiftCreatureNew, Entity, Integer> generalMeleePredicate = (creatureNew, possibleTarget) -> {
        if (possibleTarget == null) return -1;
        if (creatureNew.getDistance(possibleTarget) <= creatureNew.getCreatureType().getPhysicalReach())
            return 3;
        return -1;
    };
    public static final BiFunction<RiftCreatureNew, Entity, Integer> generalRangedPredicate = (creatureNew, possibleTarget) -> {
        if (possibleTarget == null) return -1;
        if (creatureNew.getDistance(possibleTarget) > creatureNew.getCreatureType().getPhysicalReach()
                && creatureNew.getDistance(possibleTarget) <= 16) //that 16 is temporary, idk really what to do with it
            return 2;
        return -1;
    };

    //some common setOnMoveHitEffect instances for use
    public static final Consumer<RiftCreatureNew> generalAttackEntityEffect = creature -> {
        creature.attackEntityAsMob(creature.getAttackTarget());
    };

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
