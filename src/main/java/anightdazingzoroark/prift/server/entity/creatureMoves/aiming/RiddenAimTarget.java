package anightdazingzoroark.prift.server.entity.creatureMoves.aiming;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A crosshair endpoint and the entity or block surface selected at that point.
 * */
public record RiddenAimTarget(
        int entityId, @NotNull Vec3d position,
        @Nullable BlockPos blockPosition, @Nullable EnumFacing blockFace
) {
    public RiddenAimTarget {
        if (blockPosition != null) blockPosition = blockPosition.toImmutable();
    }

    @Nullable
    public Entity getEntity(@NotNull World world) {
        return world.getEntityByID(this.entityId);
    }
}