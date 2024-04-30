package anightdazingzoroark.prift.server.entity.projectile;

import net.minecraft.item.Item;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ThrownBolaAnimator extends Item implements IAnimatable {
    public AnimationFactory factory = new AnimationFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rotation", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bola.spinning", true));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
