package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class Dimetrodon extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dimetrodon"));
    private static final DataParameter<Byte> TEMPERATURE = EntityDataManager.createKey(Dimetrodon.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> FORCED_TEMPERATURE = EntityDataManager.createKey(Dimetrodon.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> FORCED_TEMPERATURE_TIME = EntityDataManager.createKey(Dimetrodon.class, DataSerializers.VARINT);

    private RiftCreaturePart neckPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Dimetrodon(World worldIn) {
        super(worldIn, RiftCreatureType.DIMETRODON);
        this.minCreatureHealth = DimetrodonConfig.getMinHealth();
        this.maxCreatureHealth = DimetrodonConfig.getMaxHealth();
        this.setSize(1f, 1f);
        this.favoriteFood = DimetrodonConfig.dimetrodonFavoriteFood;
        this.tamingFood = DimetrodonConfig.dimetrodonTamingFood;
        this.experienceValue = 3;
        this.speed = 0.20D;
        this.isRideable = false;
        this.attackWidth = 3f;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TEMPERATURE, (byte) EggTemperature.NEUTRAL.ordinal());
        this.dataManager.register(FORCED_TEMPERATURE, false);
        this.dataManager.register(FORCED_TEMPERATURE_TIME, 0);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(DimetrodonConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, DimetrodonConfig.dimetrodonTargets, DimetrodonConfig.dimetrodonTargetBlacklist, false, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, DimetrodonConfig.dimetrodonFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, 0.52F, 0.52F));
        this.tasks.addTask(3, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(5, new RiftWander(this, 1.0D));
        this.tasks.addTask(6, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageForcedTemperatureTime();
        this.dynamicTemperature();
        this.showTemperatureParticles();
    }

    private void manageForcedTemperatureTime() {
        if (this.isTemperatureForced()) {
            if (this.getForcedTemperatureTime() > 0) this.setForcedTemperatureTime(this.getForcedTemperatureTime() - 1);
            else if (this.getForcedTemperatureTime() == 0) this.setTemperatureForced(false);
        }
    }

    private void showTemperatureParticles() {
        if (this.getTemperature().equals(EggTemperature.WARM) || this.getTemperature().equals(EggTemperature.VERY_WARM)) {
            double motionY = RiftUtil.randomInRange(0.0D, 0.15D);
            double f = this.getRNG().nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX) + this.getEntityBoundingBox().minX;
            double f1 = 0.05D * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) + this.getEntityBoundingBox().minY;
            double f2 = this.getRNG().nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ) + this.getEntityBoundingBox().minZ;
            if (this.world.isRemote) this.world.spawnParticle(EnumParticleTypes.FLAME, f, f1, f2, motionX, motionY, motionZ);
        }
        else if (this.getTemperature().equals(EggTemperature.COLD) || this.getTemperature().equals(EggTemperature.VERY_COLD)) {
            double motionY = RiftUtil.randomInRange(-0.75D, -0.25D);
            double f = this.getRNG().nextFloat() * (this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX) + this.getEntityBoundingBox().minX;
            double f1 = 0.05D * (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) + this.getEntityBoundingBox().minY;
            double f2 = this.getRNG().nextFloat() * (this.getEntityBoundingBox().maxZ - this.getEntityBoundingBox().minZ) + this.getEntityBoundingBox().minZ;
            if (this.world.isRemote) RiftInitialize.PROXY.spawnParticle("snow", f, f1, f2, 0D, motionY, 0D);
        }
    }

    private void dynamicTemperature() {
        if (!this.isTemperatureForced()) {
            EggTemperature temperature = RiftUtil.getCorrespondingTempFromBiome(this.world, this.getPosition());
            switch (temperature) {
                case VERY_COLD:
                    this.setTemperature(EggTemperature.VERY_WARM);
                    break;
                case COLD:
                    this.setTemperature(EggTemperature.WARM);
                    break;
                case WARM:
                    this.setTemperature(EggTemperature.COLD);
                    break;
                case VERY_WARM:
                    this.setTemperature(EggTemperature.VERY_COLD);
                    break;
                default:
                    this.setTemperature(EggTemperature.NEUTRAL);
                    break;
            }
        }
    }

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 1.25f, 0, 0.45f, 0.5f * scale, 0.425f * scale, 1.5f);
            this.bodyPart = new RiftMainBodyPart(this, 0, 0, 0.3f, scale, 0.5f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 0.75f, 0, 0.45f, 0.375f * scale, 0.375f * scale, 1.5f);
            this.tail0Part = new RiftCreaturePart(this, -0.8f, 0, 0.4f, 0.375f * scale, 0.375f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -1.2f, 0, 0.35f, 0.375f * scale, 0.375f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -1.6f, 0, 0.3f, 0.375f * scale, 0.375f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.25f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.neckPart != null) {
            this.world.removeEntityDangerously(this.neckPart);
            this.neckPart = null;
        }
        if (this.tail0Part != null) {
            this.world.removeEntityDangerously(this.tail0Part);
            this.tail0Part = null;
        }
        if (this.tail1Part != null) {
            this.world.removeEntityDangerously(this.tail1Part);
            this.tail1Part = null;
        }
        if (this.tail2Part != null) {
            this.world.removeEntityDangerously(this.tail2Part);
            this.tail2Part = null;
        }
    }


    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed()) {
            try {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    if (this.isTemperatureSettingItem(itemstack)) {
                        this.setTemperatureForced(true);
                        this.setTemperature(this.getTemperatureFromItem(itemstack));
                        this.setForcedTemperatureTime(this.getTemperatureTimeFromItem(itemstack));
                        if (!player.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty()) player.setHeldItem(hand, new ItemStack(Items.BOWL));
                            else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.BOWL))) player.dropItem(new ItemStack(Items.BOWL), false);
                        }
                        return true;
                    }
                }
            }
            catch (Exception e) {
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("Temperature", (byte) this.getTemperature().ordinal());
        compound.setBoolean("ForcedTemperature", this.isTemperatureForced());
        compound.setInteger("ForcedTemperatureTime", this.getForcedTemperatureTime());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Temperature")) this.setTemperature(EggTemperature.values()[compound.getByte("Temperature")]);
        this.setTemperatureForced(compound.getBoolean("ForcedTemperature"));
        this.setForcedTemperatureTime(compound.getInteger("ForcedTemperatureTime"));
    }

    private boolean isTemperatureSettingItem (ItemStack itemstack) {
        for (String item : DimetrodonConfig.dimetrodonForcedTemperatureItems) {
            int itemIdFirst = item.indexOf(":");
            int itemIdSecond = item.indexOf(":", itemIdFirst + 1);
            int itemIdThird = item.indexOf(":", itemIdSecond + 1);
            String itemId = item.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(item.substring(itemIdSecond + 1, itemIdThird));
            if (!itemstack.isEmpty() && itemstack.getItem().equals(Item.getByNameOrId(itemId)) && ((itemstack.getMetadata() == itemData) || (itemData == -1))) {
                return true;
            }
        }
        return false;
    }

    private EggTemperature getTemperatureFromItem(ItemStack itemstack) {
        for (String item : DimetrodonConfig.dimetrodonForcedTemperatureItems) {
            int itemIdFirst = item.indexOf(":");
            int itemIdSecond = item.indexOf(":", itemIdFirst + 1);
            int itemIdThird = item.indexOf(":", itemIdSecond + 1);
            int itemIdFourth = item.indexOf(":", itemIdThird + 1);
            String itemId = item.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(item.substring(itemIdSecond + 1, itemIdThird));
            String temperatureType = item.substring(itemIdFourth + 1);
            if (!itemstack.isEmpty() && itemstack.getItem().equals(Item.getByNameOrId(itemId)) && (itemstack.getMetadata() == itemData) || (itemData == -1)) {
                return EggTemperature.valueOf(temperatureType);
            }
        }
        return null;
    }

    private int getTemperatureTimeFromItem(ItemStack itemstack) {
        for (String item : DimetrodonConfig.dimetrodonForcedTemperatureItems) {
            int itemIdFirst = item.indexOf(":");
            int itemIdSecond = item.indexOf(":", itemIdFirst + 1);
            int itemIdThird = item.indexOf(":", itemIdSecond + 1);
            int itemIdFourth = item.indexOf(":", itemIdThird + 1);
            String itemId = item.substring(0, itemIdSecond);
            int itemData = Integer.parseInt(item.substring(itemIdSecond + 1, itemIdThird));
            int time = Integer.parseInt(item.substring(itemIdThird + 1, itemIdFourth));
            if (!itemstack.isEmpty() && itemstack.getItem().equals(Item.getByNameOrId(itemId)) && (itemstack.getMetadata() == itemData) || (itemData == -1)) {
                return time;
            }
        }
        return 0;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) {
            this.applyEnchantments(this, entityIn);
            if (this.getTemperature().equals(EggTemperature.VERY_WARM)) entityIn.setFire(15);
            else if (this.getTemperature().equals(EggTemperature.WARM)) entityIn.setFire(5);
            else if (this.getTemperature().equals(EggTemperature.COLD)) {
                EntityLivingBase entityLivingBase = (EntityLivingBase)entityIn;
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 0));
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 0));
            }
            else if (this.getTemperature().equals(EggTemperature.VERY_COLD)) {
                EntityLivingBase entityLivingBase = (EntityLivingBase)entityIn;
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 300, 0));
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 300, 0));
            }
        }
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.4f, 1.25f);
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public int slotCount() {
        return 9;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    public EggTemperature getTemperature() {
        return EggTemperature.values()[this.dataManager.get(TEMPERATURE).byteValue()];
    }

    public void setTemperature(EggTemperature value) {
        this.dataManager.set(TEMPERATURE, (byte)value.ordinal());
    }

    public boolean isTemperatureForced() {
        return this.dataManager.get(FORCED_TEMPERATURE);
    }

    public void setTemperatureForced(boolean value) {
        this.dataManager.set(FORCED_TEMPERATURE, value);
    }

    public int getForcedTemperatureTime() {
        return this.dataManager.get(FORCED_TEMPERATURE_TIME);
    }

    public void setForcedTemperatureTime(int value) {
        this.dataManager.set(FORCED_TEMPERATURE_TIME, value);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::dimetrodonMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::dimetrodonAttack));
    }

    private <E extends IAnimatable> PlayState dimetrodonMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState dimetrodonAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }
}
