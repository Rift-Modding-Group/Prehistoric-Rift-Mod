package anightdazingzoroark.prift.api.projectile;

import anightdazingzoroark.prift.api.creature.ICreature;
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

    @NotNull
    ICreature getShooter();
}
