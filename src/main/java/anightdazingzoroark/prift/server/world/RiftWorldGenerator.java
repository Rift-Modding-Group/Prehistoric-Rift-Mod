package anightdazingzoroark.prift.server.world;

import anightdazingzoroark.prift.server.world.feature.RiftWorldFeature;
import anightdazingzoroark.prift.server.world.feature.RiftedStoneMoundFeature;
import anightdazingzoroark.prift.server.world.feature.SmokenutBushFeature;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class RiftWorldGenerator implements IWorldGenerator {
    private static final int SPAWN_WEIGHT_BOUND = 100;
    private static final List<RiftWorldFeature> FEATURES = List.of(new SmokenutBushFeature(), new RiftedStoneMoundFeature());

    @Override
    public void generate(
            @NotNull Random random, int chunkX, int chunkZ, @NotNull World world,
            @NotNull IChunkGenerator chunkGenerator, @NotNull IChunkProvider chunkProvider
    ) {
        //overworld only for now
        if (world.provider.getDimension() != 0) return;

        //iterate over all features
        for (RiftWorldFeature feature : FEATURES) {
            int weight = Math.clamp(feature.getSpawnWeight(), 0, SPAWN_WEIGHT_BOUND);
            if (weight == 0 || random.nextInt(SPAWN_WEIGHT_BOUND) >= weight) continue;

            //only pass loaded chunks to features; generation must never load a neighboring chunk.
            Chunk chunk = chunkProvider.getLoadedChunk(chunkX, chunkZ);
            if (chunk != null) {
                feature.generate(random, world, chunk);
            }
        }
    }
}