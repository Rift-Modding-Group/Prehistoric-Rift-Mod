package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.Entity;

public interface ILeapAttackingMob extends ILeapingMob {
    Entity getControlledLeapTarget();
    void setControlledLeapTarget(Entity value);
    boolean startLeapingToTarget();
    void setStartLeapToTarget(boolean value);
    float leapWidth();
}
