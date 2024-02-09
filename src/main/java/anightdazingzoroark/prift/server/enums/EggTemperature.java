package anightdazingzoroark.prift.server.enums;

public enum EggTemperature {
    VERY_COLD(0),
    COLD(1),
    NEUTRAL(2),
    WARM(3),
    VERY_WARM(4);

    private final int tempStrength;

    EggTemperature(int tempStrength) {
        this.tempStrength = tempStrength;
    }

    public int getTempStrength() {
        return this.tempStrength;
    }
}
