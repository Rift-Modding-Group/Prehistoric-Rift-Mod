package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.AnomalocarisConfig;
import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IGrabber;
import anightdazingzoroark.prift.server.message.RiftGrabberTargeting;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import java.util.Arrays;
import java.util.List;

public class Anomalocaris extends RiftWaterCreature implements IGrabber {
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
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(INVISIBLE, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, AnomalocarisConfig.anomalocarisTargets, AnomalocarisConfig.anomalocarisTargetBlacklist, true, true, true));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, AnomalocarisConfig.anomalocarisFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
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
    }

    private void manageInvisibility() {
        if (!this.world.isRemote) {
            if (!this.isTamed() && !this.isUsingInvisibility() && this.invisibilityTimeout == 0) this.setUseInvisibility(true);
            else if (this.isTamed() && this.getControllingPassenger() == null) this.setUseInvisibility(false);
            if (this.invisibilityTimeout > 0) this.invisibilityTimeout--;
        }
    }

    public void manageGrabVictim() {
        if (this.getGrabVictim() != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            if (!this.getGrabVictim().isEntityAlive()) this.setGrabVictim(null);
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

    }

    //temporary, will replace this with a mechanism where if the attack animation starts
    //the invisibility wears off and will wear off some time after the target dies
    //or some time after the target breaks free from their grasp
    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (!this.world.isRemote) {
            this.setUseInvisibility(false);
            this.invisibilityTimeout = 200;
        }
        this.heal((float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())/2f);
        if (entityIn.isEntityAlive() && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entityIn;
            RiftEntityProperties entityProperties = EntityPropertiesHandler.INSTANCE.getProperties(entityLivingBase, RiftEntityProperties.class);
            if (entityIn instanceof EntityPlayer) {
                if (!AnomalocarisConfig.anomalocarisGrabWhitelist && !this.blacklist().contains("minecraft:player") && !entityProperties.isCaptured) {
                    this.setGrabVictim(entityLivingBase);
                    entityProperties.isCaptured = true;
                }
                else if (AnomalocarisConfig.anomalocarisGrabWhitelist && this.blacklist().contains("minecraft:player") && !entityProperties.isCaptured) {
                    this.setGrabVictim(entityLivingBase);
                    entityProperties.isCaptured = true;
                }
            }
            else {
                if (!AnomalocarisConfig.anomalocarisGrabWhitelist && !this.blacklist().contains(EntityList.getKey(entityIn).toString()) && !entityProperties.isCaptured) {
                    this.setGrabVictim(entityLivingBase);
                    entityProperties.isCaptured = true;
                }
                else if (AnomalocarisConfig.anomalocarisGrabWhitelist && this.blacklist().contains(EntityList.getKey(entityIn).toString()) && !entityProperties.isCaptured) {
                    this.setGrabVictim(entityLivingBase);
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
