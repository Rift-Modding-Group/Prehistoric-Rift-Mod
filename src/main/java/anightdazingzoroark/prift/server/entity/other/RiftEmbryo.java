package anightdazingzoroark.prift.server.entity.other;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

//this only exists so that an embryo will be rendered in the
//ui for pregnant mammals
public class RiftEmbryo extends EntityAnimal implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);

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
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
