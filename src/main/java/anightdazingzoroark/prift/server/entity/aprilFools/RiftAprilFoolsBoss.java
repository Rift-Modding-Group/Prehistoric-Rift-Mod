package anightdazingzoroark.prift.server.entity.aprilFools;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import java.util.List;

public abstract class RiftAprilFoolsBoss extends EntityLiving implements IAnimatable<AnimationDataEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

    public RiftAprilFoolsBoss(World worldIn) {
        super(worldIn);
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }
}
