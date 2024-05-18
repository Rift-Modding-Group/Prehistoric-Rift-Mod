package anightdazingzoroark.prift.server.entity.interfaces;

public interface IMammal {
    void setPregnant(boolean value, int timer);
    boolean isPregnant();
    void setPregnancyTimer(int value);
    int getPregnancyTimer();
}
