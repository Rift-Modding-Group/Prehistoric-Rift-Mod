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
    private static final DataParameter<Byte> WEAPON = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> LAUNCHING = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
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
        this.dataManager.register(WEAPON, (byte)RiftLargeWeaponType.NONE.ordinal());
        this.dataManager.register(LAUNCHING, false);
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

        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureUseMoveUnmounted(this));

        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(7, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageWeaponCooldown();
        if (!this.world.isRemote) this.manageCatapultAnims();
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

    private void manageWeaponCooldown() {
        if (this.getLeftClickCooldown() > 0) this.setLeftClickCooldown(this.getLeftClickCooldown() - 1);
    }

    /*
    @SideOnly(Side.CLIENT)
    @Override
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (this.isBeingRidden()) {
            if (this.getControllingPassenger() != null) {
                if (this.getControllingPassenger().equals(player)) {
                    RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 0, settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()));
                        RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 1, !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()));
                        RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 3, !settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && settings.keyBindPickBlock.isKeyDown()));

                    if (settings.keyBindAttack.isKeyDown() && !this.isActing()) {
                        if (RiftUtil.isUsingSSR()) {
                            SSRCompatUtils.SSRHitResult hitResult = SSRCompatUtils.createHitResult(this);

                            if (this.hasLeftClickChargeBar()) RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                            else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, 0, 0, hitResult.entity, hitResult.blockPos));
                        }
                        else {
                            if (player.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) {
                                if (this.getLeftClickCooldown() == 0) RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                            }
                            else RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, 0, 0));
                        }
                    }
                    else if (settings.keyBindUseItem.isKeyDown() && !this.isActing() && this.canUseRightClick() && !(player.getHeldItemMainhand().getItem() instanceof ItemFood) && !(player.getHeldItemMainhand().getItem() instanceof ItemMonsterPlacer) && !RiftUtil.checkInMountItemWhitelist(player.getHeldItemMainhand().getItem())) {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, 1, 0));
                    }
                    else if (!settings.keyBindUseItem.isKeyDown() && !this.canUseRightClick()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(this, 1, true));
                    }
//                    else if (this.isUsingSpacebar() && this.canUseSpacebar()) {
//                        RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(this, 2, false));
//                        RiftMessages.WRAPPER.sendToServer(new RiftApatosaurusManagePassengers(this));
//                        this.addPassengersFromSpacebar();
//                    }
                    else if (settings.keyBindPickBlock.isKeyDown() && !this.isActing()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 3));
                    }
                    else if (!settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown() && !settings.keyBindPickBlock.isKeyDown()) {
                        if (RiftUtil.isUsingSSR()) {
                            SSRCompatUtils.SSRHitResult hitResult = SSRCompatUtils.createHitResult(this);
                            if (this.hasLeftClickChargeBar()) {
                                if (this.getLeftClickUse() > 0) RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, 0, this.getLeftClickUse(), hitResult.entity, hitResult.blockPos));
                            }
                        }
                        else {
                            if (this.hasLeftClickChargeBar()) {
                                if (this.getLeftClickUse() > 0) RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, 0, this.getLeftClickUse()));
                            }
                        }

//                        if (!this.canUseSpacebar()) RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(this, 2, true));
                        if (this.getMiddleClickUse() > 0) {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, 3, 0));
                            this.setMiddleClickUse(0);
                        }
                    }
                }
            }
        }
    }
    */

    //i have no fucking idea why but this breaks the apato's ability to use catapults
    //just wtf
    public void manageLoaded() {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            boolean flag1 = false;
            boolean flag2 = this.isBeingRidden() && (this.getControllingPassenger() instanceof EntityPlayer && ((EntityPlayer) this.getControllingPassenger()).isCreative());
            for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
                if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                    if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CATAPULT_BOULDER)) {
                        flag1 = true;
                        break;
                    }
                }
            }
            this.setLoaded(flag1 || flag2);
        }
        else this.setLoaded(false);
    }

    private void manageCatapultAnims() {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            if (!this.world.isRemote) {
                EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
                if (!this.isCharging() && this.isUsingLeftClick() && rider.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) this.setCharging(true);
                else if (this.isCharging() && !this.isUsingLeftClick()) this.setCharging(false);

                if (this.isLaunching()) {
                    this.launchTick++;
                    if (this.launchTick > 8) {
                        this.setLaunching(false);
                        this.launchTick = 0;
                    }
                }
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("Weapon", (byte) this.getWeapon().ordinal());
        this.writeWorkstationDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Weapon")) this.setWeapon(RiftLargeWeaponType.values()[compound.getByte("Weapon")]);
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
        TileEntity te = this.world.getTileEntity(this.getWorkstationPos());
        int dirF = te instanceof TileEntitySemiManualBase ? -1 : 1;
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

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {}

    private void manageCannonFiring() {
        EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        int indexToRemove = -1;
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CANNONBALL)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            RiftCannonball cannonball = new RiftCannonball(this.world, this, rider);
            cannonball.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, 1.6F, 1.0F);
            this.world.spawnEntity(cannonball);
            this.creatureInventory.getStackInSlot(indexToRemove).setCount(0);
            this.setLeftClickCooldown(30);
        }
    }

    private void manageMortarFiring(int holdAmount) {
        EntityPlayer rider = (EntityPlayer)this.getControllingPassenger();
        int launchDist = RiftUtil.clamp((int)(0.1D * holdAmount) + 6, 6, 16);
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        int indexToRemove = -1;
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.MORTAR_SHELL)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            RiftMortarShell mortarShell = new RiftMortarShell(this.world, this, rider);
            mortarShell.shoot(this, launchDist);
            this.world.spawnEntity(mortarShell);
            this.creatureInventory.getStackInSlot(indexToRemove).setCount(0);
            this.setLeftClickCooldown(Math.max(holdAmount * 2, 60));
        }
    }

    private void manageCatapultFiring(int holdAmount) {
        EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        int indexToRemove = -1;
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CATAPULT_BOULDER)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            this.setLaunching(true);
            RiftCatapultBoulder boulder = new RiftCatapultBoulder(this.world, this, rider);
            float velocity = RiftUtil.clamp((float) holdAmount * 0.015f + 1.5f, 1.5f, 3f);
            float power = RiftUtil.clamp(0.03f * holdAmount + 3f, 3f, 6f);
            boulder.setPower(power);
            boulder.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, velocity, 1.0F);
            this.world.spawnEntity(boulder);
            this.creatureInventory.getStackInSlot(indexToRemove).setCount(0);
            this.setLeftClickCooldown(Math.max(holdAmount * 2, 60));
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed()) {
            try {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    if (itemstack.getItem() instanceof RiftLargeWeaponItem && this.getWeapon().equals(RiftLargeWeaponType.NONE)) {
                        if (itemstack.getItem().equals(RiftItems.CANNON)) {
                            this.setWeapon(RiftLargeWeaponType.CANNON);
                            this.consumeItemFromStack(player, itemstack);
                            return true;
                        }
                        else if (itemstack.getItem().equals(RiftItems.MORTAR)) {
                            this.setWeapon(RiftLargeWeaponType.MORTAR);
                            this.consumeItemFromStack(player, itemstack);
                            return true;
                        }
                        else if (itemstack.getItem().equals(RiftItems.CATAPULT)) {
                            this.setWeapon(RiftLargeWeaponType.CATAPULT);
                            this.consumeItemFromStack(player, itemstack);
                            return true;
                        }
                    }
                    else if (itemstack.getItem().equals(RiftItems.WRENCH) && !this.getWeapon().equals(RiftLargeWeaponType.NONE)) {
                        if (!player.capabilities.isCreativeMode) {
                            switch (this.getWeapon()) {
                                case CANNON:
                                    player.inventory.addItemStackToInventory(new ItemStack(RiftItems.CANNON));
                                    break;
                                case MORTAR:
                                    player.inventory.addItemStackToInventory(new ItemStack(RiftItems.MORTAR));
                                    break;
                                case CATAPULT:
                                    player.inventory.addItemStackToInventory(new ItemStack(RiftItems.CATAPULT));
                                    break;
                            }
                        }
                        this.setWeapon(RiftLargeWeaponType.NONE);
                        return true;
                    }
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        return super.processInteract(player, hand);
    }

    public void useWhipAttack() {
        AxisAlignedBB area = this.getEntityBoundingBox().grow(4D, 4D, 4D);
        List<EntityLivingBase> list = new ArrayList<>();
        for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, area, null)) {
            if (!entity.isRiding()) {
                if (entity instanceof EntityPlayer) {
                    if (!entity.getUniqueID().equals(this.getOwnerId())) list.add(entity);
                }
                else if (entity instanceof EntityTameable) {
                    if ((((EntityTameable) entity).isTamed())) {
                        if (!((EntityTameable) entity).getOwner().equals(this.getOwner())) list.add(entity);
                    }
                    else list.add(entity);
                }
                else list.add(entity);
            }
        }
        list.remove(this);

        for (EntityLivingBase entity : list) {
            double d0 = this.posX - entity.posX;
            double d1 = this.posZ - entity.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            entity.knockBack(this, 1, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
            entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
        }
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
    public boolean hasLeftClickChargeBar() {
        return !this.getWeapon().equals(RiftLargeWeaponType.NONE);
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.35f, 2.25f};
    }

    @Override
    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        if (!this.world.isRemote) this.setSaddled(saddle.getItem() == RiftItems.APATOSAURUS_PLATFORM && !saddle.isEmpty());
    }

    public RiftLargeWeaponType getWeapon() {
        return RiftLargeWeaponType.values()[this.dataManager.get(WEAPON).byteValue()];
    }

    public void setWeapon(RiftLargeWeaponType value) {
        this.dataManager.set(WEAPON, (byte) value.ordinal());
    }

    public boolean isLaunching() {
        return this.dataManager.get(LAUNCHING);
    }

    public void setLaunching(boolean value) {
        this.dataManager.set(LAUNCHING, value);
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
        this.setActing(value);
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
        data.addAnimationController(new AnimationController(this, "movement", 0, this::apatosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::apatosaurusAttack));
        data.addAnimationController(new AnimationController(this, "weaponResize", 0, this::apatosaurusWeaponSize));
        data.addAnimationController(new AnimationController(this, "catapultCharge", 0, this::apatosaurusCatapultCharge));
        data.addAnimationController(new AnimationController(this, "catapultLaunch", 0, this::apatosaurusCatapultLaunch));
    }

    private <E extends IAnimatable> PlayState apatosaurusMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.sitting", true));
                return PlayState.CONTINUE;
            }
            if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking() && !this.isTailWhipping()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.walk", true));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState apatosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.stomp", false));
            return PlayState.CONTINUE;
        }
        else if (this.isTailWhipping()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.tail_whip", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState apatosaurusWeaponSize(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.weapon_size_change", true));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState apatosaurusCatapultCharge(AnimationEvent<E> event) {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            if (this.isCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.charge_catapult", true));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState apatosaurusCatapultLaunch(AnimationEvent<E> event) {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            if (this.isLaunching()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.launch_catapult", false));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
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
