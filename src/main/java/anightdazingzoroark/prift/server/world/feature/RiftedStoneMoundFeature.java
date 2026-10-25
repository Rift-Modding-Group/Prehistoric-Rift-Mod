package anightdazingzoroark.prift.server.world.feature;

import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.config.RiftGeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class RiftedStoneMoundFeature extends RiftWorldFeature {
    private static final Set<Material> SOFT_MATERIALS = Set.of(Material.AIR, Material.PLANTS, Material.VINE, Material.SNOW);

    @Override
    public int getSpawnWeight() {
        return RiftGeneralConfig.worldGen.riftedMoundWeight;
    }

    @Override
    @NotNull
    public Set<Block> getValidGroundBlocks() {
        return Set.of(
                Blocks.GRASS, Blocks.DIRT, Blocks.STONE, Blocks.GRAVEL, Blocks.SAND,
                Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.CLAY, Blocks.HARDENED_CLAY,
                Blocks.STAINED_HARDENED_CLAY, Blocks.SNOW, Blocks.MYCELIUM, Blocks.GRASS_PATH
        );
    }

    @Override
    @NotNull
    protected String[] getSpawnBiomes() {
        return RiftGeneralConfig.worldGen.riftedMoundBiomes;
    }

    @Override
    @NotNull
    protected List<GenerationPlan> createGenerationPlans(@NotNull Random random, @NotNull World world, @NotNull Chunk chunk) {
        int radius = random.nextInt(2, 4);
        int height = random.nextInt(4, 8);
        double angle = random.nextDouble() * Math.TAU;
        double lean = random.nextDouble(0.2D, 0.5D);
        List<BlockPos> offsets = new ArrayList<>();
        for (int y = -3; y < height; y++) {
            int layerRadius = Math.max(0, radius - Math.max(0, y) / 2);
            int leanX = (int) (lean * y * Math.cos(angle));
            int leanZ = (int) (lean * y * Math.sin(angle));
            for (int x = -layerRadius; x <= layerRadius; x++) {
                for (int z = -layerRadius; z <= layerRadius; z++) {
                    if (x * x + z * z <= layerRadius * layerRadius) {
                        offsets.add(new BlockPos(x + leanX, y, z + leanZ));
                    }
                }
            }
        }

        // Radius, buried base and lean reach at most four blocks from the center.
        // Keep the footprint one block inside the chunk to leave room for neighboring block checks.
        int localX = random.nextInt(5, 11);
        int localZ = random.nextInt(5, 11);
        BlockPos surface = new BlockPos(chunk.x * 16 + localX, chunk.getHeightValue(localX, localZ), chunk.z * 16 + localZ);
        while (surface.getY() > 4 && SOFT_MATERIALS.contains(chunk.getBlockState(surface.down()).getMaterial())) {
            surface = surface.down();
        }
        return List.of(new GenerationPlan(surface, offsets.stream().map(surface::add).toList()));
    }

    @Override
    protected boolean isValidPlacement(
            @NotNull GenerationPlan plan, @NotNull World world,
            @NotNull Chunk chunk, @NotNull Set<Block> validGroundBlocks
    ) {
        if (plan.origin().getY() <= 4) return false;

        // Validate the entire mound before changing terrain: no water, trees, tile entities or unsupported bases.
        for (BlockPos pos : plan.positions()) {
            IBlockState existing = chunk.getBlockState(pos);
            Material material = existing.getMaterial();
            if (existing.getBlock() == Blocks.BEDROCK || existing.getBlock().hasTileEntity(existing)
                    || (!SOFT_MATERIALS.contains(material) && !validGroundBlocks.contains(existing.getBlock()))
            ) {
                return false;
            }
            if (pos.getY() == plan.origin().getY() - 3) {
                IBlockState support = chunk.getBlockState(pos.down());
                if (!validGroundBlocks.contains(support.getBlock()) || !support.isFullCube()
                        || support.getBlock().hasTileEntity(support)
                ) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    @NotNull
    protected IBlockState getBlockToPlace(@NotNull Random random) {
        int chance = random.nextInt(100);
        if (chance >= 95) return RiftBlocks.RIFT_SHARD_ORE.getDefaultState();
        if (chance >= 50) return RiftBlocks.RIFTED_COBBLESTONE.getDefaultState();
        return RiftBlocks.RIFTED_STONE.getDefaultState();
    }
}