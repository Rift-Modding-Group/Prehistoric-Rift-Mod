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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
    private final PathNavigateRiftWaterCreature waterNavigate;
    private final PathNavigateGround landNavigate;
    private boolean amphibiousInWater;

    public RiftWaterCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn, creatureType);
        this.moveHelper = new RiftWaterCreatureMoveHelper(this);
        this.waterNavigate = new PathNavigateRiftWaterCreature(this, this.world);
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
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 0, settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 1, !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown()));

                RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, 0, RiftControls.mountAscend.isKeyDown() && !RiftControls.mountDescend.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, 1, RiftControls.mountDescend.isKeyDown() && !RiftControls.mountAscend.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftHoverChangeControl(this, 2, RiftControls.mountDescend.isKeyDown() || RiftControls.mountAscend.isKeyDown()));

                if (settings.keyBindAttack.isKeyDown() && !this.isActing() && this.getLeftClickCooldown() == 0) {
                    if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) {
                        if (ShoulderInstance.getInstance().doShoulderSurfing()) {
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
                    if (this.hasRightClickChargeBar()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 1));
                    }
                    else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 1, 0));
                }
                else if (!settings.keyBindUseItem.isKeyDown() && !this.canUseRightClick()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(this, 1, true));
                }
                else if (!settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown()) {
                    Entity toBeAttacked = null;
                    if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
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
                }
            }
        }
    }

    public void updateParts() {
        if (this.headPart != null) this.headPart.onUpdate();
    }

    public void removeParts() {
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

    public boolean canFlop() {
        return false;
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

    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateRiftWaterCreature(this, this.world);
    }

    @Override
    public boolean getCanSpawnHere() {
        return true;
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
}
