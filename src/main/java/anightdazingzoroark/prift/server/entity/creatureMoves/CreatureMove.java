package anightdazingzoroark.prift.server.entity.creatureMoves;

public enum CreatureMove {
    BOUNCE(RiftBounceMove.class, MoveAnimType.STATUS, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    TACKLE(RiftTackleMove.class, MoveAnimType.CHARGE, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, true, false, new CreatureMoveCondition().setCheckForTarget()),
    HEADBUTT(RiftHeadbuttMove.class, MoveAnimType.HEAD, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    STOMP(RiftStompMove.class, MoveAnimType.STOMP, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    SCRATCH(RiftScratchMove.class, MoveAnimType.CLAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    BITE(RiftBiteMove.class, MoveAnimType.JAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    SNARL(RiftSnarlMove.class, MoveAnimType.GROWL, ChargeType.NONE, 0, new int[]{0}, 0, 60, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    POWER_ROAR(RiftPowerRoarMove.class, MoveAnimType.ROAR, ChargeType.GRADIENT_THEN_USE, 0, new int[]{10, 20}, 100, 200, false, false, false, new CreatureMoveCondition().setCheckForHit().setRNGChance(4)),
    TAIL_SLAP(RiftTailSlapMove.class, MoveAnimType.TAIL, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    THAGOMIZE(RiftThagomizeMove.class, MoveAnimType.TAIL, ChargeType.GRADIENT_THEN_USE, 80, new int[]{10, 20}, 100, 200, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    PLATE_FLING(RiftPlateFlingMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{2}, 0, 100, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    CHARGE(RiftChargeMove.class, MoveAnimType.CHARGE, ChargeType.GRADIENT_THEN_USE, 75, new int[]{10, 20}, 100, 200, true, true, false, new CreatureMoveCondition().setCheckForTarget()),
    BIDE(RiftBideMove.class, MoveAnimType.DEFENSE, ChargeType.GRADIENT_THEN_USE, 0, new int[]{20, 40}, 100, 200, false, false, true, new CreatureMoveCondition().setCheckForTarget()), //basically like the since removed move from pokemon, absorb all damage you take, then reflect it all back to the opponent
    SELF_DESTRUCT(RiftSelfDestructMove.class, MoveAnimType.SELF_DESTRUCTION, ChargeType.NONE, 300, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()), //will be given only to anky for now
    POUNCE(RiftPounceMove.class, MoveAnimType.LEAP, ChargeType.COOLDOWN_ONLY, 50, new int[]{6}, 0, 100, false, true, false, new CreatureMoveCondition().setCheckForTarget()),
    PACK_CALL(RiftPackCallMove.class, MoveAnimType.ROAR, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 3600, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    TAIL_WHIP(RiftTailWhipMove.class, MoveAnimType.TAIL, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    POWER_BLOW(RiftPowerBlowMove.class, MoveAnimType.BLOW, ChargeType.GRADIENT_THEN_USE, 0, new int[]{10, 20}, 100, 200, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    SHOCK_BLAST(RiftShockBlastMove.class, MoveAnimType.ROAR, ChargeType.COOLDOWN_ONLY, 0, new int[]{20}, 0, 3600, false, false, false, new CreatureMoveCondition().setCheckForHit().setRNGChance(4)),
    DEATH_ROLL(RiftDeathRollMove.class, MoveAnimType.SPIN, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{10, 20}, 100, 200, false, true, true, new CreatureMoveCondition().setCheckForTarget()),
    LUNGE(RiftLungeMove.class, MoveAnimType.CHARGE, ChargeType.COOLDOWN_ONLY, 25, new int[]{8}, 0, 100, false, true, false, new CreatureMoveCondition().setCheckForTarget()),
    GRAB(RiftGrabMove.class, MoveAnimType.GRAB, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    LIFE_DRAIN(RiftLifeDrainMove.class, MoveAnimType.GRAB, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{4, 12}, 50, 100, false, true, true, new CreatureMoveCondition().setCheckForTarget()),
    CLOAK(RiftCloakMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 200, false, false, false, new CreatureMoveCondition().setCheckForUncloaked()),
    LIGHT_BLAST(RiftLightBlastMove.class, MoveAnimType.ROAR, ChargeType.BUILDUP, 60, new int[]{20}, 10, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    SNIFF(RiftSniffMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 100, false, false, false, new CreatureMoveCondition().setInterval(3000)),
    LEAP(RiftLeapMove.class, MoveAnimType.LEAP, ChargeType.COOLDOWN_ONLY, 0, new int[]{6}, 0, 100, false, true, false, new CreatureMoveCondition()),
    POISON_CLAW(RiftPoisonClawMove.class, MoveAnimType.CLAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    POISON_SPIT(RiftPoisonSpitMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{2}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    SHELLTER(RiftShellterMove.class, MoveAnimType.DEFENSE, ChargeType.GRADIENT_WHILE_USE, 0, new int[]{0, 0}, 100, 100, false, true, true, new CreatureMoveCondition().setBelowHealthPercentage(0.25)),
    SHELL_SPIN(RiftShellSpinMove.class, MoveAnimType.SPIN, ChargeType.GRADIENT_WHILE_USE, 45, new int[]{20, 40}, 100, 300, false, true, true, new CreatureMoveCondition().setCheckForTarget()),
    POISON_TRAP(RiftPoisonTrapMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{10}, 0, 200, false, false, false, new CreatureMoveCondition()),
    KICK(RiftKickMove.class, MoveAnimType.KICK, ChargeType.COOLDOWN_ONLY, 25, new int[]{6}, 0, 100, false, false, false, new CreatureMoveCondition().setCheckForTarget()),
    PECK(RiftPeckMove.class, MoveAnimType.BEAK, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()), //peck wih a beak
    LEECH(RiftLeechMove.class, MoveAnimType.JAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, new CreatureMoveCondition().setCheckForTarget()), //bite to deal little damage but drain some health
    VENOM_BOMB(RiftVenomBombMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{5, 15}, 0, 100, false, false, false, new CreatureMoveCondition().setCheckForTarget()), //spit a bomb that explodes and deals lots of damage + poisons mobs in range
    CLIMATE_BLAST(RiftClimateBlastMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 25, new int[]{8}, 0, 100, false, false, false, new CreatureMoveCondition().setCheckForTarget()), //blast fire or frost depending on temperature
    BURROW(RiftBurrowMove.class, MoveAnimType.BURROW, ChargeType.GRADIENT_WHILE_USE, 40, new int[]{4, 8}, 100, 200, false, true, true, new CreatureMoveCondition().setCheckForTarget()), //burrow into the ground
    PLATE_SCATTER(RiftPlateScatterMove.class, MoveAnimType.SCATTER, ChargeType.COOLDOWN_ONLY, 50, new int[]{20}, 0, 150, false, false, false, new CreatureMoveCondition().setCheckForTarget()), //scatter plates
    GNASH(RiftGnashMove.class, MoveAnimType.GNASH, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{20, 40}, 100, 300, false, true, true, new CreatureMoveCondition().setCheckForTarget()), //user picks up and damages a target by thrashing them around, then throws them
    MUDBALL(RiftMudballMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 30, new int[]{0}, 0, 100, false, false, false, new CreatureMoveCondition().setCheckForTarget()); //user throws mud to blind target

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
    public final CreatureMoveCondition moveCondition; //condition for whether or not a creature can use that move

    CreatureMove(Class<? extends RiftCreatureMove> creatureMove, MoveAnimType moveType, ChargeType chargeType, int basePower, int[] energyUse, int maxUse, int maxCooldown, boolean chargeUpAffectsUseTime, boolean useTimeIsInfinite, boolean stopUponFullCharge, CreatureMoveCondition moveCondition) {
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
        this.moveCondition = moveCondition;
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
        RANGED(MoveType.RANGED), //involves spitting/breathing a projectile
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
        GNASH(MoveType.MELEE), //involves grabbing a creature to damage them
        SELF_DESTRUCTION(MoveType.MELEE), //involves suicide in order to do something
        GROWL(MoveType.SUPPORT), //involves growling as a form of support
        THROW(MoveType.RANGED); //involves throwing a projectile

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