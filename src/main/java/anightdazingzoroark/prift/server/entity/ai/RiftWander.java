package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
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
                if (!this.creature.isSitting()
                        && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.creature.busyAtWork()
                        && !this.creature.isBeingRidden()
                        && !this.creature.isInWater()) return super.shouldExecute();
                else return false;
            }
            else {
                if (!this.creature.isSitting()
                        && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.creature.busyAtWork()
                        && !this.creature.isBeingRidden()) return super.shouldExecute();
                else return false;
            }
        }
        else {
            boolean isHerdLeader = this.creature instanceof IHerder ? ((IHerder)this.creature).isHerdLeader() : false;
            boolean isStrayFromHerd = this.creature instanceof IHerder ? !((IHerder)this.creature).isHerdLeader() && !((IHerder)this.creature).hasHerdLeader() : true;
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
        boolean isNotInWater = this.creature instanceof RiftWaterCreature ? !this.creature.isInWater() : true;
        boolean hasNoHerdLeader = this.creature instanceof IHerder ? !((IHerder)this.creature).hasHerdLeader() : true;

        return this.creature.getEnergy() > 0 && hasNoHerdLeader && isNotInWater && !this.creature.busyAtWork() && super.shouldContinueExecuting();
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        Vec3d pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);

        //change wandering position around creature box
        if (this.creature.isTamed() && pos != null) {
            BlockPos blockPos = new BlockPos(pos);
            for (int i = 0; !this.withinHomeDistance(blockPos) && i < 10; i++) pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
        }
        return pos;
    }

    private boolean withinHomeDistance(BlockPos pos) {
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.creature.world.getTileEntity(this.creature.getHomePos());

        if (creatureBox == null) return false;

        return this.creature.getDistanceSq(pos) <= creatureBox.getWanderRange() * creatureBox.getWanderRange();
    }
}
