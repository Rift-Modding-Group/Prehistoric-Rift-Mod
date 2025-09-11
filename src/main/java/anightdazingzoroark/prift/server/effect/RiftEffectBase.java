package anightdazingzoroark.prift.server.effect;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
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

    public abstract void onEffectAdded(EntityLivingBase entityLivingBase);

    public abstract void onEffectRemoved(EntityLivingBase entityLivingBase);

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        this.onEffectAdded(entityLivingBaseIn);
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        this.onEffectRemoved(entityLivingBaseIn);
    }
}
