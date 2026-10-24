package anightdazingzoroark.prift.client.camera;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder.RiddenCrosshairType;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimArea;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import anightdazingzoroark.prift.client.CameraHandler.CameraFrame;
import anightdazingzoroark.prift.client.CameraHandler.RidingAimSettings;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftRidingStartAimMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Selects targets from a camera frame and publishes the rider's aim.
 * */
@SideOnly(Side.CLIENT)
public class RidingAimTargeting {
    public static void sendUpdate(
            @NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature, int ridingAimMove,
            @NotNull CameraFrame frame, @NotNull RidingAimSettings settings, float cameraFov, double aimDistance
    ) {
        double meleeReachSquared = settings.meleeReach() * settings.meleeReach();
        Vec3d aimStart = frame.position();
        Vec3d aimEnd = aimStart.add(frame.lookVector().scale(aimDistance));
        RayTraceResult blockHit = minecraft.world.rayTraceBlocks(aimStart, aimEnd, false, true, false);
        Vec3d aimPosition = blockHit == null ? aimEnd : blockHit.hitVec;
        double closestDistanceSquared = aimStart.squareDistanceTo(aimPosition);
        Entity aimedEntity = null;
        Vec3d creatureAimOrigin = riddenCreature.getPositionEyes(1f);

        //---look for entities hit by crosshair---
        AxisAlignedBB searchBounds = new AxisAlignedBB(aimStart, aimEnd).grow(1D);
        List<Entity> candidates = minecraft.world.getEntitiesInAABBexcluding(
                minecraft.player, searchBounds, entity -> entity != null && entity.canBeCollidedWith()
        );
        for (Entity candidate : candidates) {
            if (candidate.getLowestRidingEntity() == minecraft.player.getLowestRidingEntity()) continue;

            AxisAlignedBB targetBounds = candidate.getEntityBoundingBox().grow(candidate.getCollisionBorderSize());
            RayTraceResult entityHit = targetBounds.calculateIntercept(aimStart, aimEnd);
            Vec3d hitPosition = entityHit == null ? null : entityHit.hitVec;
            if (targetBounds.contains(aimStart)) hitPosition = aimStart;
            if (hitPosition == null) continue;
            if (settings.melee() && hitPosition.squareDistanceTo(creatureAimOrigin) > meleeReachSquared) continue;

            double hitDistanceSquared = aimStart.squareDistanceTo(hitPosition);
            if (hitDistanceSquared >= closestDistanceSquared) continue;

            closestDistanceSquared = hitDistanceSquared;
            aimedEntity = candidate instanceof MultiPartEntityPart part ? (Entity) part.parent : candidate;
            aimPosition = hitPosition;
        }

        //---apply crosshair hit results---
        boolean hasCrosshairTarget = aimedEntity != null || blockHit != null;

        //--melee attack endpoint--
        if (settings.melee() && aimedEntity == null && aimPosition.squareDistanceTo(creatureAimOrigin) > meleeReachSquared) {
            blockHit = null;
            hasCrosshairTarget = false;

            aimPosition = frame.focusPosition();
        }
        //--block hit to break blocks--
        if (!settings.melee() || hasCrosshairTarget) {
            RayTraceResult creatureBlockHit = minecraft.world.rayTraceBlocks(
                    creatureAimOrigin, aimPosition, false, true, false
            );
            if (creatureBlockHit != null
                    && creatureAimOrigin.squareDistanceTo(creatureBlockHit.hitVec) + 1E-6D < creatureAimOrigin.squareDistanceTo(aimPosition)
            ) {
                aimedEntity = null;
                aimPosition = creatureBlockHit.hitVec;
                blockHit = creatureBlockHit;
                hasCrosshairTarget = true;
            }
        }
        boolean targetlessRanged = !settings.melee() && !hasCrosshairTarget;
        RiddenAimArea aimArea = null;
        if (settings.crosshairType() == RiddenCrosshairType.WIDE) {
            double verticalTangent = Math.tan(Math.toRadians(cameraFov) * 0.5D);
            aimArea = new RiddenAimArea(
                    aimStart, frame.lookVector(),
                    verticalTangent * 2D * RiddenAimArea.HORIZONTAL_SCREEN_RADIUS,
                    verticalTangent * 2D * RiddenAimArea.VERTICAL_SCREEN_RADIUS
            );
        }
        RiddenAimTarget aimTarget = new RiddenAimTarget(
                aimedEntity == null ? -1 : aimedEntity.getEntityId(), aimPosition,
                aimedEntity == null && blockHit != null ? blockHit.getBlockPos() : null,
                aimedEntity == null && blockHit != null ? blockHit.sideHit : null
        );
        riddenCreature.setRiddenAimingFromClient(aimTarget, ridingAimMove);
        RiftMessages.WRAPPER.sendToServer(new RiftRidingStartAimMessage(
                ridingAimMove, targetlessRanged, aimTarget, aimArea
        ));
    }
}
