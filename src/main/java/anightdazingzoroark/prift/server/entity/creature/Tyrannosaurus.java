package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualBase;
import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.*;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IApexPredator;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import com.google.common.base.Predicate;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Tyrannosaurus extends RiftCreature implements IApexPredator, IWorkstationUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/tyrannosaurus"));
    private static final Predicate<EntityLivingBase> WEAKNESS_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(GeneralConfig.apexAffectedBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && !blacklist.contains("minecraft:player") && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof IApexPredator) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
            else {
                if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && !(entity instanceof IApexPredator) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
        }
    };
    private static final Predicate<EntityLivingBase> WEAKNESS_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(GeneralConfig.apexAffectedBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && blacklist.contains("minecraft:player") && entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof IApexPredator) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
            }
            else return false;
        }
    };
    private static final DataParameter<Boolean> ROARING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> STOMPING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> USING_WORKSTATION = EntityDataManager.createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> WORKSTATION_X_POS = EntityDataManager.createKey(Tyrannosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Y_POS = EntityDataManager.createKey(Tyrannosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WORKSTATION_Z_POS = EntityDataManager.createKey(Tyrannosaurus.class, DataSerializers.VARINT);
    public int roarCooldownTicks;
    public int roarCharge;

    public Tyrannosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.TYRANNOSAURUS);
        this.setSize(3.25f, 4f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 50;
        this.speed = 0.20D;
        this.roarCooldownTicks = 0;
        this.roarCharge = 0;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STOMPING, false);
        this.dataManager.register(ROARING, false);
        this.dataManager.register(USING_WORKSTATION, false);
        this.dataManager.register(WORKSTATION_X_POS, 0);
        this.dataManager.register(WORKSTATION_Y_POS, 0);
        this.dataManager.register(WORKSTATION_Z_POS, 0);
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftCreatureOperateWorkstation(this));
        this.tasks.addTask(1, new RiftLandDwellerSwim(this));
        this.tasks.addTask(2, new RiftMate(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureWarnTarget(this, 1.25f, 0.5f));
        this.tasks.addTask(5, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(6, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
        this.tasks.addTask(10, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.isBaby()) this.manageApplyApexEffect();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeWorkstationDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readWorkstationDataFromNBT(compound);
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.STOMP, CreatureMove.POWER_ROAR));
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.HEADBUTT, CreatureMove.POWER_ROAR));
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.TACKLE, CreatureMove.POWER_ROAR));
        possibleMoves.add(2, Arrays.asList(CreatureMove.BITE, CreatureMove.GNASH, CreatureMove.POWER_ROAR));
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.SHOCK_BLAST, CreatureMove.POWER_ROAR));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.STOMP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(10D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(7.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_STOMP_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.ROAR, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(22.5)
                .defineRecoverFromUseLength(7.5)
                .setChargeUpToUseSound(RiftSounds.TYRANNOSAURUS_ROAR)
                .setChargeUpToUseParticles("roar", 64, this.posX, this.posY, this.posZ)
                .setChargeUpToUseParticleYBounds(0, (int) this.height)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.HEAD, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_HEAD_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.CHARGE, new RiftCreatureMoveAnimator(this)
                .defineStartMoveDelayLength(5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.TYRANNOSAURUS_CHARGE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.GNASH, new RiftCreatureMoveAnimator(this)
                .defineStartMoveDelayLength(5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .setUseDurationSound(RiftSounds.GENERIC_GNASH_MOVE_GNASHING)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    @Override
    public void manageApplyApexEffect() {
        Predicate<EntityLivingBase> targetPredicate = GeneralConfig.apexAffectedWhitelist ? WEAKNESS_WHITELIST : WEAKNESS_BLACKLIST;
        for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEffectCastArea(), targetPredicate)) {
            if (RiftUtil.checkForNoAssociations(this, entityLivingBase)
            && !RiftUtil.hasPotionEffect(entityLivingBase, MobEffects.WEAKNESS)) entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
        }
    }

    @Override
    public AxisAlignedBB getEffectCastArea() {
        return this.getEntityBoundingBox().grow(16.0D, 16.0D, 16.0D);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.5f, 3.25f};
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
            workstations.put("prift:blow_powered_turbine", true);
        }
        return workstations;
    }

    @Override
    public BlockPos workstationUseFromPos() {
        IBlockState blockState = this.world.getBlockState(this.getWorkstationPos());
        TileEntity te = this.world.getTileEntity(this.getWorkstationPos());
        int downF = 0;
        int dirF = te instanceof TileEntitySemiManualBase ? -1 : 1;
        if (GeneralConfig.canUseMM()) {
            if (te != null) downF = te instanceof TileEntityBlowPoweredTurbine ? -1 : 0;
        }
        if (blockState.getMaterial().isSolid()) {
            EnumFacing direction = blockState.getValue(BlockHorizontal.FACING);
            switch (direction) {
                case NORTH:
                    return this.getWorkstationPos().add(0, downF, -4 * dirF);
                case SOUTH:
                    return this.getWorkstationPos().add(0, downF, 4 * dirF);
                case EAST:
                    return this.getWorkstationPos().add(4 * dirF, downF, 0);
                case WEST:
                    return this.getWorkstationPos().add(-4 * dirF, downF, 0);
            }
        }
        return null;
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

    public boolean canBeKnockedBack() {
        return true;
    }

    public void setRoaring(boolean value) {
        this.dataManager.set(ROARING, value);
    }

    public boolean isRoaring() {
        return this.dataManager.get(ROARING);
    }

    public void setStomping(boolean value) {
        this.dataManager.set(STOMPING, value);
    }

    public boolean isStomping() {
        return this.dataManager.get(STOMPING);
    }

    @Override
    public int slotCount() {
        return 54;
    }

    public float attackWidth() {
        return 4f;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.TYRANNOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.TYRANNOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.TYRANNOSAURUS_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.TYRANNOSAURUS_WARN;
    }
}
