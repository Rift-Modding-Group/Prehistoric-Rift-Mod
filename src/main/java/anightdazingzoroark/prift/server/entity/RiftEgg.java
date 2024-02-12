package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.config.json.JsonTemperature;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RiftEgg extends EntityTameable implements IAnimatable {
    private static final DataParameter<Integer> HATCH_TIME = EntityDataManager.<Integer>createKey(RiftEgg.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> EGG_TYPE = EntityDataManager.createKey(RiftEgg.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> TEMPERATURE = EntityDataManager.createKey(RiftEgg.class, DataSerializers.BYTE);

    public AnimationFactory factory = new AnimationFactory(this);

    public RiftEgg(World worldIn) {
        super(worldIn);
        this.setSize(1F, 1F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HATCH_TIME, 20);
        this.dataManager.register(EGG_TYPE, (byte) RiftCreatureType.TYRANNOSAURUS.ordinal());
        this.dataManager.register(TEMPERATURE, (byte) EggTemperature.NEUTRAL.ordinal());
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        //manage temperature
        if (!this.world.isRemote) this.temperatureFromExtSources();

        //manage hatching
        if (this.getTemperature().equals(this.getCreatureType().getEggTemperature())) this.setHatchTime(this.getHatchTime() - 1);
        if (this.getHatchTime() == 0) {
            RiftCreature creature = this.getCreatureType().invokeClass(this.world);
            creature.setHealth((float) creature.minCreatureHealth);
            creature.setAgeInDays(0);

            if (this.getOwnerId() != null) {
                creature.setTamed(true);
                creature.setOwnerId(this.getOwnerId());
                creature.setTameStatus(TameStatusType.SIT);
                creature.setTameBehavior(TameBehaviorType.PASSIVE);
            }

            creature.setLocationAndAngles(Math.floor(this.posX), Math.floor(this.posY) + 1, Math.floor(this.posZ), this.world.rand.nextFloat() * 360.0F, 0.0F);
            if (!this.world.isRemote) {
                List<EntityPlayer> nearby = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - 50.0, this.posY - 50.0, this.posZ - 50.0, this.posX + 50.0, this.posY + 50.0, this.posZ + 50.0));
                for (EntityPlayer player : nearby) {
                    if (player.getUniqueID().equals(this.getOwnerId())) player.sendStatusMessage(new TextComponentTranslation("prift.notify.egg_hatched"), false);
                }
                this.world.spawnEntity(creature);
            }
            this.setDead();
        }
    }

    private void temperatureFromExtSources() {
        if (Loader.isModLoaded(RiftInitialize.SIMPLE_DIFFICULTY_MOD_ID)) {
            //for default temperature
            float temperatureValue = (TemperatureEnum.NORMAL.getUpperBound() + TemperatureEnum.COLD.getUpperBound()) / 2;

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

            //for dimetrodons
            for (EntityLivingBase entityLivingBase : world.getEntities(EntityLiving.class, new Predicate<EntityLiving>() {
                @Override
                public boolean apply(@Nullable EntityLiving input) {
                    return true;
                }
            })) {
                if (entityLivingBase instanceof Dimetrodon) {
                    Dimetrodon dimetrodon = (Dimetrodon) entityLivingBase;
                    if (dimetrodon.getEntityBoundingBox().grow(8.0D).intersects(this.getEntityBoundingBox())) {
                        switch (dimetrodon.getTemperature()) {
                            case VERY_COLD:
                                temperatureValue += this.changeByDistanceFromDimetrodon(DimetrodonConfig.dimetrodonVeryColdValue, dimetrodon.getEntityBoundingBox());
                                break;
                            case COLD:
                                temperatureValue += this.changeByDistanceFromDimetrodon(DimetrodonConfig.dimetrodonColdValue, dimetrodon.getEntityBoundingBox());
                                break;
                            case WARM:
                                temperatureValue += this.changeByDistanceFromDimetrodon(DimetrodonConfig.dimetrodonWarmValue, dimetrodon.getEntityBoundingBox());
                                break;
                            case VERY_WARM:
                                temperatureValue += this.changeByDistanceFromDimetrodon(DimetrodonConfig.dimetrodonVeryWarmValue, dimetrodon.getEntityBoundingBox());
                                break;
                        }
                    }
                }
            }

            //final setting of temperature
            if (temperatureValue >= 0f && temperatureValue <= 5f) this.setTemperature(EggTemperature.VERY_COLD);
            else if (temperatureValue >= 6f && temperatureValue <= 10f) this.setTemperature(EggTemperature.COLD);
            else if (temperatureValue >= 11f && temperatureValue <= 14f) this.setTemperature(EggTemperature.NEUTRAL);
            else if (temperatureValue >= 15f && temperatureValue <= 19f) this.setTemperature(EggTemperature.WARM);
            else if (temperatureValue >= 20f) this.setTemperature(EggTemperature.VERY_WARM);
        }
        else {
            float biomeTempWeight = (float)RiftUtil.getCorrespondingTempFromBiome(this.world, this.getPosition()).getTempStrength();
            List<Float> tempList = new ArrayList<>();
            tempList.add(biomeTempWeight);
            for (Dimetrodon dimetrodon : this.world.getEntities(Dimetrodon.class, new Predicate<Dimetrodon>() {
                @Override
                public boolean apply(@Nullable Dimetrodon input) {
                    return true;
                }
            })) {
                AxisAlignedBB dimetrodonBoundBox = dimetrodon.getEntityBoundingBox().grow(8.0D);
                if (dimetrodonBoundBox.intersects(this.getEntityBoundingBox())) {
                    tempList.add((float)dimetrodon.getTemperature().getTempStrength());
                }
            }
            int tempValue = Math.round((float)tempList.stream().mapToDouble(Float::doubleValue).average().getAsDouble());
            this.setTemperature(EggTemperature.values()[tempValue]);
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

    private float changeByDistanceFromDimetrodon(float temperatureValue, AxisAlignedBB dimetrodonAABB) {
        // First bounding box, grown by double its size
        AxisAlignedBB firstBoundingBox = dimetrodonAABB.grow(2.0D);

        // Second bounding box, grown to a fixed 8-block radius from the center
        AxisAlignedBB secondBoundingBox = dimetrodonAABB.grow(8.0D);

        Vec3d posVec = new Vec3d(this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ());
        if (firstBoundingBox.contains(posVec) && secondBoundingBox.contains(posVec)) return temperatureValue;
        else if (!firstBoundingBox.contains(posVec) && secondBoundingBox.contains(posVec)) {
            double distFromCenterX = this.getPosition().getX() - dimetrodonAABB.getCenter().x;
            double distFromCenterY = this.getPosition().getY() - dimetrodonAABB.getCenter().y;
            double distFromCenterZ = this.getPosition().getZ() - dimetrodonAABB.getCenter().z;
            double dist = Math.sqrt(distFromCenterX * distFromCenterX + distFromCenterY * distFromCenterY + distFromCenterZ * distFromCenterZ);
            if (dist > 2 && dist <= 8) {
                return (-temperatureValue * ((float) dist - 8f))/6f;
            }
        }
        return 0f;
    }
    //simple difficulty compat ends here

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (this.getCreatureType() == RiftCreatureType.DODO) {
                ItemStack eggStack = new ItemStack(this.getCreatureType().eggItem);
                if (!player.capabilities.isCreativeMode) player.inventory.addItemStackToInventory(eggStack);
                this.setDead();
            }
            else {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    ItemStack eggStack = new ItemStack(this.getCreatureType().eggItem);
                    if (!player.capabilities.isCreativeMode) player.inventory.addItemStackToInventory(eggStack);
                    this.setDead();
                    return true;
                }
                else {
                    ITextComponent itextcomponent = new TextComponentString(this.getOwner().getName());
                    player.sendStatusMessage(new TextComponentTranslation("reminder.not_egg_owner", itextcomponent), false);
                }
            }
            return true;
        }
        else {
            try {
                if (this.getOwnerId().equals(player.getUniqueID())) {
                    ClientProxy.EGG = this;
                    player.openGui(RiftInitialize.instance, ServerProxy.GUI_EGG, world, (int) posX, (int) posY, (int) posZ);
                    return true;
                }
            }
            catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        this.setDead();
        return super.hitByEntity(entityIn);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("HatchTime", this.getHatchTime());
        compound.setByte("CreatureType", (byte)this.getCreatureType().ordinal());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHatchTime(compound.getInteger("HatchTime"));
        if (compound.hasKey("CreatureType")) this.setCreatureType(RiftCreatureType.values()[compound.getByte("CreatureType")]);
    }

    public int getHatchTime() {
        return this.dataManager.get(HATCH_TIME).intValue();
    }

    public void setHatchTime(int time) {
        this.dataManager.set(HATCH_TIME, time);
    }

    public int[] getHatchTimeMinutes() {
        int minutes = (int)((float)this.getHatchTime() / 1200F);
        int seconds = (int)((float)this.getHatchTime() / 20F);
        seconds = seconds - (minutes * 60);
        return new int[]{minutes, seconds};
    }

    public boolean isInRightTemperature() {
        return this.getTemperature().equals(this.getCreatureType().getEggTemperature());
    }

    public RiftCreatureType getCreatureType() {
        return RiftCreatureType.values()[this.dataManager.get(EGG_TYPE).byteValue()];
    }

    public void setCreatureType(RiftCreatureType type) {
        this.dataManager.set(EGG_TYPE, (byte) type.ordinal());
    }

    public EggTemperature getTemperature() {
        return EggTemperature.values()[this.dataManager.get(TEMPERATURE).byteValue()];
    }

    public void setTemperature(EggTemperature value) {
        this.dataManager.set(TEMPERATURE, (byte)value.ordinal());
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
