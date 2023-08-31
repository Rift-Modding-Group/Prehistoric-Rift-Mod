package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.ClientProxy;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.ai.RiftAggressiveModeGetTargets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class RiftCreature extends EntityTameable implements IAnimatable, IInventoryChangedListener {
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
    public InventoryBasic creatureInventory;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
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
        if (this.getOwnerId().equals(player.getUniqueID())) {
            if (this.isFavoriteFood(itemstack) && this.isChild() && this.getHealth() == this.getMaxHealth()) {
                this.consumeItemFromStack(player, itemstack);
                this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
            }
            else if (this.isFavoriteFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                this.consumeItemFromStack(player, itemstack);
                this.heal((float)((ItemFood)itemstack.getItem()).getHealAmount(itemstack) * 3F);
            }
            else if (itemstack.isEmpty()) {
                ClientProxy.CREATURE = this;
                player.openGui(RiftInitialize.instance, ServerProxy.GUI_DIAL, world, 0, 0, 0);
            }
            return true;
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
            for (int i = 0; i < creatureInventory.getSizeInventory(); ++i) {
                ItemStack itemstack = creatureInventory.getStackInSlot(i);
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
                if (j <= inventorySize) {
                    creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
                }
            }
        }
        else {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                creatureInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
    }

    public void initInventory() {
        int inventorySize = this.slotCount() + (this.canBeSaddled() ? 1 : 0);
        creatureInventory = new InventoryBasic("creatureInventory", false, inventorySize);
        creatureInventory.setCustomName(this.getName());
        if (creatureInventory != null) {
            for (int i = 0; i < inventorySize; ++i) {
                ItemStack itemStack = creatureInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    creatureInventory.setInventorySlotContents(i, itemStack.copy());
                }
            }
        }
        this.creatureInventory.addInventoryChangeListener(this);
    }

    public void onInventoryChanged(IInventory invBasic) {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        System.out.println(saddle.getItem() == Items.SADDLE && !saddle.isEmpty());
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
}
