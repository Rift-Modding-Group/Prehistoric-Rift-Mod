package anightdazingzoroark.prift.server.entity.other;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;
import anightdazingzoroark.riftlib.core.IAnimatable;

import javax.annotation.Nullable;

//this only exists so that an embryo will be rendered in the
//ui for pregnant mammals
public class RiftEmbryo extends EntityAnimal implements IAnimatable<AnimationDataEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

    public RiftEmbryo(World worldIn) {
        super(worldIn);
        this.setSize(1f, 1f);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void registerControllers(AnimationDataEntity animationData) {

    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }
}
