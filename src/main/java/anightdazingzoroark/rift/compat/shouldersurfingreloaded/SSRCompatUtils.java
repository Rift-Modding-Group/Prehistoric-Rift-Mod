package anightdazingzoroark.rift.compat.shouldersurfingreloaded;

import com.teamderpy.shouldersurfing.client.ShoulderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SSRCompatUtils {
    public static RayTraceResult getEntities(double reach) {
        double doubleReach = reach * reach;
        Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        Vec3d viewVector = cameraEntity.getLook(1.0F).scale(reach);
        Vec3d eyePosition = cameraEntity.getPositionEyes(partialTick);
        double searchDistance = Math.min(64, reach);
        AxisAlignedBB aabb = cameraEntity.getEntityBoundingBox().expand(viewVector.x * searchDistance, viewVector.y * searchDistance, viewVector.z * searchDistance).grow(1.0D, 1.0D, 1.0D);

        ShoulderHelper.ShoulderLook look = ShoulderHelper.shoulderSurfingLook(cameraEntity, partialTick, doubleReach);
        Vec3d from = eyePosition.add(look.headOffset());
        Vec3d to = look.traceEndPos();
        aabb = aabb.offset(look.headOffset());

        List<Entity> entities = Minecraft.getMinecraft().world.getEntitiesInAABBexcluding(cameraEntity, aabb, null);
        Vec3d entityHitVec = null;
        Entity entityResult = null;
        double minEntityReachSq = doubleReach;

        for(Entity entity : entities) {
            AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(from, to);

            if(axisalignedbb.contains(eyePosition)) {
                if (minEntityReachSq >= 0.0D) {
                    entityResult = entity;
                    entityHitVec = raytraceresult == null ? eyePosition : raytraceresult.hitVec;
                    minEntityReachSq = 0.0D;
                }
            }
            else if (raytraceresult != null) {
                double distanceSq = eyePosition.squareDistanceTo(raytraceresult.hitVec);

                if (distanceSq < minEntityReachSq || minEntityReachSq == 0.0D) {
                    if (entity == cameraEntity.getRidingEntity() && !entity.canRiderInteract()) {
                        if(minEntityReachSq == 0.0D) {
                            entityResult = entity;
                            entityHitVec = raytraceresult.hitVec;
                        }
                    }
                    else {
                        entityResult = entity;
                        entityHitVec = raytraceresult.hitVec;
                        minEntityReachSq = distanceSq;
                    }
                }
            }
        }

        if (entityResult == null) {
            return null;
        }

        return new RayTraceResult(entityResult, entityHitVec);
    }
}
