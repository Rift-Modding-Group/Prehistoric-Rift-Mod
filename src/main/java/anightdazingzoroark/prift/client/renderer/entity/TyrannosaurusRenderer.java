package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class TyrannosaurusRenderer extends RiftCreatureRenderer {
    public TyrannosaurusRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.5f, 3.25f);

        GeckoLibCache.getInstance().parser.setValue("tyrannosaurus_move_use", this.moveAnimModifier(animatable));

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("chest").get().setHidden(true);

        //change size and rotate neck
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }

    private double moveAnimModifier(RiftCreature animatable) {
        if (animatable.currentCreatureMove() == null) return 0;
        if (animatable.currentCreatureMove().moveType == CreatureMove.MoveType.STATUS) {
            return RiftUtil.slopeResult(animatable.getCurrentMoveUse(), true, 0, animatable.currentCreatureMove().maxUse, 0, 20D);
        }
        return 0;
    }
}
