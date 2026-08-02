package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

/**
 * Ground navigator for creatures for this mod
 * */
public class RiftCreaturePathNavigate extends PathNavigateGround {
    private final RiftCreature creature;

    public RiftCreaturePathNavigate(RiftCreature creature, World world) {
        super(creature, world);
        this.creature = creature;
    }

    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new RiftCreatureWalkNodeProcessor((RiftCreature)this.entity);
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    protected boolean canNavigate() {
        return this.creature.getNavigation().getCanWalk() && super.canNavigate();
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity target, double speed) {
        Path path = this.getPathToEntityLiving(target);
        boolean startedPath = path != null && this.setPath(path, speed);
        if (target == this.creature.getAttackTarget()) {
            this.creature.setUnableToPathToTarget(!startedPath);
        }
        return startedPath;
    }
}
