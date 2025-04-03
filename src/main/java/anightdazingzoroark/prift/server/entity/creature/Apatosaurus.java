package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.SSRCompatUtils;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualBase;
import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.items.RiftLargeWeaponItem;
import anightdazingzoroark.prift.server.message.*;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
    private int launchTick;
    public boolean dismount = false;
    private RiftCreaturePart neck0Part;
    private RiftCreaturePart neck1Part;
    private RiftCreaturePart neck2Part;
    private RiftCreaturePart neck3Part;
    private RiftCreaturePart neck4Part;
    private RiftCreaturePart neck5Part;
    private RiftCreaturePart leftBackLegPart;
    private RiftCreaturePart rightBackLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;

    public Apatosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.APATOSAURUS);
        this.setSize(4f, 3f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 50;
        this.speed = 0.15D;
        this.isRideable = true;
        this.launchTick = 0;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 6.625f, 0, 4.5125f, 0.625f, 0.5f, 2f);
        this.bodyPart = new RiftCreaturePart(this, -0.75f, 0, 1.35f, 1.65f, 1f, 1f);
        this.neck0Part = new RiftCreaturePart(this, 5.75f, 0, 4.25f, 0.5f, 0.5f, 1.5f);
        this.neck1Part = new RiftCreaturePart(this, 5f, 0, 3.75f, 0.5f, 0.5f, 1.5f);
        this.neck2Part = new RiftCreaturePart(this, 4.25f, 0, 3.25f, 0.5f, 0.5f, 1.5f);
        this.neck3Part = new RiftCreaturePart(this, 3.5f, 0, 2.75f, 0.5f, 0.625f, 1.5f);
        this.neck4Part = new RiftCreaturePart(this, 2.75f, 0, 2.5f, 0.5f, 0.625f, 1.5f);
        this.neck5Part = new RiftCreaturePart(this, 1.75f, 0, 2.25f, 0.625f, 0.625f, 1.5f);
        this.leftBackLegPart = new RiftCreaturePart(this, 2.375f, -150, 0, 0.625f, 1.25f, 0.5f);
        this.rightBackLegPart = new RiftCreaturePart(this, 2.375f, 150, 0, 0.625f, 1.25f, 0.5f);
        this.tail0Part = new RiftCreaturePart(this, -3.25f, 0, 1.9f, 0.675f, 0.625f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -4.75f, 0, 1.8f, 0.625f, 0.6f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -6f, 0, 1.7f, 0.625f, 0.6f, 0.5f);
        this.tail3Part = new RiftCreaturePart(this, -7.25f, 0, 1.7f, 0.625f, 0.45f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neck0Part,
            this.neck1Part,
            this.neck2Part,
            this.neck3Part,
            this.neck4Part,
            this.neck5Part,
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

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftUseSemiManualMachine(this, 3f, 3f));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));

        this.tasks.addTask(3, new RiftCreatureUseLargeWeaponMounted(this));
        this.tasks.addTask(4, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(5, new RiftCreatureUseMoveUnmounted(this));

        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(7, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //passenger stuff
        if (this.getPassengers().size() == 1) this.dismount = false;
        else if (this.getPassengers().size() > 1) this.dismount = true;
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -1.125f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neck0Part != null) this.neck0Part.setPositionAndUpdate(this.neck0Part.posX, this.neck0Part.posY + sitOffset, this.neck0Part.posZ);
        if (this.neck1Part != null) this.neck1Part.setPositionAndUpdate(this.neck1Part.posX, this.neck1Part.posY + sitOffset, this.neck1Part.posZ);
        if (this.neck2Part != null) this.neck2Part.setPositionAndUpdate(this.neck2Part.posX, this.neck2Part.posY + sitOffset, this.neck2Part.posZ);
        if (this.neck3Part != null) this.neck3Part.setPositionAndUpdate(this.neck3Part.posX, this.neck3Part.posY + sitOffset, this.neck3Part.posZ);
        if (this.neck4Part != null) this.neck4Part.setPositionAndUpdate(this.neck4Part.posX, this.neck4Part.posY + sitOffset, this.neck4Part.posZ);
        if (this.neck5Part != null) this.neck5Part.setPositionAndUpdate(this.neck5Part.posX, this.neck5Part.posY + sitOffset, this.neck5Part.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
        if (this.tail3Part != null) this.tail3Part.setPositionAndUpdate(this.tail3Part.posX, this.tail3Part.posY + sitOffset, this.tail3Part.posZ);
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

    public boolean isUsingWorkAnim() {
        return this.isAttacking();
    }

    public void setUsingWorkAnim(boolean value) {
        this.setAttacking(value);
    }

    public SoundEvent useAnimSound() {
        return RiftSounds.SEMI_MANUAL_MACHINE_RESET;
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
    public int slotCount() {
        return 54;
    }

    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            if (passenger.equals(this.getControllingPassenger()) && passenger.equals(this.getOwner())) super.updatePassenger(passenger);
            else {
                if (this.getPassengerTwo() == null) this.setPassengerTwo((EntityLivingBase) passenger);
                else if (this.getPassengerOne() == null) this.setPassengerOne((EntityLivingBase) passenger);
                if (this.getPassengerOne() != null) {
                    if (this.getPassengerOne().equals(passenger)) {
                        passenger.setPosition(passengerPosOne().x, passengerPosOne().y + passenger.height, passengerPosOne().z);
                        ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
                    }
                }
                if (this.getPassengerTwo() != null) {
                    if (this.getPassengerTwo().equals(passenger)) {
                        passenger.setPosition(passengerPosTwo().x, passengerPosTwo().y + passenger.height, passengerPosTwo().z);
                        ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
                    }
                }
            }
            if (this.isDead) passenger.dismountRidingEntity();
            if (passenger.isDead) {
                if (this.getPassengerOne() != null) {
                    if (this.getPassengerOne().equals(passenger)) {
                        passenger.dismountRidingEntity();
                        this.setPassengerOne(null);
                    }
                }
                if (this.getPassengerTwo() != null) {
                    if (this.getPassengerTwo().equals(passenger)) {
                        passenger.dismountRidingEntity();
                        this.setPassengerTwo(null);
                    }
                }
            }
        }
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Arrays.asList(CreatureMove.STOMP, CreatureMove.TAIL_WHIP, CreatureMove.BIDE);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Arrays.asList(CreatureMove.STOMP, CreatureMove.TAIL_WHIP, CreatureMove.BIDE);
    }

    @Override
    public Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveType.STOMP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(50D)
                .defineChargeUpToUseLength(2D)
                .defineRecoverFromUseLength(8D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.TAIL, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(1D)
                .defineRecoverFromUseLength(4D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.DEFENSE, this.currentCreatureMove() == CreatureMove.BIDE ?
                new RiftCreatureMoveAnimator(this)
                        .defineStartMoveDelayLength(5D)
                        .defineChargeUpToUseLength(2.5D)
                        .defineRecoverFromUseLength(7.5D)
                        .finalizePoints()
        : null);
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 6f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (1) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (1) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + 1.25, zOffset);
    }

    public Vec3d passengerPosOne() {
        float seatOneX = (float)(this.posX + (-0.25) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float seatOneZ = (float)(this.posZ + (-0.25) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(seatOneX, this.posY + 2.25, seatOneZ);
    }

    public Vec3d passengerPosTwo() {
        float seatTwoX = (float)(this.posX + (-2) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float seatTwoZ = (float)(this.posZ + (-2) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(seatTwoX, this.posY + 2.25, seatTwoZ);
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
