package anightdazingzoroark.prift.server.entity.creatureinterface;

import net.minecraft.util.math.AxisAlignedBB;

public interface IApexPredator {
    void manageApplyApexEffect();
    AxisAlignedBB getEffectCastArea();
}
