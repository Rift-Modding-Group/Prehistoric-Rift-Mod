package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.world.World;

public class RiftWaterCreatureFollowOwner extends EntityAIBase {
    private final RiftWaterCreature creature;
    private final double followSpeed;
    private World world;
    private final PathNavigate pathfinder;
    private final float maxDist;
    private final float minDist;

    public RiftWaterCreatureFollowOwner(RiftWaterCreature creature, double followSpeedIn, float minDist, float maxDist) {
        this.creature = creature;
        this.world = creature.world;
        this.pathfinder = creature.getNavigator();
        this.followSpeed = followSpeedIn;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
