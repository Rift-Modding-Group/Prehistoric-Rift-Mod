package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.UtahraptorConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftClimber;
import anightdazingzoroark.prift.server.entity.interfaces.ILeapingMob;
import anightdazingzoroark.prift.server.entity.interfaces.IPackHunter;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utahraptor extends RiftCreature implements ILeapingMob, IPackHunter {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/utahraptor"));
    private static final DataParameter<Boolean> PACK_BUFFING = EntityDataManager.createKey(Utahraptor.class, DataSerializers.BOOLEAN);
    private int packBuffCooldown;
    private boolean contLeapAttackFlag;
    private EntityLivingBase contLeapTarget;
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Utahraptor(World worldIn) {
        super(worldIn, RiftCreatureType.UTAHRAPTOR);
        this.minCreatureHealth = UtahraptorConfig.getMinHealth();
        this.maxCreatureHealth = UtahraptorConfig.getMaxHealth();
        this.setSize(1.25f, 1.5f);
        this.experienceValue = 10;
        this.favoriteFood = UtahraptorConfig.utahraptorFavoriteFood;
        this.tamingFood = UtahraptorConfig.utahraptorTamingFood;
        this.speed = 0.35D;
        this.isRideable = true;
        this.attackWidth = 2f;
        this.leapWidth = 16f;
        this.packBuffCooldown = 0;
        this.contLeapAttackFlag = true;
        this.maxRightClickCooldown = 1800f;
        this.saddleItem = UtahraptorConfig.utahraptorSaddleItem;
        this.attackDamage = UtahraptorConfig.damage;
        this.healthLevelMultiplier = UtahraptorConfig.healthMultiplier;
        this.damageLevelMultiplier = UtahraptorConfig.damageMultiplier;
        this.densityLimit = UtahraptorConfig.utahraptorDensityLimit;
        this.targetList = RiftUtil.creatureTargets(UtahraptorConfig.utahraptorTargets, UtahraptorConfig.utahraptorTargetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PACK_BUFFING, false);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftPackBuff(this, 1.68f, 0f, 90f));
        this.tasks.addTask(4, new RiftControlledAttack(this, 0.28F, 0.28F));
        this.tasks.addTask(4, new RiftControlledPackBuff(this, 1.68f, 0f));
        this.tasks.addTask(5, new RiftLeapAttack(this, 6f, 160));
        this.tasks.addTask(6, new RiftAttack(this, 1.0D, 0.28F, 0.28F));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(8, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(9, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(10, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(11, new RiftHerdMemberFollow(this));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanLeap();
        this.manageCanPackBuff();
        if (!this.world.isRemote) {
            this.setClimbing(this.collidedHorizontally);
            if (this.onGround && this.isLeaping() && this.isActing()) {
                this.setLeaping(false);
                this.contLeapAttackFlag = true;
            }
            if (!this.onGround && this.isLeaping() && this.isBeingRidden()) {
                if (this.contLeapAttackFlag) {
                    AxisAlignedBB leapHithbox = this.getEntityBoundingBox().grow(0.75D);
                    List<EntityLivingBase> leapedEntities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, leapHithbox, null);
                    if (leapedEntities.contains(this.contLeapTarget)) {
                        this.attackEntityAsMob(this.contLeapTarget);
                        this.contLeapAttackFlag = false;
                        this.contLeapTarget = null;
                    }
                }
            }
        }
    }

    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 2f, 0, 1.7f, scale, 0.6f * scale, 1.5f);
            this.bodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.9f, scale, 0.8f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 1.5f, 0, 1.2f, 0.7f * scale, 0.7f * scale, 1.5f);
            this.hipPart = new RiftCreaturePart(this, 0, 0, 0.7f, scale, scale, 1f);
            this.tail0Part = new RiftCreaturePart(this, -0.9f, 0, 1f, 0.7f * scale, 0.6f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -1.5f, 0, 0.95f, 0.6f * scale, 0.6f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -2.1f, 0, 0.9f, 0.6f * scale, 0.6f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.hipPart != null) this.hipPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.45f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
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
        if (this.hipPart != null) {
            this.world.removeEntityDangerously(this.hipPart);
            this.hipPart = null;
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

    private void manageCanLeap() {
        if (this.leapCooldown > 0) this.leapCooldown--;
    }

    private void manageCanPackBuff() {
        if (this.packBuffCooldown > 0) this.packBuffCooldown--;
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
    }

    public void fall(float distance, float damageMultiplier) {}

    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateRiftClimber(this, worldIn);
    }

    private void leapToControlledTargetLoc() {
        if (!this.world.isRemote) {
            this.setLeaping(true);
            UUID ownerID =  this.getOwnerId();
            List<EntityLivingBase> potTargetListL = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(this.leapWidth), new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    if (input instanceof EntityTameable) {
                        EntityTameable inpTameable = (EntityTameable)input;
                        if (inpTameable.isTamed()) {
                            return !ownerID.equals(inpTameable.getOwnerId());
                        }
                        else return true;
                    }
                    return true;
                }
            });
            potTargetListL.remove(this);
            potTargetListL.remove(this.getControllingPassenger());

            if (!potTargetListL.isEmpty()) {
                this.contLeapTarget = RiftUtil.findClosestEntity(this, potTargetListL);
                double dx = this.contLeapTarget.posX - this.posX;
                double dz = this.contLeapTarget.posZ - this.posZ;
                double dist = Math.sqrt(dx * dx + dz * dz);

                double velY = Math.sqrt(2 * RiftUtil.gravity * 6f);
                double totalTime = velY / RiftUtil.gravity;
                double velXZ = dist * 2 / totalTime;

                double angleToTarget = Math.atan2(dz, dx);

                this.motionX = velXZ * Math.cos(angleToTarget);
                this.motionZ = velXZ * Math.sin(angleToTarget);
                this.motionY = velY;
            }
            else this.setAttacking(true);
        }
    }

    private void leapToControlledTargetLoc(EntityLivingBase target) {
        if (!this.world.isRemote) {
            this.setLeaping(true);
            boolean canLeapFlag = true;

            if (target instanceof EntityTameable) {
                canLeapFlag = ((EntityTameable)target).isTamed();
            }

            if (canLeapFlag) {
                this.contLeapTarget = target;

                double dx = this.contLeapTarget.posX - this.posX;
                double dz = this.contLeapTarget.posZ - this.posZ;
                double dist = Math.sqrt(dx * dx + dz * dz);

                double velY = Math.sqrt(2 * RiftUtil.gravity * 6f);
                double totalTime = velY / RiftUtil.gravity;
                double velXZ = dist * 2 / totalTime;

                double angleToTarget = Math.atan2(dz, dx);

                this.motionX = velXZ * Math.cos(angleToTarget);
                this.motionZ = velXZ * Math.sin(angleToTarget);
                this.motionY = velY;
            }
        }
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public double followRange() {
        return 4D;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    public void setPackBuffing(boolean value) {
        this.dataManager.set(PACK_BUFFING, Boolean.valueOf(value));
        this.setActing(value);
    }

    public boolean isPackBuffing() {
        return this.dataManager.get(PACK_BUFFING);
    }

    public void setPackBuffCooldown(int value) {
        this.packBuffCooldown = value;
    }

    public int getPackBuffCooldown() {
        return this.packBuffCooldown;
    }

    public List<PotionEffect> packBuffEffect() {
        List<PotionEffect> packBuffEffects = new ArrayList<>();
        packBuffEffects.add(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
        packBuffEffects.add(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
        return packBuffEffects;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (0.05) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (0.05) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing()) {
                        UUID ownerID =  this.getOwnerId();
                        List<EntityLivingBase> potTargetListM = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(this.attackWidth, this.attackWidth, this.attackWidth).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
                            @Override
                            public boolean apply(@Nullable EntityLivingBase input) {
                                if (input instanceof EntityTameable) {
                                    EntityTameable inpTameable = (EntityTameable)input;
                                    if (inpTameable.isTamed()) {
                                        return !ownerID.equals(inpTameable.getOwnerId());
                                    }
                                    else return true;
                                }
                                return true;
                            }
                        });
                        potTargetListM.remove(this);
                        potTargetListM.remove(this.getControllingPassenger());

                        if (!potTargetListM.isEmpty()) this.setAttacking(true);
                        else if (this.onGround) this.leapToControlledTargetLoc();
                    }
                }
                else {
                    if (!this.isActing()) {
                        UUID ownerID =  this.getOwnerId();
                        List<EntityLivingBase> potTargetListM = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(this.attackWidth, this.attackWidth, this.attackWidth).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
                            @Override
                            public boolean apply(@Nullable EntityLivingBase input) {
                                if (input instanceof EntityTameable) {
                                    EntityTameable inpTameable = (EntityTameable)input;
                                    if (inpTameable.isTamed()) {
                                        return !ownerID.equals(inpTameable.getOwnerId());
                                    }
                                    else return true;
                                }
                                return true;
                            }
                        });
                        potTargetListM.remove(this);
                        potTargetListM.remove(this.getControllingPassenger());

                        if (!potTargetListM.isEmpty()) {
                            this.ssrTarget = target;
                            this.setAttacking(true);
                        }
                        else if (this.onGround) this.leapToControlledTargetLoc(target);
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (!this.isActing()) {
                UUID ownerID =  this.getOwnerId();
                List<Utahraptor> tamedPackList = this.world.getEntitiesWithinAABB(Utahraptor.class, this.herdBoundingBox(), new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature input) {
                        if (input.isTamed()) {
                            return ownerID.equals(input.getOwnerId());
                        }
                        return false;
                    }
                });
                tamedPackList.remove(this);
                if (tamedPackList.size() >= 2) this.setPackBuffing(true);
                else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_pack_members", this.getName()), false);
                this.setRightClickUse(0);
            }
        }
        if (control == 2) {
            final float leapHeight = Math.min(6f, 0.25f * holdAmount + 1);
            final float g = 0.08f;
            if (this.getEnergy() > 6) {
                if (this.isMoving(false)) {
                    double dx = (16 * Math.sin(-Math.toRadians(this.rotationYaw)));
                    double dz = (16 * Math.cos(Math.toRadians(this.rotationYaw)));

                    double velY = Math.sqrt(2 * g * leapHeight);
                    double totalTime = velY / g;

                    this.motionX = this.motionX + dx / totalTime;
                    this.motionZ = this.motionZ + dz / totalTime;
                    this.motionY = velY;
                }
                else this.motionY = Math.sqrt(2 * g * leapHeight);
                this.setEnergy(this.getEnergy() - Math.min(6, (int)(0.25D * holdAmount + 1D)));
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
            this.setSpacebarUse(0);
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
    public boolean hasSpacebarChargeBar() {
        return true;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1f);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::utahraptorMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::utahraptorAttack));
        data.addAnimationController(new AnimationController(this, "pack_buff", 0, this::utahraptorPackBuff));
    }

    private <E extends IAnimatable> PlayState utahraptorMovement(AnimationEvent<E> event) {
        if (event.isMoving() && this.onGround) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.walk", true));
            return PlayState.CONTINUE;
        }
        else if (event.isMoving() && !this.onGround && !this.isSitting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.pounce", true));
            return PlayState.CONTINUE;
        }
        else if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.sitting", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState utahraptorAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.attack", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState utahraptorPackBuff(AnimationEvent<E> event) {
        if (this.isPackBuffing()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.pack_buffing", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.UTAHRAPTOR_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.UTAHRAPTOR_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.UTAHRAPTOR_DEATH;
    }

    public SoundEvent getCallSound() {
        return RiftSounds.UTAHRAPTOR_CALL;
    }
}
