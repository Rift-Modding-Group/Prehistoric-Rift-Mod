package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.config.json.JsonTemperature;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.config.ModConfig;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        if (this.getTemperature().equals(this.getCreatureType().getEggTemperature()) || GeneralConfig.quickEggHatch) this.setHatchTime(this.getHatchTime() - 1);
        if (this.getHatchTime() == 0) {
            RiftCreature creature = this.getCreatureType().invokeClass(this.world);
            creature.setHealth((float) (creature.minCreatureHealth + (0.1) * (creature.getLevel()) * (creature.minCreatureHealth)));
            creature.setAgeInDays(0);

            if (this.getOwnerId() != null && this.getCreatureType() != RiftCreatureType.DODO) {
                creature.setTamed(true);
                creature.setOwnerId(this.getOwnerId());
                creature.setTameBehavior(TameBehaviorType.PASSIVE);
                creature.setAcquisitionInfo(CreatureAcquisitionInfo.AcquisitionMethod.BORN, System.currentTimeMillis() / 1000L);
            }

            creature.setLocationAndAngles(Math.floor(this.posX), Math.floor(this.posY) + 1, Math.floor(this.posZ), this.world.rand.nextFloat() * 360.0F, 0.0F);
            if (!this.world.isRemote) {
                EntityPlayer owner = (EntityPlayer) this.getOwner();

                //update journal
                if (PlayerJournalProgressHelper.getUnlockedCreatures(owner).containsKey(this.getCreatureType()) && !PlayerJournalProgressHelper.getUnlockedCreatures(owner).get(this.getCreatureType())) {
                    PlayerJournalProgressHelper.unlockCreature(owner, this.getCreatureType());
                    owner.sendStatusMessage(new TextComponentTranslation("reminder.unlocked_journal_entry", this.getCreatureType().getTranslatedName(), RiftControls.openParty.getDisplayName()), false);
                }

                if (this.getCreatureType() != RiftCreatureType.DODO) {
                    //update party of owner
                    if (NewPlayerTamedCreaturesHelper.canAddToParty(owner)) {
                        creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                        NewPlayerTamedCreaturesHelper.addCreatureToParty(owner, creature);
                        owner.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished_to_party", new TextComponentString(this.getName())), false);
                    }
                    //update box of owner
                    else if (NewPlayerTamedCreaturesHelper.canAddCreatureToBox(owner)) {
                        creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                        NewPlayerTamedCreaturesHelper.addCreatureToBox(owner, creature);
                        owner.sendStatusMessage(new TextComponentTranslation("reminder.taming_finished_to_box", new TextComponentString(this.getName())), false);
                    }
                }
                else this.world.spawnEntity(creature);
            }
            this.setDead();
        }
    }

    private void temperatureFromExtSources() {
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

            //for dimetrodons
            for (EntityLivingBase entityLivingBase : world.getEntities(EntityLiving.class, new Predicate<EntityLiving>() {
                @Override
                public boolean apply(@Nullable EntityLiving input) {
                    return true;
                }
            })) {
                if (entityLivingBase instanceof Dimetrodon) {
                    Dimetrodon dimetrodon = (Dimetrodon) entityLivingBase;
                    List<BlockPos> affectedBlockPositions = Lists.<BlockPos>newArrayList();
                    Set<BlockPos> set = Sets.<BlockPos>newHashSet();
                    int i = 16;
                    for (int j = 0; j < 16; ++j) {
                        for (int k = 0; k < 16; ++k) {
                            for (int l = 0; l < 16; ++l) {
                                if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                                    double d0 = ((float)j / 15.0F * 2.0F - 1.0F);
                                    double d1 = ((float)k / 15.0F * 2.0F - 1.0F);
                                    double d2 = ((float)l / 15.0F * 2.0F - 1.0F);
                                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                                    d0 /= d3;
                                    d1 /= d3;
                                    d2 /= d3;
                                    float f = 8f * (0.7F + this.world.rand.nextFloat() * 0.6F);
                                    double d4 = dimetrodon.posX;
                                    double d6 = dimetrodon.posY;
                                    double d8 = dimetrodon.posZ;

                                    for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                        BlockPos blockpos = new BlockPos(d4, d6, d8);
                                        IBlockState iblockstate = this.world.getBlockState(blockpos);
                                        if (iblockstate.getMaterial() == Material.AIR) {
                                            set.add(blockpos);
                                            d4 += d0 * 0.30000001192092896D;
                                            d6 += d1 * 0.30000001192092896D;
                                            d8 += d2 * 0.30000001192092896D;
                                        }
                                        else {
                                            // If a solid block is encountered, stop the propagation in this direction
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    affectedBlockPositions.addAll(set);
                    for (BlockPos blockPos : affectedBlockPositions) {
                        if (blockPos.equals(this.getPosition())) {
                            switch (dimetrodon.getTemperature()) {
                                case VERY_COLD:
                                    temperatureValue += this.changeTempByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.veryColdTemperatureValue, dimetrodon.getPosition());
                                    break;
                                case COLD:
                                    temperatureValue += this.changeTempByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.coldTemperatureValue, dimetrodon.getPosition());
                                    break;
                                case WARM:
                                    temperatureValue += this.changeTempByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.warmTemperatureValue, dimetrodon.getPosition());
                                    break;
                                case VERY_WARM:
                                    temperatureValue += this.changeTempByDistance(((DimetrodonConfig) RiftConfigHandler.getConfig(RiftCreatureType.DIMETRODON)).simpleDifficulty.veryWarmTemperatureValue, dimetrodon.getPosition());
                                    break;
                            }
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

    private float changeTempByDistance(float origTemperature, BlockPos blockPos) {
        float distance = (float)Math.sqrt(blockPos.distanceSq(this.posX, this.posY, this.posZ));
        return ((-origTemperature/8f) * RiftUtil.clamp(distance, 0f, 8f)) + origTemperature;
    }

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
                    player.openGui(RiftInitialize.instance, RiftGui.GUI_EGG, world, this.getEntityId(), (int) posY, (int) posZ);
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
