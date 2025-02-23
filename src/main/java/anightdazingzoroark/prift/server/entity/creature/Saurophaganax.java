package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.SaurophaganaxConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
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

public class Saurophaganax extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/saurophaganax"));
    private static final DataParameter<Boolean> USING_LIGHT_BLAST = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LIGHT_BLAST_CHARGE = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> WEAKENED = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.BOOLEAN);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart bodyFrontPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Saurophaganax(World worldIn) {
        super(worldIn, RiftCreatureType.SAUROPHAGANAX);
        this.setSize(2f, 3f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 20;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.25D;
        this.targetList = RiftUtil.creatureTargets(((SaurophaganaxConfig) RiftConfigHandler.getConfig(this.creatureType)).general.targetWhitelist);

        this.headPart = new RiftCreaturePart(this, 3.5f, 0, 2f, 0.6f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0f, 0, 1.125f, 1f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 2.5f, 0, 1.7f, 0.5f, 0.65f, 1.5f);
        this.bodyFrontPart = new RiftCreaturePart(this, 1.5f, 0, 1.125f, 0.8f, 0.8f, 1f);
        this.tail0Part = new RiftCreaturePart(this, -1.5f, 0, 1.4f, 0.6f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -2.5f, 0, 1.4f, 0.5f, 0.5f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -3.5f, 0, 1.4f, 0.5f, 0.5f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.bodyFrontPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
        this.dataManager.register(USING_LIGHT_BLAST, false);
        this.dataManager.register(LIGHT_BLAST_CHARGE, 0);
        this.dataManager.register(WEAKENED, false);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //manage weakening
        if (!this.world.isRemote) {
            this.setWeakened(this.world.isDaytime() && !this.isInCave());
            if (this.world.isDaytime() && !this.isInCave()) this.setLightBlastCharge(0);
            if (this.isTamed()) {
                this.setRightClickUse(this.lightBlastCharge() * 10);
            }

            if (this.isWeakened() && !this.isInWater()) {
                this.setSpeed(this.speed * 0.5);
                this.setWaterSpeed(this.waterSpeed * 0.5);
            }
            else {
                this.setSpeed(this.speed);
                this.setWaterSpeed(this.waterSpeed);
            }
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftSleepAtDay(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftSaurophaganaxUseLightBlast(this));
        this.tasks.addTask(4, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.52F, 0.24F));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
        this.tasks.addTask(10, new RiftLookAround(this));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sleepOffsetBody = this.isSleeping() ? -1.2f : 0;
        float sleepOffsetNeck = this.isSleeping() ? -1.7f : 0;
        float sleepOffsetHead = this.isSleeping() ? -2f : 0;

        float sitOffset = (this.isSitting() && !this.isBeingRidden() && !this.isSleeping()) ? -0.6f : 0;

        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sleepOffsetBody + sitOffset, this.bodyPart.posZ);
        if (this.bodyFrontPart != null) this.bodyFrontPart.setPositionAndUpdate(this.bodyFrontPart.posX, this.bodyFrontPart.posY + sleepOffsetBody + sitOffset, this.bodyFrontPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sleepOffsetBody + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sleepOffsetBody + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sleepOffsetBody + sitOffset, this.tail2Part.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sleepOffsetNeck + sitOffset, this.neckPart.posZ);
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sleepOffsetHead + sitOffset, this.headPart.posZ);
    }

    @Override
    protected void manageAttributes() {
        super.manageAttributes();
        if (this.isWeakened()) {
            //attack
            double leveledAttackValue = this.attackDamage + (double)Math.round((this.getLevel() - 1) * this.damageLevelMultiplier);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(leveledAttackValue * 0.1);
        }
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.2f, 2f};
    }

    @Override
    public int slotCount() {
        return 27;
    }

    public float attackWidth() {
        return 3.5f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (0.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (0.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + 0.35, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.forcedAttackTarget = target;
                    this.forcedBreakPos = pos;
                    this.setAttacking(true);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6 && this.lightBlastCharge() >= 10) {
                if (!this.isActing()) {
                    this.setUsingLightBlast(true);
                }
            }
            else if (this.getEnergy() > 6 && this.lightBlastCharge() < 10) ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_light_blast_charge", this.getName()), false);
            else if (this.getEnergy() <= 6) ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (!(entityIn instanceof EntityPlayer) && !this.isWeakened() && this.lightBlastCharge() < 10) {
            if (this.getTargetList().contains(EntityList.getKey(entityIn).toString())) {
                this.setLightBlastCharge(this.lightBlastCharge() + 1);
            }
        }
        return super.attackEntityAsMob(entityIn);
    }

    public void useLightBlast() {
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(8D), new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                if (entityLivingBase instanceof EntityPlayer) {
                    return getTargetList().contains("minecraft:player") && !entityLivingBase.getUniqueID().equals(getOwnerId());
                }
                else if (entityLivingBase instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entityLivingBase;
                    if (tameable.isTamed()) return getTargetList().contains(EntityList.getKey(tameable).toString()) && !tameable.getUniqueID().equals(getOwnerId());
                    else return getTargetList().contains(EntityList.getKey(entityLivingBase).toString());
                }
                else return getTargetList().contains(EntityList.getKey(entityLivingBase).toString());
            }
        });
        for (EntityLivingBase entityT : list) {
            entityT.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())/4F);
            entityT.setFire(30);
        }
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return true;
    }

    @Override
    public boolean alwaysShowRightClickUse() {
        return true;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    public boolean isUsingLightBlast() {
        return this.dataManager.get(USING_LIGHT_BLAST);
    }

    public void setUsingLightBlast(boolean value) {
        this.dataManager.set(USING_LIGHT_BLAST, value);
        this.setActing(value);
    }

    public int lightBlastCharge() {
        return this.dataManager.get(LIGHT_BLAST_CHARGE);
    }

    public void setLightBlastCharge(int value) {
        this.dataManager.set(LIGHT_BLAST_CHARGE, value);
    }

    public boolean isWeakened() {
        return this.dataManager.get(WEAKENED);
    }

    public void setWeakened(boolean value) {
        this.dataManager.set(WEAKENED, value);
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::saurophaganaxMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::saurophaganaxAttack));
        data.addAnimationController(new AnimationController(this, "lightBlast", 0, this::saurophaganaxLightBlast));
        data.addAnimationController(new AnimationController(this, "tiredPose", 0, this::saurophaganaxTired));
    }

    private <E extends IAnimatable> PlayState saurophaganaxMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (!this.isSleeping() && this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.sitting", true));
                return PlayState.CONTINUE;
            }
            if (!this.isSleeping() && (event.isMoving() || (this.isSitting() && this.hasTarget()))) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.walk", true));
                return PlayState.CONTINUE;
            }
            if (this.isSleeping()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.sleeping", true));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState saurophaganaxAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.attack", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState saurophaganaxLightBlast(AnimationEvent<E> event) {
        if (this.isUsingLightBlast()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.light_blast", false));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.setup", true));
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState saurophaganaxTired(AnimationEvent<E> event) {
        if (this.isWeakened() && !this.isSleeping()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.tired_pose", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.SAUROPHAGANAX_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.SAUROPHAGANAX_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.SAUROPHAGANAX_DEATH;
    }
}
