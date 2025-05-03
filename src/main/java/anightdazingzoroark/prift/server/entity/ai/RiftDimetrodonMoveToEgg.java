package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.google.common.base.Predicate;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public class RiftDimetrodonMoveToEgg extends EntityAIBase {
    private final Dimetrodon dimetrodon;
    private final double speed;

    public RiftDimetrodonMoveToEgg(Dimetrodon dimetrodon, double speed) {
        this.dimetrodon = dimetrodon;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.dimetrodon.isTamed()
                && !this.dimetrodon.isBaby()
                && this.dimetrodon.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                && !this.dimetrodon.isSitting()
                && !this.dimetrodon.isTakingCareOfEgg()
                && this.dimetrodon.getHomePos() != null
                && this.dimetrodon.getEnergy() > 6) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.dimetrodon.world.getTileEntity(this.dimetrodon.getHomePos());

            if (creatureBox == null) return false;

            //get all eggs in range of creature box
            AxisAlignedBB creatureBoxAABB = new AxisAlignedBB(creatureBox.getPos().getX() - creatureBox.getWanderRange(),
                    creatureBox.getPos().getY() - creatureBox.getWanderRange(),
                    creatureBox.getPos().getZ() - creatureBox.getWanderRange(),
                    creatureBox.getPos().getX() + creatureBox.getWanderRange(),
                    creatureBox.getPos().getY() + creatureBox.getWanderRange(),
                    creatureBox.getPos().getZ() + creatureBox.getWanderRange());
            List<RiftEgg> availableEggs = this.dimetrodon.world.getEntitiesWithinAABB(RiftEgg.class, creatureBoxAABB, new Predicate<RiftEgg>() {
                @Override
                public boolean apply(@Nullable RiftEgg egg) {
                    //ensures that its obtained circularly
                    return egg != null && egg.getDistanceSq(dimetrodon.getHomePos()) <= creatureBox.getWanderRange() * creatureBox.getWanderRange();
                }
            });

            //get egg closest to dimetrodon
            RiftEgg closestEgg = null;
            for (RiftEgg egg : availableEggs) {
                if (closestEgg == null) closestEgg = egg;
                else {
                    if (egg.getDistanceSq(this.dimetrodon.posX, this.dimetrodon.posY, this.dimetrodon.posZ) <= closestEgg.getDistanceSq(this.dimetrodon.posX, this.dimetrodon.posY, this.dimetrodon.posZ)) {
                        closestEgg = egg;
                    }
                }
            }
            this.dimetrodon.eggTarget = closestEgg;
            return this.dimetrodon.eggTarget != null;
        }
        return false;
    }

    @Override
    public void resetTask() {
        if (this.dimetrodon.eggTarget != null && this.dimetrodon.eggTarget.isEntityAlive()) {
            this.dimetrodon.setSitting(true);
            this.dimetrodon.setTakingCareOfEgg(true);
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        boolean initTest = this.dimetrodon.eggTarget != null && this.dimetrodon.eggTarget.isEntityAlive() && this.dimetrodon.getEnergy() > 0 && this.dimetrodon.eggTarget.getDistance(this.dimetrodon) >= 4f;

        //for checking if egg is within range of creature box
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.dimetrodon.world.getTileEntity(this.dimetrodon.getHomePos());

        if (creatureBox == null) return initTest;

        boolean eggInRange = this.dimetrodon.eggTarget.getDistanceSq(dimetrodon.getHomePos()) <= creatureBox.getWanderRange() * creatureBox.getWanderRange();;

        return initTest && eggInRange;
    }

    @Override
    public void updateTask() {
        this.dimetrodon.getLookHelper().setLookPosition(this.dimetrodon.eggTarget.posX, this.dimetrodon.eggTarget.posY, this.dimetrodon.eggTarget.posZ, 30, 30);
        this.dimetrodon.getMoveHelper().setMoveTo(this.dimetrodon.eggTarget.posX, this.dimetrodon.eggTarget.posY, this.dimetrodon.eggTarget.posZ, this.speed);
    }
}
