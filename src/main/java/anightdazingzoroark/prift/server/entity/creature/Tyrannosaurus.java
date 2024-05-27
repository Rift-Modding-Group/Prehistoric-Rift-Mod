package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.config.*;
import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.server.entity.*;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IApexPredator;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Tyrannosaurus extends RiftCreature implements IAnimatable, IApexPredator, IWorkstationUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/tyrannosaurus"));
    private static final Predicate<EntityLivingBase> WEAKNESS_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(GeneralConfig.apexAffectedBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && !blacklist.contains("minecraft:player") && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof IApexPredator) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
            else {
                if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && !(entity instanceof IApexPredator) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
        }
    };
    private static final Predicate<EntityLivingBase> WEAKNESS_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(GeneralConfig.apexAffectedBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && blacklist.contains("minecraft:player") && entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof IApexPredator) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
            }
            else return false;
        }
    };
    private static final Predicate<EntityLivingBase> ROAR_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(TyrannosaurusConfig.tyrannosaurusRoarTargetBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) return entity.isEntityAlive() && !blacklist.contains("minecraft:player");
                else return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
            }
            else return entity.isEntityAlive() && !(entity instanceof RiftEgg);
        }
    };
    private static final Predicate<EntityLivingBase> ROAR_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(TyrannosaurusConfig.tyrannosaurusRoarTargetBlacklist);

            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) return entity.isEntityAlive() && blacklist.contains("minecraft:player");
                else return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
            }
            else return false;
        }
    };
    private static final DataParameter<Boolean> ROARING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_ROAR = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    public int roarCooldownTicks;
    public int roarCharge;
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart leftLegPart;
    private RiftCreaturePart rightLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;

    public Tyrannosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.TYRANNOSAURUS);
        this.minCreatureHealth = TyrannosaurusConfig.getMinHealth();
        this.maxCreatureHealth = TyrannosaurusConfig.getMaxHealth();
        this.setSize(3.25f, 4f);
        this.forcedBreakBlockRad = 3;
        this.favoriteFood = TyrannosaurusConfig.tyrannosaurusFavoriteFood;
        this.tamingFood = TyrannosaurusConfig.tyrannosaurusBreedingFood;
        this.experienceValue = 50;
        this.speed = 0.20D;
        this.roarCooldownTicks = 0;
        this.roarCharge = 0;
        this.isRideable = true;
        this.attackWidth = 6.5f;
        this.saddleItem = TyrannosaurusConfig.tyrannosaurusSaddleItem;
        this.attackDamage = TyrannosaurusConfig.damage;
        this.healthLevelMultiplier = TyrannosaurusConfig.healthMultiplier;
        this.damageLevelMultiplier = TyrannosaurusConfig.damageMultiplier;
        this.densityLimit = TyrannosaurusConfig.tyrannosaurusDensityLimit;
        this.targetList = RiftUtil.creatureTargets(TyrannosaurusConfig.tyrannosaurusTargets, TyrannosaurusConfig.tyrannosaurusTargetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CAN_ROAR, true);
        this.dataManager.register(ROARING, false);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTyrannosaurusRoar(this));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftBlowIntoTurbine(this, 30f, 2.08f, 0.64f));
        this.tasks.addTask(1, new RiftLandDwellerSwim(this));
        this.tasks.addTask(2, new RiftMate(this));
        this.tasks.addTask(3, new RiftResetAnimatedPose(this, 1.68F, 1));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(4, new RiftAttack(this, 1.0D, 0.52F, 0.24F));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 6.0F, 2.0F));
        this.tasks.addTask(6, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(7, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanRoar();
        if (!this.isBaby()) this.manageApplyApexEffect();
    }

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.removeParts();
            this.oldScale = scale;
            this.headPart = new RiftCreaturePart(this, 3f, 0, 3f, 0.75f * scale, 0.5f * scale, 1.5f);
            this.bodyPart = new RiftCreaturePart(this, 0, 0, 2f, 0.75f * scale, 0.75f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 1.75f, 0, 2.75f, 0.5f * scale, 0.5f * scale, 1.5f);
            this.hipPart = new RiftCreaturePart(this, -2f, 0, 2f, 0.75f * scale, 0.75f * scale, 1);
            this.leftLegPart = new RiftCreaturePart(this, 2.875f, -156, 0, 0.4f * scale, 1.135f * scale, 0.5f);
            this.rightLegPart = new RiftCreaturePart(this, 2.875f, 156, 0, 0.4f * scale, 1.135f * scale, 0.5f);
            this.tail0Part = new RiftCreaturePart(this, -4.25f, 0, 2.25f, 0.5f * scale, 0.6f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -5.625f, 0, 2.25f, 0.5f * scale, 0.5f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -7f, 0, 2.25f, 0.5f * scale, 0.45f * scale, 0.5f);
            this.tail3Part = new RiftCreaturePart(this, -8.375f, 0, 2.25f, 0.5f * scale, 0.4f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.hipPart != null) this.hipPart.onUpdate();
        if (this.leftLegPart != null) this.leftLegPart.onUpdate();
        if (this.rightLegPart != null) this.rightLegPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();
        if (this.tail3Part != null) this.tail3Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -1f : 0.25f;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
        if (this.tail3Part != null) this.tail3Part.setPositionAndUpdate(this.tail3Part.posX, this.tail3Part.posY + sitOffset, this.tail3Part.posZ);
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
        if (this.leftLegPart != null) {
            this.world.removeEntityDangerously(this.leftLegPart);
            this.leftLegPart = null;
        }
        if (this.rightLegPart != null) {
            this.world.removeEntityDangerously(this.rightLegPart);
            this.rightLegPart = null;
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

    private void manageCanRoar() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanRoar(true);
    }

    @Override
    public void manageApplyApexEffect() {
        Predicate<EntityLivingBase> targetPredicate = GeneralConfig.apexAffectedWhitelist ? WEAKNESS_WHITELIST : WEAKNESS_BLACKLIST;
        for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEffectCastArea(), targetPredicate)) {
            if (this.isTamed() && entityLivingBase instanceof EntityPlayer) {
                if (!entityLivingBase.getUniqueID().equals(this.getOwnerId())) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
                }
            }
            else if (this.isTamed() && entityLivingBase instanceof EntityTameable) {
                if (((EntityTameable) entityLivingBase).isTamed()) {
                    if (!((EntityTameable) entityLivingBase).getOwnerId().equals(this.getOwnerId())) {
                        entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
                    }
                }
                else {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
                }
            }
            else if (!this.isTamed()) {
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
            }
        }
    }

    @Override
    public AxisAlignedBB getEffectCastArea() {
        return this.getEntityBoundingBox().grow(16.0D, 16.0D, 16.0D);
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.5f, 3.25f);
    }

    //stuff below this comment is for roar stuff
    public void roar(float strength) {
        Predicate<EntityLivingBase> targetPredicate = TyrannosaurusConfig.tyrannosaurusRoarTargetsWhitelist ? ROAR_WHITELIST : ROAR_BLACKLIST;
        for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getRoarArea((double)strength * 6d), targetPredicate)) {
            if (entity != this) {
                if (this.isTamed() && entity instanceof EntityTameable) {
                    if (((EntityTameable) entity).isTamed()) {
                        if (!((EntityTameable) entity).getOwner().equals(this.getOwner())) {
                            entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
                            this.roarKnockback(entity, strength);
                        }
                    }
                    else {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
                        this.roarKnockback(entity, strength);
                    }
                }
                else if (this.isTamed() && entity instanceof EntityPlayer) {
                    if (!this.getOwner().equals(entity)) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
                        this.roarKnockback(entity, strength);
                    }
                }
                else {
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this), 8f * strength / 3f - 2f);
                    this.roarKnockback(entity, strength);
                }
            }
        }
        this.roarBreakBlocks(strength);
    }

    private void roarKnockback(EntityLivingBase target, float strength) {
        double d0 = this.posX - target.posX;
        double d1 = this.posZ - target.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        target.knockBack(this, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
    }

    protected AxisAlignedBB getRoarArea(double targetDistance) {
        return this.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    private void roarBreakBlocks(float strength) {
        List<BlockPos> affectedBlockPositions = Lists.<BlockPos>newArrayList();
        boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this);
        if (canBreak) {
            if (!this.world.isRemote) {
                Set<BlockPos> set = Sets.<BlockPos>newHashSet();
                for (int j = 0; j < 16; ++j) {
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                                double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                                double d1 = Math.abs((double)((float)k / 15.0F * 2.0F - 1.0F));
                                double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                                d0 = d0 / d3;
                                d1 = d1 / d3;
                                d2 = d2 / d3;
                                float f = (strength * 4) * (0.7F + this.world.rand.nextFloat() * 0.6F);
                                double d4 = this.posX;
                                double d6 = this.posY;
                                double d8 = this.posZ;

                                for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                    BlockPos blockpos = new BlockPos(d4, d6, d8);
                                    IBlockState iblockstate = this.world.getBlockState(blockpos);
                                    Block block = iblockstate.getBlock();

                                    if (iblockstate.getMaterial() != Material.AIR) {
                                        if (this.checkBasedOnStrength(block, iblockstate)) f -= 0.24F;
                                        else f -= (1200F + 0.3F) * 0.3F;

                                        if (f > 0.0F) set.add(blockpos);
                                    }

                                    d4 += d0 * 0.30000001192092896D;
                                    d6 += d1 * 0.30000001192092896D;
                                    d8 += d2 * 0.30000001192092896D;
                                }
                            }
                        }
                    }
                }
                affectedBlockPositions.addAll(set);
                for (BlockPos blockPos : affectedBlockPositions) this.world.destroyBlock(blockPos, false);
            }
        }
    }
    //end of roar stuff

    @Override
    public boolean canUseWorkstation() {
        return GeneralConfig.canUseMM();
    }

    @Override
    public boolean isWorkstation(BlockPos pos) {
        Block block = this.world.getBlockState(pos).getBlock();
        if (GeneralConfig.canUseMM()) {
            if (block instanceof BlockBlowPoweredTurbine) return true;
        }
        return false;
    }

    @Override
    public BlockPos workstationUseFromPos() {
        IBlockState blockState = this.world.getBlockState(this.getWorkstationPos());
        int downF = 0;
        if (GeneralConfig.canUseMM()) {
            TileEntity te = this.world.getTileEntity(this.getWorkstationPos());
            if (te != null) downF = te instanceof TileEntityBlowPoweredTurbine ? -1 : 0;
        }
        if (blockState.getMaterial().isSolid()) {
            EnumFacing direction = blockState.getValue(BlockHorizontal.FACING);
            switch (direction) {
                case NORTH:
                    return this.getWorkstationPos().add(0, downF, -4);
                case SOUTH:
                    return this.getWorkstationPos().add(0, downF, 4);
                case EAST:
                    return this.getWorkstationPos().add(4, downF, 0);
                case WEST:
                    return this.getWorkstationPos().add(-4, downF, 0);
            }
        }
        return null;
    }

    public boolean isUsingWorkAnim() {
        return this.isRoaring();
    }

    public void setUsingWorkAnim(boolean value) {
        this.setRoaring(value);
    }

    public SoundEvent useAnimSound() {
        return RiftSounds.TYRANNOSAURUS_ROAR;
    }

    public void setRoaring(boolean value) {
        this.dataManager.set(ROARING, Boolean.valueOf(value));
        this.setActing(value);
    }

    public boolean isRoaring() {
        return this.dataManager.get(ROARING);
    }

    public void setCanRoar(boolean value) {
        this.dataManager.set(CAN_ROAR, Boolean.valueOf(value));
    }

    public boolean canRoar() {
        return this.dataManager.get(CAN_ROAR);
    }

    public boolean isTameableByFeeding() {
        return false;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 54;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY + 2.125, this.posZ);
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return super.shouldRender(camera) || this.inFrustrum(camera, this.neckPart) || this.inFrustrum(camera, this.hipPart) || this.inFrustrum(camera, this.leftLegPart) || this.inFrustrum(camera, this.rightLegPart) || this.inFrustrum(camera, this.tail0Part) || this.inFrustrum(camera, this.tail1Part) || this.inFrustrum(camera, this.tail2Part) || this.inFrustrum(camera, this.tail3Part);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing()) this.setAttacking(true);
                }
                else {
                    if (!this.isActing()) {
                        this.ssrTarget = target;
                        this.setAttacking(true);
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6) {
                if (this.canRoar() && !this.isActing()) {
                    this.setActing(true);
                    this.setCanRoar(false);
                    this.roar(0.015f * Math.min(holdAmount, 100) + 1.5f);
                    this.setEnergy(this.getEnergy() - (int)(0.06d * (double)Math.min(holdAmount, 100) + 6d));
                    this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                    this.playSound(RiftSounds.TYRANNOSAURUS_ROAR, 2, 1);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return true;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::tyrannosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attacking", 0, this::tyrannosaurusAttack));
        data.addAnimationController(new AnimationController(this, "roaring", 0, this::tyrannosaurusRoar));
        data.addAnimationController(new AnimationController(this, "controlled_roar", 0, this::tyrannosaurusControlledRoar));
    }

    private <E extends IAnimatable> PlayState tyrannosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState tyrannosaurusLook(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.look", true));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState tyrannosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.attack", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState tyrannosaurusRoar(AnimationEvent<E> event) {
        if (this.isRoaring()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.roar", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState tyrannosaurusControlledRoar(AnimationEvent<E> event) {
        if (this.getRightClickCooldown() == 0) {
            if (this.getRightClickUse() > 0 && this.getRightClickUse() < 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.use_roar_p1", false));
            else if (this.getRightClickUse() >= 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.use_roar_p1_hold", true));
        }
        else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.use_roar_p2", false));
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.TYRANNOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.TYRANNOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.TYRANNOSAURUS_DEATH;
    }
}
