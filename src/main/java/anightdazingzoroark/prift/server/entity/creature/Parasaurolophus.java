package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.ParasaurolophusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Parasaurolophus extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/parasaurolophus"));
    private static final DataParameter<Boolean> BLOWING = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_BLOW = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);

    public Parasaurolophus(World worldIn) {
        super(worldIn, RiftCreatureType.PARASAUROLOPHUS);
        this.minCreatureHealth = ParasaurolophusConfig.getMinHealth();
        this.maxCreatureHealth = ParasaurolophusConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = ParasaurolophusConfig.parasaurolophusFavoriteFood;
        this.tamingFood = ParasaurolophusConfig.parasaurolophusTamingFood;
        this.experienceValue = 20;
        this.speed = 0.25D;
        this.attackWidth = 3.5f;
        this.saddleItem = ParasaurolophusConfig.parasaurolophusSaddleItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BLOWING, false);
        this.dataManager.register(CAN_BLOW, true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ParasaurolophusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftResetAnimatedPose(this, 1.52F, 1));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(3, new RiftParasaurolophusBlow(this));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(6, new RiftHerdMemberFollow(this, 8D, 4D, 1D));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanBlow();
    }

    private void manageCanBlow() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanBlow(true);
    }

    //blowing stuff starts here
    public void useBlow(float strength) {
        double dist = this.getEntityBoundingBox().maxX - this.getEntityBoundingBox().minX + 8D;
        Vec3d vec3d = this.getPositionEyes(1.0F);
        Vec3d vec3d1 = this.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        Entity rider = this.getControllingPassenger();
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(5.0D, 5.0D, 5.0D), null);
        double d2 = d1;
        for (EntityLivingBase entity : list) {
            AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow((double) entity.getCollisionBorderSize() + 2F);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (entity != this && entity != rider) {
                if (entity instanceof Parasaurolophus) {
                    if ((((Parasaurolophus)entity).isTamed() && !this.isTamed()) || (!((Parasaurolophus)entity).isTamed() && this.isTamed())) {
                        if (axisalignedbb.contains(vec3d)) {
                            if (d2 >= 0.0D) {
                                this.parasaurKnockback(entity, strength);
                                d2 = 0.0D;
                            }
                        }
                        else if (raytraceresult != null) {
                            double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                            if (d3 < d2 || d2 == 0.0D) {
                                this.parasaurKnockback(entity, strength);
                                d2 = d3;
                            }
                        }
                    }
                }
                else {
                    System.out.println("not another parasaur");
                    if (axisalignedbb.contains(vec3d)) {
                        System.out.println("cond 1");
                        if (d2 >= 0.0D) {
                            this.parasaurKnockback(entity, strength);
                            d2 = 0.0D;
                        }
                    }
                    else if (raytraceresult != null) {
                        System.out.println("cond 2");
                        double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                        if (d3 < d2 || d2 == 0.0D) {
                            this.parasaurKnockback(entity, strength);
                            d2 = d3;
                        }
                    }
                }
            }
        }
    }

    public void parasaurKnockback(EntityLivingBase entity, float strength) {
        double d0 = this.posX - entity.posX;
        double d1 = this.posZ - entity.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        entity.knockBack(this, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
    }
    //blowing stuff ends here

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 0.35, this.posZ);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (target == null) {
                    if (!this.isActing()) this.setAttacking(true);
                }
                else {
                    if (!this.isActing()) {
                        this.ssrTarget = target;
                        this.setAttacking(true);
                    }
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            if (this.getEnergy() > 6) {
                if (this.canBlow() && !this.isActing()) {
                    this.setActing(true);
                    this.setCanBlow(false);
                    this.useBlow(RiftUtil.clamp(0.04f * holdAmount + 2f, 2f, 6f));
                    this.setEnergy(this.getEnergy() - (int)(0.05d * (double)Math.min(holdAmount, 100) + 1d));
                    this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
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
        return false;
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    @Override
    public boolean isTameableByFeeding() {
        return true;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    public boolean isBlowing() {
        return this.dataManager.get(BLOWING);
    }

    public void setBlowing(boolean value) {
        this.dataManager.set(BLOWING, value);
        this.setActing(value);
    }

    public boolean canBlow() {
        return this.dataManager.get(CAN_BLOW);
    }

    public void setCanBlow(boolean value) {
        this.dataManager.set(CAN_BLOW, value);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::parasaurolophusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::parasaurolophusAttack));
        data.addAnimationController(new AnimationController(this, "blow", 0, this::parasaurolophusBlow));
        data.addAnimationController(new AnimationController(this, "controlledBlow", 0, this::parasaurolophusControlledBlow));
    }

    private <E extends IAnimatable> PlayState parasaurolophusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusBlow(AnimationEvent<E> event) {
        if (this.isBlowing()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.blow", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState parasaurolophusControlledBlow(AnimationEvent<E> event) {
        if (this.getRightClickCooldown() == 0) {
            if (this.getRightClickUse() > 0 && this.getRightClickUse() < 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.use_blow_p1", false));
            else if (this.getRightClickUse() >= 100) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.use_blow_p1_hold", true));
        }
        else event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.parasaurolophus.use_blow_p2", false));
        return PlayState.CONTINUE;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.PARASAUROLOPHUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.PARASAUROLOPHUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.PARASAUROLOPHUS_DEATH;
    }
}
