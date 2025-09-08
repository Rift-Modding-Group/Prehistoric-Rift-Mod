package anightdazingzoroark.prift.server.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

public class RiftEffectParalysis extends RiftEffectBase {
    protected RiftEffectParalysis() {
        super(true, 0xFFFF00);

        //reduce movement speed
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.MOVEMENT_SPEED,
                "e7c20e2e-b8c5-4a32-9f82-65ac5f7dcb00",
                -0.15D,
                2
        );
        //reduce melee damage
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_DAMAGE,
                "43ba417e-8a40-4ccd-ad05-f870ade97f23",
                -0.15D,
                2
        );
        //reduce mining speed
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_SPEED,
                "1c1dfbe7-394a-4a50-8db0-870b6c1f4b4a",
                -0.10D,
                2
        );
    }

    public String name() {
        return "paralysis";
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {}
}
