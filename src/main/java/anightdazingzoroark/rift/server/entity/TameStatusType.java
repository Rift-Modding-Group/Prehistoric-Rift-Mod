package anightdazingzoroark.rift.server.entity;

public enum TameStatusType {
    STAND,
    SIT,
    WANDER;

    public final TameStatusType next() {
        return TameStatusType.values()[(this.ordinal() + 1) % TameStatusType.values().length];
    }
}
