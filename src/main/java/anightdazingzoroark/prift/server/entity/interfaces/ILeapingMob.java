package anightdazingzoroark.prift.server.entity.interfaces;

public interface ILeapingMob {
    default float getLeapHeight(int holdAmount, int min, int max) {
        return Math.min(6f, 0.25f * holdAmount + 1);
    }

    boolean isLeaping();
    void setLeaping(boolean value);
    float getLeapPower();
    void setLeapPower(float value);
}
