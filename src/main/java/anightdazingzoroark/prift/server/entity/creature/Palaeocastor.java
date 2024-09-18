package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.PalaeocastorConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
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
import java.util.List;

public class Palaeocastor extends RiftCreature implements IImpregnable, IHarvestWhenWandering {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/palaeocastor"));
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(Palaeocastor.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(Palaeocastor.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean> HARVESTING = EntityDataManager.createKey(Palaeocastor.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Palaeocastor.class, DataSerializers.BOOLEAN);
    public RiftCreaturePart neckPart;
    public RiftCreaturePart tailPart;

    public Palaeocastor(World worldIn) {
        super(worldIn, RiftCreatureType.PALAEOCASTOR);
        this.setSize(0.75f, 0.75f);
        this.favoriteFood = ((PalaeocastorConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((PalaeocastorConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.speed = 0.25D;
        this.attackDamage = RiftConfigHandler.getConfig(this.creatureType).stats.baseDamage;
        this.experienceValue = 3;

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.125f, 0.625f, 0.625f, 1f);
        this.headPart = new RiftCreaturePart(this, 0.75f, 0, 0.25f, 0.5f, 0.5f, 1.5f);
        this.neckPart = new RiftCreaturePart(this, 0.5f, 0, 0.25f, 0.4f, 0.4f, 1.5f);
        this.tailPart = new RiftCreaturePart(this, -0.6f, 0, 0.25f, 0.5f, 0.5f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.bodyPart,
            this.headPart,
            this.neckPart,
            this.tailPart
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
        this.dataManager.register(HARVESTING, false);
        this.dataManager.register(CAN_HARVEST, false);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        //this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(4, new RiftHarvestOnWander(this, 1.25f, 1f));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(6, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(7, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.world.isRemote) this.createBaby(this);
    }

    @Override
    public void updateParts() {
        super.updateParts();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writePregnancyDataToNBT(compound);
        this.writeHarvestWanderDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readPregnancyDataFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.25f, 1f);
    }

    public float attackWidth() {
        return 2f;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {}

    @Override
    public List<String> blocksToHarvest() {
        return ((PalaeocastorConfig) RiftConfigHandler.getConfig(this.creatureType)).general.harvestableBlocks;
    }

    public void setHarvesting(boolean value) {
        this.dataManager.set(HARVESTING, value);
    }

    public boolean isHarvesting() {
        return this.dataManager.get(HARVESTING);
    }

    public void setCanHarvest(boolean value) {
        this.dataManager.set(CAN_HARVEST, value);
    }

    public boolean canHarvest() {
        return this.dataManager.get(CAN_HARVEST);
    }

    public void setPregnant(boolean value, int timer) {
        this.dataManager.set(PREGNANT, value);
        this.dataManager.set(PREGNANCY_TIMER, timer);
    }

    public boolean isPregnant() {
        return this.dataManager.get(PREGNANT);
    }

    public void setPregnancyTimer(int value) {
        this.dataManager.set(PREGNANCY_TIMER, value);
    }

    public int getPregnancyTimer() {
        return this.dataManager.get(PREGNANCY_TIMER);
    }

    @Override
    public int slotCount() {
        return 9;
    }

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

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::palaeocastorMovement));
        data.addAnimationController(new AnimationController(this, "dig", 0, this::palaeocastorDig));
    }

    private <E extends IAnimatable> PlayState palaeocastorMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isSitting() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.palaeocastor.sitting", true));
                return PlayState.CONTINUE;
            }
            if ((event.isMoving() || (this.isSitting() && this.hasTarget()))) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.palaeocastor.walk", true));
                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState palaeocastorDig(AnimationEvent<E> event) {
        if (this.isHarvesting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.palaeocastor.dig", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.PALAEOCASTOR_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.PALAEOCASTOR_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.PALAEOCASTOR_DEATH;
    }
}
