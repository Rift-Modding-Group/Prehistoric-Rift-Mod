package anightdazingzoroark.prift.server.entity.creatureMoves;

import net.minecraft.client.resources.I18n;

/*
  TODO: this must be eventually changed to be more like what i did for creature projectiles
   my pp gets so hard looking at it that it must be a standard for stuff like this
 */
public enum CreatureMove {
    BOUNCE(RiftBounceMove.class, MoveAnimType.STATUS, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    TACKLE(RiftTackleMove.class, MoveAnimType.CHARGE, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, true, false, TargetRequirement.HAS_TARGET),
    HEADBUTT(RiftHeadbuttMove.class, MoveAnimType.HEAD, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    STOMP(RiftStompMove.class, MoveAnimType.STOMP, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    SCRATCH(RiftScratchMove.class, MoveAnimType.CLAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    BITE(RiftBiteMove.class, MoveAnimType.JAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    SNARL(RiftSnarlMove.class, MoveAnimType.GROWL, ChargeType.NONE, 0, new int[]{0}, 0, 60, false, false, false, TargetRequirement.HAS_TARGET),
    POWER_ROAR(RiftPowerRoarMove.class, MoveAnimType.ROAR, ChargeType.GRADIENT_THEN_USE, 0, new int[]{10, 20}, 100, 200, false, false, false, TargetRequirement.HAS_TARGET),
    TAIL_SLAP(RiftTailSlapMove.class, MoveAnimType.TAIL, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    THAGOMIZE(RiftThagomizeMove.class, MoveAnimType.TAIL, ChargeType.GRADIENT_THEN_USE, 80, new int[]{10, 20}, 100, 200, false, false, false, TargetRequirement.HAS_TARGET),
    PLATE_FLING(RiftPlateFlingMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{2}, 0, 100, false, false, false, TargetRequirement.HAS_TARGET),
    CHARGE(RiftChargeMove.class, MoveAnimType.CHARGE, ChargeType.GRADIENT_THEN_USE, 75, new int[]{10, 20}, 100, 200, true, true, false, TargetRequirement.HAS_TARGET),
    BIDE(RiftBideMove.class, MoveAnimType.DEFENSE, ChargeType.GRADIENT_THEN_USE, 0, new int[]{20, 40}, 100, 200, false, false, true, TargetRequirement.HAS_TARGET), //basically like the since removed move from pokemon, absorb all damage you take, then reflect it all back to the opponent
    SELF_DESTRUCT(RiftSelfDestructMove.class, MoveAnimType.SELF_DESTRUCTION, ChargeType.NONE, 300, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    POUNCE(RiftPounceMove.class, MoveAnimType.LEAP, ChargeType.COOLDOWN_ONLY, 50, new int[]{6}, 0, 100, false, true, false, TargetRequirement.HAS_TARGET),
    PACK_CALL(RiftPackCallMove.class, MoveAnimType.ROAR, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 3600, false, false, false, TargetRequirement.HAS_TARGET),
    TAIL_WHIP(RiftTailWhipMove.class, MoveAnimType.TAIL, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    POWER_BLOW(RiftPowerBlowMove.class, MoveAnimType.BLOW, ChargeType.GRADIENT_THEN_USE, 0, new int[]{10, 20}, 100, 200, false, false, false, TargetRequirement.HAS_TARGET),
    SHOCK_BLAST(RiftShockBlastMove.class, MoveAnimType.ROAR, ChargeType.COOLDOWN_ONLY, 0, new int[]{20}, 0, 3600, false, false, false, TargetRequirement.HAS_TARGET),
    DEATH_ROLL(RiftDeathRollMove.class, MoveAnimType.SPIN, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{10, 20}, 100, 200, false, true, true, TargetRequirement.HAS_TARGET),
    LUNGE(RiftLungeMove.class, MoveAnimType.CHARGE, ChargeType.COOLDOWN_ONLY, 25, new int[]{8}, 0, 100, false, true, false, TargetRequirement.HAS_TARGET),
    GRAB(RiftGrabMove.class, MoveAnimType.GRAB, ChargeType.NONE, 0, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    LIFE_DRAIN(RiftLifeDrainMove.class, MoveAnimType.GRAB, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{4, 12}, 50, 100, false, true, true, TargetRequirement.HAS_TARGET),
    CLOAK(RiftCloakMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 200, false, false, false, TargetRequirement.HAS_NO_TARGET),
    LIGHT_BLAST(RiftLightBlastMove.class, MoveAnimType.ROAR, ChargeType.BUILDUP, 60, new int[]{20}, 10, 0, false, false, false, TargetRequirement.HAS_NO_TARGET),
    SNIFF(RiftSniffMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 100, false, false, false, TargetRequirement.HAS_NO_TARGET),
    LEAP(RiftLeapMove.class, MoveAnimType.LEAP, ChargeType.COOLDOWN_ONLY, 0, new int[]{6}, 0, 100, false, true, false, TargetRequirement.HAS_NO_TARGET),
    POISON_CLAW(RiftPoisonClawMove.class, MoveAnimType.CLAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    POISON_SPIT(RiftPoisonSpitMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{2}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    SHELLTER(RiftShellterMove.class, MoveAnimType.DEFENSE, ChargeType.GRADIENT_WHILE_USE, 0, new int[]{0, 0}, 100, 100, false, true, true, TargetRequirement.TARGET_DOESNT_MATTER),
    SHELL_SPIN(RiftShellSpinMove.class, MoveAnimType.SPIN, ChargeType.GRADIENT_WHILE_USE, 45, new int[]{20, 40}, 100, 300, false, true, true, TargetRequirement.HAS_TARGET),
    POISON_TRAP(RiftPoisonTrapMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{10}, 0, 200, false, false, false, TargetRequirement.HAS_NO_TARGET),
    KICK(RiftKickMove.class, MoveAnimType.KICK, ChargeType.COOLDOWN_ONLY, 25, new int[]{6}, 0, 100, false, false, false, TargetRequirement.HAS_TARGET),
    PECK(RiftPeckMove.class, MoveAnimType.BEAK, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET),
    LEECH(RiftLeechMove.class, MoveAnimType.JAW, ChargeType.NONE, 25, new int[]{0}, 0, 0, false, false, false, TargetRequirement.HAS_TARGET), //bite to deal little damage but drain some health
    VENOM_BOMB(RiftVenomBombMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 25, new int[]{5, 15}, 0, 100, false, false, false, TargetRequirement.HAS_TARGET), //spit a bomb that explodes and deals lots of damage + poisons mobs in range
    CLIMATE_BLAST(RiftClimateBlastMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 25, new int[]{8}, 0, 100, false, false, false, TargetRequirement.HAS_TARGET), //blast fire or frost depending on temperature
    BURROW(RiftBurrowMove.class, MoveAnimType.BURROW, ChargeType.GRADIENT_WHILE_USE, 40, new int[]{4, 8}, 100, 200, false, true, true, TargetRequirement.HAS_TARGET), //burrow into the ground
    PLATE_SCATTER(RiftPlateScatterMove.class, MoveAnimType.SCATTER, ChargeType.COOLDOWN_ONLY, 50, new int[]{20}, 0, 150, false, false, false, TargetRequirement.HAS_TARGET), //scatter plates
    GNASH(RiftGnashMove.class, MoveAnimType.GNASH, ChargeType.GRADIENT_WHILE_USE, 60, new int[]{20, 40}, 100, 300, false, true, true, TargetRequirement.HAS_TARGET), //user picks up and damages a target by thrashing them around, then throws them
    MUDBALL(RiftMudballMove.class, MoveAnimType.RANGED, ChargeType.COOLDOWN_ONLY, 30, new int[]{0}, 0, 100, false, false, false, TargetRequirement.HAS_TARGET), //user throws mud to blind target
    HYPNOSIS_POWDER(RiftHypnosisPowderMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 300, false, false, false, TargetRequirement.HAS_TARGET),
    POISON_POWDER(RiftPoisonPowderMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 300, false, false, false, TargetRequirement.HAS_TARGET),
    ITCHING_POWDER(RiftItchingPowderMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 100, false, false, false, TargetRequirement.HAS_TARGET),
    PARALYZING_POWDER(RiftParalyzingPowderMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 300, false, false, false, TargetRequirement.HAS_TARGET),
    RAGE_POWDER(RiftRagePowderMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 300, false, false, false, TargetRequirement.HAS_TARGET),
    SLEEP_POWDER(RiftSleepPowderMove.class, MoveAnimType.STATUS, ChargeType.COOLDOWN_ONLY, 0, new int[]{0}, 0, 300, false, false, false, TargetRequirement.HAS_TARGET);

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
    public final TargetRequirement targetRequirement; //conditions for whether or not a creature can use that move

    CreatureMove(Class<? extends RiftCreatureMove> creatureMove, MoveAnimType moveType, ChargeType chargeType, int basePower, int[] energyUse, int maxUse, int maxCooldown, boolean chargeUpAffectsUseTime, boolean useTimeIsInfinite, boolean stopUponFullCharge, TargetRequirement targetRequirement) {
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
        this.targetRequirement = targetRequirement;
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

    public String getFriendlyName() {
        return this.name().charAt(0)+this.name().substring(1).toLowerCase();
    }

    public String getTranslatedName() {
        return I18n.format("creature_move."+this.name().toLowerCase()+".name");
    }

    public String getTranslatedDescription() {
        return I18n.format("creature_move."+this.name().toLowerCase()+".description");
    }

    public static CreatureMove safeValueOf(String value) {
        for (CreatureMove move : CreatureMove.values()) {
            if (move.toString().equals(value)) return move;
        }
        return null;
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

    public enum TargetRequirement {
        HAS_TARGET,
        HAS_NO_TARGET,
        TARGET_DOESNT_MATTER
    }
}