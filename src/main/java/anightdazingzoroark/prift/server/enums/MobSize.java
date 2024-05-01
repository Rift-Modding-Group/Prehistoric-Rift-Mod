package anightdazingzoroark.prift.server.enums;

public enum MobSize {
    VERY_SMALL,
    SMALL,
    MEDIUM,
    LARGE,
    VERY_LARGE;

    public static MobSize safeValueOf(String string) {
        for (MobSize mobSize : MobSize.values()) {
            if (mobSize.name().equalsIgnoreCase(string)) {
                return mobSize;
            }
        }
        return null;
    }
}
