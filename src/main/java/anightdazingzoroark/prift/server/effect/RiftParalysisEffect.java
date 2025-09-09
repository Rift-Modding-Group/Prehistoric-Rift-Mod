package anightdazingzoroark.prift.server.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

public class RiftParalysisEffect extends RiftEffectBase {
    protected RiftParalysisEffect() {
        super(true, 0xFFFF00);

        //remove movement speed
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.MOVEMENT_SPEED,
                "e7c20e2e-b8c5-4a32-9f82-65ac5f7dcb00",
                -1D,
                2
        );
        //remove melee damage
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_DAMAGE,
                "43ba417e-8a40-4ccd-ad05-f870ade97f23",
                -1D,
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
