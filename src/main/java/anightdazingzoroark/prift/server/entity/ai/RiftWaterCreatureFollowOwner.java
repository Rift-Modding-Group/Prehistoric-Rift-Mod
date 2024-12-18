package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class RiftWaterCreatureFollowOwner extends EntityAIBase {
    private final RiftWaterCreature creature;
    private final double followSpeed;
    private World world;
    private final float maxDist;
    private final float minDist;
    private EntityLivingBase owner;
    private int timeToRecalcPath;

    public RiftWaterCreatureFollowOwner(RiftWaterCreature creature, double followSpeedIn, float minDist, float maxDist) {
        this.creature = creature;
        this.world = creature.world;
        this.followSpeed = followSpeedIn;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.creature.getOwner();

        if (entitylivingbase == null) return false;
        else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).isSpectator()) return false;
        else if (this.creature.isSitting()) return false;
        else if (this.creature.getDeploymentType() != PlayerTamedCreatures.DeploymentType.PARTY) return false;
        else if (this.creature.getDistanceSq(entitylivingbase) < (double)(this.minDist * this.minDist)) return false;
        else {
            this.owner = entitylivingbase;
            if (this.creature.isAmphibious()) return this.creature.getEnergy() > 0;
            else return this.creature.getEnergy() > 0 && this.owner.isInWater();
        }
    }

    public void startExecuting() {
        this.timeToRecalcPath = 0;
    }

    public void resetTask() {
        this.owner = null;
        this.creature.getNavigator().clearPath();
    }

    public boolean shouldContinueExecuting() {
        if (this.creature.isAmphibious()) return this.creature.getDistanceSq(this.owner) > (double)(this.maxDist * this.maxDist)
                && !this.creature.isSitting()
                && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY;
        else return this.owner.isInWater()
                && this.creature.getDistanceSq(this.owner) > (double)(this.maxDist * this.maxDist)
                && !this.creature.isSitting()
                && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY;
    }

    public void updateTask() {
        this.creature.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float)this.creature.getVerticalFaceSpeed());
        if (!this.creature.isSitting() && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (!this.creature.getNavigator().tryMoveToEntityLiving(this.owner, this.followSpeed)) {
                    if (!this.creature.getLeashed() && !this.creature.isRiding()) {
                        if (this.creature.getDistanceSq(this.owner) >= 144.0D) {
                            int i = MathHelper.floor(this.owner.posX) - 2;
                            int j = MathHelper.floor(this.owner.posZ) - 2;
                            int k = MathHelper.floor(this.owner.getEntityBoundingBox().minY);

                            for (int l = 0; l <= 4; ++l) {
                                for (int i1 = 0; i1 <= 4; ++i1) {
                                    if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(i, j, k, l, i1)) {
                                        this.creature.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.creature.rotationYaw, this.creature.rotationPitch);
                                        this.creature.getNavigator().clearPath();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset){
        BlockPos blockpos = new BlockPos(x + xOffset, y - 1, z + zOffset);
        IBlockState iblockstate = this.world.getBlockState(blockpos);
        if (this.creature.isAmphibious()) {
            return (iblockstate.getBlockFaceShape(this.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(this.creature) && this.world.isAirBlock(blockpos.up()) && this.world.isAirBlock(blockpos.up(2))) || iblockstate.getMaterial().isLiquid();
        }
        else return iblockstate.getMaterial().isLiquid();
    }
}
