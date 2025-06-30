package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.MobSize;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.*;

public class Apatosaurus extends RiftCreature implements IWorkstationUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/apatosaurus"));
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOADED = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> TAIL_WHIPPING = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> PASSENGER_ONE_UUID = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.STRING);
    private static final DataParameter<String> PASSENGER_TWO_UUID = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> USING_WORKSTATION = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WORKSTATION_X_POS = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Y_POS = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Z_POS = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.VARINT);
    public boolean dismount = false;

    public Apatosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.APATOSAURUS);
        this.setSize(4f, 3f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 50;
        this.speed = 0.15D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHARGING, false);
        this.dataManager.register(LOADED, false);
        this.dataManager.register(TAIL_WHIPPING, false);
        this.dataManager.register(PASSENGER_ONE_UUID, RiftUtil.nilUUID.toString());
        this.dataManager.register(PASSENGER_TWO_UUID, RiftUtil.nilUUID.toString());
        this.dataManager.register(USING_WORKSTATION, false);
        this.dataManager.register(WORKSTATION_X_POS, 0);
        this.dataManager.register(WORKSTATION_Y_POS, 0);
        this.dataManager.register(WORKSTATION_Z_POS, 0);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftCreatureOperateWorkstation(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));

        this.tasks.addTask(3, new RiftCreatureUseLargeWeaponMounted(this));
        this.tasks.addTask(4, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(5, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(6, new RiftCreatureUseMoveUnmounted(this));

        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
        this.tasks.addTask(10, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //passenger stuff
        if (this.getPassengers().size() == 1) this.dismount = false;
        else if (this.getPassengers().size() > 1) this.dismount = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeWorkstationDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readWorkstationDataFromNBT(compound);
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

    public boolean isWorkstation(BlockPos pos) {
        Block block = this.world.getBlockState(pos).getBlock();
        if (GeneralConfig.canUseMM()) {
            return block instanceof BlockSemiManualBase;
        }
        return false;
    }

    @Override
    public BlockPos workstationUseFromPos() {
        IBlockState blockState = this.world.getBlockState(this.getWorkstationPos());
        if (blockState.getMaterial().isSolid()) {
            EnumFacing direction = blockState.getValue(BlockHorizontal.FACING);
            switch (direction) {
                case NORTH:
                    return this.getWorkstationPos().add(0, 0, 5);
                case SOUTH:
                    return this.getWorkstationPos().add(0, 0, -5);
                case EAST:
                    return this.getWorkstationPos().add(-5, 0, 0);
                case WEST:
                    return this.getWorkstationPos().add(5, 0, 0);
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

    public boolean canBeKnockedBack() {
        return true;
    }

    @Override
    public int slotCount() {
        return 54;
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Arrays.asList(CreatureMove.STOMP, CreatureMove.TAIL_WHIP, CreatureMove.BIDE));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.STOMP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(50D)
                .defineChargeUpToUseLength(2D)
                .defineRecoverFromUseLength(8D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_STOMP_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.TAIL, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(1D)
                .defineRecoverFromUseLength(4D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_TAIL_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.DEFENSE, this.currentCreatureMove() == CreatureMove.BIDE ?
                new RiftCreatureMoveAnimator(this)
                        .defineStartMoveDelayLength(5D)
                        .defineChargeUpToUseLength(2.5D)
                        .defineRecoverFromUseLength(7.5D)
                        .setChargeUpToUseSound(RiftSounds.APATOSAURUS_BIDE_RELEASE)
                        .finalizePoints()
        : null);
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 6f;
    }

    public void addPassengersManual() {
        AxisAlignedBB area = this.getEntityBoundingBox().grow(4D, 4D, 4D);
        int passengerSize = this.getPassengers().size();
        if (passengerSize == 1) {
            for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, area, new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    return !input.isRiding();
                }
            })) {
                for (int i = 0; i < 3 - passengerSize; i++) {
                    boolean canAccept = RiftUtil.isAppropriateSize(entity, MobSize.safeValueOf(RiftConfigHandler.getConfig(this.creatureType).general.maximumPassengerSize ));
                    if (entity != null && !entity.equals(this) && !(entity instanceof EntityPlayer) && canAccept) {
                        entity.startRiding(this, true);
                        this.dismount = true;
                    }
                }
            }
        }
        else if (passengerSize > 1) {
            if (this.dismount) {
                for (Entity entity : this.getPassengers()) {
                    if (!entity.equals(this.getControllingPassenger()) && !(entity instanceof EntityPlayer)) {
                        entity.dismountRidingEntity();
                    }
                }
                this.setPassengerOne(null);
                this.setPassengerTwo(null);
                this.dismount = false;
            }
            else {
                for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, area, new Predicate<EntityLivingBase>() {
                    @Override
                    public boolean apply(@Nullable EntityLivingBase input) {
                        return !input.isRiding();
                    }
                })) {
                    for (int i = 0; i < 3 - passengerSize; i++) {
                        boolean canAccept = RiftUtil.isAppropriateSize(entity, MobSize.safeValueOf(RiftConfigHandler.getConfig(this.creatureType).general.maximumPassengerSize));
                        if (entity != null && !entity.equals(this) && !(entity instanceof EntityPlayer) && canAccept) {
                            entity.startRiding(this, true);
                            this.dismount = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.35f, 2.25f};
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    public void setCharging(boolean value) {
        this.dataManager.set(CHARGING, value);
    }

    public boolean isLoaded() {
        return this.dataManager.get(LOADED);
    }

    public void setLoaded(boolean value) {
        this.dataManager.set(LOADED, value);
    }

    public boolean isTailWhipping() {
        return this.dataManager.get(TAIL_WHIPPING);
    }

    public void setTailWhipping(boolean value) {
        this.dataManager.set(TAIL_WHIPPING, value);
    }

    public EntityLivingBase getPassengerOne() {
        return RiftUtil.getEntityFromUUID(this.world, UUID.fromString(this.dataManager.get(PASSENGER_ONE_UUID)));
    }

    public void setPassengerOne(EntityLivingBase entity) {
        if (entity == null) this.dataManager.set(PASSENGER_ONE_UUID, RiftUtil.nilUUID.toString());
        else this.dataManager.set(PASSENGER_ONE_UUID, entity.getUniqueID().toString());
    }

    public EntityLivingBase getPassengerTwo() {
        return RiftUtil.getEntityFromUUID(this.world, UUID.fromString(this.dataManager.get(PASSENGER_TWO_UUID)));
    }

    public void setPassengerTwo(EntityLivingBase entity) {
        if (entity == null) this.dataManager.set(PASSENGER_TWO_UUID, RiftUtil.nilUUID.toString());
        else this.dataManager.set(PASSENGER_TWO_UUID, entity.getUniqueID().toString());
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "weaponResize", 0, this::apatosaurusWeaponSize));
    }

    private <E extends IAnimatable> PlayState apatosaurusWeaponSize(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.weapon_size_change", true));
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.APATOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.APATOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.APATOSAURUS_DEATH;
    }
}
