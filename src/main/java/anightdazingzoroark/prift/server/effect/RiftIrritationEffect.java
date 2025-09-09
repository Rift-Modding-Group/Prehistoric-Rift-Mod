package anightdazingzoroark.prift.server.effect;

import net.minecraft.entity.EntityLivingBase;

public class RiftIrritationEffect extends RiftEffectBase {
    public RiftIrritationEffect() {
        super(true, 0xc0c0c0);
    }

    @Override
    public String name() {
        return "irritation";
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {

    }
}
