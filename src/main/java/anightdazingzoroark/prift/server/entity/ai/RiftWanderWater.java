package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RiftWanderWater extends EntityAIWander {
    private final RiftWaterCreature waterCreature;

    public RiftWanderWater(RiftWaterCreature creatureIn, double speedIn) {
        super(creatureIn, speedIn, 1);
        this.waterCreature = creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (this.waterCreature.isTamed()) {
            if (this.waterCreature.getEnergy() > this.waterCreature.getWeaknessEnergy()
                    && this.waterCreature.creatureBoxWithinReach()
                    && !this.waterCreature.isSitting()
                    && this.waterCreature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                    && !this.waterCreature.isBeingRidden()
                    && this.waterCreature.isInWater()) return super.shouldExecute();
            else return false;
        }
        else {
            boolean isHerdLeader = this.waterCreature.isHerdLeader();
            boolean isStrayFromHerd = !this.waterCreature.canDoHerding() || !this.waterCreature.isHerdLeader() && !this.waterCreature.hasHerdLeader();

            if (isHerdLeader && this.waterCreature.isInWater()) return super.shouldExecute();
            else if (isStrayFromHerd && this.waterCreature.isInWater()) return super.shouldExecute();
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        boolean hasNoHerdLeader = !this.waterCreature.hasHerdLeader();
        return this.waterCreature.getEnergy() > this.waterCreature.getWeaknessEnergy() && this.waterCreature.creatureBoxWithinReach() && hasNoHerdLeader && this.waterCreature.isInWater() && super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        this.waterCreature.getNavigator().clearPath();
    }

    private boolean isWaterDestination(Vec3d pos) {
        if (pos == null) return false;
        BlockPos blockPos = new BlockPos(pos);
        return this.waterCreature.world.getBlockState(blockPos).getMaterial() == Material.WATER;
    }

    private boolean withinHomeDistance(Vec3d pos) {
        if (pos == null || this.waterCreature.getHomePos() == null) return false;

        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.waterCreature.world.getTileEntity(this.waterCreature.getHomePos());

        if (creatureBox == null) return false;

        return creatureBox.posWithinDeploymentRange(new BlockPos(pos));
    }
}