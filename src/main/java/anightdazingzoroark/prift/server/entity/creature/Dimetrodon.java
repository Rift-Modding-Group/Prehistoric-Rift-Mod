package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.config.json.JsonTemperature;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
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
        this.setSize(1f, 1f);
        this.favoriteFood = ((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.experienceValue = 10;
        this.speed = 0.20D;
        this.isRideable = false;
        this.targetList = RiftUtil.creatureTargets(((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.targetWhitelist, ((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 1.25f, 0, 0.45f, 0.5f, 0.425f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.3f, 1f, 0.5f, 1f);
        this.neckPart = new RiftCreaturePart(this, 0.75f, 0, 0.45f, 0.375f, 0.375f, 1.5f);
        this.tail0Part = new RiftCreaturePart(this, -0.8f, 0, 0.4f, 0.375f, 0.375f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.2f, 0, 0.35f, 0.375f, 0.375f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -1.6f, 0, 0.3f, 0.375f, 0.375f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TEMPERATURE, (byte) EggTemperature.NEUTRAL.ordinal());
        this.dataManager.register(FORCED_TEMPERATURE, false);
        this.dataManager.register(FORCED_TEMPERATURE_TIME, 0);
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 0.52F, 0.52F));
        this.tasks.addTask(4, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        //this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageForcedTemperatureTime();
        if (!this.world.isRemote) this.dynamicTemperature();
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
            if (GeneralConfig.canUseSimpleDiff()) {
                //for default temperature
                float temperatureValue = (TemperatureEnum.NORMAL.getUpperBound() + TemperatureEnum.COLD.getUpperBound()) / 2f;

                //for altitude
                if (this.world.provider.isSurfaceWorld()) {
                    temperatureValue += -1.0f * (Math.abs(((64.0f - (float)this.posY) / 64.0f * ModConfig.server.temperature.altitudeMultiplier) + 1.0f));
                }

                //for biome
                float biomeAverage = (this.getTempForBiome(this.world.getBiome(this.getPosition().add(10,0,0))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(-10,0,0))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(0,0,10))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(0,0,-10))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(7,0,7))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(7,0,-7))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(-7,0,7))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition().add(-7,0,-7))) +
                        this.getTempForBiome(this.world.getBiome(this.getPosition())))/9.0f;
                temperatureValue += this.dynamicTempUnderground(this.normalizeToPlusMinus(biomeAverage) * ModConfig.server.temperature.biomeMultiplier);

                //for dimension
                JsonTemperature tempInfoDim = JsonConfig.dimensionTemperature.get(""+world.provider.getDimension());
                if (tempInfoDim != null) temperatureValue += tempInfoDim.temperature;

                //for snow
                if(this.world.isRaining() && this.world.canSeeSky(this.getPosition())) {
                    Biome biome = world.getBiome(this.getPosition());
                    if (biome.getEnableSnow()) temperatureValue += ModConfig.server.temperature.snowValue;
                    else {
                        if (this.world.canSnowAt(this.getPosition(), false)) temperatureValue += ModConfig.server.temperature.snowValue;
                    }
                }

                //fluids or rain
                IBlockState state = this.world.getBlockState(this.getPosition());
                Block block = state.getBlock();

                if (block instanceof IFluidBlock) {
                    //Modded fluid
                    Fluid fluid = ((IFluidBlock)block).getFluid();
                    if(fluid != null) {
                        JsonTemperature tempInfoFluid = JsonConfig.fluidTemperatures.get(fluid.getName());
                        if (tempInfoFluid != null) temperatureValue += tempInfoFluid.temperature;
                    }
                }

                //vanilla fluid, or modded fluid with no override, or no fluid at all, or rain
                if (state.getMaterial() == Material.WATER) temperatureValue += ModConfig.server.temperature.wetValue;
                else if(world.isRainingAt(this.getPosition())) temperatureValue += ModConfig.server.temperature.wetValue;

                //for time
                if (this.world.provider.isSurfaceWorld()) {
                    long time = this.world.getWorldTime() % 24000;
                    if ((time >= 12000 || ModConfig.server.temperature.timeTemperatureDay) && (time < 12000 || ModConfig.server.temperature.timeTemperatureNight)) {
                        float timetemperature = (Math.abs(((time % 12000.0f) - 6000.0f)/6000.0f) - 1.0f) * ModConfig.server.temperature.timeMultiplier;
                        if (time < 12000) timetemperature *= -1.0f;
                        float biomeMultiplier = 1.0f + (Math.abs(this.normalizeToPlusMinus(this.getTempForBiome(this.world.getBiome(this.getPosition())))) * ((float)ModConfig.server.temperature.timeBiomeMultiplier - 1.0f));
                        timetemperature *= biomeMultiplier;
                        //for shade
                        int shadeConf = ModConfig.server.temperature.timeTemperatureShade;
                        if (timetemperature > 0 && shadeConf != 0 && !this.world.canSeeSky(this.getPosition()) && !this.world.canSeeSky(this.getPosition().up())) {
                            timetemperature = Math.max(0, timetemperature + shadeConf);
                        }
                        temperatureValue += this.dynamicTempUnderground(timetemperature);
                    }
                }

                if (temperatureValue >= 0f && temperatureValue <= 5f) this.setTemperature(EggTemperature.VERY_WARM);
                else if (temperatureValue >= 6f && temperatureValue <= 10f) this.setTemperature(EggTemperature.WARM);
                else if (temperatureValue >= 11f && temperatureValue <= 14f) this.setTemperature(EggTemperature.NEUTRAL);
                else if (temperatureValue >= 15f && temperatureValue <= 19f) this.setTemperature(EggTemperature.COLD);
                else if (temperatureValue >= 20f) this.setTemperature(EggTemperature.VERY_COLD);
            }
            else {
                if ((this.world.isRaining() && this.world.getBiome(this.getPosition()).canRain()) || this.inWater) {
                    EggTemperature temperature = RiftUtil.getCorrespondingTempFromBiome(this.world, this.getPosition());
                    switch (temperature) {
                        case VERY_COLD:
                            this.setTemperature(EggTemperature.VERY_WARM);
                        case COLD:
                            this.setTemperature(EggTemperature.VERY_WARM);
                        case NEUTRAL:
                            this.setTemperature(EggTemperature.WARM);
                            break;
                        case WARM:
                            this.setTemperature(EggTemperature.NEUTRAL);
                            break;
                        case VERY_WARM:
                            this.setTemperature(EggTemperature.COLD);
                            break;
                    }
                }
                else {
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
        }
    }

    //for simple difficulty compat
    private float normalizeToPlusMinus(float value) {
        return (value * 2.0f) - 1.0f;
    }

    private float dynamicTempUnderground(float temperature) {
        if (this.posY >= 64) return temperature;

        if(!ModConfig.server.temperature.undergroundEffect || !this.world.provider.isSurfaceWorld()) return temperature;

        if (this.world.canSeeSky(this.getPosition()) || this.world.canSeeSky(this.getPosition().up())) return temperature;

        int cutoff = ModConfig.server.temperature.undergroundEffectCutoff;

        if (this.posY <= cutoff || cutoff == 64) return 0.0f;

        return temperature * (float)((float)this.posY - cutoff) / (64.0f - cutoff);
    }

    protected float getTempForBiome(Biome biome) {
        return MathHelper.clamp(biome.getDefaultTemperature(), 0.0f, 1.35f)/1.35f;
    }
    //simple difficulty compat ends here

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.25f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
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
        boolean flag = false;
        for (DimetrodonConfig.TemperatureChangingItem item : ((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.temperatureChangingItems) {
            if (!flag) flag = RiftUtil.itemStackEqualToString(itemstack, item.itemId);
        }
        return flag;
    }

    private EggTemperature getTemperatureFromItem(ItemStack itemstack) {
        DimetrodonConfig.TemperatureChangingItem itemToGet = new DimetrodonConfig.TemperatureChangingItem("", "NEUTRAL", 0);
        boolean flag = false;
        for (DimetrodonConfig.TemperatureChangingItem item : ((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.temperatureChangingItems) {
            if (!flag) {
                flag = RiftUtil.itemStackEqualToString(itemstack, item.itemId);
                itemToGet = item;
            }
        }
        if (flag) return EggTemperature.valueOf(itemToGet.temperatureMode);
        return null;
    }

    private int getTemperatureTimeFromItem(ItemStack itemstack) {
        DimetrodonConfig.TemperatureChangingItem itemToGet = new DimetrodonConfig.TemperatureChangingItem("", "NEUTRAL", 0);
        boolean flag = false;
        for (DimetrodonConfig.TemperatureChangingItem item : ((DimetrodonConfig)RiftConfigHandler.getConfig(this.creatureType)).general.temperatureChangingItems) {
            if (!flag) {
                flag = RiftUtil.itemStackEqualToString(itemstack, item.itemId);
                itemToGet = item;
            }
        }
        if (flag) return itemToGet.ticks;
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
    public float[] ageScaleParams() {
        return new float[]{0.4f, 1.25f};
    }

    public float attackWidth() {
        return 3f;
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
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {}

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
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::dimetrodonMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::dimetrodonAttack));
    }

    private <E extends IAnimatable> PlayState dimetrodonMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.sitting", true));
                return PlayState.CONTINUE;
            }
            if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.walk", true));
                return PlayState.CONTINUE;
            }
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

    protected SoundEvent getAmbientSound() {
        return RiftSounds.DIMETRODON_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DIMETRODON_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DIMETRODON_DEATH;
    }
}
