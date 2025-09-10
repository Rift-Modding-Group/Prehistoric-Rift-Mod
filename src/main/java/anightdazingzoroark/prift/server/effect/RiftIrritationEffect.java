package anightdazingzoroark.prift.server.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

public class RiftIrritationEffect extends RiftEffectBase {
    public RiftIrritationEffect() {
        super(true, 0xc0c0c0);

        //reduce mining speed
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_SPEED,
                "2c161099-fa51-4ea3-88d6-2c1e04fb2ee6",
                -0.5D,
                1
        );
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
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {}
}
