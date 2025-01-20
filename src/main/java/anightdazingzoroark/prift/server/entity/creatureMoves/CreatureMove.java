package anightdazingzoroark.prift.server.entity.creatureMoves;

public enum CreatureMove {
    BOUNCE(RiftBounceMove.class, MoveType.STATUS, ChargeType.NONE, 0, 0, 0),
    TACKLE(RiftTackleMove.class, MoveType.WEIGHT, ChargeType.NONE, 25, 0, 0),
    HEADBUTT(RiftHeadbuttMove.class, MoveType.CHARGE, ChargeType.NONE, 25, 0, 0),
    STOMP(RiftStompMove.class, MoveType.STOMP, ChargeType.NONE, 25, 0, 0),
    SCRATCH(RiftScratchMove.class, MoveType.CLAW, ChargeType.NONE, 25, 0, 0),
    BITE(RiftBiteMove.class, MoveType.JAW, ChargeType.NONE, 25, 0, 0),
    SNARL(RiftSnarlMove.class, MoveType.STATUS, ChargeType.NONE, 0, 0, 60),
    POWER_ROAR(RiftPowerRoarMove.class, MoveType.STATUS, ChargeType.GRADIENT_THEN_USE, 0, 100, 200);

    public final Class<? extends RiftCreatureMove> creatureMove;
    public final MoveType moveType;
    public final ChargeType chargeType;
    public final int basePower;
    public final int maxUse; //this is in ticks
    public final int maxCooldown; //this is in ticks

    CreatureMove(Class<? extends RiftCreatureMove> creatureMove, MoveType moveType, ChargeType chargeType, int basePower, int maxUse, int maxCooldown) {
        this.creatureMove = creatureMove;
        this.moveType = moveType;
        this.chargeType = chargeType;
        this.basePower = basePower;
        this.maxUse = maxUse;
        this.maxCooldown = maxCooldown;
    }

    public RiftCreatureMove invokeMove() {
        RiftCreatureMove move = null;
        if (RiftCreatureMove.class.isAssignableFrom(this.creatureMove)) {
            try {
                move = this.creatureMove.getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (move == null) move = new RiftBounceMove();
        return move;
    }

    public enum MoveType {
        CHARGE, //headbutt based
        TAIL, //tail based
        STOMP, //stomp based
        CLAW, //claw based
        WEIGHT, //body based (like tackling or slamming with your body)
        JAW, //jaw/mouth based attacks
        RANGED, //attacks that are ranged or elemental attacks
        STATUS; //do not do damage
    }

    public enum ChargeType {
        GRADIENT_THEN_USE, //charges, then performs an action, then recharges based on a provided gradient and charge time
        GRADIENT_WHILE_USE, //charges while performing an action, then recharges based on a provided gradient and charge time
        COOLDOWN_ONLY, //does not charge, but has a recharge time
        NONE //no charging and no cooldown (and thus no charge bar on ui)
    }
}