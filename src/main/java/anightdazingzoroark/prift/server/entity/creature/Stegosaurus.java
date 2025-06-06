package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockLeadPoweredCrank;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.*;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.world.World;

import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Stegosaurus extends RiftCreature implements ILeadWorkstationUser, IHarvestWhenWandering, ITurretModeUser, IWorkstationUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/stegosaurus"));
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_LEAD_FOR_WORK = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEAD_WORK_X_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Y_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Z_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> TURRET_MODE = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> TURRET_TARGET = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> USING_WORKSTATION = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WORKSTATION_X_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Y_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Z_POS = EntityDataManager.createKey(Stegosaurus.class, DataSerializers.VARINT);
    public int strongAttackCharge;
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart leftBackLegPart;
    private RiftCreaturePart rightBackLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;

    public Stegosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.STEGOSAURUS);
        this.setSize(2.125f, 2.5f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.175D;
        this.isRideable = true;
        this.strongAttackCharge = 0;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 3.125f, 0, 1.1f, 0.6f, 0.5f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0.55f, 0, 0.8f, 0.875f, 0.75f, 1f);
        this.neckPart = new RiftCreaturePart(this, 2.1f, 0, 1f, 0.4f, 0.5f, 1.5f);
        this.hipPart = new RiftCreaturePart(this, -1.2f, 0, 0.8f, 0.875f, 0.9f, 1f);
        this.leftBackLegPart = new RiftCreaturePart(this, 1.5f, -150, 0f, 0.5f, 1f, 0.5f);
        this.rightBackLegPart = new RiftCreaturePart(this, 1.5f, 150, 0f, 0.5f, 1f, 0.5f);
        this.tail0Part = new RiftCreaturePart(this, -2.5f, 0, 1f, 0.6f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -3.5f, 0, 1f, 0.4f, 0.5f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -4.25f, 0, 1f, 0.4f, 0.5f, 0.5f);
        this.tail3Part = new RiftCreaturePart(this, -5.25f, 0, 1f, 0.6f, 0.5f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.hipPart,
            this.leftBackLegPart,
            this.rightBackLegPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part,
            this.tail3Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
        this.dataManager.register(USING_LEAD_FOR_WORK, false);
        this.dataManager.register(LEAD_WORK_X_POS, 0);
        this.dataManager.register(LEAD_WORK_Y_POS, 0);
        this.dataManager.register(LEAD_WORK_Z_POS, 0);
        this.dataManager.register(TURRET_MODE, false);
        this.dataManager.register(TURRET_TARGET, (byte) TurretModeTargeting.HOSTILES.ordinal());
        this.dataManager.register(USING_WORKSTATION, false);
        this.dataManager.register(WORKSTATION_X_POS, 0);
        this.dataManager.register(WORKSTATION_Y_POS, 0);
        this.dataManager.register(WORKSTATION_Z_POS, 0);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTurretModeTargeting(this, true));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftUseLeadPoweredCrank(this));
        this.tasks.addTask(1, new RiftCreatureOperateWorkstation(this));
        this.tasks.addTask(2, new RiftMate(this));
        this.tasks.addTask(3, new RiftLandDwellerSwim(this));

        this.tasks.addTask(4, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(5, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(6, new RiftCreatureUseMoveUnmounted(this));

        this.tasks.addTask(7, new RiftHarvestOnWander(this, 0.96F, 0.36F));
        this.tasks.addTask(8, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(9, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(10, new RiftHerdMemberFollow(this));
        this.tasks.addTask(11, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.5f : 0.25f;
        float tail1SitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.6f : 0.5f;
        float tail2SitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.8f : 0.5f;
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
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
        this.writeLeadWorkDataToNBT(compound);
        this.writeTurretModeDataToNBT(compound);
        this.writeWorkstationDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
        this.readLeadWorkDataFromNBT(compound);
        this.readTurretModeDataFromNBT(compound);
        this.readWorkstationDataFromNBT(compound);
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.PLATE_FLING, CreatureMove.THAGOMIZE));
        possibleMoves.add(1, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.PLATE_FLING, CreatureMove.PLATE_SCATTER));
        return possibleMoves;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 7.5f;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.TAIL, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(10D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(17.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_TAIL_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.RANGED, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(10D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(7.5D)
                .setChargeUpToUseSound(RiftSounds.STEGOSAURUS_RANGED_MOVE)
                .finalizePoints());
        return moveMap;
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
        return GeneralConfig.canUseMM() && block instanceof BlockLeadPoweredCrank;
    }

    public int pullPower() {
        return 15;
    }

    @Override
    public Map<String, Boolean> getWorkstations() {
        Map<String, Boolean> workstations = new HashMap<>();
        if (GeneralConfig.canUsePyrotech()) {
            workstations.put("pyrotech:chopping_block", true);
        }
        return workstations;
    }

    @Override
    public BlockPos workstationUseFromPos() {
        return this.getWorkstationPos();
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

    public boolean hasWorkstation() {
        return this.dataManager.get(USING_WORKSTATION);
    }

    public BlockPos getWorkstationPos() {
        return new BlockPos(this.dataManager.get(WORKSTATION_X_POS), this.dataManager.get(WORKSTATION_Y_POS), this.dataManager.get(WORKSTATION_Z_POS));
    }

    @Override
    public List<String> blocksToHarvest() {
        return RiftConfigHandler.getConfig(this.creatureType).general.harvestableBlocks;
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

    public boolean canBeKnockedBack() {
        return true;
    }

    @Override
    public boolean isTurretMode() {
        return this.dataManager.get(TURRET_MODE);
    }

    @Override
    public void setTurretMode(boolean value) {
        this.dataManager.set(TURRET_MODE, value);
    }

    public TurretModeTargeting getTurretTargeting() {
        return TurretModeTargeting.values()[this.dataManager.get(TURRET_TARGET).byteValue()];
    }
    public void setTurretModeTargeting(TurretModeTargeting turretModeTargeting) {
        this.dataManager.set(TURRET_TARGET, (byte) turretModeTargeting.ordinal());
    }

    @Override
    public double herdFollowRange() {
        return 4D;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 2.125f};
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
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

    public SoundEvent rangedAttackSound() {
        return SoundEvents.ENTITY_ARROW_SHOOT;
    }
}
