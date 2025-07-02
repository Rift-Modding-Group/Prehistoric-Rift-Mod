package anightdazingzoroark.prift;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import com.teamderpy.shouldersurfing.client.ShoulderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class SSRCompatUtils {
    public static RayTraceResult getEntities(RiftCreature creature) {
        final int reach = 16;
        final float creatureReach = creature.width + creature.attackWidth() + 1f;
        Entity cameraEntity = Minecraft.getMinecraft().player;
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

        ShoulderHelper.ShoulderLook look = ShoulderHelper.shoulderSurfingLook(cameraEntity, partialTick, reach * reach);
        Vec3d startPos = cameraEntity.getPositionEyes(1.0F).add(look.headOffset());;
        Vec3d endPos = look.traceEndPos();

        Entity pointedEntity = null;
        Vec3d entityHitVec = null;
        double closestDistance = reach;

        List<Entity> entities = creature.world.getEntitiesInAABBexcluding(creature, cameraEntity.getEntityBoundingBox().grow(reach), new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                boolean withinCreatureReach = entity.getDistance(creature) <= creatureReach;
                if (entity == null) return false;
                if (entity instanceof MultiPartEntityPart) {
                    MultiPartEntityPart entityPart = (MultiPartEntityPart) entity;
                    return !entityPart.parent.equals(creature) && withinCreatureReach;
                }
                else if (entity instanceof RiftCreature) {
                    RiftCreature creatureIn = (RiftCreature) entity;
                    return !creatureIn.equals(creature) && withinCreatureReach;
                }
                else if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    return !player.isSpectator() && !player.equals(cameraEntity) && withinCreatureReach;
                }
                return withinCreatureReach;
            }
        });

        for (Entity entity : entities) {
            AxisAlignedBB boundingBox = entity.getEntityBoundingBox().grow(0.3);
            RayTraceResult entityResult = boundingBox.calculateIntercept(startPos, endPos);

            if (entityResult != null) {
                double distance = startPos.distanceTo(entityResult.hitVec);

                if (distance < closestDistance) {
                    pointedEntity = entity;
                    entityHitVec = entityResult.hitVec;
                    closestDistance = distance;
                }
            }
        }

        return new RayTraceResult(pointedEntity, entityHitVec);
    }

    public static BlockPos getBlock(RiftCreature creature) {
        final int reach = 16;
        final float creatureReach = creature.width + creature.attackWidth() + 1f;
        Entity cameraEntity = Minecraft.getMinecraft().player;
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

        ShoulderHelper.ShoulderLook look = ShoulderHelper.shoulderSurfingLook(cameraEntity, partialTick, reach * reach);
        Vec3d startPos = cameraEntity.getPositionEyes(1.0F).add(look.headOffset());;
        Vec3d endPos = look.traceEndPos();

        RayTraceResult rayTraceResult = cameraEntity.world.rayTraceBlocks(startPos, endPos, false, false, false);

        if (rayTraceResult != null
                && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK
                && creature.getDistanceSq(rayTraceResult.getBlockPos()) <= Math.pow(creatureReach, 2)
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
