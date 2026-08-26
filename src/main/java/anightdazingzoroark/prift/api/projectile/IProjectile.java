package anightdazingzoroark.prift.api.projectile;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * api version of RiftProjectile
 */
public interface IProjectile {
    World getEntityWorld();

    Random getRNG();

    boolean isOnGround();

    BlockPos getPosition();

    Vec3d getPositionVector();

    @NotNull
    ICreature getShooter();
}