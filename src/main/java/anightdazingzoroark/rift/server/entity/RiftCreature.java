package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import anightdazingzoroark.rift.server.message.RiftChangeInventoryFromMenu;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftStartRiding;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public abstract class RiftCreature extends EntityTameable implements IAnimatable {
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ENERGY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ACTING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private int energyMod;
    private int energyRegenMod;
    private int energyRegenModDelay;
    public int energyActionMod;
    private int energyActionModCountdown;
    private int eatFromInvCooldown;
    private int eatFromInvForEnergyCooldown;
    private boolean informLowEnergy;
    private boolean informNoEnergy;
    public final RiftCreatureType creatureType;
    public AnimationFactory factory = new AnimationFactory(this);
    public boolean isRideable;
    public RiftCreatureInventory creatureInventory;
    private boolean steerable = true;

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
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
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
                if (this.isFavoriteFood(itemstack) && this.isChild() && this.getHealth() == this.getMaxHealth()) {
                    this.consumeItemFromStack(player, itemstack);
                    this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
                }
                else if (this.isFavoriteFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    this.consumeItemFromStack(player, itemstack);
                    this.heal((float)((ItemFood)itemstack.getItem()).getHealAmount(itemstack) * 3F);
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

    public abstract void controlInput(int control, int holdAmount);

    public void controlAttack() {
        for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, getControlAttackArea(), null)) {
            if (entityLivingBase != this) {
                if (this.isTamed() && entityLivingBase instanceof EntityPlayer) {
                    if (!entityLivingBase.getUniqueID().equals(this.getOwnerId())) {
                        this.attackEntityAsMob(entityLivingBase);
                    }
                }
                else if (this.isTamed() && entityLivingBase instanceof EntityTameable) {
                    if (((EntityTameable) entityLivingBase).isTamed()) {
                        if (!((EntityTameable) entityLivingBase).getOwner().equals(this.getOwner())) {
                            this.attackEntityAsMob(entityLivingBase);
                        }
                    }
                    else {
                        this.attackEntityAsMob(entityLivingBase);
                    }
                }
                else {
                    this.attackEntityAsMob(entityLivingBase);
                }
            }
        }
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
