package anightdazingzoroark.prift.compat.simpledifficulty;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import com.charles445.simpledifficulty.temperature.ModifierBase;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ModifierDimetrodon extends ModifierBase {
    public ModifierDimetrodon() {
        super("Dimetrodon");
    }

    @Override
    public float getWorldInfluence(World world, BlockPos pos) {
        float temperature = 0f;
        for (EntityLivingBase entityLivingBase : world.getEntities(EntityLiving.class, new Predicate<EntityLiving>() {
            @Override
            public boolean apply(@Nullable EntityLiving input) {
                return true;
            }
        })) {
            if (entityLivingBase instanceof Dimetrodon) {
                Dimetrodon dimetrodon = (Dimetrodon) entityLivingBase;
                List<BlockPos> affectedBlockPositions = Lists.<BlockPos>newArrayList();
                Set<BlockPos> set = Sets.<BlockPos>newHashSet();
                int i = 16;
                for (int j = 0; j < 16; ++j) {
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                                double d0 = ((float)j / 15.0F * 2.0F - 1.0F);
                                double d1 = ((float)k / 15.0F * 2.0F - 1.0F);
                                double d2 = ((float)l / 15.0F * 2.0F - 1.0F);
                                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                                d0 /= d3;
                                d1 /= d3;
                                d2 /= d3;
                                float f = 8f * (0.7F + world.rand.nextFloat() * 0.6F);
                                double d4 = dimetrodon.posX;
                                double d6 = dimetrodon.posY;
                                double d8 = dimetrodon.posZ;

                                for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                    BlockPos blockpos = new BlockPos(d4, d6, d8);
                                    IBlockState iblockstate = world.getBlockState(blockpos);
                                    if (iblockstate.getMaterial() == Material.AIR) {
                                        set.add(blockpos);
                                        d4 += d0 * 0.30000001192092896D;
                                        d6 += d1 * 0.30000001192092896D;
                                        d8 += d2 * 0.30000001192092896D;
                                    } else {
                                        // If a solid block is encountered, stop the propagation in this direction
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                affectedBlockPositions.addAll(set);
                for (BlockPos blockPos : affectedBlockPositions) {
                    if (blockPos.equals(pos)) {
                        switch (dimetrodon.getTemperature()) {
                            case VERY_COLD:
                                temperature += this.changeByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.veryColdTemperatureValue, dimetrodon.getPosition(), pos);
                                break;
                            case COLD:
                                temperature += this.changeByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.coldTemperatureValue, dimetrodon.getPosition(), pos);
                                break;
                            case WARM:
                                temperature += this.changeByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.warmTemperatureValue, dimetrodon.getPosition(), pos);
                                break;
                            case VERY_WARM:
                                temperature += this.changeByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.veryWarmTemperatureValue, dimetrodon.getPosition(), pos);
                                break;
                        }
                    }
                }
            }
        }
        return temperature;
    }

    private float changeByDistance(float origTemperature, BlockPos blockPos, BlockPos playerBlockPos) {
        float distance = (float)Math.sqrt(blockPos.distanceSq(playerBlockPos.getX(), playerBlockPos.getY(), playerBlockPos.getZ()));
        return ((-origTemperature/8f) * RiftUtil.clamp(distance, 0f, 8f)) + origTemperature;
    }
}
