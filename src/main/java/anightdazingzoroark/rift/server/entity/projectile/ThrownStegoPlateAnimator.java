package anightdazingzoroark.rift.server.entity.projectile;

import net.minecraft.item.Item;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

//geckolib for 1.12.2 is weird like bruv
public class ThrownStegoPlateAnimator extends Item implements IAnimatable {
    public AnimationFactory factory = new AnimationFactory(this);

    public ThrownStegoPlateAnimator() {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
