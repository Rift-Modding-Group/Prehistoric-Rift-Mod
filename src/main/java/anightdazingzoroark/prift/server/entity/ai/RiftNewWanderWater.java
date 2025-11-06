package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelperBase;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftWaterCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.sun.javafx.geom.Vec2d;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftNewWanderWater extends EntityAIBase {
    private final RiftWaterCreature waterCreature;
    private final double speed;

    private int angleDirection; //this is the angle the creature is to look towards for horizontal movement

    //horizontal movement
    private int horizontalMovementTime;
    private int maxHorizontalMovementTime;
    private final int[] maxHorizontalMovementTimeRange = new int[]{3, 5};

    //horizontal angle displacement
    private boolean angleDirectionInverted;
    private int angleDirectionChangeCount;
    private int maxAngleDirectionChangeCount;
    private final int[] maxAngleDirectionChangeCountRange = new int[]{5, 12};

    //vertical movement
    private RiftCreatureMoveHelperBase.VerticalMoveOption verticalMoveOption = RiftCreatureMoveHelperBase.VerticalMoveOption.NONE;
    private int verticalMovementTime;
    private int maxVerticalMovementTime;
    private final int[] maxVerticalMovementTimeRange = new int[]{1, 5};

    public RiftNewWanderWater(RiftWaterCreature waterCreature, double speed) {
        this.waterCreature = waterCreature;
        this.speed = speed;
    }

    @Override
    public boolean shouldExecute() {
        if (this.waterCreature.isTamed()) {
            return this.waterCreature.getEnergy() > this.waterCreature.getWeaknessEnergy()
                    && this.waterCreature.creatureBoxWithinReach()
                    && !this.waterCreature.isSitting()
                    && this.waterCreature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                    && !this.waterCreature.isBeingRidden()
                    && this.waterCreature.isInWater();
        }
        else {
            boolean isHerdLeader = this.waterCreature.isHerdLeader();
            boolean isStrayFromHerd = !this.waterCreature.canDoHerding() || !this.waterCreature.isHerdLeader() && !this.waterCreature.hasHerdLeader();

            return (isHerdLeader && this.waterCreature.isInWater()) || (isStrayFromHerd && this.waterCreature.isInWater());
        }
    }

    @Override
    public void startExecuting() {
        this.waterCreature.setIsWanderingInWater(true);
        this.angleDirection = (int) this.waterCreature.rotationYaw;

        //init horizontal movement timer
        this.resetHorizontalMovementFlags();

        //init horizontal angle change flags
        this.angleDirectionInverted = this.waterCreature.world.rand.nextBoolean();
        this.resetHorizontalAngleDisplacementFlags();

        //init vertical movement flags
        this.verticalMoveOption = RiftCreatureMoveHelperBase.VerticalMoveOption.values()[this.waterCreature.world.rand.nextInt(RiftCreatureMoveHelperBase.VerticalMoveOption.values().length)];
        this.resetVerticalMovementFlags();
    }

    @Override
    public void resetTask() {
        this.waterCreature.setIsWanderingInWater(false);
    }

    @Override
    public void updateTask() {
        //move horizontally
        ((RiftWaterCreatureMoveHelper) this.waterCreature.getMoveHelper()).setMoveTo(this.angleDirection, this.verticalMoveOption, this.horizontalSwimSpeed());

        //deal with horizontal movement timer and the resulting horizontal angle displacement
        if (this.horizontalMovementTime < this.maxHorizontalMovementTime) this.horizontalMovementTime++;
        else {
            this.angleDirection += this.angleDirectionInverted ? -30 : 30;
            this.angleDirectionChangeCount++;
            this.resetHorizontalMovementFlags();
        }

        //deal with horizontal angle displacement count
        if (this.angleDirectionChangeCount > this.maxAngleDirectionChangeCount) {
            this.angleDirectionInverted = !this.angleDirectionInverted;
            this.resetHorizontalAngleDisplacementFlags();
        }

        //deal with vertical movement timer
        if (this.verticalMovementTime < this.maxVerticalMovementTime) this.verticalMovementTime++;
        else {
            this.verticalMoveOption = RiftCreatureMoveHelperBase.VerticalMoveOption.values()[this.waterCreature.world.rand.nextInt(RiftCreatureMoveHelperBase.VerticalMoveOption.values().length)];
            this.resetVerticalMovementFlags();
        }

        //when doing horizontal movement, if block in front is not solid, reverse direction
        //must get blocks 4 blocks ahead
        for (int i = 0; i <= 4; i++) {
            int xDispToCheck = (int) ((i + Math.ceil(this.waterCreature.width / 2)) * Math.cos(Math.toRadians(this.angleDirection)));
            int zDispToCheck = (int) ((i + Math.ceil(this.waterCreature.width / 2)) * Math.sin(Math.toRadians(this.angleDirection)));
            BlockPos posToCheck = this.waterCreature.getPosition().add(xDispToCheck, 0, zDispToCheck);

            if (!this.isWaterDestination(posToCheck) && !this.withinHomeDistance(posToCheck)) {
                //reverse angle direction
                int initAngleResult = this.angleDirection + 180;
                if (initAngleResult >= 180) initAngleResult -= 360;
                else if (initAngleResult <= -180) initAngleResult += 360;
                this.angleDirection = initAngleResult;
                break;
            }
        }
    }

    private void resetHorizontalMovementFlags() {
        this.horizontalMovementTime = 0;
        this.maxHorizontalMovementTime = 20 * RiftUtil.randomInRange(this.maxHorizontalMovementTimeRange[0], this.maxHorizontalMovementTimeRange[1]);
    }

    private void resetHorizontalAngleDisplacementFlags() {
        this.angleDirectionChangeCount = 0;
        this.maxAngleDirectionChangeCount = 20 * RiftUtil.randomInRange(this.maxAngleDirectionChangeCountRange[0], this.maxAngleDirectionChangeCountRange[1]);
    }

    private void resetVerticalMovementFlags() {
        this.verticalMovementTime = 0;
        this.maxVerticalMovementTime = 20 * RiftUtil.randomInRange(this.maxVerticalMovementTimeRange[0], this.maxVerticalMovementTimeRange[1]);
    }

    private float horizontalSwimSpeed() {
        return (float) (this.speed * this.waterCreature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
    }

    private boolean isWaterDestination(BlockPos pos) {
        if (pos == null) return false;
        return this.waterCreature.world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    private boolean withinHomeDistance(BlockPos pos) {
        if (pos == null || this.waterCreature.getHomePos() == null) return false;

        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.waterCreature.world.getTileEntity(this.waterCreature.getHomePos());

        if (creatureBox == null) return false;

        return creatureBox.posWithinDeploymentRange(pos);
    }
}
