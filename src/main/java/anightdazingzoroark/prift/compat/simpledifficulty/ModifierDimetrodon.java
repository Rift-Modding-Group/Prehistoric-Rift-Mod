package anightdazingzoroark.prift.compat.simpledifficulty;

import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import com.charles445.simpledifficulty.temperature.ModifierBase;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ModifierDimetrodon extends ModifierBase {
    public ModifierDimetrodon() {
        super("Dimetrodon");
    }

    @Override
    public float getWorldInfluence(World world, BlockPos pos) {
        AxisAlignedBB playerAABB = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ());
        float dimetrodonTemperature = 0f;
        for (EntityLivingBase entityLivingBase : world.getEntities(EntityLiving.class, new Predicate<EntityLiving>() {
            @Override
            public boolean apply(@Nullable EntityLiving input) {
                return true;
            }
        })) {
            if (entityLivingBase instanceof Dimetrodon) {
                Dimetrodon dimetrodon = (Dimetrodon) entityLivingBase;
                if (dimetrodon.getEntityBoundingBox().grow(8.0D).intersects(playerAABB)) {
                    switch (dimetrodon.getTemperature()) {
                        case VERY_COLD:
                            dimetrodonTemperature += this.changeByDistance(DimetrodonConfig.dimetrodonVeryColdValue, pos, dimetrodon.getEntityBoundingBox());
                            break;
                        case COLD:
                            dimetrodonTemperature += this.changeByDistance(DimetrodonConfig.dimetrodonColdValue, pos, dimetrodon.getEntityBoundingBox());
                            break;
                        case WARM:
                            dimetrodonTemperature += this.changeByDistance(DimetrodonConfig.dimetrodonWarmValue, pos, dimetrodon.getEntityBoundingBox());
                            break;
                        case VERY_WARM:
                            dimetrodonTemperature += this.changeByDistance(DimetrodonConfig.dimetrodonVeryWarmValue, pos, dimetrodon.getEntityBoundingBox());
                            break;
                    }
                }
            }
        }
        return dimetrodonTemperature;
    }

    private float changeByDistance(float temperatureValue, BlockPos blockPos, AxisAlignedBB dimetrodonAABB) {
        // First bounding box, grown by double its size
        AxisAlignedBB firstBoundingBox = dimetrodonAABB.grow(2.0D);

        // Second bounding box, grown to a fixed 8-block radius from the center
        AxisAlignedBB secondBoundingBox = dimetrodonAABB.grow(8.0D);

        Vec3d posVec = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (firstBoundingBox.contains(posVec) && secondBoundingBox.contains(posVec)) return temperatureValue;
        else if (!firstBoundingBox.contains(posVec) && secondBoundingBox.contains(posVec)) {
            double distFromCenterX = blockPos.getX() - dimetrodonAABB.getCenter().x;
            double distFromCenterY = blockPos.getY() - dimetrodonAABB.getCenter().y;
            double distFromCenterZ = blockPos.getZ() - dimetrodonAABB.getCenter().z;
            double dist = Math.sqrt(distFromCenterX * distFromCenterX + distFromCenterY * distFromCenterY + distFromCenterZ * distFromCenterZ);
            if (dist > 2 && dist <= 8) {
                return (-temperatureValue * ((float) dist - 8f))/6f;
            }
        }
        return 0f;
    }
}
