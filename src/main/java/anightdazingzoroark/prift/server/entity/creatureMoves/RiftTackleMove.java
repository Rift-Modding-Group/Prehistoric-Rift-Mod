package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSetEntityMotion;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RiftTackleMove extends RiftCreatureMove {
    private BlockPos targetPosForTackle;
    private int chargeTime;
    private int maxChargeTime;
    private double chargeDirectionToPosX;
    private double chargeDirectionToPosZ;
    private final double tackleMaxDist = 4;
    private final int chargeSpeed = 4;

    public RiftTackleMove() {
        super(CreatureMove.TACKLE);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();

        //this is only relevant when unmounted
        if (target != null) {
            //get charge distance
            double unnormalizedDirectionX = target.posX - user.posX;
            double unnormalizedDirectionZ = target.posZ - user.posZ;
            double angleToTarget = Math.atan2(unnormalizedDirectionZ, unnormalizedDirectionX);
            double chargeDistX = this.tackleMaxDist * Math.cos(angleToTarget);
            double chargeDistZ = this.tackleMaxDist * Math.sin(angleToTarget);

            //get charge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(unnormalizedDirectionX, 2) + Math.pow(unnormalizedDirectionZ, 2));
            this.chargeDirectionToPosX = unnormalizedDirectionX / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = unnormalizedDirectionZ / unnormalizedMagnitude;

            //get charge time
            //the point at which it stops when unmounted is doubled, so the max charge time here
            //is to be halved to make it consistent with when mounted
            this.maxChargeTime = (int)Math.round(Math.sqrt(chargeDistX * chargeDistX + chargeDistZ * chargeDistZ) / (this.chargeSpeed * 2));
        }
        //this is only relevant when mounted
        else {
            //get charge direction
            double unnormalizedMagnitude = Math.sqrt(Math.pow(user.getLookVec().x, 2) + Math.pow(user.getLookVec().z, 2));
            this.chargeDirectionToPosX = user.getLookVec().x / unnormalizedMagnitude;
            this.chargeDirectionToPosZ = user.getLookVec().z / unnormalizedMagnitude;

            //get charge time
            this.maxChargeTime = (int) Math.ceil(this.tackleMaxDist / this.chargeSpeed);
        }
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        user.setCanMove(true);
        AxisAlignedBB tackleDetectHitbox = user.getEntityBoundingBox();
        AxisAlignedBB tackleEffectHitbox = tackleDetectHitbox.grow(2D);

        //stop if it hits a mob
        List<Entity> tackledEntities = user.world.getEntitiesWithinAABB(Entity.class, tackleDetectHitbox.grow(1D), this.generalEntityPredicate(user));

        //stop if it hits a block
        boolean hitBlocksFlag = false;
        breakBlocksLoop: for (int x = MathHelper.floor(tackleDetectHitbox.minX); x < MathHelper.ceil(tackleDetectHitbox.maxX); x++) {
            for (int z = MathHelper.floor(tackleDetectHitbox.minZ); z < MathHelper.ceil(tackleDetectHitbox.maxZ); z++) {
                IBlockState state = user.world.getBlockState(new BlockPos(x, user.posY, z));
                IBlockState stateUp = user.world.getBlockState(new BlockPos(x, user.posY + 1, z));

                if (state.getMaterial() != Material.AIR && stateUp.getMaterial() != Material.AIR) {
                    hitBlocksFlag = true;
                    break breakBlocksLoop;
                }
            }
        }

        if (hitBlocksFlag || !tackledEntities.isEmpty() || this.chargeTime >= this.maxChargeTime) {
            user.motionX = 0;
            user.motionZ = 0;
            user.velocityChanged = true;

            //damage all entities it charged into
            if (!tackledEntities.isEmpty()) {
                List<Entity> entitiesToDamage = user.world.getEntitiesWithinAABB(Entity.class, tackleEffectHitbox, this.generalEntityPredicate(user));
                for (Entity entity : entitiesToDamage) {
                    user.attackEntityAsMob(entity);
                }
            }

            //forcibly stop the move
            this.forceStopFlag = true;
        }
        else {
            //for some reason when being ridden and tryin to make a creature
            //charge/lunge into a wall theres a good chance they'll stop prematurely
            //so there's this shit instead
            if (user.isBeingRidden() && user.getControllingPassenger() != null)
                RiftMessages.WRAPPER.sendToAll(new RiftSetEntityMotion(user, this.chargeDirectionToPosX * this.chargeSpeed, this.chargeDirectionToPosZ * this.chargeSpeed));
            else
                user.move(MoverType.SELF, this.chargeDirectionToPosX * this.chargeSpeed, user.motionY, this.chargeDirectionToPosZ * this.chargeSpeed);

            this.chargeTime++;
            if (this.useValue > 0) this.useValue--; //this only matters when using while mounted
        }
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }
}
