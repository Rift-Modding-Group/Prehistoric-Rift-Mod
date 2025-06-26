package anightdazingzoroark.prift.server.entity.projectile;

import net.minecraft.item.Item;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

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
