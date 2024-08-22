package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockLeadPoweredCrank;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.StegosaurusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.List;

public class Stegosaurus extends RiftCreature implements IAnimatable, IRangedAttackMob, ILeadWorkstationUser, IHarvestWhenWandering, IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/stegosaurus"));
    private static final DataParameter<Boolean> STRONG_ATTACKING = EntityDataManager.<Boolean>createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_LEAD_FOR_WORK = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEAD_WORK_X_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Y_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Z_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    public int strongAttackCharge;
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart leftBackLegPart;
    private RiftCreaturePart rightBackLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Stegosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.STEGOSAURUS);
        this.setSize(2.125f, 2.5f);
        this.favoriteFood = ((StegosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((StegosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.175D;
        this.isRideable = true;
        this.attackWidth = 7.5f;
        this.rangedWidth = 12f;
        this.strongAttackCharge = 0;
        this.saddleItem = ((StegosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STRONG_ATTACKING, false);
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
        this.dataManager.register(USING_LEAD_FOR_WORK, false);
        this.dataManager.register(LEAD_WORK_X_POS, 0);
        this.dataManager.register(LEAD_WORK_Y_POS, 0);
        this.dataManager.register(LEAD_WORK_Z_POS, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTurretModeTargeting(this, true));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftUseLeadPoweredCrank(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftResetAnimatedPose(this, 0.56F, 1));
        this.tasks.addTask(3, new RiftRangedAttack(this, false, 1.0D, 1.52F, 1.04F));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.96F, 0.36F));
        this.tasks.addTask(3, new RiftStegosaurusControlledStrongAttack(this, 0.72F, 0.12F));
        this.tasks.addTask(4, new RiftAttack(this, 1.0D, 0.96F, 0.36F));
        this.tasks.addTask(5, new RiftHarvestOnWander(this, 0.96F, 0.36F));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(8, new RiftHerdMemberFollow(this));
        this.tasks.addTask(9, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(10, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanStrongAttack();
        this.manageCanControlledPlateFling();
    }

    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 3.125f, 0, 1.1f, 0.6f * scale, 0.5f * scale, 1.5f);
            this.bodyPart = new RiftCreaturePart(this, 0.55f, 0, 0.8f, 0.875f * scale, 0.75f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 2.1f, 0, 1f, 0.4f * scale, 0.5f * scale, 1.5f);
            this.hipPart = new RiftCreaturePart(this, -1.2f, 0, 0.8f, 0.875f * scale, 0.9f * scale, 1f);
            this.leftBackLegPart = new RiftCreaturePart(this, 1.5f, -150, 0f, 0.5f * scale, scale, 0.5f);
            this.rightBackLegPart = new RiftCreaturePart(this, 1.5f, 150, 0f, 0.5f * scale, scale, 0.5f);
            this.tail0Part = new RiftCreaturePart(this, -2.5f, 0, 1f, 0.6f * scale, 0.6f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -3.5f, 0, 1f, 0.4f * scale, 0.5f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -4.25f, 0, 1f, 0.4f * scale, 0.5f * scale, 0.5f);
            this.tail3Part = new RiftCreaturePart(this, -5.25f, 0, 1f, 0.6f * scale, 0.5f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.hipPart != null) this.hipPart.onUpdate();
        if (this.leftBackLegPart != null) this.leftBackLegPart.onUpdate();
        if (this.rightBackLegPart != null) this.rightBackLegPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();
        if (this.tail3Part != null) this.tail3Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.5f : 0.25f;
        float tail1SitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.6f : 0.5f;
        float tail2SitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.8f : 0.5f;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + tail1SitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + tail2SitOffset, this.tail2Part.posZ);
        if (this.tail3Part != null) this.tail3Part.setPositionAndUpdate(this.tail3Part.posX, this.tail3Part.posY + tail2SitOffset, this.tail3Part.posZ);
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.neckPart != null) {
            this.world.removeEntityDangerously(this.neckPart);
            this.neckPart = null;
        }
        if (this.hipPart != null) {
            this.world.removeEntityDangerously(this.hipPart);
            this.hipPart = null;
        }
        if (this.leftBackLegPart != null) {
            this.world.removeEntityDangerously(this.leftBackLegPart);
            this.leftBackLegPart = null;
        }
        if (this.rightBackLegPart != null) {
            this.world.removeEntityDangerously(this.rightBackLegPart);
            this.rightBackLegPart = null;
        }
        if (this.tail0Part != null) {
            this.world.removeEntityDangerously(this.tail0Part);
            this.tail0Part = null;
        }
        if (this.tail1Part != null) {
            this.world.removeEntityDangerously(this.tail1Part);
            this.tail1Part = null;
        }
        if (this.tail2Part != null) {
            this.world.removeEntityDangerously(this.tail2Part);
            this.tail2Part = null;
        }
        if (this.tail3Part != null) {
            this.world.removeEntityDangerously(this.tail3Part);
            this.tail3Part = null;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
        this.writeLeadWorkDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
        this.readLeadWorkDataFromNBT(compound);
    }

    private void manageCanStrongAttack() {
        if (this.getLeftClickCooldown() > 0) this.setLeftClickCooldown(this.getLeftClickCooldown() - 1);
    }

    private void manageCanControlledPlateFling() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
    }

    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - thrownStegoPlate.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        thrownStegoPlate.setVariant(this.getVariant());
        thrownStegoPlate.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 5F);
        thrownStegoPlate.setDamage(4D + (double)(this.getLevel())/10D);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(thrownStegoPlate);
    }

    public void setSwingingArms(boolean swingingArms) {}

    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-1) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-1) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + 0.6, zOffset);
    }

    @Override
    public boolean canBeAttachedForWork() {
        return GeneralConfig.canUseMM();
    }

    public boolean isAttachableForWork(BlockPos pos) {
        Block block = this.world.getBlockState(pos).getBlock();
        if (GeneralConfig.canUseMM()) {
            if (block instanceof BlockLeadPoweredCrank) return true;
        }
        return false;
    }

    public int pullPower() {
        return 15;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return super.shouldRender(camera) || this.inFrustrum(camera, this.neckPart) || this.inFrustrum(camera, this.hipPart) || this.inFrustrum(camera, this.leftBackLegPart) || this.inFrustrum(camera, this.rightBackLegPart) || this.inFrustrum(camera, this.tail0Part) || this.inFrustrum(camera, this.tail1Part) || this.inFrustrum(camera, this.tail2Part) || this.inFrustrum(camera, this.tail3Part);
    }

    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (this.getLeftClickCooldown() == 0) {
                    if (target == null) {
                        if (!this.isActing()) {
                            if (holdAmount <= 10)  this.setAttacking(true);
                            else {
                                this.setIsStrongAttacking(true);
                                this.strongAttackCharge = RiftUtil.clamp(holdAmount, 10, 100);
                                this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                            }
                        }
                    }
                    else {
                        if (!this.isActing()) {
                            this.ssrTarget = target;
                            if (holdAmount <= 10) this.setAttacking(true);
                            else {
                                this.setIsStrongAttacking(true);
                                this.strongAttackCharge = RiftUtil.clamp(holdAmount, 10, 100);
                                this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                            }
                        }
                    }
                }
                this.setLeftClickUse(0);
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("prift.notify.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6) {
                if (this.getRightClickCooldown() == 0) {
                    if (!this.isActing()) {
                        this.setActing(true);
                        this.controlRangedAttack(RiftUtil.clamp(holdAmount, 0, 100));
                        this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                        this.setEnergy(this.getEnergy() - (int)(0.09D * (double)holdAmount + 1D));
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("prift.notify.insufficient_energy", this.getName()), false);
        }
    }

    @Override
    public List<String> blocksToHarvest() {
        return ((StegosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.harvestableBlocks;
    }

    public int harvestRange() {
        return 5;
    }

    public void setHarvesting(boolean value) {
        this.setAttacking(value);
    }

    public boolean isHarvesting() {
        return this.isAttacking();
    }

    public void setCanHarvest(boolean value) {
        this.dataManager.set(CAN_HARVEST, value);
    }

    public boolean canHarvest() {
        return this.dataManager.get(CAN_HARVEST);
    }

    public boolean isUsingLeadForWork() {
        return this.dataManager.get(USING_LEAD_FOR_WORK);
    }

    public void setLeadAttachPos(double x, double y, double z) {
        this.dataManager.set(USING_LEAD_FOR_WORK, true);
        this.dataManager.set(LEAD_WORK_X_POS, (int)x);
        this.dataManager.set(LEAD_WORK_Y_POS, (int)y);
        this.dataManager.set(LEAD_WORK_Z_POS, (int)z);
    }

    public BlockPos getLeadWorkPos() {
        return new BlockPos(this.dataManager.get(LEAD_WORK_X_POS), this.dataManager.get(LEAD_WORK_Y_POS), this.dataManager.get(LEAD_WORK_Z_POS));
    }

    public void clearLeadAttachPos(boolean destroyed) {
        this.dataManager.set(USING_LEAD_FOR_WORK, false);
        this.dataManager.set(LEAD_WORK_X_POS, 0);
        this.dataManager.set(LEAD_WORK_Y_POS, 0);
        this.dataManager.set(LEAD_WORK_Z_POS, 0);
        EntityPlayer player = (EntityPlayer)this.getOwner();
        if (!this.world.isRemote) this.clearLeadAttachPosMessage(destroyed, player);
    }

    @Override
    public AxisAlignedBB breakRange() {
        return new AxisAlignedBB(-1, 0, -1, 1, 0, 1);
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return true;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return true;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    public boolean canDoTurretMode() {
        return true;
    }

    public void strongControlAttack() {
        EntityLivingBase target;
        if (this.ssrTarget == null) target = this.getControlAttackTargets(this.attackWidth);
        else target = this.ssrTarget;
        if (target != null) {
            if (this.isTamed() && target instanceof EntityPlayer) {
                if (!target.getUniqueID().equals(this.getOwnerId())) this.attackEntityAsMobStrong(target);
            }
            else if (this.isTamed() && target instanceof EntityTameable) {
                if (((EntityTameable) target).isTamed()) {
                    if (!((EntityTameable) target).getOwner().equals(this.getOwner())) this.attackEntityAsMobStrong(target);
                }
                else this.attackEntityAsMobStrong(target);
            }
            else this.attackEntityAsMobStrong(target);
        }

        //break blocks
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        int height = (int)(Math.ceil(this.height)) + (this.isBeingRidden() ? (this.getControllingPassenger() != null ? (int)(Math.ceil(this.getControllingPassenger().height)) : 0) : 0);
        int radius = (int)(Math.ceil(this.width)) + this.forcedBreakBlockRad;
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos tempPos = pos.add(x, y, z);
                    IBlockState iblockstate = this.world.getBlockState(tempPos);
                    Block block = iblockstate.getBlock();
                    if (iblockstate.getMaterial() != Material.AIR && this.checkBasedOnStrength(block, iblockstate)) {
                        this.world.destroyBlock(tempPos, true);
                    }
                }
            }
        }
    }

    @Override
    public void controlRangedAttack(double strength) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(this.world, this, (EntityPlayer)this.getControllingPassenger());
        thrownStegoPlate.setDamage(strength * 0.04D + 4D + (double)(this.getLevel())/10D);
        thrownStegoPlate.setIsCritical(strength >= 50);
        thrownStegoPlate.setVariant(this.getVariant());
        float velocity = (float) strength * 0.015f + 1.5f;
        thrownStegoPlate.shoot(this, this.rotationPitch, this.rotationYaw, 0.0F, velocity, 1.0F);
        this.world.spawnEntity(thrownStegoPlate);
    }

    private boolean attackEntityAsMobStrong(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), ((float) this.strongAttackCharge - 100f)/3f + 30f + (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if (flag) {
            RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entityIn, RiftEntityProperties.class);
            this.applyEnchantments(this, entityIn);
            if (((StegosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.canInflictBleed) properties.setBleeding(0, 200);
        }
        return flag;
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public RiftCreature getHerder() {
        return this;
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
    }

    public int getHerdSize() {
        return this.herdSize;
    }

    public void setHerdSize(int value) {
        this.herdSize = value;
    }

    public double followRange() {
        return 4D;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 2.125f);
    }

    public boolean isStrongAttacking() {
        return this.dataManager.get(STRONG_ATTACKING);
    }

    public void setIsStrongAttacking(boolean value) {
        this.dataManager.set(STRONG_ATTACKING, value);
        this.setUsingLeftClick(value);
        this.setActing(value);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::stegosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::stegosaurusAttack));
        data.addAnimationController(new AnimationController(this, "controlled_plate_fling", 0, this::stegosaurusControlledPlateFling));
    }

    private <E extends IAnimatable> PlayState stegosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState stegosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.attack", false));
        }
        else if (this.isStrongAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.strong_attack", false));
        }
        else if (this.isRangedAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.plate_fling", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState stegosaurusControlledPlateFling(AnimationEvent<E> event) {
        if (this.getRightClickCooldown() == 0) {
            if (this.getRightClickUse() > 0 && this.getRightClickUse() < 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.use_plate_fling_p1", false));
            else if (this.getRightClickUse() >= 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.use_plate_fling_p1_hold", true));
        }
        else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stegosaurus.use_plate_fling_p2", false));
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.STEGOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.STEGOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.STEGOSAURUS_DEATH;
    }
}
