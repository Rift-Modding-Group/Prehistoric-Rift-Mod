package anightdazingzoroark.prift;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import com.teamderpy.shouldersurfing.client.ShoulderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class SSRCompatUtils {
    public static RayTraceResult getEntities(RiftCreature creature) {
        final int reach = 64; //8 squared
        Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        Vec3d viewVector = cameraEntity.getLook(1.0F).scale(reach);
        Vec3d eyePosition = cameraEntity.getPositionEyes(partialTick);
        AxisAlignedBB aabb = cameraEntity.getEntityBoundingBox().expand(viewVector.x * reach, viewVector.y * reach, viewVector.z * reach).grow(1.0D, 1.0D, 1.0D);

        ShoulderHelper.ShoulderLook look = ShoulderHelper.shoulderSurfingLook(cameraEntity, partialTick, reach);
        Vec3d from = eyePosition.add(look.headOffset());
        Vec3d to = look.traceEndPos();
        aabb = aabb.offset(look.headOffset());

        List<Entity> entities = Minecraft.getMinecraft().world.getEntitiesWithinAABB(EntityLivingBase.class, aabb, new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                int attackReach = (int) (creature.getEntityBoundingBox().maxX - creature.getEntityBoundingBox().minX + creature.attackWidth());
                if (entity.equals(cameraEntity)) return false;
                else if (entity instanceof RiftCreature && entity.equals(creature)) return false;
                return entity.getDistanceSq(creature) <= Math.pow(attackReach, 2);
            }
        });
        Vec3d entityHitVec = null;
        Entity entityResult = null;
        double minEntityReachSq = reach;

        for (Entity entity : entities) {
            AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(from, to);

            if (axisalignedbb.contains(eyePosition)) {
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
                        if (minEntityReachSq == 0.0D) {
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

        return new RayTraceResult(entityResult, entityHitVec);
    }

    public static BlockPos getBlock(RiftCreature creature) {
        final int reach = 64; //8 squared
        int attackReach = (int) (creature.getEntityBoundingBox().maxX - creature.getEntityBoundingBox().minX + creature.attackWidth());
        Entity cameraEntity = Minecraft.getMinecraft().getRenderViewEntity();
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        Vec3d eyesPos = cameraEntity.getPositionEyes(1.0F);

        ShoulderHelper.ShoulderLook look = ShoulderHelper.shoulderSurfingLook(cameraEntity, partialTick, reach * reach);
        Vec3d from = eyesPos.add(look.headOffset());
        Vec3d to = look.traceEndPos();

        RayTraceResult rayTraceResult = cameraEntity.world.rayTraceBlocks(from, to, false, false, false);

        if (rayTraceResult != null
                && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK
                && creature.getDistanceSq(rayTraceResult.getBlockPos()) <= attackReach * attackReach
        ) return rayTraceResult.getBlockPos();
        return null;
    }

    public static SSRHitResult createHitResult(RiftCreature creature) {
        return new SSRHitResult(getEntities(creature).entityHit, getBlock(creature));
    }

    public static class SSRHitResult {
        public final Entity entity;
        public final BlockPos blockPos;

        public SSRHitResult(Entity entity, BlockPos blockPos) {
            this.entity = entity;
            this.blockPos = blockPos;
        }
    }
}
