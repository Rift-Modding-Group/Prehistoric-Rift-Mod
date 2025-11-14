package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class RiftWander extends EntityAIWander {
    protected final RiftCreature creature;

    public RiftWander(RiftCreature creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public RiftWander(RiftCreature creatureIn, double speedIn, int chance) {
        super(creatureIn, speedIn, chance);
        this.creature = creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isSleeping()) return false;
        else if (this.creature.isTamed()) {
            if (this.creature instanceof RiftWaterCreature) {
                return super.shouldExecute()
                        && this.creature.creatureBoxWithinReach()
                        && !this.creature.isSitting()
                        && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.creature.busyAtWork()
                        && !this.creature.isBeingRidden()
                        && !this.creature.isInWater();
            }
            else {
                return super.shouldExecute()
                        && this.creature.creatureBoxWithinReach()
                        && !this.creature.isSitting()
                        && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.creature.busyAtWork()
                        && !this.creature.isBeingRidden();
            }
        }
        else {
            boolean isHerdLeader = this.creature.isHerdLeader();
            boolean isStrayFromHerd = !this.creature.canDoHerding() || (!this.creature.isHerdLeader() && !this.creature.hasHerdLeader());
            if (this.creature instanceof RiftWaterCreature) {
                if (isHerdLeader && !this.creature.isInWater()) return super.shouldExecute();
                else if (isStrayFromHerd && !this.creature.isInWater()) return super.shouldExecute();
                else return false;
            }
            else {
                if (isHerdLeader) return super.shouldExecute();
                else if (isStrayFromHerd) return super.shouldExecute();
                else return false;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        boolean isNotInWater = !(this.creature instanceof RiftWaterCreature) || !this.creature.isInWater();
        boolean hasNoHerdLeader = !this.creature.hasHerdLeader();

        return this.creature.creatureBoxWithinReach() && hasNoHerdLeader && isNotInWater && !this.creature.busyAtWork() && super.shouldContinueExecuting();
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        for (int i = 0; i < 10; i++) {
            int possibleXOffset = RiftUtil.randomInRange(-8, 8);
            int possibleYOffset = RiftUtil.randomInRange(-3, 3);
            int possibleZOffset = RiftUtil.randomInRange(-8, 8);

            BlockPos posToCheck = this.creature.getPosition().add(possibleXOffset, possibleYOffset, possibleZOffset);
            if (!this.creature.isTamed()) return new Vec3d(posToCheck);
            else if (this.creature.isTamed() && this.withinHomeDistance(posToCheck)) return new Vec3d(posToCheck);
        }

        return null;
    }

    private boolean withinHomeDistance(BlockPos pos) {
        if (pos == null || this.creature.getHomePos() == null) return false;

        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.creature.world.getTileEntity(this.creature.getHomePos());

        if (creatureBox == null) return false;

        return creatureBox.posWithinDeploymentRange(pos);
    }
}
