package anightdazingzoroark.prift.compat.simpledifficulty;

import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import com.charles445.simpledifficulty.temperature.ModifierBase;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
                            dimetrodonTemperature -= 15f;
                            break;
                        case COLD:
                            dimetrodonTemperature -= 10f;
                            break;
                        case WARM:
                            dimetrodonTemperature += 10f;
                            break;
                        case VERY_WARM:
                            dimetrodonTemperature += 15f;
                            break;
                    }
                }
            }
        }
        return dimetrodonTemperature;
    }
}
