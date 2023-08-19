package anightdazingzoroark.rift.server.entity;

public enum TameBehaviorType {
    ASSIST,
    NEUTRAL,
    AGGRESSIVE,
    PASSIVE,
    TURRET;

    public final TameBehaviorType next() {
        int value = (this.ordinal() + 1) % TameBehaviorType.values().length;
        if (value == 4) {
            return TameBehaviorType.values()[0];
        }
        return TameBehaviorType.values()[value];
    }

    public final TameBehaviorType nextCanTurret() {
        return TameBehaviorType.values()[(this.ordinal() + 1) % TameBehaviorType.values().length];
    }
}
