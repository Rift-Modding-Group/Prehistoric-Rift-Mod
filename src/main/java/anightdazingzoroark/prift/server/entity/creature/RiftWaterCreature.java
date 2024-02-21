package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftWaterCreature;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftWaterCreatureMoveHelper;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RiftWaterCreature extends RiftCreature {
    public RiftWaterCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn, creatureType);
        this.moveHelper = new RiftWaterCreatureMoveHelper(this);
        this.setPathPriority(PathNodeType.WATER, 0);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //flipping around on land
        if (!this.isDead && !this.isInWater() && this.onGround && this.collidedVertically) {
            this.motionX += 0.05 * (this.rand.nextFloat() * 2 - 1);
            this.motionY += 0.5;
            this.motionZ += 0.05 * (this.rand.nextFloat() * 2 - 1);

            this.onGround = false;
            this.isAirBorne = true;
        }
    }

    public void onEntityUpdate() {
        int i = this.getAir();
        super.onEntityUpdate();

        //underwater breathing
        if (this.isEntityAlive() && !this.isInWater() && !this.isAmphibious()) {
            --i;
            this.setAir(i);

            if (this.getAir() == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        }
        else if (this.isAmphibious() && this.isEntityAlive()) this.setAir(300);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

    @Override
    public boolean canBreatheUnderwater()
    {
        return true;
    }

    public abstract boolean isAmphibious();

    @Override
    public boolean isPushedByWater()
    {
        return false;
    }

    @Override
    public boolean isNotColliding()
    {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }

    @Override
    public boolean getCanSpawnHere() {
        return true;
    }

    public void manageHerding() {
        this.herdCheckCountdown--;
        if (this.herdCheckCountdown <= 0) {
            //add members to herd
            //should modify this so that it can only follow mobs that it can path to
            List<RiftWaterCreature> potentialHerders = this.world.getEntitiesWithinAABB(this.getClass(), this.getHerdBoundingBox(), new Predicate<RiftWaterCreature>() {
                @Override
                public boolean apply(@Nullable RiftWaterCreature input) {
                    return !input.isTamed() && input.isInWater();
                }
            });
            int herdLeaderId = Collections.min(potentialHerders.stream().map(RiftWaterCreature::getEntityId).collect(Collectors.toList()));
            this.setHerdLeader((RiftCreature) this.world.getEntityByID(herdLeaderId));
            this.herdCheckCountdown = RiftUtil.randomInRange(10, 15) * 20;
        }
    }

    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateRiftWaterCreature(this, worldIn);
    }

    protected boolean canTriggerWalking() {
        return false;
    }
}
