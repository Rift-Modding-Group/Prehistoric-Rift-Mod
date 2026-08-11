package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class RiftCreatureMoveHelper extends RiftCreatureMoveHelperBase {
    public static final double STANDARD_JUMP_HEIGHT = 1D;
    private static final int WATER_LAND_PATH_RETRY_TICKS = 10;

    private int waterLandPathRetryTicks;

    public RiftCreatureMoveHelper(RiftCreature creature) {
        super(creature);
    }

    @Override
    protected void onWait() {
        this.leapHelper.resetDelay();
        this.stopWalkingControls();
    }

    @Override
    protected void onMoveTo() {
        this.updateMoveTo(CreatureAction.MOVE_TO);
    }

    @Override
    protected void onStrafe() {
        float creatureSpeed = (float)this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
        float finalSpeed = (float)this.speed * creatureSpeed;

        this.creature.setAIMoveSpeed(finalSpeed);
        this.creature.setMoveForward(this.moveForward);
        this.creature.setMoveStrafing(this.moveStrafe);
        this.creatureAction = CreatureAction.WAIT;
        this.leapHelper.resetDelay();
    }

    @Override
    protected void onJumping() {
        if (this.creature.bodyTouchingLiquid()) {
            this.creatureAction = CreatureAction.WAIT;
            this.leapHelper.resetDelay();
            return;
        }
        this.creature.setAIMoveSpeed((float)(this.speed * this.creature
                .getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .getAttributeValue()));
        if (this.creature.onGround) this.creatureAction = CreatureAction.WAIT;
    }

    @Override
    protected void onCharge() {
        this.updateMoveTo(CreatureAction.CHARGE);
    }

    @Override
    protected void onLeap() {
        if (this.leapHelper.tryStartLeap()) this.leapHelper.continueLeap();
    }

    /**
     * charging and moving have the same necessary code
     * so we got this here
     * */
    private void updateMoveTo(CreatureAction requestedAction) {
        this.creatureAction = CreatureAction.WAIT;

        double displacementX = this.posX - this.entity.posX;
        double displacementZ = this.posZ - this.entity.posZ;
        double displacementY = this.posY - this.entity.posY;
        double horizontalDisplacementSq = displacementX * displacementX + displacementZ * displacementZ;
        double totalDisplacementSq = horizontalDisplacementSq + displacementY * displacementY;
        boolean inLiquid = this.creature.bodyTouchingLiquid();
        boolean followingWaterPath = this.creature.getCreaturePathNavigate().isFollowingWaterPath();
        if (!inLiquid) this.waterLandPathRetryTicks = 0;
        else if (this.waterLandPathRetryTicks > 0) this.waterLandPathRetryTicks--;

        if (inLiquid && requestedAction == CreatureAction.MOVE_TO
                && this.creature.getCreaturePathNavigate().noPath()
                && this.waterLandPathRetryTicks <= 0
        ) {
            BlockPos requestedBlock = new BlockPos(this.posX, this.posY, this.posZ);
            if (this.creature.world.getBlockState(requestedBlock).getMaterial() == Material.AIR
                    && this.creature.world.getBlockState(requestedBlock.down()).getMaterial().isSolid()
            ) {
                this.waterLandPathRetryTicks = WATER_LAND_PATH_RETRY_TICKS;
                if (this.creature.getCreaturePathNavigate().tryMoveToPositionUsingWater(requestedBlock, this.speed)) {
                    this.stopWalkingControls();
                    this.leapHelper.resetDelay();
                    return;
                }
            }
        }

        double relevantDisplacementSq = inLiquid || followingWaterPath
                ? horizontalDisplacementSq
                : totalDisplacementSq;

        if (relevantDisplacementSq >= 2.5E-7D) {
            if (horizontalDisplacementSq >= 2.5E-7D) {
                float targetYaw = (float)(MathHelper.atan2(displacementZ, displacementX) * 180D / Math.PI) - 90f;
                this.creature.rotationYaw = this.limitAngle(this.creature.rotationYaw, targetYaw, 90f);
            }
            this.creature.setMoveStrafing(0f);
            this.creature.setAIMoveSpeed(
                    (float)(this.speed * this.creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue())
            );

            CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
            double maximumClearance = navigation.getCanLeap() ?
                    navigation.getLeapHeight() : STANDARD_JUMP_HEIGHT + 0.125D;
            double obstacleClearance = this.leapHelper.getObstacleClearance(this.posX, this.posZ, maximumClearance);
            boolean leapHandled = !inLiquid && !followingWaterPath && this.leapHelper.tryHandleLeap(
                    this.posX, this.posY, this.posZ,
                    obstacleClearance, requestedAction
            );

            if (!leapHandled) {
                this.leapHelper.resetDelay();

                boolean closeToWaypoint = horizontalDisplacementSq < Math.max(1f, this.creature.width * this.creature.width);
                boolean standardJumpTransition = (obstacleClearance > this.creature.stepHeight
                        && obstacleClearance <= STANDARD_JUMP_HEIGHT + 0.125D)
                        || (closeToWaypoint
                        && displacementY > this.creature.stepHeight
                        && displacementY <= STANDARD_JUMP_HEIGHT + 0.125D);
                if (!inLiquid && !followingWaterPath
                        && navigation.getCanWalk() && this.creature.onGround && standardJumpTransition) {
                    this.creature.getJumpHelper().setJumping();
                    this.creatureAction = CreatureAction.JUMPING;
                }
            }
        }
        else {
            this.stopWalkingControls();
            this.leapHelper.resetDelay();
        }
    }
}
