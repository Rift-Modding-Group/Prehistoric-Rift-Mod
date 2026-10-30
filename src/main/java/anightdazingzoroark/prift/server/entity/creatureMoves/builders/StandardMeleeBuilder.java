package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;

public abstract class StandardMeleeBuilder extends CreatureMoveBuilder {
    public StandardMeleeBuilder() {
        this.setMakesContact()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.02f);
    }
}
