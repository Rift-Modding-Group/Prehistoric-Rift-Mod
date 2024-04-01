package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.AnomalocarisConfig;
import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.config.UtahraptorConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IGrabber;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.message.RiftGrabberTargeting;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSetGrabTarget;
import com.google.common.base.Predicate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Anomalocaris extends RiftWaterCreature implements IGrabber {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/anomalocaris"));
    private static final DataParameter<Boolean> INVISIBLE = EntityDataManager.createKey(Anomalocaris.class, DataSerializers.BOOLEAN);
    private int invisibilityTimeout = 0;
    private EntityLivingBase grabVictim = null;

    public Anomalocaris(World worldIn) {
        super(worldIn, RiftCreatureType.ANOMALOCARIS);
        this.setSize(2f, 0.75f);
        this.minCreatureHealth = AnomalocarisConfig.getMinHealth();
        this.maxCreatureHealth = AnomalocarisConfig.getMaxHealth();
        this.favoriteFood = AnomalocarisConfig.anomalocarisFavoriteFood;
        this.tamingFood = AnomalocarisConfig.anomalocarisTamingFood;
        this.experienceValue = 10;
        this.isRideable = true;
        this.saddleItem = AnomalocarisConfig.anomalocarisSaddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 5D;
        this.attackWidth = 3f;
        this.attackDamage = AnomalocarisConfig.damage;
        this.healthLevelMultiplier = AnomalocarisConfig.healthMultiplier;
        this.damageLevelMultiplier = AnomalocarisConfig.damageMultiplier;
        this.densityLimit = AnomalocarisConfig.anomalocarisDensityLimit;
        this.targetList = RiftUtil.creatureTargets(AnomalocarisConfig.anomalocarisTargets, AnomalocarisConfig.anomalocarisTargetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(INVISIBLE, true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, AnomalocarisConfig.anomalocarisFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.36F));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.52F, 0.36F));
        this.tasks.addTask(6, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageInvisibility();
        this.manageGrabVictim();
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

    public void manageGrabVictim() {
        if (this.getGrabVictim() != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            if (!this.getGrabVictim().isEntityAlive()) RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, null));
            else RiftMessages.WRAPPER.sendToServer(new RiftGrabberTargeting(this, this.getGrabVictim()));
        }
        else this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0);
    }

    @Override
    public void resetParts(float scale) {

    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 1f, 2f);
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
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing()) this.setAttacking(true);
                }
                else {
                    if (!this.isActing() && !this.isUsingRightClick()) {
                        this.ssrTarget = target;
                        this.setAttacking(true);
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (!this.isActing()) {
                if (this.getGrabVictim() == null) {
                    UUID ownerID = this.getOwnerId();
                    List<String> blackList = Arrays.asList(AnomalocarisConfig.anomalocarisGrabBlacklist);
                    List<EntityLivingBase> potGrabList = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(this.attackWidth).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
                        @Override
                        public boolean apply(@Nullable EntityLivingBase input) {
                            RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(input, RiftEntityProperties.class);
                            if (input instanceof EntityPlayer) {
                                if (!AnomalocarisConfig.anomalocarisGrabWhitelist && !blackList.contains("minecraft:player")) {
                                    return !input.getUniqueID().equals(ownerID) && !properties.isCaptured;
                                }
                                else if (AnomalocarisConfig.anomalocarisGrabWhitelist && blackList.contains("minecraft:player")) {
                                    return !input.getUniqueID().equals(ownerID) && !properties.isCaptured;
                                }
                            }
                            else {
                                if (!AnomalocarisConfig.anomalocarisGrabWhitelist && !blackList.contains(EntityList.getKey(input).toString())) {
                                    return !properties.isCaptured;
                                }
                                else if (AnomalocarisConfig.anomalocarisGrabWhitelist && blackList.contains(EntityList.getKey(input).toString())) {
                                    return !properties.isCaptured;
                                }
                            }
                            return false;
                        }
                    });
                    if (!potGrabList.isEmpty()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, potGrabList.get(0)));
                        EntityPropertiesHandler.INSTANCE.getProperties(potGrabList.get(0), RiftEntityProperties.class).isCaptured = true;
                        this.setActing(true);
                    }
                }
                else {
                    EntityPropertiesHandler.INSTANCE.getProperties(this.getGrabVictim(), RiftEntityProperties.class).isCaptured = false;
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
            RiftEntityProperties entityProperties = EntityPropertiesHandler.INSTANCE.getProperties(entityLivingBase, RiftEntityProperties.class);
            if (entityIn instanceof EntityPlayer) {
                if (!AnomalocarisConfig.anomalocarisGrabWhitelist && !this.blacklist().contains("minecraft:player") && !entityProperties.isCaptured) {
                    RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, entityLivingBase));
                    entityProperties.isCaptured = true;
                }
                else if (AnomalocarisConfig.anomalocarisGrabWhitelist && this.blacklist().contains("minecraft:player") && !entityProperties.isCaptured) {
                    RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, entityLivingBase));
                    entityProperties.isCaptured = true;
                }
            }
            else {
                if (!AnomalocarisConfig.anomalocarisGrabWhitelist && !this.blacklist().contains(EntityList.getKey(entityIn).toString()) && !entityProperties.isCaptured) {
                    RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, entityLivingBase));
                    entityProperties.isCaptured = true;
                }
                else if (AnomalocarisConfig.anomalocarisGrabWhitelist && this.blacklist().contains(EntityList.getKey(entityIn).toString()) && !entityProperties.isCaptured) {
                    RiftMessages.WRAPPER.sendToServer(new RiftSetGrabTarget(this, entityLivingBase));
                    entityProperties.isCaptured = true;
                }
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
        if (this.getGrabVictim() != null) EntityPropertiesHandler.INSTANCE.getProperties(this.getGrabVictim(), RiftEntityProperties.class).isCaptured = false;
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
        if (this.getTameStatus().equals(TameStatusType.SIT)) {
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

    public void setTameStatus(TameStatusType tameStatus) {
        super.setTameStatus(tameStatus);
        if (tameStatus.equals(TameStatusType.SIT)) {
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
    public boolean canBeSaddled() {
        return true;
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
    public List<String> blacklist() {
        return Arrays.asList(AnomalocarisConfig.anomalocarisGrabBlacklist);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
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
}
