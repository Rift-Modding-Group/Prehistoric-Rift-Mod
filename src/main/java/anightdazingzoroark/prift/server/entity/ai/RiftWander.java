package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
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
                        && this.creature.getEnergy() > this.creature.getWeaknessEnergy()
                        && this.creature.creatureBoxWithinReach()
                        && !this.creature.isSitting()
                        && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.creature.busyAtWork()
                        && !this.creature.isBeingRidden()
                        && !this.creature.isInWater();
            }
            else {
                return super.shouldExecute()
                        && this.creature.getEnergy() > this.creature.getWeaknessEnergy()
                        && this.creature.creatureBoxWithinReach()
                        && !this.creature.isSitting()
                        && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.creature.busyAtWork()
                        && !this.creature.isBeingRidden();
            }
        }
        else {
            boolean isHerdLeader = this.creature.isHerdLeader();
            boolean isStrayFromHerd = !this.creature.canDoHerding() || !this.creature.isHerdLeader() && !this.creature.hasHerdLeader();
            if (this.creature instanceof RiftWaterCreature) {
                if (isHerdLeader && !this.creature.isInWater()) return super.shouldExecute() && this.creature.getEnergy() > this.creature.getWeaknessEnergy();
                else if (isStrayFromHerd && !this.creature.isInWater()) return super.shouldExecute() && this.creature.getEnergy() > this.creature.getWeaknessEnergy();
                else return false;
            }
            else {
                if (isHerdLeader) return super.shouldExecute() && this.creature.getEnergy() > this.creature.getWeaknessEnergy();
                else if (isStrayFromHerd) return super.shouldExecute() && this.creature.getEnergy() > this.creature.getWeaknessEnergy();
                else return false;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        boolean isNotInWater = !(this.creature instanceof RiftWaterCreature) || !this.creature.isInWater();
        boolean hasNoHerdLeader = !this.creature.hasHerdLeader();

        return this.creature.getEnergy() > this.creature.getWeaknessEnergy() && this.creature.creatureBoxWithinReach() && hasNoHerdLeader && isNotInWater && !this.creature.busyAtWork() && super.shouldContinueExecuting();
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        Vec3d pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);

        //change wandering position around creature box
        if (this.creature.isTamed() && this.creature.creatureBoxWithinReach()) {
            for (int i = 0; i < 10; i++) {
                if (this.vectorWithinHomeDistance(pos)) break;
                else pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
            }
            if (!this.vectorWithinHomeDistance(pos)) pos = this.getPosition();
        }

        return pos;
    }

    private boolean vectorWithinHomeDistance(Vec3d pos) {
        if (pos == null || this.creature.getHomePos() == null) return false;

        RiftNewTileEntityCreatureBox creatureBox = (RiftNewTileEntityCreatureBox) this.creature.world.getTileEntity(this.creature.getHomePos());

        if (creatureBox == null) return false;

        return creatureBox.posWithinDeploymentRange(new BlockPos(pos));
    }
}
