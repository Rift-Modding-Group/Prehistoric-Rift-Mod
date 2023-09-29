package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import anightdazingzoroark.rift.server.message.*;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.lwjgl.Sys;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public abstract class RiftCreature extends EntityTameable implements IAnimatable {
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ENERGY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ACTING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> RIGHT_CLICK_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RIGHT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private int energyMod;
    private int energyRegenMod;
    private int energyRegenModDelay;
    public int energyActionMod;
    private int energyActionModCountdown;
    private int eatFromInvCooldown;
    private int eatFromInvForEnergyCooldown;
    private boolean informLowEnergy;
    private boolean informNoEnergy;
    public boolean cannotUseRightClick;
    public final RiftCreatureType creatureType;
    public AnimationFactory factory = new AnimationFactory(this);
    public boolean isRideable;
    public RiftCreatureInventory creatureInventory;
    private boolean steerable = true;
    public EntityLivingBase ssrTarget;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.initInventory();
        this.energyMod = 0;
        this.energyRegenMod = 0;
        this.energyRegenModDelay = 0;
        this.energyActionMod = 0;
        this.energyActionModCountdown = 0;
        this.eatFromInvCooldown = 0;
        this.eatFromInvForEnergyCooldown = 0;
        this.informLowEnergy = false;
        this.informNoEnergy = false;
        this.cannotUseRightClick = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, Boolean.FALSE);
        this.dataManager.register(VARIANT, rand.nextInt(4));
        this.dataManager.register(STATUS, (byte) TameStatusType.STAND.ordinal());
        this.dataManager.register(BEHAVIOR, (byte) TameBehaviorType.ASSIST.ordinal());
        this.dataManager.register(SADDLED, Boolean.FALSE);
        this.dataManager.register(ENERGY, 20);
        this.dataManager.register(ACTING, Boolean.FALSE);
        this.dataManager.register(CAN_USE_RIGHT_CLICK, Boolean.FALSE);
        this.dataManager.register(USING_RIGHT_CLICK, Boolean.FALSE);
        this.dataManager.register(RIGHT_CLICK_USE, 0);
        this.dataManager.register(RIGHT_CLICK_COOLDOWN, 0);
        this.dataManager.register(HAS_TARGET, Boolean.FALSE);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) {
            this.setHasTarget(this.getAttackTarget() != null);
        }
        if (this.isTamed() && !this.world.isRemote) {
            this.updateEnergyMove();
            this.updateEnergyActions();
            this.resetEnergyActionMod();
            this.lowEnergyEffects();
            this.eatFromInventory();
            if (this.isBeingRidden()) this.informRiderEnergy();
        }
    }

    private void updateEnergyMove() {
        if (this.isMoving()) {
            this.energyMod++;
            this.energyRegenMod = 0;
            this.energyRegenModDelay = 0;
            if (this.isBeingRidden()) {
                if (this.energyMod > this.creatureType.getMaxEnergyModMovement() * (this.getControllingPassenger().isSprinting() ? 3/4 : 1)) {
                    this.setEnergy(this.getEnergy() - 1);
                    this.energyMod = 0;
                }
            }
            else {
                if (this.energyMod > this.creatureType.getMaxEnergyModMovement()) {
                    this.setEnergy(this.getEnergy() - 1);
                    this.energyMod = 0;
                }
            }
        }
        else if (!this.isActing()) {
            this.energyMod = 0;
            if (this.energyRegenModDelay <= 20) {
                this.energyRegenModDelay++;
            }
            else {
                this.energyRegenMod++;
            }
            if (this.energyRegenMod > this.creatureType.getMaxEnergyRegenMod()) {
                this.setEnergy(this.getEnergy() + 1);
                this.energyRegenMod = 0;
            }
        }
        else {
            this.energyMod = 0;
            this.energyRegenMod = 0;
            this.energyRegenModDelay = 0;
        }
    }

    public abstract void updateEnergyActions();

    private void resetEnergyActionMod() {
        if (!this.isActing() && this.energyActionMod > 0) {
            this.energyActionModCountdown++;
            if (this.energyActionModCountdown > 60) {
                this.energyActionMod = 0;
                this.energyActionModCountdown = 0;
            }
        }
    }

    private void lowEnergyEffects() {
        if (this.getEnergy() > 0 && this.getEnergy() <= 6) {
            this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 2));
        }
        else if (this.getEnergy() == 0) {
            this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 255));
        }
    }

    private void eatFromInventory() {
        int minSlot = this.canBeSaddled() ? 1 : 0;
        if (this.getHealth() < this.getMaxHealth()) {
            this.eatFromInvCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (this.isFavoriteFood(itemInSlot) && this.eatFromInvCooldown > 60) {
                    this.heal((float)((ItemFood)itemInSlot.getItem()).getHealAmount(itemInSlot) * 3F);
                    itemInSlot.setCount(itemInSlot.getCount() - 1);
                    this.eatFromInvCooldown = 0;
                }
            }
        }
        else {
            this.eatFromInvCooldown = 0;
        }

        if (this.getEnergy() < 20) {
            this.eatFromInvForEnergyCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (RiftUtil.isEnergyRegenItem(itemInSlot.getItem(), this.creatureType.getCreatureDiet()) && this.eatFromInvForEnergyCooldown > 60) {
                    this.setEnergy(this.getEnergy() + RiftUtil.getEnergyRegenItemValue(itemInSlot.getItem(), this.creatureType.getCreatureDiet()));
                    itemInSlot.setCount(itemInSlot.getCount() - 1);
                    this.eatFromInvForEnergyCooldown = 0;
                }
            }
        }
        else {
            this.eatFromInvForEnergyCooldown = 0;
        }
    }

    private void informRiderEnergy() {
        if (!this.informLowEnergy && this.getEnergy() <= 6 && this.getEnergy() > 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("rift.notify.low_energy", this.getName()), false);
            this.informLowEnergy = true;
        }
        if (this.informLowEnergy && this.getEnergy() > 6) {
            this.informLowEnergy = false;
        }
        if (!this.informNoEnergy && this.getEnergy() == 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("rift.notify.no_energy", this.getName()), false);
            this.informNoEnergy = true;
        }
        if (this.informNoEnergy && this.getEnergy() > 0) {
            this.informNoEnergy = false;
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) {
            this.applyEnchantments(this, entityIn);
        }
        return flag;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.getOwnerId() != null) {
            if (this.getOwnerId().equals(player.getUniqueID())) {
                if (this.isFavoriteFood(itemstack) && !itemstack.isEmpty() && this.isChild() && this.getHealth() == this.getMaxHealth()) {
                    this.consumeItemFromStack(player, itemstack);
                    this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
                }
                else if (this.isFavoriteFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    this.consumeItemFromStack(player, itemstack);
                    this.heal((float) this.getFavoriteFoodHeal(itemstack));
                }
                else if (RiftUtil.isEnergyRegenItem(itemstack.getItem(), this.creatureType.getCreatureDiet()) && this.getEnergy() < 20) {
                    this.consumeItemFromStack(player, itemstack);
                    this.setEnergy(this.getEnergy() + RiftUtil.getEnergyRegenItemValue(itemstack.getItem(), this.creatureType.getCreatureDiet()));
                }
                else if (itemstack.isEmpty() && !this.isSaddled()) {
                    player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId() ,0, 0);
                }
                else if (itemstack.isEmpty() && this.isSaddled() && !player.isSneaking()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
                }
                else if (itemstack.isEmpty() && this.isSaddled() && player.isSneaking()) {
                    player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, this.getEntityId() ,0, 0);
                }
                return true;
            }
        }
        return false;
    }

    public abstract boolean isFavoriteFood(ItemStack stack);

    public int getFavoriteFoodHeal(ItemStack stack) {
        for (String foodItem : RiftConfig.tyrannosaurusFavoriteFood) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            double percentage = Double.parseDouble(foodItem.substring(itemIdThird + 1));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == 32767) return (int)(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() * percentage);
                else if (stack.getMetadata() == itemData) return (int)(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue() * percentage);
            }
        }
        return 0;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setByte("TameStatus", (byte) this.getTameStatus().ordinal());
        compound.setByte("TameBehavior", (byte) this.getTameBehavior().ordinal());
        compound.setBoolean("Saddled", this.isSaddled());
        if (creatureType != null) {
            NBTTagList nbttaglist = new NBTTagList();
            for (int i = 0; i < this.creatureInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = this.creatureInventory.getStackInSlot(i);
                if (!itemstack.isEmpty()) {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.setByte("Slot", (byte) i);
                    itemstack.writeToNBT(nbttagcompound);
                    nbttaglist.appendTag(nbttagcompound);
                }
            }
            compound.setTag("Items", nbttaglist);
        }
        compound.setInteger("Energy", this.getEnergy());
        compound.setBoolean("HasTarget", this.hasTarget());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        if (compound.hasKey("TameStatus")) this.setTameStatus(TameStatusType.values()[compound.getByte("TameStatus")]);
        if (compound.hasKey("TameBehavior")) this.setTameBehavior(TameBehaviorType.values()[compound.getByte("TameBehavior")]);
        this.setSaddled(compound.getBoolean("Saddled"));
        if (creatureInventory != null) {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                int inventorySize = this.slotCount() + (this.canBeSaddled() ? 1 : 0);
                if (j < inventorySize) this.creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        else {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                this.creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
        this.setEnergy(compound.getInteger("Energy"));
        this.setHasTarget(compound.getBoolean("HasTarget"));
    }

    private void initInventory() {
        int inventorySize = this.slotCount() + (this.canBeSaddled() ? 1 : 0);
        this.creatureInventory = new RiftCreatureInventory("creatureInventory", inventorySize, this);
        this.creatureInventory.setCustomName(this.getName());
        if (this.creatureInventory != null) {
            for (int i = 0; i < inventorySize; ++i) {
                ItemStack itemStack = this.creatureInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    this.creatureInventory.setInventorySlotContents(i, itemStack.copy());
                }
            }
        }
    }

    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        if (!this.world.isRemote) this.setSaddled(saddle.getItem() == Items.SADDLE && !saddle.isEmpty());
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    public void setAttacking(boolean value) {
        this.dataManager.set(ATTACKING, Boolean.valueOf(value));
    }

    public TameStatusType getTameStatus() {
        return TameStatusType.values()[this.dataManager.get(STATUS).byteValue()];
    }

    public void setTameStatus(TameStatusType tameStatus) {
        this.dataManager.set(STATUS, (byte) tameStatus.ordinal());
    }

    public int getEnergy() {
        return RiftUtil.clamp(this.dataManager.get(ENERGY).intValue(), 0, 20);
    }

    public void setEnergy(int energy) {
        this.dataManager.set(ENERGY, RiftUtil.clamp(energy, 0, 20));
    }

    public TameBehaviorType getTameBehavior() {
        return TameBehaviorType.values()[this.dataManager.get(BEHAVIOR).byteValue()];
    }
    public void setTameBehavior(TameBehaviorType tameBehavior) {
        this.dataManager.set(BEHAVIOR, (byte) tameBehavior.ordinal());
    }

    public boolean isSaddled() {
        return this.dataManager.get(SADDLED);
    }

    public void setSaddled(boolean value) {
        this.dataManager.set(SADDLED, Boolean.valueOf(value));
    }

    public boolean isActing() {
        return this.dataManager.get(ACTING);
    }

    public void setActing(boolean value) {
        this.dataManager.set(ACTING, Boolean.valueOf(value));
    }

    public boolean canUseRightClick() {
        return this.dataManager.get(CAN_USE_RIGHT_CLICK);
    }

    public void setCanUseRightClick(boolean value) {
        this.dataManager.set(CAN_USE_RIGHT_CLICK, Boolean.valueOf(value));
    }

    public boolean isUsingRightClick() {
        return this.dataManager.get(USING_RIGHT_CLICK);
    }

    public void setUsingRightClick(boolean value) {
        this.dataManager.set(USING_RIGHT_CLICK, Boolean.valueOf(value));
    }

    public int getRightClickUse() {
        return this.dataManager.get(RIGHT_CLICK_USE).intValue();
    }

    public void setRightClickUse(int value) {
        this.dataManager.set(RIGHT_CLICK_USE, value);
    }

    public int getRightClickCooldown() {
        return this.dataManager.get(RIGHT_CLICK_COOLDOWN);
    }

    public void setRightClickCooldown(int value) {
        this.dataManager.set(RIGHT_CLICK_COOLDOWN, value);
    }

    public boolean hasTarget() {
        return this.dataManager.get(HAS_TARGET);
    }

    public void setHasTarget(boolean value) {
        this.dataManager.set(HAS_TARGET, value);
    }

    public boolean isMoving() {
        double fallMotion = !this.onGround ? this.motionY : 0;
        return Math.sqrt((this.motionX * this.motionX) + (fallMotion * fallMotion) + (this.motionZ * this.motionZ)) > 0;
    }

    public boolean isApexPredator() {
        return false;
    }

    public boolean canBeSaddled() {
        return false;
    }

    public int slotCount() {
        return 0;
    }

    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        passenger.setPosition(riderPos().x, riderPos().y + passenger.height, riderPos().z);
        ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
        if (this.isDead) passenger.dismountRidingEntity();
    }

    public abstract Vec3d riderPos();

    public abstract void controlInput(int control, int holdAmount, EntityLivingBase target);

    public void controlAttack() {
        EntityLivingBase target;
        if (this.ssrTarget == null) target = this.getControlAttackTargets();
        else target = this.ssrTarget;
        if (target != null) {
            if (this.isTamed() && target instanceof EntityPlayer) {
                if (!target.getUniqueID().equals(this.getOwnerId())) {
                    this.attackEntityAsMob(target);
                }
            }
            else if (this.isTamed() && target instanceof EntityTameable) {
                if (((EntityTameable) target).isTamed()) {
                    if (!((EntityTameable) target).getOwner().equals(this.getOwner())) {
                        this.attackEntityAsMob(target);
                    }
                }
                else {
                    this.attackEntityAsMob(target);
                }
            }
            else {
                this.attackEntityAsMob(target);
            }
        }
    }

    public EntityLivingBase getControlAttackTargets() {
        double dist = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX;
        Vec3d vec3d = this.getPositionEyes(1.0F);
        Vec3d vec3d1 = this.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        Entity pointedEntity = null;
        Entity rider = this.getControllingPassenger();
        List<Entity> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(1.0D, 1.0D, 1.0D), null);
        double d2 = d1;
        for (Entity potentialTarget : list) {
            AxisAlignedBB axisalignedbb = potentialTarget.getEntityBoundingBox().grow((double) potentialTarget.getCollisionBorderSize() + 2F);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (potentialTarget != this && potentialTarget != rider) {
                if (axisalignedbb.contains(vec3d)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = potentialTarget;
                        d2 = 0.0D;
                    }
                }
                else if (raytraceresult != null) {
                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        if (potentialTarget.getLowestRidingEntity() == rider.getLowestRidingEntity() && !rider.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = potentialTarget;
                            }
                        }
                        else {
                            pointedEntity = potentialTarget;
                            d2 = d3;
                        }
                    }
                }
            }
        }
        return (EntityLivingBase) pointedEntity;
    }

    public AxisAlignedBB getControlAttackArea() {
        return this.getEntityBoundingBox().grow(4D, 5.0D, 4D);
    }

    @Override
    public boolean canPassengerSteer() {
        return false;
    }

    @Override
    public boolean canBeSteered() {
        return this.steerable;
    }

    public void setCanBeSteered(boolean value) {
        this.steerable = value;
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer && this.getAttackTarget() != passenger) {
                EntityPlayer player = (EntityPlayer) passenger;
                if (this.isTamed() && this.isOwner(player)) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSaddled() && this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null) {
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
                float moveSpeedMod = (this.getEnergy() >= 6 ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float moveSpeed = (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * moveSpeedMod;
                this.setAIMoveSpeed(this.onGround ? moveSpeed + (controller.isSprinting() && this.getEnergy() >= 6 ? moveSpeed * 0.3f : 0) : 2);
                super.travel(strafe, vertical, forward);
            }
        }
        else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            super.travel(strafe, vertical, forward);
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public abstract void registerControllers(AnimationData data);

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public class RiftCreatureInventory extends ContainerHorseChest {
        public RiftCreatureInventory(String inventoryTitle, int slotCount, RiftCreature creature) {
            super(inventoryTitle, slotCount);
            this.addInventoryChangeListener(new RiftCreatureInvListener(creature));
        }

        public void setInventoryFromData(RiftChangeInventoryFromMenu.RiftCreatureInvData data) {
            ItemStack[] contents = data.getInventoryContents();

            if (contents.length != getSizeInventory()) {
                throw new IllegalArgumentException("Invalid inventory size");
            }

            for (int i = 0; i < getSizeInventory(); i++) {
                setInventorySlotContents(i, contents[i]);
            }
        }
    }

    class RiftCreatureInvListener implements IInventoryChangedListener {
        RiftCreature creature;

        public RiftCreatureInvListener(RiftCreature creature) {
            this.creature = creature;
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {
            creature.refreshInventory();
        }
    }
}
