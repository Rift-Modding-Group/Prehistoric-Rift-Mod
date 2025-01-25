package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.Triceratops;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class TriceratopsRenderer extends RiftCreatureRenderer {
    public TriceratopsRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.3f, 1.75f);

        //variables
        GeckoLibCache.getInstance().parser.setValue("head_type_move_body_front", this.headTypeMoveAnimModifier(animatable, "bodyFront", -1));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_neck", this.headTypeMoveAnimModifier(animatable, "neck", -1));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_left_front_leg_x", this.headTypeMoveAnimModifier(animatable, "leftFrontLeg", 0));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_left_front_leg_y", this.headTypeMoveAnimModifier(animatable, "leftFrontLeg", 1));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_left_front_leg_z", this.headTypeMoveAnimModifier(animatable, "leftFrontLeg", 2));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_right_front_leg_x", this.headTypeMoveAnimModifier(animatable, "rightFrontLeg", 0));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_right_front_leg_y", this.headTypeMoveAnimModifier(animatable, "rightFrontLeg", 1));
        GeckoLibCache.getInstance().parser.setValue("head_type_move_right_front_leg_z", this.headTypeMoveAnimModifier(animatable, "rightFrontLeg", 2));

        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_body_back", this.stompTypeMoveModifier(animatable, "bodyBack"));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_neck", this.stompTypeMoveModifier(animatable, "neck"));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_left_front_leg", this.stompTypeMoveModifier(animatable, "leftFrontLeg"));
        GeckoLibCache.getInstance().parser.setValue("stomp_type_move_right_front_leg", this.stompTypeMoveModifier(animatable, "rightFrontLeg"));

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("chest").get().setHidden(true);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }

    private double headTypeMoveAnimModifier(RiftCreature animatable, String bodyPart, int axis) {
        switch (bodyPart) {
            case "bodyFront":
                return this.partRotationBasedOnMove(animatable, 0, 5, 5, 5, 0.8, 0);
            case "neck":
                return this.partRotationBasedOnMove(animatable, 0, 12.5, -17.5, 0, 0.75, 0);
            case "leftFrontLeg":
                if (axis == 0) return this.partRotationBasedOnMove(animatable, 0, -4.83, -4.83, -4.83, 0.8, 0);
                else if (axis == 1) return this.partRotationBasedOnMove(animatable, 0, 1.29, 1.29, 1.29, 0.8, 0);
                else if (axis == 2) return this.partRotationBasedOnMove(animatable, 0, 14.95, 14.95, 14.95, 0.8, 0);
            case "rightFrontLeg":
                if (axis == 0) return this.partRotationBasedOnMove(animatable, 0, -4.83, -4.83, -4.83, 0.8, 0);
                else if (axis == 1) return this.partRotationBasedOnMove(animatable, 0, -1.29, -1.29, -1.29, 0.8, 0);
                else if (axis == 2) return this.partRotationBasedOnMove(animatable, 0, -14.95, -14.95, -14.95, 0.8, 0);
        }
        return 0;
    }

    private double stompTypeMoveModifier(RiftCreature animatable, String bodyPart) {
        switch (bodyPart) {
            case "bodyBack":
                return this.partRotationBasedOnMove(animatable, 0, -20, 0, 0);
            case "neck":
                return this.partRotationBasedOnMove(animatable, 0, 20, 20, 0);
            case "leftFrontLeg":
                return this.partRotationBasedOnMove(animatable, 0, 20, 0, 0);
            case "rightFrontLeg":
                return this.partRotationBasedOnMove(animatable, 0, 20, 0, 0);
        }
        return 0;
    }
}
