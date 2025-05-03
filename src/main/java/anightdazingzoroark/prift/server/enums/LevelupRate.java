package anightdazingzoroark.prift.server.enums;

public enum LevelupRate {
    VERY_SLOW(1.6D),
    SLOW(1.4D),
    NORMAL(1.2D),
    FAST(1D),
    VERY_FAST(0.8D);

    private final double rate;

    LevelupRate(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return this.rate;
    }
}
