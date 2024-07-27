package anightdazingzoroark.prift.server.entity.interfaces;

public interface IChargingMob {
    boolean isLoweringHead();
    void setLowerHead(boolean value);
    boolean canCharge();
    void setCanCharge(boolean value);
    boolean isStartCharging();
    void setStartCharging(boolean value);
    boolean isCharging();
    void setIsCharging(boolean value);
    boolean isEndCharging();
    void setEndCharging(boolean value);
    default boolean isNotUtilizingCharging() {
        return !this.isLoweringHead() && !this.isStartCharging() && !this.isCharging() && !this.isEndCharging();
    }
    float chargeWidth();
}
