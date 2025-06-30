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
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Triceratops extends RiftCreature implements IWorkstationUser, ILeadWorkstationUser, IHarvestWhenWandering {
    private static final DataParameter<Boolean> STOMPING = EntityDataManager.createKey(Triceratops.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Triceratops.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Triceratops.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOWER_HEAD = EntityDataManager.createKey(Triceratops.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_WORKSTATION = EntityDataManager.createKey(Triceratops.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WORKSTATION_X_POS = EntityDataManager.createKey(Triceratops.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Y_POS = EntityDataManager.createKey(Triceratops.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Z_POS = EntityDataManager.createKey(Triceratops.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> USING_LEAD_FOR_WORK = EntityDataManager.createKey(Triceratops.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEAD_WORK_X_POS = EntityDataManager.createKey(Triceratops.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Y_POS = EntityDataManager.createKey(Triceratops.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEAD_WORK_Z_POS = EntityDataManager.createKey(Triceratops.class, DataSerializers.VARINT);
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/triceratops"));
    private RiftCreaturePart hipPart;
    private RiftCreaturePart leftBackLegPart;
    private RiftCreaturePart rightBackLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Triceratops(World worldIn) {
        super(worldIn, RiftCreatureType.TRICERATOPS);
        this.setSize(2f, 2f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.15D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 2.875f, 0, 1.2f, 1f, 1.25f, 1f).setInvulnerable();
        this.bodyPart = new RiftCreaturePart(this, 1.125f, 0, 0.8f, 1f, 0.875f, 1f);
        this.hipPart = new RiftCreaturePart(this, -0.25f, 0, 0.7f, 1f, 1f, 1f);
        this.leftBackLegPart = new RiftCreaturePart(this, 0.9f, -115, 0, 0.6f, 1.125f, 0.5f);
        this.rightBackLegPart = new RiftCreaturePart(this, 0.9f, 115, 0, 0.6f, 1.125f, 0.5f);
        this.tail0Part = new RiftCreaturePart(this, -1.5f, 0, 0.8f, 0.8f, 0.75f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -2.5f, 0, 1f, 0.6f, 0.6f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -3.375f, 0, 0.9f, 0.6f, 0.5f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.hipPart,
            this.leftBackLegPart,
            this.rightBackLegPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STOMPING, false);
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
        this.dataManager.register(LOWER_HEAD, false);
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
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftUseLeadPoweredCrank(this));
        this.tasks.addTask(0, new RiftCreatureOperateWorkstation(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));

        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(5, new RiftCreatureUseMoveUnmounted(this));

        this.tasks.addTask(6, new RiftHarvestOnWander(this, 0.72F, 0.48F));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(8, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(9, new RiftHerdMemberFollow(this));
        this.tasks.addTask(10, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.65f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
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

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.STOMP, CreatureMove.CHARGE));
        possibleMoves.add(3, Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.TACKLE, CreatureMove.CHARGE));
        possibleMoves.add(1, Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.TACKLE, CreatureMove.BIDE));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.HEAD, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(10D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(7.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_HEAD_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.STOMP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(10D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(7.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_STOMP_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.CHARGE, new RiftCreatureMoveAnimator(this)
                .defineStartMoveDelayLength(5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpSound(RiftSounds.TRICERATOPS_CHARGE_UP, true)
                .setChargeUpToUseSound(RiftSounds.TRICERATOPS_CHARGE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.DEFENSE, this.currentCreatureMove() == CreatureMove.BIDE ?
                new RiftCreatureMoveAnimator(this)
                        .defineStartMoveDelayLength(5D)
                        .defineChargeUpToUseLength(2.5D)
                        .defineRecoverFromUseLength(7.5D)
                        .setChargeUpToUseSound(RiftSounds.TRICERATOPS_BIDE_RELEASE)
                        .finalizePoints()
                : null);
        return moveMap;
    }
    //move related stuff ends here

    @Override
    public float attackWidth() {
        return 5f;
    }

    @Override
    public float rangedWidth() {
        return 32f;
    }

    public boolean isStomping() {
        return this.dataManager.get(STOMPING);
    }

    public void setStomping(boolean value) {
        this.dataManager.set(STOMPING, value);
    }

    @Override
    public Map<String, Boolean> getWorkstations() {
        Map<String, Boolean> workstations = new HashMap<>();
        if (GeneralConfig.canUseMM()) {
            workstations.put("prift:semi_manual_extractor", true);
            workstations.put("prift:semi_manual_extractor_top", false);
            workstations.put("prift:semi_manual_presser", true);
            workstations.put("prift:semi_manual_presser_top", false);
            workstations.put("prift:semi_manual_extruder", true);
            workstations.put("prift:semi_manual_extruder_top", false);
            workstations.put("prift:semi_manual_hammerer", true);
            workstations.put("prift:semi_manual_hammerer_false", false);
        }
        return workstations;
    }

    @Override
    public BlockPos workstationUseFromPos() {
        IBlockState blockState = this.world.getBlockState(this.getWorkstationPos());
        if (blockState.getMaterial().isSolid()) {
            EnumFacing direction = blockState.getValue(BlockHorizontal.FACING);
            switch (direction) {
                case NORTH:
                    return this.getWorkstationPos().add(0, 0, 4);
                case SOUTH:
                    return this.getWorkstationPos().add(0, 0, -4);
                case EAST:
                    return this.getWorkstationPos().add(-4, 0, 0);
                case WEST:
                    return this.getWorkstationPos().add(4, 0, 0);
            }
        }
        return null;
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

    public boolean canBeKnockedBack() {
        return true;
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

    @Override
    public AxisAlignedBB breakRange() {
        return new AxisAlignedBB(-1, -1, -1, 1, 1, 1);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1.75f};
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
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.TRICERATOPS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.TRICERATOPS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.TRICERATOPS_DEATH;
    }
}
