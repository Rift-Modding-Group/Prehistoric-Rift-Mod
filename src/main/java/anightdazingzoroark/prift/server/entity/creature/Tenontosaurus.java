package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Tenontosaurus extends RiftCreature {
    public Tenontosaurus(World worldIn) {
        //super(worldIn, RiftCreatureType.TENONTOSAURUS);
        super(worldIn, null);
    }

    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        return null;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        return Collections.emptyMap();
    }

    @Override
    public float[] ageScaleParams() {
        return new float[0];
    }

    @Override
    public float attackWidth() {
        return 0;
    }
}
