package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;

public class PlaceholderBuilder extends CreatureMoveBuilder {
    public PlaceholderBuilder() {
        this.setMoveType(CreatureMoveBuilder.MoveType.STATUS);
    }
}
