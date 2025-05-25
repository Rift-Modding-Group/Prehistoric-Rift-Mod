package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.List;

public class RiftLungeMove extends RiftCreatureMove {
    private BlockPos targetPosForLunge;
    private int lungeTime;
    private int maxLungeTime;
    private double lungeDirectionToPosX;
    private double lungeDirectionToPosY;
    private double lungeDirectionToPosZ;
    private final double lungeVelocity = 8D;

    public RiftLungeMove() {
        super(CreatureMove.LUNGE);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.isInWater()) return true;
        else return user.onGround && user.posY >= target.posY - 1 && user.posY <= target.posY + 1;
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user.onGround || user.isInWater();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.disableCanRotateMounted();
        if (target != null) this.targetPosForLunge = new BlockPos(target.posX, user.posY, target.posZ);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        //stop if it hits a mob
        AxisAlignedBB lungeHitbox = user.getEntityBoundingBox().grow(3D);
        List<Entity> chargedIntoEntities = user.world.getEntitiesWithinAABB(Entity.class, lungeHitbox, new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity instanceof RiftCreaturePart) {
                    RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                    return !parent.equals(user)
                            && RiftUtil.checkForNoAssociations(user, parent)
                            && ((parent instanceof IHerder && user instanceof IHerder) ?
                            ((IHerder)user).getHerdLeader() != null && ((IHerder)parent).getHerdLeader() != null && !((IHerder)user).getHerdLeader().equals(((IHerder)parent).getHerdLeader())
                            : true);
                }
                else if (entity instanceof RiftCreature) {
                    return  !entity.equals(user)
                            && RiftUtil.checkForNoAssociations(user, entity)
                            && ((entity instanceof IHerder && user instanceof IHerder) ?
                            ((IHerder)user).getHerdLeader() != null && ((IHerder)entity).getHerdLeader() != null && !((IHerder)user).getHerdLeader().equals(((IHerder)entity).getHerdLeader())
                            : true);
                }
                else if (entity instanceof EntityLivingBase) return RiftUtil.checkForNoAssociations(user, entity) && !entity.equals(user);
                else return false;
            }
        });

        //stop if it hits a block
        boolean hitBlocksFlag = false;
        hitBlocksLoop: for (int x = MathHelper.floor(lungeHitbox.minX); x < MathHelper.ceil(lungeHitbox.maxX); x++) {
            for (int z = MathHelper.floor(lungeHitbox.minZ); z < MathHelper.ceil(lungeHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR
                && state.getMaterial() != Material.WATER && stateUp.getMaterial() != Material.WATER) {
                    hitBlocksFlag = true;
                    break hitBlocksLoop;
                }
            }
        }

        if (hitBlocksFlag || !chargedIntoEntities.isEmpty() || this.lungeTime > this.maxLungeTime) {
            //stop charging
            user.velocityChanged = true;
            user.motionX = 0;
            user.motionY = 0;
            user.motionZ = 0;

            //damage all entities it charged into
            if (!chargedIntoEntities.isEmpty()) for (Entity entity : chargedIntoEntities) {
                if (entity instanceof RiftCreaturePart) {
                    RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                    user.attackEntityAsMob(parent);
                }
                user.attackEntityAsMob(entity);
            }

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            user.motionX = this.lungeDirectionToPosX * this.lungeVelocity;
            user.motionY = this.lungeDirectionToPosY * this.lungeVelocity;
            user.motionZ = this.lungeDirectionToPosZ * this.lungeVelocity;
            user.velocityChanged = true;
            this.lungeTime++;
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (this.targetPosForLunge != null) {
            //in water, lunging is 3-dimensional
            if (user.isInWater()) {
                //get charge distance
                double unnormalizedDirectionX = this.targetPosForLunge.getX() - user.posX;
                double unnormalizedDirectionY = this.targetPosForLunge.getY() - user.posY;
                double unnormalizedDirectionZ = this.targetPosForLunge.getZ() - user.posZ;

                double angleXZYToTarget = Math.atan2(unnormalizedDirectionY, Math.sqrt(unnormalizedDirectionX * unnormalizedDirectionX + unnormalizedDirectionZ * unnormalizedDirectionZ));

                double chargeDistXZ = user.rangedWidth() * Math.cos(angleXZYToTarget);
                double chargeDistY = user.rangedWidth() * Math.sin(angleXZYToTarget);

                //get charge direction
                double unnormalizedMagnitude = Math.sqrt(unnormalizedDirectionX * unnormalizedDirectionX + unnormalizedDirectionY * unnormalizedDirectionY + unnormalizedDirectionZ * unnormalizedDirectionZ);
                this.lungeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
                this.lungeDirectionToPosY = unnormalizedDirectionY / unnormalizedMagnitude;
                this.lungeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;

                //get charge time
                this.maxLungeTime = (int)Math.round(Math.sqrt(chargeDistXZ * chargeDistXZ + chargeDistY * chargeDistY) * 1.5D / (this.lungeVelocity * 2));
            }
            //on land it's 2-dimensional
            else {
                //get charge distance
                double unnormalizedDirectionX = this.targetPosForLunge.getX() - user.posX;
                double unnormalizedDirectionZ = this.targetPosForLunge.getZ() - user.posZ;
                double angleToTarget = Math.atan2(unnormalizedDirectionZ, unnormalizedDirectionX);
                double chargeDistX = user.rangedWidth() * Math.cos(angleToTarget);
                double chargeDistZ = user.rangedWidth() * Math.sin(angleToTarget);

                //get charge direction
                double unnormalizedMagnitude = Math.sqrt(Math.pow(unnormalizedDirectionX, 2) + Math.pow(unnormalizedDirectionZ, 2));
                this.lungeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
                this.lungeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;

                //get charge time
                this.maxLungeTime = (int)Math.round(Math.sqrt(chargeDistX * chargeDistX + chargeDistZ * chargeDistZ) * 1.5D / (this.lungeVelocity * 2));
            }
        }
        else {
            //get lunge direction
            this.lungeDirectionToPosX = user.getLookVec().normalize().x;
            if (user.isInWater()) this.lungeDirectionToPosY = user.getLookVec().normalize().y;
            this.lungeDirectionToPosZ = user.getLookVec().normalize().z;

            //get lunge time
            this.maxLungeTime = (int) Math.ceil(user.rangedWidth() * 1.5D / this.lungeVelocity);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.enableCanRotateMounted();
    }
}
