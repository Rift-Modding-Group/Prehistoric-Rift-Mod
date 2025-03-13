package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IGrabber;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSetGrabTarget;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
import java.util.UUID;

public class Anomalocaris extends RiftWaterCreature implements IGrabber {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/anomalocaris"));
    private static final DataParameter<Boolean> INVISIBLE = EntityDataManager.createKey(Anomalocaris.class, DataSerializers.BOOLEAN);
    private int invisibilityTimeout = 0;
    private EntityLivingBase grabVictim = null;
    public RiftCreaturePart tailPart;

    public Anomalocaris(World worldIn) {
        super(worldIn, RiftCreatureType.ANOMALOCARIS);
        this.setSize(2f, 0.75f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 10;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 5D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 1.25f, 0, 0f, 0.5f, 0.5f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0f, 1f, 0.375f, 1f);
        this.tailPart = new RiftCreaturePart(this, -1.75f, 0, 0f, 0.75f, 0.5f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
                this.headPart,
                this.bodyPart,
                this.tailPart
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(INVISIBLE, true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.36F));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.52F, 0.36F));
        this.tasks.addTask(6, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(8, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageInvisibility();
        this.resetIsActing();
    }

    private void resetIsActing() {
        if (this.isActing() && !this.isUsingRightClick() && !this.isUsingLeftClick()) {
            this.setActing(false);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("UsingInvis", this.isUsingInvisibility());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setUseInvisibility(compound.getBoolean("UsingInvis"));
    }

    private void manageInvisibility() {
        if (!this.world.isRemote) {
            if (!this.isUsingInvisibility() && this.invisibilityTimeout == 0) this.setUseInvisibility(true);
            if (this.invisibilityTimeout > 0) this.invisibilityTimeout--;
        }
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{1f, 2f};
    }

    public float attackWidth() {
        return 3f;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 1.75, this.posZ);
    }

    public Vec3d grabLocation() {
        float xOffset = (float)(this.posX + Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null && this.grabVictim == null) {
                    if (!this.isActing()) this.setAttacking(true);
                }
                else if (target != null && this.grabVictim == null) {
                    if (!this.isActing() && !this.isUsingRightClick()) {
                        this.forcedAttackTarget = target;
                        this.forcedBreakPos = pos;
                        this.setAttacking(true);
                    }
                }
                else {
                    if (!this.isActing() && !this.isUsingRightClick()) {
                        this.forcedAttackTarget = this.grabVictim;
                        this.setAttacking(true);
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (!this.isActing()) {
                if (this.getGrabVictim() == null) {
                    if (target != null && RiftUtil.isUsingSSR()) {
                        EntityLivingBase entityLivingBase = target instanceof RiftCreaturePart ? ((RiftCreaturePart)target).getParent() : (target instanceof EntityLivingBase ? (EntityLivingBase) target : null);
                        if (entityLivingBase != null) {
                            boolean canGrabFlag;

                            if (target instanceof EntityPlayer) {
                                canGrabFlag = !target.getUniqueID().equals(this.getOwnerId()) && !NonPotionEffectsHelper.isCaptured(target) && RiftUtil.isAppropriateSize(entityLivingBase, MobSize.safeValueOf(RiftConfigHandler.getConfig(this.creatureType).general.maximumGrabTargetSize ));
                            }
                            else {
                                canGrabFlag = !target.equals(this) && !NonPotionEffectsHelper.isCaptured(target) && RiftUtil.isAppropriateSize(entityLivingBase, MobSize.safeValueOf(RiftConfigHandler.getConfig(this.creatureType).general.maximumGrabTargetSize));
                            }

                            if (canGrabFlag) {
                                RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, entityLivingBase));
                                NonPotionEffectsHelper.setCaptured(target, true);
                                this.setActing(true);
                            }
                        }
                    }
                    else if (target == null && !RiftUtil.isUsingSSR()) {
                        UUID ownerID = this.getOwnerId();
                        List<EntityLivingBase> potGrabList = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(this.attackWidth()).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
                            @Override
                            public boolean apply(@Nullable EntityLivingBase input) {
                                if (input instanceof EntityPlayer) {
                                    return !input.getUniqueID().equals(ownerID) && !NonPotionEffectsHelper.isCaptured(input) && RiftUtil.isAppropriateSize(input, MobSize.safeValueOf(RiftConfigHandler.getConfig(creatureType).general.maximumGrabTargetSize));
                                }
                                else {
                                    return !NonPotionEffectsHelper.isCaptured(input) && RiftUtil.isAppropriateSize(input, MobSize.safeValueOf(RiftConfigHandler.getConfig(creatureType).general.maximumGrabTargetSize));
                                }
                            }
                        });
                        potGrabList.remove(this);
                        if (!potGrabList.isEmpty()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, potGrabList.get(0)));
                            NonPotionEffectsHelper.setCaptured(potGrabList.get(0), true);
                            this.setActing(true);
                        }
                    }
                }
                else {
                    NonPotionEffectsHelper.setCaptured(this.getGrabVictim(), false);
                    RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, null));
                    this.setActing(true);
                }
            }
            this.setRightClickUse(0);
        }
        if (control == 3) {
            if (!this.world.isRemote) {
                this.invisibilityTimeout = !this.isUsingInvisibility() ? 0 : -1;
                this.setUseInvisibility(!this.isUsingInvisibility());
            }
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (!this.world.isRemote) {
            this.setUseInvisibility(false);
            this.invisibilityTimeout = 200;
        }
        this.heal((float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())/2f);
        boolean playerRideFlag = (this.isBeingRidden() && this.getControllingPassenger() instanceof EntityPlayer);
        if (entityIn.isEntityAlive() && entityIn instanceof EntityLivingBase && !playerRideFlag) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entityIn;

            if (RiftUtil.isAppropriateSize(entityLivingBase, MobSize.safeValueOf(RiftConfigHandler.getConfig(this.creatureType).general.maximumGrabTargetSize))) {
                RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, entityLivingBase));
                NonPotionEffectsHelper.setCaptured(entityLivingBase, true);
            }
        }
        return super.attackEntityAsMob(entityIn);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.world.isRemote) {
            this.setUseInvisibility(false);
            this.invisibilityTimeout = 200;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (this.getGrabVictim() != null) NonPotionEffectsHelper.setCaptured(this.getGrabVictim(), false);
    }

    public void setTamedBy(EntityPlayer player) {
        super.setTamedBy(player);
        if (!this.world.isRemote) {
            this.setUseInvisibility(false);
            this.invisibilityTimeout = -1;
        }
    }

    public void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (this.isSitting()) {
            if (!this.world.isRemote) {
                this.setUseInvisibility(false);
                this.invisibilityTimeout = -1;
            }
        }
        else {
            if (!this.world.isRemote) {
                this.setUseInvisibility(false);
                this.invisibilityTimeout = 200;
            }
        }
    }

    public void setSitting(boolean value) {
        super.setSitting(value);
        if (value) {
            if (!this.world.isRemote) {
                this.setUseInvisibility(false);
                this.invisibilityTimeout = -1;
            }
        }
        else {
            if (!this.world.isRemote) {
                this.setUseInvisibility(false);
                this.invisibilityTimeout = 200;
            }
        }
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    @Override
    public boolean isAmphibious() {
        return false;
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    public boolean isUsingInvisibility() {
        return this.dataManager.get(INVISIBLE);
    }

    public void setUseInvisibility(boolean value) {
        this.dataManager.set(INVISIBLE, value);
    }

    public EntityLivingBase getGrabVictim() {
        return this.grabVictim;
    }

    public void setGrabVictim(EntityLivingBase entityLivingBase) {
        this.grabVictim = entityLivingBase;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::anomalocarisMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::anomalocarisAttack));
    }

    private <E extends IAnimatable> PlayState anomalocarisMovement(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.anomalocaris.swim", true));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState anomalocarisAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.anomalocaris.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.ANOMALOCARIS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.ANOMALOCARIS_DEATH;
    }
}
