package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityMoveHelper;
import org.jetbrains.annotations.NotNull;

public abstract class RiftCreatureMoveHelperBase extends EntityMoveHelper {
    protected final RiftCreature creature;
    @NotNull
    protected CreatureAction creatureAction = CreatureAction.WAIT;

    public RiftCreatureMoveHelperBase(RiftCreature creature) {
        super(creature);
        this.creature = creature;
    }

    public enum CreatureAction {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING, //classic jump upwards to move upwards
        CHARGE, //charge
        LEAP;
    }
}
