package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftSwimmer;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftWaterCreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class RiftWaterCreature extends RiftCreature {
    private static final DataParameter<Boolean> ASCENDING = EntityDataManager.createKey(RiftWaterCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DESCENDING = EntityDataManager.createKey(RiftWaterCreature.class, DataSerializers.BOOLEAN);

    private final PathNavigateSwimmer waterNavigate;
    private final PathNavigateGround landNavigate;
    private boolean amphibiousInWater;

    public RiftWaterCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn, creatureType);
        this.waterNavigate = new PathNavigateRiftSwimmer(this, this.world);
        this.landNavigate = new PathNavigateGround(this, this.world);
        //non-amphibious creatures are water only, and it is to be always presumed they will be spawned in an
        //remain in water, hence this
        if (!this.isAmphibious()) {
            this.moveHelper = new RiftWaterCreatureMoveHelper(this);
            this.navigator = this.waterNavigate;
        }
        //amphibious creatures are presumed to have been spawned on land, so here
        else {
            this.moveHelper = new RiftCreatureMoveHelper(this);
            this.navigator = this.landNavigate;
            this.setPathPriority(PathNodeType.WATER, 0f); //amphibious creatures can path to water like regular blocks
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ASCENDING, false);
        this.dataManager.register(DESCENDING, false);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        this.movementManager();
        this.targetOnLandManage();
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

    private void targetOnLandManage() {
        if (this.getAttackTarget() != null) {
            if (!this.getAttackTarget().isInWater() && this.isInWater() && !this.isAmphibious()) this.setAttackTarget(null);
        }
    }

    private void movementManager() {
        //for flopping on land
        if (!this.isDead && !this.isInWater() && this.onGround && this.collidedVertically && this.canFlop()) {
            this.motionX += 0.05 * (this.rand.nextFloat() * 2 - 1);
            this.motionY += 0.5;
            this.motionZ += 0.05 * (this.rand.nextFloat() * 2 - 1);

            this.onGround = false;
            this.isAirBorne = true;
        }

        //for changing navigator on land for amphibious
        if (this.isAmphibious()) {
            //land to water
            if (this.isInWater() && !this.amphibiousInWater) {
                this.amphibiousInWater = true;
                this.navigator = this.waterNavigate;
                this.moveHelper = this.moveHelperToWaterMoveHelper();
                //this.setPathPriority(PathNodeType.WATER, 0);
                this.navigator.clearPath();
            }
            //water to land
            else if (!this.isInWater() && this.amphibiousInWater) {
                this.amphibiousInWater = false;
                this.navigator = this.landNavigate;
                this.moveHelper = this.waterMoveHelperToMoveHelper();
                //this.setPathPriority(PathNodeType.WATER, this.defaultWaterCost);
                this.navigator.clearPath();
            }
        }
    }

    private RiftCreatureMoveHelper waterMoveHelperToMoveHelper() {
        if (this.moveHelper instanceof RiftWaterCreatureMoveHelper) {
            RiftWaterCreatureMoveHelper waterCreatureMoveHelper = (RiftWaterCreatureMoveHelper) this.moveHelper;
            return waterCreatureMoveHelper.convertToMoveHelper(this);
        }
        else if (this.moveHelper instanceof RiftCreatureMoveHelper) return (RiftCreatureMoveHelper) this.moveHelper;
        else return null;
    }

    private RiftWaterCreatureMoveHelper moveHelperToWaterMoveHelper() {
        if (this.moveHelper instanceof RiftCreatureMoveHelper) {
            RiftCreatureMoveHelper creatureMoveHelper = (RiftCreatureMoveHelper) this.moveHelper;
            return creatureMoveHelper.convertToWaterMoveHelper(this);
        }
        else if (this.moveHelper instanceof RiftWaterCreatureMoveHelper) return (RiftWaterCreatureMoveHelper) this.moveHelper;
        else return null;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isInWater() {
        if (this.getBodyHitbox() != null) {
            return this.world.getBlockState(this.getBodyHitbox().getPosition()).getMaterial() == Material.WATER;
        }
        return this.world.getBlockState(this.getPosition()).getMaterial() == Material.WATER;
    }

    public boolean canFlop() {
        return false;
    }

    public abstract boolean isAmphibious();

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean isNotColliding() {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }

    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, this.world);
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    public boolean isUsingSwimControls() {
        return this.getIsAscending() || this.getIsDescending();
    }

    public boolean getIsAscending() {
        return this.dataManager.get(ASCENDING);
    }

    public void setIsAscending(boolean value) {
        this.dataManager.set(ASCENDING, value);
    }

    public boolean getIsDescending() {
        return this.dataManager.get(DESCENDING);
    }

    public void setIsDescending(boolean value) {
        this.dataManager.set(DESCENDING, value);
    }

    //herding stuff starts here
    public boolean canDoHerding() {
        return super.canDoHerding() && this.isInWater();
    }
    //herding stuff ends here

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.canMove() || this.isTurretMode()) {
            this.superTravel(0, this.isInWater() ? 0 : vertical, 0);
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            return;
        }

        if (this.isSaddled() && this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null && this.isInWater()) {
                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;
                this.stepHeight = 1.0F;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                this.fallDistance = 0;
                float moveSpeedMod = (this.getEnergy() > this.getWeaknessEnergy() ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = ((float)(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) - riderSpeed) * moveSpeedMod;

                if (this.getIsAscending()) this.motionY = 0.25D;
                else if (this.getIsDescending()) this.motionY = -0.25D;

                this.setAIMoveSpeed(this.onGround ? moveSpeed + (controller.isSprinting() && this.getEnergy() > this.getWeaknessEnergy() ? moveSpeed * 0.25f : 0) : 2);

                this.moveRelative(strafe, this.isUsingSwimControls() ? vertical : 0, forward, 0.02f);
                this.motionX *= 0.8D;
                this.motionY *= 0.8D;
                this.motionZ *= 0.8D;
                this.move(MoverType.SELF, this.motionX, (this.isUsingSwimControls() || this.currentCreatureMove() != null && this.currentCreatureMove().moveAnimType == CreatureMove.MoveAnimType.CHARGE) ? this.motionY : 0, this.motionZ);

                if (this.isAmphibious() && forward > 0) {
                    if (this.getBodyHitbox() != null && this.getBodyHitbox().isInWater()) {
                        if (this.posY >= RiftUtil.highestWaterPos(this) - 2 && this.posY <= RiftUtil.highestWaterPos(this) + 2) {
                            double xMove = this.width * Math.sin(-Math.toRadians(this.rotationYaw));
                            double zMove = this.width * Math.cos(Math.toRadians(this.rotationYaw));

                            //if creature is in water, is breaching, and if bottom of their body hitbox is in front of
                            //solid block with air on top, it will leave water
                            BlockPos aheadBodyHitbox = new BlockPos(this.posX + xMove, RiftUtil.highestWaterPos(this), this.posZ + zMove);
                            BlockPos aboveAheadBodyHitbox = aheadBodyHitbox.up();
                            if (this.world.getBlockState(aheadBodyHitbox).getMaterial().isSolid() && !this.world.getBlockState(aboveAheadBodyHitbox).getMaterial().isSolid()) {
                                RiftMessages.WRAPPER.sendToServer(new RiftForceChangePos(this, this.posX + xMove, RiftUtil.highestWaterPos(this) + 1.0, this.posZ + zMove));
                            }

                            //if creature is in water, is breaching, and if bottom of their main collision hitbox is in front of
                            //solid block with water on top, it will go there
                            BlockPos aheadMainHitbox = new BlockPos(this.posX + xMove, this.posY, this.posZ + zMove);
                            BlockPos aboveAheadMainHitbox = aheadMainHitbox.up();
                            if (this.world.getBlockState(aheadMainHitbox).getMaterial().isSolid() && this.world.getBlockState(aboveAheadMainHitbox).getMaterial().isLiquid()) {
                                RiftMessages.WRAPPER.sendToServer(new RiftForceChangePos(this, this.posX + xMove, RiftUtil.highestWaterPos(this) + 1.0, this.posZ + zMove));
                            }
                        }
                    }
                }
            }
            else super.travel(strafe, vertical, forward);
        }
        else {
            this.stepHeight = 1f;
            if (this.isInWater() && !this.getIsCharging()) {
                this.fallDistance = 0;

                if (this.getNavigator().noPath()) {
                    this.moveRelative(0, 0, 0, 0.02f);
                    this.motionX = 0;
                    this.motionY = 0;
                    this.motionZ = 0;
                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                }
                else {
                    this.moveRelative(strafe, vertical, forward, 0.02f);
                    this.motionX *= 0.8;
                    this.motionY *= 0.8;
                    this.motionZ *= 0.8;
                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                }
            }
            else super.travel(strafe, vertical, forward);
        }
    }
}
