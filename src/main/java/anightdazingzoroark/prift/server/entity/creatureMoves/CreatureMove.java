package anightdazingzoroark.prift.server.entity.creatureMoves;

public enum CreatureMove {
    BOUNCE(RiftBounceMove.class, MoveType.STATUS, ChargeType.NONE, 0, 0, 0, false, false, false),
    TACKLE(RiftTackleMove.class, MoveType.CHARGE, ChargeType.NONE, 25, 0, 0, false, false, false),
    HEADBUTT(RiftHeadbuttMove.class, MoveType.HEAD, ChargeType.NONE, 25, 0, 0, false, false, false),
    STOMP(RiftStompMove.class, MoveType.STOMP, ChargeType.NONE, 25, 0, 0, false, false, false),
    SCRATCH(RiftScratchMove.class, MoveType.CLAW, ChargeType.NONE, 25, 0, 0, false, false, false),
    BITE(RiftBiteMove.class, MoveType.JAW, ChargeType.NONE, 25, 0, 0, false, false, false),
    SNARL(RiftSnarlMove.class, MoveType.STATUS, ChargeType.NONE, 0, 0, 60, false, false, false),
    POWER_ROAR(RiftPowerRoarMove.class, MoveType.STATUS, ChargeType.GRADIENT_THEN_USE, 0, 100, 200, false, false, false),
    TAIL_SLAP(RiftTailSlapMove.class, MoveType.TAIL, ChargeType.NONE, 25, 0, 0, false, false, false),
    THAGOMIZE(RiftThagomizeMove.class, MoveType.TAIL, ChargeType.GRADIENT_THEN_USE, 80, 100, 200, false, false, false),
    PLATE_FLING(RiftPlateFlingMove.class, MoveType.RANGED, ChargeType.COOLDOWN_ONLY, 25, 0, 100, false, false, false),
    CHARGE(RiftChargeMove.class, MoveType.CHARGE, ChargeType.GRADIENT_THEN_USE, 75, 100, 200, true, true, false),
    BIDE(RiftBideMove.class, MoveType.DEFENSE, ChargeType.GRADIENT_THEN_USE, 0, 100, 200, false, false, true), //basically like the since removed move from pokemon, absorb all damage you take, then reflect it all back to the opponent
    SELF_DESTRUCT(null, MoveType.RANGED, ChargeType.NONE, 300, 0, 0, false, false, false), //will be given only to anky for now
    LEAP(RiftLeapMove.class, MoveType.CHARGE, ChargeType.COOLDOWN_ONLY, 50, 0, 100, false, true, false),
    PACK_CALL(RiftPackCallMove.class, MoveType.STATUS, ChargeType.COOLDOWN_ONLY, 0, 0, 3600, false, false, false),
    TAIL_WHIP(RiftTailWhipMove.class, MoveType.TAIL, ChargeType.NONE, 0, 0, 0, false, false, false),
    POWER_BLOW(RiftPowerBlowMove.class, MoveType.RANGED, ChargeType.GRADIENT_THEN_USE, 0, 100, 200, false, false, false),
    SHOCK_BLAST(RiftShockBlastMove.class, MoveType.STATUS, ChargeType.COOLDOWN_ONLY, 0, 0, 3600, false, false, false),
    DEATH_ROLL(RiftDeathRollMove.class, MoveType.SPIN, ChargeType.GRADIENT_WHILE_USE, 60, 100, 200, false, true, true),
    LUNGE(RiftLungeMove.class, MoveType.CHARGE, ChargeType.COOLDOWN_ONLY, 25, 0, 100, false, true, false),
    GRAB(RiftGrabMove.class, MoveType.GRAB, ChargeType.NONE, 0, 0, 0, false, false, false),
    LIFE_DRAIN(RiftLifeDrain.class, MoveType.GRAB, ChargeType.GRADIENT_WHILE_USE, 60, 50, 100, false, true, true),
    CLOAK(RiftCloak.class, MoveType.STATUS, ChargeType.COOLDOWN_ONLY, 0, 0, 200, false, false, false),
    LIGHT_BLAST(null, MoveType.STATUS, ChargeType.BUILDUP, 60, 0, 0, false, false, false);

    public final Class<? extends RiftCreatureMove> creatureMove;
    public final MoveType moveType;
    public final ChargeType chargeType;
    public final int basePower;
    public final int maxUse; //this is in ticks
    public final int maxCooldown; //this is in ticks
    public final boolean chargeUpAffectsUseTime;
    public final boolean useTimeIsInfinite;
    public final boolean stopUponFullCharge; //stop using or charging up when the use bar is full

    CreatureMove(Class<? extends RiftCreatureMove> creatureMove, MoveType moveType, ChargeType chargeType, int basePower, int maxUse, int maxCooldown, boolean chargeUpAffectsUseTime, boolean useTimeIsInfinite, boolean stopUponFullCharge) {
        this.creatureMove = creatureMove;
        this.moveType = moveType;
        this.chargeType = chargeType;
        this.basePower = basePower;
        this.maxUse = maxUse;
        this.maxCooldown = maxCooldown;
        this.chargeUpAffectsUseTime = chargeUpAffectsUseTime;
        this.useTimeIsInfinite = useTimeIsInfinite;
        this.stopUponFullCharge = stopUponFullCharge;
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

    public String moveTypeName() {
        if (this == BIDE) return "bide";
        return this.moveType.toString().toLowerCase()+"_type";
    }

    public enum MoveType {
        CHARGE, //movement based (always requires movement to use)
        HEAD, //head hitting based
        TAIL, //tail based
        STOMP, //stomp based
        CLAW, //claw based
        JAW, //jaw/mouth based attacks
        RANGED, //attacks that are ranged or elemental attacks
        DEFENSE, //for things that involve defending like hiding in shell or using a shield
        SPIN, //attacks that involve spinning
        GRAB, //for moves that involve grabbing a creature
        STATUS //do not do damage, or damage isn't really important
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