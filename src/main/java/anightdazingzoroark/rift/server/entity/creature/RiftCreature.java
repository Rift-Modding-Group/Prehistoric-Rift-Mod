package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import anightdazingzoroark.rift.server.items.RiftItems;
import anightdazingzoroark.rift.server.message.*;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RiftCreature extends EntityTameable implements IAnimatable {
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RANGED_ATTACKING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> STATUS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> BEHAVIOR = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ENERGY = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ACTING = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_USE_LEFT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_LEFT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEFT_CLICK_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEFT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> CAN_USE_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_RIGHT_CLICK = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> RIGHT_CLICK_USE = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RIGHT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> AGE_TICKS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> JUST_SPAWNED = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> HERD_LEADER_ID = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TAME_PROGRESS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_HOME_POS = EntityDataManager.createKey(RiftCreature.class, DataSerializers.BOOLEAN);
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
    public double minCreatureHealth = 20D;
    public double maxCreatureHealth = 20D;
    public double speed;
    private int herdCheckCountdown;
    public float attackWidth;
    public float rangedWidth;
    private int tickUse;
    private BlockPos homePosition;

    public RiftCreature(World worldIn, RiftCreatureType creatureType) {
        super(worldIn);
        this.creatureType = creatureType;
        this.minCreatureHealth = creatureType.getMinHealth();
        this.maxCreatureHealth = creatureType.getMaxHealth();
        this.setSpeed(0f);
        this.setScaleForAge(false);
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
        this.heal((float)maxCreatureHealth);
        this.herdCheckCountdown = 0;
        this.tickUse = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, Boolean.FALSE);
        this.dataManager.register(RANGED_ATTACKING, Boolean.FALSE);
        this.dataManager.register(VARIANT, rand.nextInt(4));
        this.dataManager.register(STATUS, (byte) TameStatusType.STAND.ordinal());
        this.dataManager.register(BEHAVIOR, (byte) TameBehaviorType.ASSIST.ordinal());
        this.dataManager.register(SADDLED, Boolean.FALSE);
        this.dataManager.register(ENERGY, 20);
        this.dataManager.register(ACTING, Boolean.FALSE);
        this.dataManager.register(CAN_USE_LEFT_CLICK, Boolean.TRUE);
        this.dataManager.register(USING_LEFT_CLICK, Boolean.FALSE);
        this.dataManager.register(LEFT_CLICK_USE, 0);
        this.dataManager.register(LEFT_CLICK_COOLDOWN, 0);
        this.dataManager.register(CAN_USE_RIGHT_CLICK, Boolean.FALSE);
        this.dataManager.register(USING_RIGHT_CLICK, Boolean.FALSE);
        this.dataManager.register(RIGHT_CLICK_USE, 0);
        this.dataManager.register(RIGHT_CLICK_COOLDOWN, 0);
        this.dataManager.register(HAS_TARGET, Boolean.FALSE);
        this.dataManager.register(AGE_TICKS, 0);
        this.dataManager.register(JUST_SPAWNED, true);
        this.dataManager.register(HERD_LEADER_ID, this.getEntityId());
        this.dataManager.register(TAME_PROGRESS, 0);
        this.dataManager.register(HAS_HOME_POS, Boolean.FALSE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setAgeInDays(1);
        return livingdata;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //disable default growth system
        if (this.world.isRemote) this.setScaleForAge(false);
        if (this.getGrowingAge() < 0) this.setGrowingAge(0);

        if (!this.world.isRemote) {
            this.setHasTarget(this.getAttackTarget() != null);
            this.setAgeInTicks(this.getAgeInTicks() + 1);
            this.manageAttributes();
            if (this.canDoHerding()) this.manageHerding();
        }
        if (this.isTamed() && !this.world.isRemote) {
            this.updateEnergyMove();
            this.updateEnergyActions();
            this.resetEnergyActionMod();
            this.lowEnergyEffects();
            this.eatFromInventory();
            if (this.isBeingRidden()) this.informRiderEnergy();
            this.manageTargetingBySitting();
        }
    }

    @Override
    public void setScaleForAge(boolean child) {
        float scale = RiftUtil.clamp(Math.min((0.75f/24000f) * (this.getAgeInTicks() - 24000f) + 1f, 1f), 0.25f, 1f);
        this.setScale(scale);
    }

    private void updateEnergyMove() {
        if (this.isMoving() && !this.isActing()) {
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
            if (this.energyRegenModDelay <= 20) this.energyRegenModDelay++;
            else this.energyRegenMod++;
            if (this.energyRegenMod > this.creatureType.getMaxEnergyRegenMod()) {
                this.setEnergy(this.getEnergy() + 1);
                this.energyRegenMod = 0;
                this.energyActionMod = 0;
            }
        }
    }

    private void updateEnergyActions() {
        if (this.energyActionMod >= this.creatureType.getMaxEnergyModAction()) {
            this.setEnergy(this.getEnergy() - 2);
            this.energyActionMod = 0;
        }
    }

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
        if (this.getEnergy() > 0 && this.getEnergy() <= 6) this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 2));
        else if (this.getEnergy() == 0) this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 255));
    }

    private void eatFromInventory() {
        int minSlot = this.canBeSaddled() ? 1 : 0;
        if (this.getHealth() < this.getMaxHealth()) {
            this.eatFromInvCooldown++;
            for (int i = this.creatureInventory.getSizeInventory(); i >= minSlot; i--) {
                ItemStack itemInSlot = this.creatureInventory.getStackInSlot(i);
                if (this.isFavoriteFood(itemInSlot) && this.eatFromInvCooldown > 60) {
                    this.heal((float) this.getFavoriteFoodHeal(itemInSlot));
                    itemInSlot.setCount(itemInSlot.getCount() - 1);
                    this.eatFromInvCooldown = 0;
                }
            }
        }
        else this.eatFromInvCooldown = 0;

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
        else this.eatFromInvForEnergyCooldown = 0;
    }

    private void informRiderEnergy() {
        if (!this.informLowEnergy && this.getEnergy() <= 6 && this.getEnergy() > 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.low_energy", this.getName()), false);
            this.informLowEnergy = true;
        }
        if (this.informLowEnergy && this.getEnergy() > 6) {
            this.informLowEnergy = false;
        }
        if (!this.informNoEnergy && this.getEnergy() == 0) {
            ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.no_energy", this.getName()), false);
            this.informNoEnergy = true;
        }
        if (this.informNoEnergy && this.getEnergy() > 0) this.informNoEnergy = false;
    }

    private void manageTargetingBySitting() {
        if (!this.isBeingRidden()) this.setSitting(this.getTameStatus() == TameStatusType.SIT);
        else this.setSitting(this.getAttackTarget() == null);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) this.applyEnchantments(this, entityIn);
        return flag;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed()) {
            if (this.getOwnerId().equals(player.getUniqueID())) {
                if (this.isFavoriteFood(itemstack) && !itemstack.isEmpty() && this.isBaby() && this.getHealth() == this.getMaxHealth()) {
                    this.consumeItemFromStack(player, itemstack);
                    this.setAgeInTicks(this.getAgeInTicks() + this.getFavoriteFoodGrowth(itemstack));
                    this.showGrowthParticles();
                }
                else if (this.isFavoriteFood(itemstack) && !RiftUtil.isEnergyRegenItem(itemstack.getItem(), this.creatureType.getCreatureDiet()) && this.getHealth() < this.getMaxHealth()) {
                    this.consumeItemFromStack(player, itemstack);
                    this.heal((float) this.getFavoriteFoodHeal(itemstack));
                }
                else if (this.isTamingFood(itemstack) && this.getHealth() >= this.getMaxHealth() && !this.isBaby() && this.getTameStatus() != TameStatusType.SIT) {
                    this.consumeItemFromStack(player, itemstack);
                    this.setInLove(player);
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
        else {
            if (this.isTamingFood(itemstack) && !itemstack.isEmpty() && (this.isTameableByFeeding() || itemstack.getItem() == RiftItems.CREATIVE_MEAL)) {
                if (this.getTamingFoodAdd(itemstack) + this.getTameProgress() >= 100) {
                    if (!this.world.isRemote) player.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished", new TextComponentString(this.getName())), false);
                    this.setTameProgress(0);
                    this.setTamed(true);
                    this.setOwnerId(player.getUniqueID());
                    if (this.isBaby()) this.setTameBehavior(TameBehaviorType.PASSIVE);
                }
                else this.setTameProgress(this.getTameProgress() + this.getTamingFoodAdd(itemstack));
            }
        }
        return false;
    }

    public void manageAttributes() {
        double healthValue = ((this.maxCreatureHealth - this.minCreatureHealth)/24000D) * (this.getAgeInTicks() - 24000D) + this.maxCreatureHealth;
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(RiftUtil.clamp(Math.floor(healthValue), this.minCreatureHealth, this.maxCreatureHealth));
        if (this.justSpawned()) {
            this.heal((float) this.maxCreatureHealth);
            this.setSpeed(this.speed);
            this.setJustSpawned(false);
        }
    }

    public boolean isFavoriteFood(ItemStack stack) {
        for (String foodItem : this.creatureType.getFavoriteFood()) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) return (stack.getMetadata() == itemData) || (itemData == 32767);
        }
        return false;
    }

    public int getFavoriteFoodHeal(ItemStack stack) {
        for (String foodItem : this.creatureType.getFavoriteFood()) {
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

    public boolean isTamingFood(ItemStack stack) {
        for (String foodItem : this.creatureType.getTamingFood()) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) return (stack.getMetadata() == itemData) || (itemData == 32767);
        }
        return false;
    }

    public int getTamingFoodAdd(ItemStack stack) {
        for (String foodItem : this.creatureType.getTamingFood()) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            int adder = (int)(Double.parseDouble(foodItem.substring(itemIdThird + 1)) * 100);
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == 32767) return adder;
                else if (stack.getMetadata() == itemData) return adder;
            }
        }
        return !stack.isEmpty() && stack.getItem() == RiftItems.CREATIVE_MEAL ? 100 : 0;
    }

    public int getFavoriteFoodGrowth(ItemStack stack) {
        for (String foodItem : this.creatureType.getFavoriteFood()) {
            int itemIdFirst = foodItem.indexOf(":");
            int itemIdSecond = foodItem.indexOf(":", itemIdFirst + 1);
            int itemIdThird = foodItem.indexOf(":", itemIdSecond + 1);
            String itemId = foodItem.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(foodItem.substring(itemIdSecond + 1, itemIdThird));
            double percentage = Double.parseDouble(foodItem.substring(itemIdThird + 1)) / 2D;
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId))) {
                if (itemData == 32767) return (int)(24000 * percentage);
                else if (stack.getMetadata() == itemData) return (int)(24000 * percentage);
            }
        }
        return 0;
    }

    private void showGrowthParticles() {
        double motionX = getRNG().nextGaussian() * 0.07D;
        double motionY = getRNG().nextGaussian() * 0.07D;
        double motionZ = getRNG().nextGaussian() * 0.07D;
        float f = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX) + this.getEntityBoundingBox().minX);
        float f1 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) + this.getEntityBoundingBox().minY);
        float f2 = (float) (getRNG().nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ) + this.getEntityBoundingBox().minZ);
        if (world.isRemote) this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, f, f1, f2, motionX, motionY, motionZ);
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        if (!this.isTamed() && this.canPickUpLoot()) {
            ItemStack itemstack = itemEntity.getItem();
            Item item = itemstack.getItem();
            EntityEquipmentSlot entityequipmentslot = getSlotForItemStack(itemstack);

            if (this.isFavoriteFood(itemstack) && this.canEquipItem(itemstack)) {
                this.setItemStackToSlot(entityequipmentslot, new ItemStack(Items.AIR));
                this.onItemPickup(itemEntity, itemstack.getCount());
                itemEntity.setDead();
            }
        }
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
        compound.setInteger("AgeTicks", this.getAgeInTicks());
        compound.setBoolean("JustSpawned", this.justSpawned());
        compound.setInteger("TameProgress", this.getTameProgress());
        compound.setBoolean("HasHomePos", this.getHasHomePos());
        if (this.homePosition != null && this.getHasHomePos()) {
            compound.setInteger("HomePosX", this.homePosition.getX());
            compound.setInteger("HomePosY", this.homePosition.getY());
            compound.setInteger("HomePosZ", this.homePosition.getZ());
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
        this.setEnergy(compound.getInteger("Energy"));
        this.setHasTarget(compound.getBoolean("HasTarget"));
        this.setAgeInTicks(compound.getInteger("AgeTicks"));
        this.setJustSpawned(compound.getBoolean("JustSpawned"));
        this.setTameProgress(compound.getInteger("TameProgress"));
        if (compound.getBoolean("HasHomePos")) this.setHomePos(compound.getInteger("HomePosX"), compound.getInteger("HomePosY"), compound.getInteger("HomePosZ"));
    }

    private void initInventory() {
        int inventorySize = this.slotCount() + (this.canBeSaddled() ? 1 : 0);
        this.creatureInventory = new RiftCreatureInventory("creatureInventory", inventorySize, this);
        this.creatureInventory.setCustomName(this.getName());
        if (this.creatureInventory != null) {
            for (int i = 0; i < inventorySize; ++i) {
                ItemStack itemStack = this.creatureInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) this.creatureInventory.setInventorySlotContents(i, itemStack.copy());
            }
        }
    }

    protected void setCreatureSize(float width, float height) {}

    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        if (!this.world.isRemote) this.setSaddled(saddle.getItem() == Items.SADDLE && !saddle.isEmpty());
    }

    //herdin stuff starts here
    public boolean canDoHerding() {
        return false;
    }

    public RiftCreature getHerdLeader() {
        return (RiftCreature) this.world.getEntityByID(this.getHerdLeaderId());
    }

    public int getHerdLeaderId() {
        return this.dataManager.get(HERD_LEADER_ID).intValue();
    }

    public boolean isHerdLeader() {
        return this.getEntityId() == this.getHerdLeaderId();
    }

    public void setHerdLeader(int value) {
        this.dataManager.set(HERD_LEADER_ID, value);
    }

    private void manageHerding() {
        this.herdCheckCountdown--;
        if (this.herdCheckCountdown <= 0) {
            //add members to herd
            List<RiftCreature> potentialHerders = this.world.getEntitiesWithinAABB(this.getClass(), this.getHerdBoundingBox(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature input) {
                    return !input.isTamed();
                }
            });
            int herdLeaderId = Collections.min(potentialHerders.stream().map(RiftCreature::getEntityId).collect(Collectors.toList()));
            this.setHerdLeader(herdLeaderId);
            this.herdCheckCountdown = RiftUtil.randomInRange(10, 15) * 20;
        }
    }

    public double getHerdDist() {
        return 24D;
    }

    public double getDoubleHerdDist() {
        return this.getHerdDist() * this.getHerdDist();
    }

    public boolean isNearHerdLeader() {
        return this.getDistanceSq(this.getHerdLeader()) <= this.getDoubleHerdDist();
    }

    public AxisAlignedBB getHerdBoundingBox() {
        return this.getEntityBoundingBox().grow(this.getHerdDist() * 2, this.getHerdDist() * 2, this.getHerdDist() * 2);
    }

    //herdin stuff stops here


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
        this.setActing(value);
    }

    public boolean isRangedAttacking() {
        return this.dataManager.get(RANGED_ATTACKING);
    }

    public void setRangedAttacking(boolean value) {
        this.dataManager.set(RANGED_ATTACKING, Boolean.valueOf(value));
        this.setActing(value);
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

    public int getTameProgress() {
        return RiftUtil.clamp(this.dataManager.get(TAME_PROGRESS), 0, 100);
    }

    public void setTameProgress(int value) {
        this.dataManager.set(TAME_PROGRESS, RiftUtil.clamp(value, 0, 100));
    }

    private void setSpeed(double value) {
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(value);
    }

    public void resetSpeed() {
        this.setSpeed(this.speed);
    }

    public void removeSpeed() {
        this.setSpeed(0D);
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

    public boolean canUseLeftClick() {
        return this.dataManager.get(CAN_USE_LEFT_CLICK);
    }

    public void setCanUseLeftClick(boolean value) {
        this.dataManager.set(CAN_USE_LEFT_CLICK, Boolean.valueOf(value));
    }

    public boolean isUsingLeftClick() {
        return this.dataManager.get(USING_LEFT_CLICK);
    }

    public void setUsingLeftClick(boolean value) {
        this.dataManager.set(USING_LEFT_CLICK, Boolean.valueOf(value));
    }

    public int getLeftClickUse() {
        return this.dataManager.get(LEFT_CLICK_USE).intValue();
    }

    public void setLeftClickUse(int value) {
        this.dataManager.set(LEFT_CLICK_USE, value);
        this.tickUse = 0;
    }

    public int getLeftClickCooldown() {
        return Math.max(0, this.dataManager.get(LEFT_CLICK_COOLDOWN));
    }

    public void setLeftClickCooldown(int value) {
        this.dataManager.set(LEFT_CLICK_COOLDOWN, Math.max(0, value));
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
        this.tickUse = 0;
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

    public int getAgeInTicks() {
        return this.dataManager.get(AGE_TICKS);
    }

    public int getAgeInDays() {
        return this.dataManager.get(AGE_TICKS) / 24000;
    }

    public void setAgeInTicks(int value) {
        this.dataManager.set(AGE_TICKS, value);
    }

    public void setAgeInDays(int value) {
        this.dataManager.set(AGE_TICKS, value * 24000);
    }

    public boolean justSpawned() {
        return this.dataManager.get(JUST_SPAWNED);
    }

    public void setJustSpawned(boolean value) {
        this.dataManager.set(JUST_SPAWNED, value);
    }

    public void setHomePos() {
        this.dataManager.set(HAS_HOME_POS, true);
        this.homePosition = new BlockPos(this);
    }

    public void setHomePos(int x, int y, int z) {
        this.dataManager.set(HAS_HOME_POS, true);
        this.homePosition = new BlockPos(x, y, z);
    }

    public void clearHomePos() {
        this.dataManager.set(HAS_HOME_POS, false);
        this.homePosition = null;
    }

    public boolean getHasHomePos() {
        return this.dataManager.get(HAS_HOME_POS);
    }

    public BlockPos getHomePos() {
        return this.homePosition;
    }

    public boolean isBaby() {
        return this.getAgeInDays() < 1;
    }

    public boolean isMoving() {
        double fallMotion = !this.onGround ? this.motionY : 0;
        return Math.sqrt((this.motionX * this.motionX) + (fallMotion * fallMotion) + (this.motionZ * this.motionZ)) > 0;
    }

    public boolean isApexPredator() {
        return false;
    }

    public boolean isTameableByFeeding() {
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

        this.rotationYaw = passenger.rotationYaw;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch = passenger.rotationPitch * 0.5f;
        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.renderYawOffset = this.rotationYaw;

        passenger.setPosition(riderPos().x, riderPos().y + passenger.height, riderPos().z);

        ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
        if (this.isDead) passenger.dismountRidingEntity();
    }

    public abstract Vec3d riderPos();

    public abstract void controlInput(int control, int holdAmount, EntityLivingBase target);

    public abstract boolean hasLeftClickChargeBar();

    public abstract boolean hasRightClickChargeBar();

    public void controlAttack() {
        EntityLivingBase target;
        if (this.ssrTarget == null) target = this.getControlAttackTargets();
        else target = this.ssrTarget;
        if (target != null) {
            if (this.isTamed() && target instanceof EntityPlayer) {
                if (!target.getUniqueID().equals(this.getOwnerId())) this.attackEntityAsMob(target);
            }
            else if (this.isTamed() && target instanceof EntityTameable) {
                if (((EntityTameable) target).isTamed()) {
                    if (!((EntityTameable) target).getOwner().equals(this.getOwner())) this.attackEntityAsMob(target);
                }
                else this.attackEntityAsMob(target);
            }
            else this.attackEntityAsMob(target);
        }
    }

    public EntityLivingBase getControlAttackTargets() {
        double dist = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX + (double)this.attackWidth;
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

    public void controlRangedAttack(double strength) {}

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
                float moveSpeedMod = (this.getEnergy() > 6 ? 1f : this.getEnergy() > 0 ? 0.5f : 0f);
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = ((float)(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()) - riderSpeed) * moveSpeedMod;
                this.setAIMoveSpeed(this.onGround ? moveSpeed + (controller.isSprinting() && this.getEnergy() > 6 ? moveSpeed * 0.3f : 0) : 2);
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
        RiftEgg egg = new RiftEgg(this.world);
        egg.setCreatureType(this.creatureType);
        egg.setOwnerId(this.getOwnerId());
        egg.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
        egg.enablePersistence();
        egg.setHatchTime(this.creatureType.getHatchTime() * 20);
        return egg;
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