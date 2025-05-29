package anightdazingzoroark.prift.server.entity.creatureMoves;

public enum CreatureMove {
    BOUNCE(RiftBounceMove.class, MoveAnimType.STATUS, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false),
    TACKLE(RiftTackleMove.class, MoveAnimType.CHARGE, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    HEADBUTT(RiftHeadbuttMove.class, MoveAnimType.HEAD, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    STOMP(RiftStompMove.class, MoveAnimType.STOMP, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    SCRATCH(RiftScratchMove.class, MoveAnimType.CLAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    BITE(RiftBiteMove.class, MoveAnimType.JAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    SNARL(RiftSnarlMove.class, MoveAnimType.STATUS, ChargeType.NONE, 0, new int[]{0}, 0, 60, false, false, false),
    POWER_ROAR(RiftPowerRoarMove.class, MoveAnimType.ROAR, ChargeType.GRADIENT_THEN_USE, 0, new int[]{10, 20}, 100, 200, false, false, false),
    TAIL_SLAP(RiftTailSlapMove.class, MoveAnimType.TAIL, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    THAGOMIZE(RiftThagomizeMove.class, MoveAnimType.TAIL, ChargeType.GRADIENT_THEN_USE, 80, new int[]{10, 20}, 100, 200, false, false, false),
    PLATE_FLING(RiftPlateFlingMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{2}, 0, 100, false, false, false),
    CHARGE(RiftChargeMove.class, MoveAnimType.CHARGE, ChargeType.GRADIENT_THEN_USE, 75, new int[]{10, 20}, 100, 200, true, true, false),
    BIDE(RiftBideMove.class, MoveAnimType.DEFENSE, ChargeType.GRADIENT_THEN_USE, 0, new int[]{20, 40}, 100, 200, false, false, true), //basically like the since removed move from pokemon, absorb all damage you take, then reflect it all back to the opponent
    SELF_DESTRUCT(null, MoveAnimType.RANGED, ChargeType.NONE, 300, new int[]{0}, 0, 0, false, false, false), //will be given only to anky for now
    POUNCE(RiftPounceMove.class, MoveAnimType.LEAP, ChargeType.COOLDOWN_ONLY, 50, new int[]{6}, 0, 100, false, true, false),
    PACK_CALL(RiftPackCallMove.class, MoveAnimType.ROAR, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 3600, false, false, false),
    TAIL_WHIP(RiftTailWhipMove.class, MoveAnimType.TAIL, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false),
    POWER_BLOW(RiftPowerBlowMove.class, MoveAnimType.BLOW, ChargeType.GRADIENT_THEN_USE, 0, new int[]{10, 20}, 100, 200, false, false, false),
    SHOCK_BLAST(RiftShockBlastMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{20}, 0, 3600, false, false, false),
    DEATH_ROLL(RiftDeathRollMove.class, MoveAnimType.SPIN, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{10, 20}, 100, 200, false, true, true),
    LUNGE(RiftLungeMove.class, MoveAnimType.CHARGE, ChargeType.COOLDOWN_ONLY, 25, new int[]{8}, 0, 100, false, true, false),
    GRAB(RiftGrabMove.class, MoveAnimType.GRAB, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false),
    LIFE_DRAIN(RiftLifeDrainMove.class, MoveAnimType.GRAB, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{4, 12}, 50, 100, false, true, true),
    CLOAK(RiftCloakMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 200, false, false, false),
    LIGHT_BLAST(RiftLightBlastMove.class, MoveAnimType.ROAR, ChargeType.BUILDUP, 60, new int[]{20}, 10, 0, false, false, false),
    SNIFF(RiftSniffMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 100, false, false, false),
    LEAP(RiftLeapMove.class, MoveAnimType.LEAP, ChargeType.COOLDOWN_ONLY, 0, new int[]{6}, 0, 100, false, true, false),
    POISON_CLAW(RiftPoisonClawMove.class, MoveAnimType.CLAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false),
    POISON_SPIT(RiftPoisonSpitMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{2}, 0, 0, false, false, false),
    SHELLTER(RiftShellterMove.class, MoveAnimType.DEFENSE, ChargeType.GRADIENT_WHILE_USE, 0, new int[]{0, 0}, 100, 100, false, true, true),
    SHELL_SPIN(RiftShellSpinMove.class, MoveAnimType.SPIN, ChargeType.GRADIENT_WHILE_USE, 45, new int[]{20, 40}, 100, 300, false, true, true),
    POISON_TRAP(RiftPoisonTrapMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{10}, 0, 200, false, false, false),
    KICK(RiftKickMove.class, MoveAnimType.KICK, ChargeType.COOLDOWN_ONLY, 25, new int[]{6}, 0, 100, false, false, false),
    PECK(RiftPeckMove.class, MoveAnimType.BEAK, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false), //peck wih a beak
    LEECH(null, MoveAnimType.JAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false), //bite to deal little damage but drain some health
    VENOM_BOMB(null, MoveAnimType.RANGED, ChargeType.GRADIENT_THEN_USE, 25, new int[]{5, 15}, 0, 100, false, false, false), //spit a bomb that explodes and deals lots of damage + poisons mobs in range
    CLIMATE_BLAST(null, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 25, new int[]{20}, 0, 100, false, false, false), //blast fire or frost depending on temperature
    BURROW(null, MoveAnimType.BURROW, ChargeType.GRADIENT_WHILE_USE, 40, new int[]{4, 8}, 100, 200, false, true, true), //burrow into the ground
    PLATE_SCATTER(null, MoveAnimType.SCATTER, ChargeType.COOLDOWN_ONLY, 50, new int[]{20}, 0, 150, false, false, false), //scatter plates
    GNASH(null, MoveAnimType.GNASH, ChargeType.COOLDOWN_ONLY, 60, new int[]{20}, 0, 600, false, false, false); //user picks up and damages a target, then spits them out

    public final Class<? extends RiftCreatureMove> creatureMove;
    public final MoveAnimType moveAnimType;
    public final ChargeType chargeType;
    public final int basePower;
    public final int[] energyUse;
    public final int maxUse; //this is in ticks
    public final int maxCooldown; //this is in ticks
    public final boolean chargeUpAffectsUseTime;
    public final boolean useTimeIsInfinite;
    public final boolean stopUponFullCharge; //stop using or charging up when the use bar is full

    CreatureMove(Class<? extends RiftCreatureMove> creatureMove, MoveAnimType moveType, ChargeType chargeType, int basePower, int[] energyUse, int maxUse, int maxCooldown, boolean chargeUpAffectsUseTime, boolean useTimeIsInfinite, boolean stopUponFullCharge) {
        this.creatureMove = creatureMove;
        this.moveAnimType = moveType;
        this.chargeType = chargeType;
        this.basePower = basePower;
        this.energyUse = energyUse;
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
        return this.moveAnimType.toString().toLowerCase()+"_type";
    }

    public enum MoveAnimType {
        CHARGE(MoveType.RANGED_SELDOM), //movement based (always requires movement to use),
        LEAP(MoveType.RANGED_SELDOM), //involves jumping
        HEAD(MoveType.MELEE), //head hitting based
        TAIL(MoveType.MELEE), //tail based
        STOMP(MoveType.MELEE), //stomp based
        CLAW(MoveType.MELEE), //claw based
        JAW(MoveType.MELEE), //jaw/mouth based attacks
        RANGED(MoveType.RANGED), //attacks that are ranged or elemental attacks
        DEFENSE(MoveType.SUPPORT), //for things that involve defending like hiding in shell or using a shield
        SPIN(MoveType.MELEE), //attacks that involve spinning
        GRAB(MoveType.MELEE), //for moves that involve grabbing a creature
        ROAR(MoveType.SUPPORT), //for moves that involve the user roaring or calling
        STATUS(MoveType.SUPPORT), //do not do damage, or damage isn't really important
        KICK(MoveType.MELEE), //involving kicking things
        BEAK(MoveType.MELEE), //involving hitting using a beak
        BLOW(MoveType.RANGED), //involves blowing
        BURROW(MoveType.RANGED_SELDOM), //involves burrowing
        SCATTER(MoveType.RANGED), //involves scattering projectiles
        GNASH(MoveType.MELEE); //involves

        public final MoveType moveType;

        MoveAnimType(MoveType moveType) {
            this.moveType = moveType;
        }
    }

    public enum MoveType {
        SUPPORT,
        RANGED,
        RANGED_SELDOM,
        MELEE
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