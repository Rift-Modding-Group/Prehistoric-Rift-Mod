package anightdazingzoroark.prift.server.entity.creatureMoves.aiming;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import valkyrienwarfare.api.RiftedValkyriumAPI;

public class RiddenAimTrace {
    @Nullable
    public static RayTraceResult trace(@NotNull RiftCreature creature, @NotNull Vec3d start, @NotNull Vec3d end) {
        if (Loader.isModLoaded("valkyrienskies")) {
            return RiftedValkyriumAPI.rayTraceBlocksIgnoringCarrierShip(creature.world, creature, start, end, false, true, false);
        }
        return creature.world.rayTraceBlocks(start, end, false, true, false);
    }
}
