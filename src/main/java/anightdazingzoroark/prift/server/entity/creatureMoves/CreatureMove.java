package anightdazingzoroark.prift.server.entity.creatureMoves;

public enum CreatureMove {
    BOUNCE(RiftBounceMove.class, MoveType.STATUS, ChargeType.NONE, 0, 0, 0, 0, 0, 0, 0, 0),
    TACKLE(RiftTackleMove.class, MoveType.CHARGE, ChargeType.NONE, 25, 0, 0, 0, 5, 0, 5, 0),
    HEADBUTT(RiftHeadbuttMove.class, MoveType.HEAD, ChargeType.NONE, 25, 0, 0, 0, 10, 2.5, 0, 7.5),
    STOMP(RiftStompMove.class, MoveType.STOMP, ChargeType.NONE, 25, 0, 0, 0, 10, 2.5, 0, 7.5),
    SCRATCH(RiftScratchMove.class, MoveType.CLAW, ChargeType.NONE, 25, 0, 0, 0, 10, 2.5, 0, 7.5),
    BITE(RiftBiteMove.class, MoveType.JAW, ChargeType.NONE, 25, 0, 0, 0, 10, 2.5, 0, 7.5),
    SNARL(RiftSnarlMove.class, MoveType.STATUS, ChargeType.NONE, 0, 0, 60, 0, 10, 2.5, 0, 7.5),
    POWER_ROAR(RiftPowerRoarMove.class, MoveType.STATUS, ChargeType.GRADIENT_THEN_USE, 0, 100, 200, 0, -1, 5, 22.5, 7.5),
    TAIL_SLAP(RiftTailSlapMove.class, MoveType.TAIL, ChargeType.NONE, 25, 0, 0, 0, 10, 2.5, 0, 7.5),
    THAGOMIZE(RiftThagomizeMove.class, MoveType.TAIL, ChargeType.GRADIENT_THEN_USE, 80, 100, 200, 0, -1, 0, 0, 0),
    PLATE_FLING(RiftPlateFlingMove.class, MoveType.RANGED, ChargeType.COOLDOWN_ONLY, 25, 0, 100, 0, 10, 2.5, 0, 7.5),
    CHARGE(RiftChargeMove.class, MoveType.CHARGE, ChargeType.GRADIENT_THEN_USE, 75, 100, 200, 5, -1, 0, 0, 0),
    BIDE(null, MoveType.DEFENSE, ChargeType.GRADIENT_THEN_USE, 80, 100, 200, 5, -1, 0, 0, 0), //basically like the since removed move from pokemon, absorb all damage you take, then reflect it all back to the opponent
    SELF_DESTRUCT(null, MoveType.RANGED, ChargeType.NONE, 300, 0, 0, 0, -1, 0, 0,0); //will be given only to anky for now

    public final Class<? extends RiftCreatureMove> creatureMove;
    public final MoveType moveType;
    public final ChargeType chargeType;
    public final int basePower;
    public final int maxUse; //this is in ticks
    public final int maxCooldown; //this is in ticks
    public final double startMoveDelay; //stage one, is basically for getting into position for certain moves, this is in ticks
    public final double chargeUp; //stage two, for charging up the attack, this is in ticks
    public final double chargeUpToUse; //stage three, for transition between charging up then use, this is in ticks
    public final double useDuration; //stage four, for utilizing the move, this is in ticks
    public final double recoverFromUse; //final stage, for stopping use of move, this is in ticks

    CreatureMove(Class<? extends RiftCreatureMove> creatureMove, MoveType moveType, ChargeType chargeType, int basePower, int maxUse, int maxCooldown, double startMoveDelay, double chargeUp, double chargeUpToUse, double useDuration, double recoverFromUse) {
        this.creatureMove = creatureMove;
        this.moveType = moveType;
        this.chargeType = chargeType;
        this.basePower = basePower;
        this.maxUse = maxUse;
        this.maxCooldown = maxCooldown;
        this.startMoveDelay = startMoveDelay;
        this.chargeUp = chargeUp;
        this.chargeUpToUse = chargeUpToUse;
        this.useDuration = useDuration;
        this.recoverFromUse = recoverFromUse;
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
        CHARGE(10, 0.25, 0.25), //movement based (always requires movement to use)
        HEAD(0, 0.625D, 0.5), //head hitting based
        TAIL(20, 0.625D, 0.5), //tail based
        STOMP(20, 0.625D, 0), //stomp based
        CLAW(20, 0.625D, 0), //claw based
        JAW(20, 0.625D, 0), //jaw/mouth based attacks
        RANGED(20, 0.625D, 0.5D), //attacks that are ranged or elemental attacks
        DEFENSE(0, 0, 0), //for things that involve defending like hiding in shell or using a shield
        STATUS(40, 0.25D, 0.125D); //do not do damage, or damage isn't really important

        public final int animTotalLength; //this is in ticks
        public final double animPercentOnUse;
        public final double animPercentToCharge;

        MoveType(int animTotalLength, double animPercentOnUse, double animPercentToCharge) {
            this.animTotalLength = animTotalLength;
            this.animPercentOnUse = animPercentOnUse;
            this.animPercentToCharge = animPercentToCharge;
        }
    }

    public enum ChargeType {
        GRADIENT_THEN_USE, //charges, then performs an action, then recharges based on a provided gradient and charge time
        GRADIENT_WHILE_USE, //charges while performing an action, then recharges based on a provided gradient and charge time
        COOLDOWN_ONLY, //does not charge, but has a recharge time
        BUILDUP, //charges after performing a specific action only, like killing creatures
        NONE; //no charging and no cooldown (and thus no charge bar on ui)

        public boolean requiresCharge() {
            return this == GRADIENT_THEN_USE || this == GRADIENT_WHILE_USE;
        }
    }
}