package anightdazingzoroark.prift.server.entity.aprilFools;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

public abstract class RiftAprilFoolsBoss extends EntityLiving implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public RiftAprilFoolsBoss(World worldIn) {
        super(worldIn);
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
