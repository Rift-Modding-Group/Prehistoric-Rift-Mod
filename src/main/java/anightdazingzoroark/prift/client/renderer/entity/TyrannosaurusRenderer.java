package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.Tyrannosaurus;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class TyrannosaurusRenderer extends RiftCreatureRenderer {
    public TyrannosaurusRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.5f, 3.25f);

        //variables
        GeckoLibCache.getInstance().parser.setValue("jaw_type_move_lower_jaw", this.jawTypeMoveAnimModifier(animatable, "lowerJaw"));
        GeckoLibCache.getInstance().parser.setValue("jaw_type_move_head", this.jawTypeMoveAnimModifier(animatable, "head"));
        GeckoLibCache.getInstance().parser.setValue("jaw_type_move_neck", this.jawTypeMoveAnimModifier(animatable, "neck"));
        GeckoLibCache.getInstance().parser.setValue("jaw_type_move_body", this.jawTypeMoveAnimModifier(animatable, "body"));

        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_left_shin_rot", this.stompTypeMoveRotationModifier(animatable, "leftShin"));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_left_shin_pos_y", this.stompTypeMovePosModifier(animatable, "leftShin", 1));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_left_shin_pos_z", this.stompTypeMovePosModifier(animatable, "leftShin", 2));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_left_leg_rot", this.stompTypeMoveRotationModifier(animatable, "leftLeg"));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_neck_rot", this.stompTypeMoveRotationModifier(animatable, "neck"));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_body_rot", this.stompTypeMoveRotationModifier(animatable, "body"));

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

    private double jawTypeMoveAnimModifier(RiftCreature animatable, String partName) {
        switch (partName) {
            case "lowerJaw":
                return this.partRotationBasedOnMove(animatable, 0, 30D, 0, 0);
            case "head":
                return this.partRotationBasedOnMove(animatable, 0, 22.5D, 22.5D, 0);
            case "neck":
                return this.partRotationBasedOnMove(animatable, 0, 2.5D, 2.5D, 0);
            case "body":
                return this.partRotationBasedOnMove(animatable, 0, -7.5D, 9, 2.5D);
        }
        return 0;
    }

    private double stompTypeMoveRotationModifier(RiftCreature animatable, String partName) {
        switch (partName) {
            case "leftShin":
                return this.partRotationBasedOnMove(animatable, 0, 75, 40, 40, 0.75D, 0);
            case "leftLeg":
                return this.partRotationBasedOnMove(animatable, 0, -75, -40, -40, 0.75D, 0);
            case "neck":
                return this.partRotationBasedOnMove(animatable, 0, 17.5D, 17.5D, 0);
            case "body":
                return this.partRotationBasedOnMove(animatable, 0, -17.5D, 5D, 0);
        }
        return 0;
    }

    private double stompTypeMovePosModifier(RiftCreature animatable, String partName, int axis) {
        //for axis
        //0 is x
        //1 is y
        //2 is z
        switch (partName) {
            case "leftShin":
                if (axis == 1) return this.partRotationBasedOnMove(animatable, 0, -1, -1, -1, 0.75D, 0);
                else if (axis == 2) return this.partRotationBasedOnMove(animatable, 0, -1, -0.375, -0.375, 0.75D, 0);
        }
        return 0;
    }

    private double moveAnimModifier(RiftCreature animatable) {
        if (animatable.currentCreatureMove() == null) return 0;
        if (animatable.currentCreatureMove().moveType == CreatureMove.MoveType.STATUS) {
            return RiftUtil.slopeResult(animatable.getCurrentMoveUse(), true, 0, animatable.currentCreatureMove().maxUse, 0, 20D);
        }
        return 0;
    }
}
