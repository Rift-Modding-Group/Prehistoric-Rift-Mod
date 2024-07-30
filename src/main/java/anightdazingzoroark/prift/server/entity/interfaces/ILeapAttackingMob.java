package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.EntityLivingBase;

public interface ILeapAttackingMob extends ILeapingMob {
    EntityLivingBase getControlledLeapTarget();
    void setControlledLeapTarget(EntityLivingBase value);
    boolean startLeapingToTarget();
    void setStartLeapToTarget(boolean value);
}
