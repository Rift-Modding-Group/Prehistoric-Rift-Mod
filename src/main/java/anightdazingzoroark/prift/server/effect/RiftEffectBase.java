package anightdazingzoroark.prift.server.effect;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public abstract class RiftEffectBase extends Potion {
    public RiftEffectBase(boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
        this.setPotionName("effect."+this.name());
        this.setRegistryName(RiftInitialize.MODID, this.name());
    }

    public abstract String name();

    public abstract boolean isReady(int duration, int amplifier);

    public abstract void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier);
}
