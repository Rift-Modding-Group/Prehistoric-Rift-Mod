package anightdazingzoroark.prift.server.world.feature;

import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import anightdazingzoroark.prift.server.block.SmokenutBush;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SmokenutBushFeature extends RiftWorldFeature {
    @Override
    public int getSpawnWeight() {
        return RiftGeneralConfig.worldGen.smokenutBushWeight;
    }

    @Override
    @NotNull
    public Set<Block> getValidGroundBlocks() {
        return Set.of(Blocks.GRASS, Blocks.DIRT);
    }

    @Override
    @NotNull
    protected String[] getSpawnBiomes() {
        return RiftGeneralConfig.worldGen.smokenutBushBiomes;
    }

    @Override
    @NotNull
    protected List<GenerationPlan> createGenerationPlans(@NotNull Random random, @NotNull World world, @NotNull Chunk chunk) {
        int originX = chunk.x * 16 + 2 + random.nextInt(12);
        int originZ = chunk.z * 16 + 2 + random.nextInt(12);
        List<GenerationPlan> plans = new ArrayList<>();
        for (int attempt = 0; attempt < RiftGeneralConfig.worldGen.smokenutBushConcentration; attempt++) {
            int x = originX + random.nextInt(5) - 2;
            int z = originZ + random.nextInt(5) - 2;
            BlockPos plantPos = world.getHeight(new BlockPos(x, 0, z));
            plans.add(new GenerationPlan(plantPos, List.of(plantPos)));
        }
        return plans;
    }

    @Override
    protected boolean isValidPlacement(
            @NotNull GenerationPlan plan, @NotNull World world,
            @NotNull Chunk chunk, @NotNull Set<Block> validGroundBlocks
    ) {
        return world.isAirBlock(plan.origin());
    }

    @Override
    @NotNull
    protected IBlockState getBlockToPlace(@NotNull Random random) {
        return RiftBlocks.SMOKENUT_BUSH.withAge(RiftBlocks.SMOKENUT_BUSH.getMaxAge()).withProperty(SmokenutBush.HAS_SMOKENUTS, true);
    }
}
