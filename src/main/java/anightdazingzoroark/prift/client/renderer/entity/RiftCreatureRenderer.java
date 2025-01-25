package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.client.ui.RiftCreatureBoxMenu;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public abstract class RiftCreatureRenderer extends GeoEntityRenderer<RiftCreature> {
    public RiftCreatureRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1f;
    }

    @Override
    public boolean shouldRender(RiftCreature livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ) || livingEntity.shouldRender(camera) || Minecraft.getMinecraft().player.isRidingOrBeingRiddenBy(livingEntity);
    }

    protected double partRotationBasedOnMove(RiftCreature creature, double startPos, double finalChargeUpPos, double finalUsePos, double finalPos) {
        if (creature.getRegularMoveTick() > 0 && creature.getRegularMoveTick() <= 10 * creature.attackChargeUpSpeed()) {
            return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                    true,
                    0,
                    10 * creature.attackChargeUpSpeed(),
                    startPos,
                    finalChargeUpPos);
        }
        else if (creature.getRegularMoveTick() > 10 * creature.attackChargeUpSpeed()
                && creature.getRegularMoveTick() <= 10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed()) {
            return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                    true,
                    10 * creature.attackChargeUpSpeed(),
                    10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed(),
                    finalChargeUpPos,
                    finalUsePos);
        }
        else if (creature.getRegularMoveTick() > 10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed()) {
            return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                    true,
                    10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed(),
                    10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed() + 7.5 * creature.attackRecoverSpeed(),
                    finalUsePos,
                    finalPos);
        }
        return 0;
    }

    protected double partRotationBasedOnMove(RiftCreature creature, double startPos, double finalChargeUpPos, double finalUsePos, double finalUseToFinalPos, double finalUseToFinalPosPercent, double finalPos) {
        double totalAnimTime = 10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed() + 7.5 * creature.attackRecoverSpeed();

        if (creature.getRegularMoveTick() > 0 && creature.getRegularMoveTick() <= 10 * creature.attackChargeUpSpeed()) {
            return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                    true,
                    0,
                    10 * creature.attackChargeUpSpeed(),
                    startPos,
                    finalChargeUpPos);
        }
        else if (creature.getRegularMoveTick() > 10 * creature.attackChargeUpSpeed()
                && creature.getRegularMoveTick() <= 10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed()) {
            return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                    true,
                    10 * creature.attackChargeUpSpeed(),
                    10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed(),
                    finalChargeUpPos,
                    finalUsePos);
        }
        else if (creature.getRegularMoveTick() > 10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed()) {
            if (creature.getRegularMoveTick() < totalAnimTime * finalUseToFinalPosPercent) {
                return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                        true,
                        10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed(),
                        totalAnimTime * finalUseToFinalPosPercent,
                        finalUsePos,
                        finalUseToFinalPos);
            }
            else {
                return RiftUtil.slopeResult(creature.getRegularMoveTick(),
                        true,
                        totalAnimTime * finalUseToFinalPosPercent,
                        10 * creature.attackChargeUpSpeed() + 2.5 * creature.chargeUpToUseSpeed() + 7.5 * creature.attackRecoverSpeed(),
                        finalUseToFinalPos,
                        finalPos);
            }
        }
        return 0;
    }
}
