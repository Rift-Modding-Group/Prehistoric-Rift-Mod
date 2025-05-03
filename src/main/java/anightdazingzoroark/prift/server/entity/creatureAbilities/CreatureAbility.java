package anightdazingzoroark.prift.server.entity.creatureAbilities;

public enum CreatureAbility {
    INTIMIDATING, //applies weakness to all nearby mobs
    TAIL_SHED, //melee attacks using the tail may launch a projectile
    RIVALRY, //upon being near a larger creature of lower level, will gain strength and resistance
    HARMLESS, //the creature can never damage no matter what
    FRENZY, //killing a creature increases the damage this creature deals, stacks up to 5 times
    OBLIVIOUS, //ignores mobs of smaller size that try to attack it unless at 50% health or less or if they attack first
    INVERTER, //basically the dimetrodons ability
    ROUGH_SKIN, //4 free armor points
    LIFESUCKER, //heals by half the amount of damage it dealt to its target
    VENOMOUS_SKIN, //any melee attack against this creature has a 25% chance of poisoning the attacker
    NIMBLE, //cannot use attacking moves when moving, but movement speed is increased after moving for 5 seconds
    THICK_FUR, //staying near this creature increases temperature
    BULLETPROOF //immunity to projectiles
}
