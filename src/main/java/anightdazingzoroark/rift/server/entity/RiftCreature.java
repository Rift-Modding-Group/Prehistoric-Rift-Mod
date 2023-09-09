package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.ai.RiftAggressiveModeGetTargets;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import anightdazingzoroark.rift.server.message.RiftChangeInventoryFromMenu;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftStartRiding;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class RiftCreature extends EntityTameable implements IAnimatable {
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    public final RiftCreatureType creatureType;
    public AnimationFactory factory = new AnimationFactory(this);
    public final RiftAggressiveModeGetTargets getAggressiveModeTargets = new RiftAggressiveModeGetTargets(this, true);
    public final EntityAIOwnerHurtByTarget defendOwner =  new EntityAIOwnerHurtByTarget(this);
    public final EntityAIOwnerHurtTarget attackForOwner = new EntityAIOwnerHurtTarget(this);
    public boolean isRideable;
    public RiftCreatureInventory creatureInventory;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.initInventory();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, Boolean.FALSE);
        this.dataManager.register(VARIANT, rand.nextInt(4));
        this.dataManager.register(STATUS, (byte) TameStatusType.STAND.ordinal());
        this.dataManager.register(BEHAVIOR, (byte) TameBehaviorType.ASSIST.ordinal());
        this.dataManager.register(SADDLED, Boolean.FALSE);
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

    public boolean isFavoriteFood(ItemStack stack) {
        return false;
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
    }

    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY, this.posZ);
    }

    public void controlAttack() {
        for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, getControlAttackArea(), null)) {
            if (entityLivingBase != this) {
                if (entityLivingBase instanceof EntityTameable) {
                    if (!((EntityTameable)entityLivingBase).getOwner().equals(this.getOwner())) {
                        entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    }
                }
                if (entityLivingBase instanceof EntityPlayer) {
                    if (!this.getOwner().equals(entityLivingBase)) {
                        entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    }
                }
                entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            }
        }
    }

    public AxisAlignedBB getControlAttackArea() {
        double xBoxMin = this.posX + Math.cos(this.getLookVec().x);
        double xBoxMax = this.posX + Math.cos(this.getLookVec().x);
        double zBoxMin = this.posZ + Math.sin(this.getLookVec().z);
        double zBoxMax = this.posZ + Math.sin(this.getLookVec().z);
        return new AxisAlignedBB(xBoxMin, this.posY, zBoxMin, xBoxMax, this.posY + 5.0D, zBoxMax);
    }

    @Override
    public boolean canPassengerSteer() {
        return false;
    }

    @Override
    public boolean canBeSteered() {
        return true;
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
                this.fallDistance = 0;
                this.setAIMoveSpeed(onGround ? (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() : 2);
                super.travel(strafe, vertical, forward);
            }
        }
        else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void registerControllers(AnimationData data) {}

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
