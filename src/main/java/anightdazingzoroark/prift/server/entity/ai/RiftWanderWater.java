package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public class RiftWanderWater extends EntityAIBase {
    private final RiftWaterCreature waterCreature;
    private final double speed;

    private boolean moveToNewPos;
    private BlockPos posToSwimTo;
    private float rotationToRotateTo;

    private boolean reverseAngle; //this is just for whether or not it should rotate left or right after completing a straight path
    private int reverseAngleCount;
    private int reverseAngleMaxCount;
    private final int[] reverseAngleMaxCountRange = new int[]{1, 5};

    private boolean swimUpwards;
    private int swimUpwardsCount;
    private int currentSwimUpwardsMaxCount;
    private final int[] swimUpwardsMaxCountRange = new int[]{2, 4};

    private int lastVerticalDisplacement;

    public RiftWanderWater(RiftWaterCreature creatureIn, double speed) {
        this.waterCreature = creatureIn;
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

    /*
    @Override
    public boolean shouldContinueExecuting() {
        boolean hasNoHerdLeader = !this.waterCreature.hasHerdLeader();
        return this.waterCreature.getEnergy() > this.waterCreature.getWeaknessEnergy() && this.waterCreature.creatureBoxWithinReach() && hasNoHerdLeader && this.waterCreature.isInWater();
    }
     */

    @Override
    public void startExecuting() {
        //reset position changers
        this.resetPositionChangers();
    }

    @Override
    public void resetTask() {
        this.waterCreature.setIsWanderingInWater(false);
    }

    @Override
    public void updateTask() {
        //move the creature
        if (this.moveToNewPos) {
            System.out.println("move to new pos");
            //go to new position
            this.waterCreature.getMoveHelper().setMoveTo(this.posToSwimTo.getX(), this.posToSwimTo.getY(), this.posToSwimTo.getZ(), this.speed);

            //check if at position or if current distance to target pos is way bigger than last distance
            System.out.println("this.posToSwimTo.distanceSq(this.waterCreature.getPosition()): "+this.posToSwimTo.distanceSq(this.waterCreature.getPosition()));
            if (this.posToSwimTo.distanceSq(this.waterCreature.getPosition()) <= 4D) {
                this.waterCreature.setIsWanderingInWater(false);
                this.moveToNewPos = false;
                this.updatePositionChangers();
            }

            //check up to 4 blocks in front of and 2 blocks above creature for if the path its going to remains valid
            int modifiedLastVerticalDisplacement = (int) Math.abs(Math.ceil(this.lastVerticalDisplacement / 2D));
            mainLoop: for (int xz = 0; xz <= 4; xz++) {
                for (int y = 0; y <= modifiedLastVerticalDisplacement; y++) {
                    System.out.println("this.swimUpwards: "+this.swimUpwards);
                    System.out.println("y: "+y);
                    int xDispToCheck = (int) ((xz + Math.ceil(this.waterCreature.width / 2)) * Math.cos(this.rotationToRotateTo));
                    int zDispToCheck = (int) ((xz + Math.ceil(this.waterCreature.width / 2)) * Math.sin(this.rotationToRotateTo));
                    int yDispToCheck = (int) (this.swimUpwards ? Math.ceil(this.waterCreature.height) + y : -y);

                    BlockPos posToCheck = this.waterCreature.getPosition().add(xDispToCheck, yDispToCheck, zDispToCheck);
                    if (!this.isWaterDestination(posToCheck) && !this.withinHomeDistance(posToCheck)) {
                        this.waterCreature.setIsWanderingInWater(false);
                        this.moveToNewPos = false;
                        this.updatePositionChangers();
                        break mainLoop;
                    }
                }
            }
        }
        //generate a new position to move to, as well as the speed to travel to reach that place
        else {
            System.out.println("look for new pos");
            //position generator is gonna be pretty unique
            //when position to swim to is null, create a completely random direction to go thats 16 blocks and 4 meters away
            if (this.posToSwimTo == null) this.posToSwimTo = this.completelyRandomPos();
            //otherwise, create a new position based on data
            else this.posToSwimTo = this.newPosBasedOnCurrentInfo();
            System.out.println("last waterCreature pos: "+this.waterCreature.getPosition());
            this.waterCreature.setIsWanderingInWater(true);
            this.moveToNewPos = true;
        }
    }

    private BlockPos completelyRandomPos() {
        BlockPos initPosition = this.waterCreature.getPosition();

        //generate x and z displacements and angle to rotate to
        this.rotationToRotateTo = (float) (this.waterCreature.world.rand.nextDouble() * 2 * Math.PI);
        double newX = 16 * Math.cos(this.rotationToRotateTo);
        double newZ = 16 * Math.sin(this.rotationToRotateTo);

        //generate y displacement
        this.swimUpwards = this.waterCreature.world.rand.nextBoolean();
        this.lastVerticalDisplacement = this.makeVerticalDisplacement();

        return initPosition.add(newX, this.lastVerticalDisplacement, newZ);
    }

    private BlockPos newPosBasedOnCurrentInfo() {
        BlockPos initPosition = this.waterCreature.getPosition();

        //generate x and z displacements based on current data
        this.rotationToRotateTo += 15 * (this.reverseAngle ? -1 : 1);
        double newX = 16 * Math.cos(this.rotationToRotateTo);
        double newZ = 16 * Math.sin(this.rotationToRotateTo);

        //generate y displacement
        this.lastVerticalDisplacement = this.makeVerticalDisplacement();

        return initPosition.add(newX, this.lastVerticalDisplacement, newZ);
    }

    private int makeVerticalDisplacement() {
        int toReturn = 0;

        for (int i = 0; i <= 4; i++) {
            int yDispToTest = this.swimUpwards ? i + (int) Math.ceil(this.waterCreature.height) : -i;
            BlockPos posToTest = this.waterCreature.getPosition().add(0, yDispToTest, 0);
            if (!this.isWaterDestination(posToTest) && !this.withinHomeDistance(posToTest)) break;
            toReturn = this.swimUpwards ? i : -1;
        }

        return toReturn;
    }

    //in an ideal open water, these update to gradually change directions to swim to
    private void updatePositionChangers() {
        //reverse angle displacement for horizontal movement randomly
        if (this.reverseAngleCount < this.reverseAngleMaxCount) this.reverseAngleCount++;
        else {
            this.reverseAngle = !this.reverseAngle;
            this.reverseAngleCount = 0;
            this.reverseAngleMaxCount = RiftUtil.randomInRange(this.reverseAngleMaxCountRange[0], this.reverseAngleMaxCountRange[1]);
        }

        //change swim upwards randomly
        if (this.swimUpwardsCount < this.currentSwimUpwardsMaxCount) this.swimUpwardsCount++;
        else {
            this.swimUpwards = !this.swimUpwards;
            this.swimUpwardsCount = 0;
            this.currentSwimUpwardsMaxCount = RiftUtil.randomInRange(this.swimUpwardsMaxCountRange[0], this.swimUpwardsMaxCountRange[1]);
        }
    }

    //if the creature bumps into an unpathable point, update these to then inverse the direction they will go to
    private void inversePositionChangers() {
        //inverse rotation to rotate to without hard resetting
        this.reverseAngle = !this.reverseAngle;
        this.rotationToRotateTo += this.reverseAngle ? -165 : 165;
        if (this.reverseAngleCount < this.reverseAngleMaxCount) this.reverseAngleCount++;
        else {
            this.reverseAngleCount = 0;
            this.reverseAngleMaxCount = RiftUtil.randomInRange(this.reverseAngleMaxCountRange[0], this.reverseAngleMaxCountRange[1]);
        }

        //inverse vertical swim direction without hard resetting
        this.swimUpwards = !this.swimUpwards;
        if (this.swimUpwardsCount < this.currentSwimUpwardsMaxCount) this.swimUpwardsCount++;
        else {
            this.reverseAngleCount = 0;
            this.currentSwimUpwardsMaxCount = RiftUtil.randomInRange(this.swimUpwardsMaxCountRange[0], this.swimUpwardsMaxCountRange[1]);
        }
    }

    private void resetPositionChangers() {
        //reset reversion of angle displacement for horizontal movement randomly
        this.reverseAngleCount = 0;
        this.reverseAngleMaxCount = RiftUtil.randomInRange(this.reverseAngleMaxCountRange[0], this.reverseAngleMaxCountRange[1]);

        //reset changing of upward swimming randomly
        this.swimUpwardsCount = 0;
        this.currentSwimUpwardsMaxCount = RiftUtil.randomInRange(this.swimUpwardsMaxCountRange[0], this.swimUpwardsMaxCountRange[1]);
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
