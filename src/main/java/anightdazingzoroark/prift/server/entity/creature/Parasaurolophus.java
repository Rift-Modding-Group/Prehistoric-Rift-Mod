package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockLeadPoweredCrank;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.ParasaurolophusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.machine.block.spi.BlockCombustionWorkerStoneBase;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi.TileCombustionWorkerStoneBase;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
import java.util.*;

public class Parasaurolophus extends RiftCreature implements IWorkstationUser, ILeadWorkstationUser, IHarvestWhenWandering, IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/parasaurolophus"));
    private static final DataParameter<Boolean> BLOWING = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_BLOW = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_WORKSTATION = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WORKSTATION_X_POS = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Y_POS = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Z_POS = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> USING_LEAD_FOR_WORK = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEAD_WORK_X_POS = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Y_POS = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Z_POS = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.VARINT);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Parasaurolophus(World worldIn) {
        super(worldIn, RiftCreatureType.PARASAUROLOPHUS);
        this.setSize(2f, 2f);
        this.favoriteFood = ((ParasaurolophusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((ParasaurolophusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.25D;
        this.isRideable = true;
        this.saddleItem = ((ParasaurolophusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 2.875f, 0, 1.48f, 0.625f, 0.5f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 1.375f, 0, 0.8f, 0.75f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 2.125f, 0, 1.125f, 0.5f, 0.7f, 1.5f);
        this.tail0Part = new RiftCreaturePart(this, -1.125f, 0, 1f, 0.4f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.625f, 0, 1.1f, 0.4f, 0.45f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -2.125f, 0, 1.05f, 0.4f, 0.45f, 0.5f);
        this.tail3Part = new RiftCreaturePart(this, -2.875f, 0, 1.1f, 0.5f, 0.35f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part,
            this.tail3Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BLOWING, false);
        this.dataManager.register(CAN_BLOW, true);
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
        this.dataManager.register(USING_WORKSTATION, false);
        this.dataManager.register(WORKSTATION_X_POS, 0);
        this.dataManager.register(WORKSTATION_Y_POS, 0);
        this.dataManager.register(WORKSTATION_Z_POS, 0);
        this.dataManager.register(USING_LEAD_FOR_WORK, false);
        this.dataManager.register(LEAD_WORK_X_POS, 0);
        this.dataManager.register(LEAD_WORK_Y_POS, 0);
        this.dataManager.register(LEAD_WORK_Z_POS, 0);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTurretModeTargeting(this, true));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftParasaurStokeCombustor(this));
        this.tasks.addTask(0, new RiftBlowIntoTurbine(this, 64f, 1.76f, 0.24f));
        this.tasks.addTask(0, new RiftUseLeadPoweredCrank(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftResetAnimatedPose(this, 1.52F, 1));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(4, new RiftParasaurolophusBlow(this));
        this.tasks.addTask(5, new RiftHarvestOnWander(this, 0.52F, 0.24F));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new RiftHerdDistanceFromOtherMembers(this, 1.5D));
        this.tasks.addTask(8, new RiftHerdMemberFollow(this));
        this.tasks.addTask(9, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(10, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanBlow();
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.6f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
        if (this.tail3Part != null) this.tail3Part.setPositionAndUpdate(this.tail3Part.posX, this.tail3Part.posY + sitOffset, this.tail3Part.posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
        this.writeWorkstationDataToNBT(compound);
        this.writeLeadWorkDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
        this.readWorkstationDataFromNBT(compound);
        this.readLeadWorkDataFromNBT(compound);
    }

    private void manageCanBlow() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanBlow(true);
    }

    //blowing stuff starts here
    public void useBlow(float strength) {
        this.useBlow(null, strength);
    }

    public void useBlow(Entity target, float strength) {
        RiftCreature thisCreature = this;
        if (target == null) {
            double dist = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX + 8D;
            Vec3d vec3d = this.getPositionEyes(1.0F);
            Vec3d vec3d1 = this.getLook(1.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
            double d1 = dist;
            Entity rider = this.getControllingPassenger();
            List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(5.0D, 5.0D, 5.0D), null);
            double d2 = d1;
            for (EntityLivingBase entity : list) {
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow((double) entity.getCollisionBorderSize() + 2F);
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

                if (entity != this && entity != rider) {
                    if (entity instanceof Parasaurolophus) {
                        if ((((Parasaurolophus)entity).isTamed() && !this.isTamed()) || (!((Parasaurolophus)entity).isTamed() && this.isTamed())) {
                            if (axisalignedbb.contains(vec3d)) {
                                if (d2 >= 0.0D) {
                                    this.parasaurKnockback(entity, strength);
                                    d2 = 0.0D;
                                }
                            }
                            else if (raytraceresult != null) {
                                double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                                if (d3 < d2 || d2 == 0.0D) {
                                    this.parasaurKnockback(entity, strength);
                                    d2 = d3;
                                }
                            }
                        }
                    }
                    else {
                        if (axisalignedbb.contains(vec3d)) {
                            if (d2 >= 0.0D) {
                                this.parasaurKnockback(entity, strength);
                                d2 = 0.0D;
                            }
                        }
                        else if (raytraceresult != null) {
                            double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                            if (d3 < d2 || d2 == 0.0D) {
                                this.parasaurKnockback(entity, strength);
                                d2 = d3;
                            }
                        }
                    }
                }
            }
        }
        else if (target instanceof EntityLivingBase) {
            AxisAlignedBB aabb = target.getEntityBoundingBox().grow(5);
            List<EntityLivingBase> entityList = this.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb, new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    return RiftUtil.checkForNoAssociations(thisCreature, input);
                }
            });
            for (EntityLivingBase entityLivingBase : entityList) this.parasaurKnockback(entityLivingBase, strength);
        }
    }

    public void parasaurKnockback(EntityLivingBase entity, float strength) {
        double d0 = this.posX - entity.posX;
        double d1 = this.posZ - entity.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        entity.knockBack(this, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
    }

    public void parsaurManualStokeHeater(float strength) {
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos tempPos = pos.add(x, 0, z);
                TileEntity tileEntity = this.world.getTileEntity(tempPos);
                if (tileEntity != null) {
                    if (tileEntity instanceof TileCombustionWorkerStoneBase) {
                        TileCombustionWorkerStoneBase stoked = (TileCombustionWorkerStoneBase) tileEntity;
                        if (stoked.hasFuel() && stoked.workerIsActive() && stoked.hasInput()) {
                            stoked.consumeAirflow(RiftUtil.clamp(0.04f * strength + 12f, 12, 16), false);
                        }
                    }
                }
            }
        }
    }
    //blowing stuff ends here

    public float attackWidth() {
        return 3.5f;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 0.35, this.posZ);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.forcedAttackTarget = target;
                    this.forcedBreakPos = pos;
                    this.setAttacking(true);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6) {
                if (this.canBlow() && !this.isActing()) {
                    this.setActing(true);
                    this.setCanBlow(false);
                    this.useBlow(target, RiftUtil.clamp(0.04f * holdAmount + 2f, 2f, 6f));
                    this.setEnergy(this.getEnergy() - (int)(0.05d * (double)Math.min(holdAmount, 100) + 1d));
                    this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                    this.playSound(RiftSounds.PARASAUROLOPHUS_BLOW, 2, 1);
                    if (GeneralConfig.canUsePyrotech()) this.parsaurManualStokeHeater(holdAmount);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
    }

    @Override
    public List<String> blocksToHarvest() {
        return ((ParasaurolophusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.harvestableBlocks;
    }

    public int harvestRange() {
        return 3;
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

    public boolean canDoTurretMode() {
        return true;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.5f);
    }

    @Override
    public boolean canUseWorkstation() {
        return GeneralConfig.canUsePyrotech() || GeneralConfig.canUseMM();
    }

    @Override
    public boolean isWorkstation(BlockPos pos) {
        Block block = this.world.getBlockState(pos).getBlock();
        if (GeneralConfig.canUsePyrotech()) {
            if (block instanceof BlockCombustionWorkerStoneBase) return true;
            if (block instanceof BlockBloomery) return true;
        }
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
        return this.isBlowing();
    }

    public void setUsingWorkAnim(boolean value) {
        this.setBlowing(value);
    }

    public SoundEvent useAnimSound() {
        return RiftSounds.PARASAUROLOPHUS_BLOW;
    }

    public void setUseWorkstation(double x, double y, double z) {
        this.dataManager.set(USING_WORKSTATION, true);
        this.dataManager.set(WORKSTATION_X_POS, (int)x);
        this.dataManager.set(WORKSTATION_Y_POS, (int)y);
        this.dataManager.set(WORKSTATION_Z_POS, (int)z);
    }

    public void clearWorkstation(boolean destroyed) {
        this.dataManager.set(USING_WORKSTATION, false);
        this.dataManager.set(WORKSTATION_X_POS, 0);
        this.dataManager.set(WORKSTATION_Y_POS, 0);
        this.dataManager.set(WORKSTATION_Z_POS, 0);
        EntityPlayer owner = (EntityPlayer) this.getOwner();
        if (!this.world.isRemote) this.clearWorkstationMessage(destroyed, owner);
    }

    public boolean isUsingWorkstation() {
        return this.dataManager.get(USING_WORKSTATION);
    }

    public BlockPos getWorkstationPos() {
        return new BlockPos(this.dataManager.get(WORKSTATION_X_POS), this.dataManager.get(WORKSTATION_Y_POS), this.dataManager.get(WORKSTATION_Z_POS));
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

    public boolean isBlowing() {
        return this.dataManager.get(BLOWING);
    }

    public void setBlowing(boolean value) {
        this.dataManager.set(BLOWING, value);
        this.setActing(value);
    }

    public boolean canBlow() {
        return this.dataManager.get(CAN_BLOW);
    }

    public void setCanBlow(boolean value) {
        this.dataManager.set(CAN_BLOW, value);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::parasaurolophusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::parasaurolophusAttack));
        data.addAnimationController(new AnimationController(this, "blow", 0, this::parasaurolophusBlow));
        data.addAnimationController(new AnimationController(this, "controlledBlow", 0, this::parasaurolophusControlledBlow));
    }

    private <E extends IAnimatable> PlayState parasaurolophusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget() && !this.isUsingWorkstation()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusBlow(AnimationEvent<E> event) {
        if (this.isBlowing()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.blow", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusControlledBlow(AnimationEvent<E> event) {
        if (this.getRightClickCooldown() == 0) {
            if (this.getRightClickUse() > 0 && this.getRightClickUse() < 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.use_blow_p1", false));
            else if (this.getRightClickUse() >= 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.use_blow_p1_hold", true));
        }
        else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.use_blow_p2", false));
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.PARASAUROLOPHUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.PARASAUROLOPHUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.PARASAUROLOPHUS_DEATH;
    }
}
