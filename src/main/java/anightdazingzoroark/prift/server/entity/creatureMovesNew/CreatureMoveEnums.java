package anightdazingzoroark.prift.server.entity.creatureMovesNew;

public class CreatureMoveEnums {
    public enum MoveType {
        PHYSICAL, //uses physical attack
        ELEMENTAL, //uses elemental attack
        STATUS //does non-damaging effects instead
    }

    //basically the type of animation to use in animating the creature
    public enum AnimType {
        BITE, //using the jaw
        CLAW, //using the claw, mainly deals with one of two of the claws
        STOMP, //for stomping
        TAIL, //for tail
        CHARGE, //for charging
        BREATH, //for breath attack
        ROAR, //for roar attack
        SPIN //for spin attacks
    }
}
