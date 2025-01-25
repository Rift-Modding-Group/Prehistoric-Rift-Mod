package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.Stegosaurus;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class StegosaurusRenderer extends RiftCreatureRenderer {
    public StegosaurusRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.3f, 2.125f);

        //variables
        GeckoLibCache.getInstance().parser.setValue("tail_type_move_tail_0_x", this.tailTypeMoveAnimModifier(animatable, "tail0", 0));
        GeckoLibCache.getInstance().parser.setValue("tail_type_move_tail_0_y", this.tailTypeMoveAnimModifier(animatable, "tail0", 1));
        GeckoLibCache.getInstance().parser.setValue("tail_type_move_tail_0_z", this.tailTypeMoveAnimModifier(animatable, "tail0", 2));
        GeckoLibCache.getInstance().parser.setValue("tail_type_move_tail_1", this.tailTypeMoveAnimModifier(animatable, "tail1", -1));
        GeckoLibCache.getInstance().parser.setValue("tail_type_move_tail_2", this.tailTypeMoveAnimModifier(animatable, "tail2", -1));

        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_body_front", this.rangedTypeMoveAnimModifier(animatable, "bodyFront", -1));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_left_front_leg_x", this.rangedTypeMoveAnimModifier(animatable, "leftFrontLeg", 0));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_left_front_leg_z", this.rangedTypeMoveAnimModifier(animatable, "leftFrontLeg", 2));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_right_front_leg_x", this.rangedTypeMoveAnimModifier(animatable, "rightFrontLeg", 0));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_right_front_leg_z", this.rangedTypeMoveAnimModifier(animatable, "rightFrontLeg", 2));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_tail_0", this.rangedTypeMoveAnimModifier(animatable, "tail0", -1));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_tail_1", this.rangedTypeMoveAnimModifier(animatable, "tail1", -1));
        GeckoLibCache.getInstance().parser.setValue("ranged_type_move_tail_2", this.rangedTypeMoveAnimModifier(animatable, "tail2", -1));

        GeckoLibCache.getInstance().parser.setValue("stegosaurus_tail_move_use", this.chargedTailTypeMoveAnimModifier(animatable));

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

    private double tailTypeMoveAnimModifier(RiftCreature animatable, String partName, int axis) {
        //for axis
        //0 is x
        //1 is y
        //2 is z
        switch (partName) {
            case "tail0":
                if (axis == 0) return this.partRotationBasedOnMove(animatable, 0, 27.5, 83.11758, 27.5, 0.75D, 0);
                else if (axis == 1) return this.partRotationBasedOnMove(animatable, 0, 0, 66.97934, 0, 0.75D, 0);
                else if (axis == 2) return this.partRotationBasedOnMove(animatable, 0, 0, 77.12241, 0, 0.75D, 0);
            case "tail1":
                return this.partRotationBasedOnMove(animatable, 0, 0, 37.5, 0, 0.75D, 0);
            case "tail2":
                return this.partRotationBasedOnMove(animatable, 0, 0, 50, 0, 0.75D, 0);
        }
        return 0;
    }

    private double rangedTypeMoveAnimModifier(RiftCreature animatable, String partName, int axis) {
        //for axis
        //0 is x
        //1 is y
        //2 is z
        switch (partName) {
            case "bodyFront":
                return this.partRotationBasedOnMove(animatable, 0, 7.5, 7.5, 7.5, 0.75D, 0);
            case "leftFrontLeg":
                if (axis == 0) return this.partRotationBasedOnMove(animatable, 0, -7.5, -7.5, -7.5, 0.75D, 0);
                else if (axis == 2) return this.partRotationBasedOnMove(animatable, 0, 17.5, 17.5, 17.5, 0.75D, 0);
            case "rightFrontLeg":
                if (axis == 0) return this.partRotationBasedOnMove(animatable, 0, -7.5, -7.5, -7.5, 0.75D, 0);
                else if (axis == 2) return this.partRotationBasedOnMove(animatable, 0, -17.5, -17.5, -17.5, 0.75D, 0);
            case "tail0":
                return this.partRotationBasedOnMove(animatable, 0, -15, 60, 0, 0.75D, 0);
            case "tail1":
                return this.partRotationBasedOnMove(animatable, 0, 0, 17.5, 0, 0.75D, 0);
            case "tail2":
                return this.partRotationBasedOnMove(animatable, 0, 0, 17.5, 0, 0.75D, 0);
        }

        return 0;
    }

    private double chargedTailTypeMoveAnimModifier(RiftCreature animatable) {
        if (animatable.currentCreatureMove() == null) return 0;
        if (animatable.currentCreatureMove().moveType == CreatureMove.MoveType.TAIL) {
            return RiftUtil.slopeResult(animatable.getCurrentMoveUse(), true, 0, animatable.currentCreatureMove().maxUse, 0, 27.5D);
        }
        return 0;
    }
}
