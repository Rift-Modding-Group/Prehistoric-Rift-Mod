package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.SSRCompatUtils;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftClimber;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftWaterCreature;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftWaterCreatureMoveHelper;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.message.*;
import com.google.common.base.Predicate;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RiftWaterCreature extends RiftCreature {
    private static final DataParameter<Boolean> USING_SWIM_CONTROLS = EntityDataManager.createKey(RiftWaterCreature.class, DataSerializers.BOOLEAN);
    protected final float defaultWaterCost;
    private final PathNavigateSwimmer waterNavigate;
    private final PathNavigateGround landNavigate;
    private boolean amphibiousInWater;
    public RiftCreaturePart bodyPart;

    public RiftWaterCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn, creatureType);
        this.moveHelper = new RiftWaterCreatureMoveHelper(this);
        this.waterNavigate = new PathNavigateSwimmer(this, this.world);
        this.landNavigate = new PathNavigateGround(this, this.world);
        this.amphibiousInWater = true;
        if (!this.isAmphibious()) {
            this.setPathPriority(PathNodeType.WATER, 0f);
            this.defaultWaterCost = 0F;
        }
        else this.defaultWaterCost = this.getPathPriority(PathNodeType.WATER);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(USING_SWIM_CONTROLS, false);
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

    @SideOnly(Side.CLIENT)
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (this.isBeingRidden()) {
            if (this.getControllingPassenger().equals(player)) {
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 0, settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 1, !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 3, !settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && settings.keyBindPickBlock.isKeyDown()));

                RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, 0, RiftControls.mountAscend.isKeyDown() && !RiftControls.mountDescend.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, 1, RiftControls.mountDescend.isKeyDown() && !RiftControls.mountAscend.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, 2, RiftControls.mountDescend.isKeyDown() || RiftControls.mountAscend.isKeyDown()));

                if (settings.keyBindAttack.isKeyDown() && !this.isActing() && this.getLeftClickCooldown() == 0) {
                    if (RiftUtil.isUsingSSR()) {
                        Entity toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                        if (this.hasLeftClickChargeBar()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                        }
                        else {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,0));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,0));
                            }
                        }
                    }
                    else {
                        if (this.hasLeftClickChargeBar()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                        }
                        else {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 0));
                        }
                    }
                }
                else if (settings.keyBindUseItem.isKeyDown() && !this.isActing() && this.getRightClickCooldown() == 0 && this.canUseRightClick() && !(player.getHeldItemMainhand().getItem() instanceof ItemFood) && !(player.getHeldItemMainhand().getItem() instanceof ItemMonsterPlacer) && !RiftUtil.checkInMountItemWhitelist(player.getHeldItemMainhand().getItem())) {
                    if (RiftUtil.isUsingSSR()) {
                        Entity toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                        if (this.hasRightClickChargeBar()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 1));
                        }
                        else {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,1));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,1));
                            }
                        }
                    }
                    else {
                        if (this.hasRightClickChargeBar()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 1));
                        }
                        else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, 0));
                    }
                }
                else if (!settings.keyBindUseItem.isKeyDown() && !this.canUseRightClick()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(this, 1, true));
                }
                else if (settings.keyBindPickBlock.isKeyDown() && !this.isActing()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 3));
                }
                else if (!settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()) {
                    Entity toBeAttacked = null;
                    if (RiftUtil.isUsingSSR()) toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                    if (this.hasLeftClickChargeBar()) {
                        if (this.getLeftClickUse() > 0) {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,0, this.getLeftClickUse()));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,0, this.getLeftClickUse()));
                            }
                        }
                    }
                    if (this.hasRightClickChargeBar()) {
                        if (this.getRightClickUse() > 0) {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId, 1, this.getRightClickUse()));
                            }
                            else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, this.getRightClickUse()));
                        }
                    }
                    if (this.getMiddleClickUse() > 0) {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 3));
                        this.setMiddleClickUse(0);
                    }
                }
            }
        }
    }

    public void updateParts() {
        if (this.bodyPart != null) this.bodyPart.onUpdate();
        if (this.headPart != null) this.headPart.onUpdate();
    }

    public void removeParts() {
        if (this.bodyPart != null) {
            this.world.removeEntityDangerously(this.bodyPart);
            this.bodyPart = null;
        }
        if (this.headPart != null) {
            this.world.removeEntityDangerously(this.headPart);
            this.headPart = null;
        }
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
            if (this.isInWater() && !this.amphibiousInWater) {
                this.navigator = this.waterNavigate;
                this.moveHelper = new RiftWaterCreatureMoveHelper(this);
                this.setPathPriority(PathNodeType.WATER, 0);
                this.amphibiousInWater = true;
                this.navigator.clearPath();
            }
            else if (!this.isInWater() && this.amphibiousInWater) {
                this.navigator = this.landNavigate;
                this.moveHelper = new EntityMoveHelper(this);
                this.setPathPriority(PathNodeType.WATER, this.defaultWaterCost);
                this.amphibiousInWater = false;
                this.navigator.clearPath();
            }
        }

        //for not sinkin in certain conditions
        if (this.isBeingRidden() && this.isInWater() && !this.isUsingSwimControls()) {
            this.motionY *= 0;
        }
        if (this.isInWater() && this.getTameStatus() == TameStatusType.SIT) {
            this.motionY *= 0;
        }
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return true;
    }

    @Override
    public boolean isInWater() {
        if (this.bodyPart != null) {
            return this.world.getBlockState(this.bodyPart.getPosition()).getMaterial() == Material.WATER;
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
        return new PathNavigateRiftWaterCreature(this, this.world);
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.testOtherCreatures();
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    //herding modifications
    public void followLeader() {
        if (this.hasHerdLeader()) {
            if (!this.getEntityBoundingBox().intersects(this.herdLeader.getEntityBoundingBox().grow(this.followRange()))) {
                this.navigator.tryMoveToEntityLiving(this.herdLeader, 1D);
            }
        }
    }

    public boolean isUsingSwimControls() {
        return this.dataManager.get(USING_SWIM_CONTROLS);
    }

    public void setUsingSwimControls(boolean value) {
        this.dataManager.set(USING_SWIM_CONTROLS, value);
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return this.inFrustrum(camera, this.headPart);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSaddled() && this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null && this.isInWater()) {
                this.rotationYaw = controller.rotationYaw;
                this.prevRotationYaw = this.rotationYaw;
                this.rotationPitch = controller.rotationPitch * 0.5f;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.renderYawOffset = this.rotationYaw;
                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;
                this.stepHeight = 1.0F;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                this.fallDistance = 0;
                float moveSpeedMod = (this.getEnergy() > 6 ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = ((float)(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) - riderSpeed) * moveSpeedMod;
                this.setAIMoveSpeed(this.onGround ? moveSpeed + (controller.isSprinting() && this.getEnergy() > 6 ? moveSpeed * 0.3f : 0) : 2);

                this.moveRelative(strafe, this.isUsingSwimControls() ? vertical : 0, forward, 0.05F);
                float f4 = 0.6F;
                float d0 = (float) EnchantmentHelper.getDepthStriderModifier(this);
                if (d0 > 3.0F) d0 = 3.0F;
                if (!this.onGround) d0 *= 0.5F;
                if (d0 > 0.0F) f4 += (0.54600006F - f4) * d0 / 3.0F;
                this.move(MoverType.SELF, this.motionX, this.isUsingSwimControls() ? this.motionY : 0, this.motionZ);
                this.motionX *= f4;
                this.motionX *= 0.900000011920929D;
                this.motionY *= 0.900000011920929D;
                this.motionY *= f4;
                this.motionZ *= 0.900000011920929D;
                this.motionZ *= f4;

                if (this.isAmphibious() && forward > 0) {
                    if (this.bodyPart != null) {
                        if (this.bodyPart.isInWater()) {
                            if (this.posY >= RiftUtil.highestWaterPos(this) - 2 && this.posY <= RiftUtil.highestWaterPos(this) + 2) {
                                double xMove = (this.width)*Math.sin(-Math.toRadians(this.rotationYaw));
                                double zMove = (this.width)*Math.cos(Math.toRadians(this.rotationYaw));
                                BlockPos ahead = new BlockPos(this.posX + xMove, RiftUtil.highestWaterPos(this), this.posZ + zMove);
                                BlockPos above = ahead.up();
                                if (this.world.getBlockState(ahead).getMaterial().isSolid() && !this.world.getBlockState(above).getMaterial().isSolid()) {
                                    RiftMessages.WRAPPER.sendToServer(new RiftForceChangePos(this, this.posX + xMove, RiftUtil.highestWaterPos(this) + 1.0, this.posZ + zMove));

                                }
                            }
                        }
                    }
                }
            }
            else super.travel(strafe, vertical, forward);
        }
        else {
            if (this.isInWater()) {
                if (this.getTameStatus() == TameStatusType.SIT || this.getNavigator().noPath()) {
                    super.travel(0, 0, 0);
                }
                else {
                    this.moveRelative(strafe, vertical, forward, 0.01f);
                    this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

                    this.motionX *= 0.9;
                    this.motionY *= 0.9;
                    this.motionZ *= 0.9;

                    if (this.getAttackTarget() == null) this.motionY -= 0.005;
                }
            }
            else super.travel(strafe, vertical, forward);
        }
    }
}
