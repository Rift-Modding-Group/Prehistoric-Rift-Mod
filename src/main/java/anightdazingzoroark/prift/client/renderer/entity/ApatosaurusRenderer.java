package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class ApatosaurusRenderer extends RiftCreatureRenderer {
    public ApatosaurusRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 1.75f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.35f, 2.25f);
        Apatosaurus apatosaurus = (Apatosaurus) animatable;

        GeckoLibCache.getInstance().parser.setValue("apatosaurus_bide_move_body_use", this.moveAnimModifierBody(animatable));
        GeckoLibCache.getInstance().parser.setValue("apatosaurus_bide_move_tail_use", this.moveAnimModifierTail(animatable));
        GeckoLibCache.getInstance().parser.setValue("apatosaurus_bide_move_leg_use", this.moveAnimModifierLeg(animatable));

        //hide saddle stuff
        model.getBone("platform").get().setHidden(!animatable.isSaddled());
        model.getBone("neck0Saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("neck1Saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("neck2Saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("furnace").get().setHidden(true);
        model.getBone("craftingTable").get().setHidden(true);
        model.getBone("leftCannon").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CANNON));
        model.getBone("rightCannon").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CANNON));
        model.getBone("leftMortar").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.MORTAR));
        model.getBone("rightMortar").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.MORTAR));
        model.getBone("leftCatapult").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT));
        model.getBone("rightCatapult").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT));
        model.getBone("leftCatapultBoulder").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT) || !apatosaurus.isLoaded());
        model.getBone("rightCatapultBoulder").get().setHidden(!apatosaurus.getWeapon().equals(RiftLargeWeaponType.CATAPULT) || !apatosaurus.isLoaded());


        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }

    private double moveAnimModifierBody(RiftCreature animatable) {
        if (animatable.currentCreatureMove() == null) return 0;
        if (animatable.currentCreatureMove().moveType == CreatureMove.MoveType.DEFENSE && animatable.currentCreatureMove() == CreatureMove.BIDE) {
            return RiftUtil.slopeResult(animatable.getCurrentMoveUse(), true, 0, animatable.currentCreatureMove().maxUse, 0, 2.5D);
        }
        return 0;
    }

    private double moveAnimModifierTail(RiftCreature animatable) {
        if (animatable.currentCreatureMove() == null) return 0;
        if (animatable.currentCreatureMove().moveType == CreatureMove.MoveType.DEFENSE && animatable.currentCreatureMove() == CreatureMove.BIDE) {
            return RiftUtil.slopeResult(animatable.getCurrentMoveUse(), true, 0, animatable.currentCreatureMove().maxUse, 0, -15D);
        }
        return 0;
    }

    private double moveAnimModifierLeg(RiftCreature animatable) {
        if (animatable.currentCreatureMove() == null) return 0;
        if (animatable.currentCreatureMove().moveType == CreatureMove.MoveType.DEFENSE && animatable.currentCreatureMove() == CreatureMove.BIDE) {
            return RiftUtil.slopeResult(animatable.getCurrentMoveUse(), true, 0, animatable.currentCreatureMove().maxUse, 0, -22.5D);
        }
        return 0;
    }
}
