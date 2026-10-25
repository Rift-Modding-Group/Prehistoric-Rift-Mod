package anightdazingzoroark.prift.server.world.feature;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class RiftWorldFeature {
    protected static final int GENERATION_FLAGS = 2 | 16;

    public abstract int getSpawnWeight();

    @NotNull
    public abstract Set<Block> getValidGroundBlocks();

    @NotNull
    protected abstract String[] getSpawnBiomes();

    @NotNull
    protected abstract List<GenerationPlan> createGenerationPlans(@NotNull Random random, @NotNull World world, @NotNull Chunk chunk);

    protected abstract boolean isValidPlacement(
            @NotNull GenerationPlan plan, @NotNull World world,
            @NotNull Chunk chunk, @NotNull Set<Block> validGroundBlocks
    );

    @NotNull
    protected abstract IBlockState getBlockToPlace(@NotNull Random random);

    public void generate(@NotNull Random random, @NotNull World world, @NotNull Chunk chunk) {
        Set<Block> validGroundBlocks = this.getValidGroundBlocks();
        String[] spawnBiomes = this.getSpawnBiomes();
        int worldHeight = world.getHeight();
        for (GenerationPlan plan : this.createGenerationPlans(random, world, chunk)) {
            BlockPos origin = plan.origin();
            if (origin.getY() <= 0 || origin.getY() >= worldHeight
                    || origin.getX() >> 4 != chunk.x || origin.getZ() >> 4 != chunk.z) continue;

            // Validate all positions before reading their terrain or placing any part of the feature.
            boolean insideChunk = plan.positions().stream().allMatch(pos ->
                    pos.getX() >> 4 == chunk.x && pos.getZ() >> 4 == chunk.z
                            && pos.getY() >= 0 && pos.getY() < worldHeight);
            if (!insideChunk || plan.positions().isEmpty()) continue;
            if (!this.isBiomeAllowed(world.getBiome(origin), spawnBiomes)
                    || !validGroundBlocks.contains(chunk.getBlockState(origin.down()).getBlock())) continue;
            if (!this.isValidPlacement(plan, world, chunk, validGroundBlocks)) continue;

            for (BlockPos pos : plan.positions()) {
                world.setBlockState(pos, this.getBlockToPlace(random), GENERATION_FLAGS);
            }
        }
    }

    protected record GenerationPlan(@NotNull BlockPos origin, @NotNull List<BlockPos> positions) {}

    protected boolean isBiomeAllowed(@NotNull Biome biome, @NotNull String[] configuredBiomes) {
        for (String configuredBiome : configuredBiomes) {
            //special case that allows the feature to spawn anywhere
            if (configuredBiome.equals("all")) return true;

            String biomeEntry = configuredBiome.trim();
            if (biomeEntry.isEmpty()) continue;

            //colon means its presumed to be a specific biome
            if (biomeEntry.contains(":")) {
                if (biome.getRegistryName() != null && biomeEntry.equalsIgnoreCase(biome.getRegistryName().toString())) {
                    return true;
                }
                continue;
            }

            //no colon means its presumed to be a biome dictionary tag
            for (BiomeDictionary.Type biomeType : BiomeDictionary.getTypes(biome)) {
                if (biomeEntry.equalsIgnoreCase(biomeType.getName())) return true;
            }
        }
        return false;
    }
}
