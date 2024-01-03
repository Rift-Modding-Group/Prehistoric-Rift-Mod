package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.util.math.AxisAlignedBB;

public interface IApexPredator {
    void manageApplyApexEffect();
    AxisAlignedBB getEffectCastArea();
}
