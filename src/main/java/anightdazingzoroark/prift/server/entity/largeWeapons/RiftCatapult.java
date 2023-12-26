package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class RiftCatapult extends RiftLargeWeapon {
    public RiftCatapult(World worldIn) {
        super(worldIn, RiftLargeWeaponType.CATAPULT);
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
