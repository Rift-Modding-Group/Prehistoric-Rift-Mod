package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class RiftBurrowMove extends RiftCreatureMove {
    private Entity burrowTarget;

    public RiftBurrowMove() {
        super(CreatureMove.BURROW);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        BlockPos userBlockPos = user.getPosition().down();
        return user.checkIfCanBreakBlock(userBlockPos);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        //for now, since no ridable mob can learn burrow yet, let's assume that this will
        //always be used by an unridable mob
        user.setEntityInvulnerable(true);
        this.burrowTarget = target;
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {
        user.setBurrowing(true);
        AxisAlignedBB burrowerDetectHitbox = user.getEntityBoundingBox().grow(0.5D);
        boolean burrowTargetHit = false;

        //as long as the creature is on a block that they can break, or in front of a 1 block tall wall which they can break,
        //they should be able to continue the move
        BlockPos userBlockPos = user.getPosition().down();

        //get block in front, and block above it
        double xMove = user.width * Math.sin(-Math.toRadians(user.rotationYaw));
        double zMove = user.width * Math.cos(Math.toRadians(user.rotationYaw));
        BlockPos inFrontPos = new BlockPos(user.posX + xMove, user.posY, user.posZ + zMove);

        //always dig towards the target
        if (this.burrowTarget != null && this.burrowTarget.isEntityAlive()) {
            user.getMoveHelper().setMoveTo(this.burrowTarget.posX, this.burrowTarget.posY, this.burrowTarget.posZ, 1.5D);

            //jumping logic
            if (user.checkIfCanBreakBlock(inFrontPos)
                    && user.world.getBlockState(inFrontPos).getMaterial().blocksMovement()
                    && !user.world.getBlockState(inFrontPos.up()).getMaterial().blocksMovement()
            )
                user.getJumpHelper().setJumping();

            for (Entity entity : user.world.getEntitiesWithinAABB(Entity.class, burrowerDetectHitbox, this.generalEntityPredicate(user))) {
                if (entity instanceof MultiPartEntityPart) {
                    Entity parent = (Entity) ((MultiPartEntityPart)entity).parent;
                    if (parent != null && parent.equals(this.burrowTarget)) {
                        burrowTargetHit = user.attackEntityAsMob(parent);
                        break;
                    }
                }
                else if (entity.equals(this.burrowTarget)) {
                    burrowTargetHit = user.attackEntityAsMob(entity);
                    break;
                }
            }
        }

        this.forceStopFlag = this.burrowTarget == null
                || !this.burrowTarget.isEntityAlive()
                || burrowTargetHit
                || !user.checkIfCanBreakBlock(userBlockPos);
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onBeforeStopExecuting(RiftCreature user) {
        user.setBurrowing(false);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setBurrowing(false);
        user.setEntityInvulnerable(false);
    }
}
