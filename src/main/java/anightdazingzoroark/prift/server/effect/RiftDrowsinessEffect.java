package anightdazingzoroark.prift.server.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

public class RiftDrowsinessEffect extends RiftEffectBase {
    public RiftDrowsinessEffect() {
        super(true, 0xdcdcdc);

        //reduce movement speed
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.MOVEMENT_SPEED,
                "c93d67a8-e0bb-4d0c-be74-f46f1e1e4261",
                -0.10D,
                2
        );
        //reduce melee damage
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_DAMAGE,
                "01701cb2-ec75-45f1-898e-2d982db2e901",
                -0.10D,
                2
        );
        //reduce mining speed
        this.registerPotionAttributeModifier(
                SharedMonsterAttributes.ATTACK_SPEED,
                "5c3af757-9dd8-428b-b73a-35bfba3fcb15",
                -0.10D,
                2
        );
    }

    @Override
    public String name() {
        return "drowsiness";
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {

    }
}
