package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.config.MegapiranhaConfig;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import com.codetaylor.mc.athenaeum.util.Properties;
import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.ParasaurolophusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import com.codetaylor.mc.pyrotech.modules.tech.machine.block.spi.BlockCombustionWorkerStoneBase;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi.TileCombustionWorkerStoneBase;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.*;

public class Parasaurolophus extends RiftCreature implements IWorkstationUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/parasaurolophus"));
    private static final DataParameter<Boolean> BLOWING = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_BLOW = EntityDataManager.createKey(Parasaurolophus.class, DataSerializers.BOOLEAN);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;

    public Parasaurolophus(World worldIn) {
        super(worldIn, RiftCreatureType.PARASAUROLOPHUS);
        this.minCreatureHealth = ParasaurolophusConfig.getMinHealth();
        this.maxCreatureHealth = ParasaurolophusConfig.getMaxHealth();
        this.setSize(2f, 2f);
        this.favoriteFood = ParasaurolophusConfig.parasaurolophusFavoriteFood;
        this.tamingFood = ParasaurolophusConfig.parasaurolophusTamingFood;
        this.experienceValue = 20;
        this.speed = 0.25D;
        this.isRideable = true;
        this.attackWidth = 3.5f;
        this.saddleItem = ParasaurolophusConfig.parasaurolophusSaddleItem;
        this.attackDamage = ParasaurolophusConfig.damage;
        this.healthLevelMultiplier = ParasaurolophusConfig.healthMultiplier;
        this.damageLevelMultiplier = ParasaurolophusConfig.damageMultiplier;
        this.densityLimit = ParasaurolophusConfig.parasaurolophusDensityLimit;
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
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTurretModeTargeting(this, true));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(0, new RiftParasaurStokeCombustor(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftResetAnimatedPose(this, 1.52F, 1));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(3, new RiftParasaurolophusBlow(this));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(6, new RiftHerdDistanceFromOtherMembers(this, 1.5D));
        this.tasks.addTask(7, new RiftHerdMemberFollow(this));
        this.tasks.addTask(8, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
        this.tasks.addTask(10, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanBlow();
    }

    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 2.875f, 0, 1.48f, 0.625f * scale, 0.5f * scale, 1.5f);
            this.bodyPart = new RiftMainBodyPart(this, 1.375f, 0, 0.8f, 0.75f * scale, 0.8f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 2.125f, 0, 1.125f, 0.5f * scale, 0.7f * scale, 1.5f);
            this.tail0Part = new RiftCreaturePart(this, -1.125f, 0, 1f, 0.4f * scale, 0.6f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -1.625f, 0, 1.1f, 0.4f * scale, 0.45f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -2.125f, 0, 1.05f, 0.4f * scale, 0.45f * scale, 0.5f);
            this.tail3Part = new RiftCreaturePart(this, -2.875f, 0, 1.1f, 0.5f * scale, 0.35f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();
        if (this.tail3Part != null) this.tail3Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.6f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
        if (this.tail3Part != null) this.tail3Part.setPositionAndUpdate(this.tail3Part.posX, this.tail3Part.posY + sitOffset, this.tail3Part.posZ);
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.neckPart != null) {
            this.world.removeEntityDangerously(this.neckPart);
            this.neckPart = null;
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
        if (this.tail3Part != null) {
            this.world.removeEntityDangerously(this.tail3Part);
            this.tail3Part = null;
        }
    }

    private void manageCanBlow() {
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);
        if (this.getRightClickCooldown() == 0) this.setCanBlow(true);
    }

    //blowing stuff starts here
    public void useBlow(float strength) {
        this.useBlow(null, strength);
    }

    public void useBlow(EntityLivingBase target, float strength) {
        if (target == null) {
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
            }
        }
        else {
            UUID ownerUUID = this.getOwnerId();
            AxisAlignedBB aabb = target.getEntityBoundingBox().grow(5);
            List<EntityLivingBase> entityList = this.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb, new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    if (input instanceof EntityPlayer) {
                        return !input.getUniqueID().equals(ownerUUID);
                    }
                    if (input instanceof EntityTameable) {
                        if (((EntityTameable)input).isTamed()) {
                            return !((EntityTameable) input).getOwnerId().equals(ownerUUID);
                        }
                    }
                    return true;
                }
            });
            for (EntityLivingBase entityLivingBase : entityList) this.parasaurKnockback(entityLivingBase, strength);
        }
    }

    public void parasaurKnockback(EntityLivingBase entity, float strength) {
        double d0 = this.posX - entity.posX;
        double d1 = this.posZ - entity.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        entity.knockBack(this, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
    }

    public void parsaurManualStokeHeater(float strength) {
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos tempPos = pos.add(x, 0, z);
                TileEntity tileEntity = this.world.getTileEntity(tempPos);
                if (tileEntity != null) {
                    if (tileEntity instanceof TileCombustionWorkerStoneBase) {
                        TileCombustionWorkerStoneBase stoked = (TileCombustionWorkerStoneBase) tileEntity;
                        if (stoked.hasFuel() && stoked.workerIsActive() && stoked.hasInput()) {
                            stoked.consumeAirflow(RiftUtil.clamp(0.04f * strength + 12f, 12, 16), false);
                        }
                    }
                }
            }
        }
    }
    //blowing stuff ends here

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 0.35, this.posZ);
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(ICamera camera) {
        return super.shouldRender(camera) || this.inFrustrum(camera, this.neckPart) || this.inFrustrum(camera, this.tail0Part) || this.inFrustrum(camera, this.tail1Part) || this.inFrustrum(camera, this.tail2Part) || this.inFrustrum(camera, this.tail3Part);
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
                    this.useBlow(target, RiftUtil.clamp(0.04f * holdAmount + 2f, 2f, 6f));
                    this.setEnergy(this.getEnergy() - (int)(0.05d * (double)Math.min(holdAmount, 100) + 1d));
                    this.setRightClickCooldown(Math.max(60, holdAmount * 2));
                    this.playSound(RiftSounds.PARASAUROLOPHUS_BLOW, 2, 1);
                    if (Loader.isModLoaded(RiftInitialize.PYROTECH_MOD_ID)) this.parsaurManualStokeHeater(holdAmount);
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

    public boolean canDoTurretMode() {
        return true;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.5f);
    }

    @Override
    public boolean canUseWorkstation() {
        return Loader.isModLoaded(RiftInitialize.PYROTECH_MOD_ID);
    }

    @Override
    public boolean isWorkstation(BlockPos pos) {
        Block block = this.world.getBlockState(pos).getBlock();
        if (Loader.isModLoaded(RiftInitialize.PYROTECH_MOD_ID)) {
            if (block instanceof BlockCombustionWorkerStoneBase) return true;
        }
        return false;
    }

    @Override
    public BlockPos workstationUseFromPos() {
        IBlockState blockState = this.world.getBlockState(this.getWorkstationPos());
        if (blockState.getMaterial().isSolid()) {
            EnumFacing direction = blockState.getValue(Properties.FACING_HORIZONTAL);
            switch (direction) {
                case NORTH:
                    return this.getWorkstationPos().add(0, 0, -4);
                case SOUTH:
                    return this.getWorkstationPos().add(0, 0, 4);
                case EAST:
                    return this.getWorkstationPos().add(4, 0, 0);
                case WEST:
                    return this.getWorkstationPos().add(-4, 0, 0);
            }
        }
        return null;
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public double followRange() {
        return 4D;
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
    public boolean canBeLeashedTo(EntityPlayer player) {
        return !this.getLeashed() && this.isTamed() && !this.getTameStatus().equals(TameStatusType.SIT) && !this.getTameStatus().equals(TameStatusType.TURRET_MODE);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::parasaurolophusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::parasaurolophusAttack));
        data.addAnimationController(new AnimationController(this, "blow", 0, this::parasaurolophusBlow));
        data.addAnimationController(new AnimationController(this, "controlledBlow", 0, this::parasaurolophusControlledBlow));
    }

    private <E extends IAnimatable> PlayState parasaurolophusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget() && !this.isUsingWorkstation()) {
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
