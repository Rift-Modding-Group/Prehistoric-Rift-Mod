package anightdazingzoroark.prift.server.entity.projectile;

import net.minecraft.item.Item;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class MudballAnimator extends Item implements IAnimatable {
    public AnimationFactory factory = new AnimationFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
