package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.EntityLivingBase;

public interface IRangedAttacker {
    void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor);
    float rangedWidth();
}
