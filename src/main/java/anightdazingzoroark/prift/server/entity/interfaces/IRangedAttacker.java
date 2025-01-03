package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;

public interface IRangedAttacker {
    void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor);
    float rangedWidth();
    SoundEvent rangedAttackSound();
}
