package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class RiftMortar extends RiftLargeWeapon {
    public RiftMortar(World worldIn) {
        super(worldIn, RiftLargeWeaponType.MORTAR, RiftItems.MORTAR, RiftItems.MORTAR_SHELL);
        this.setSize(1f, 2f);
    }

    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }
}
