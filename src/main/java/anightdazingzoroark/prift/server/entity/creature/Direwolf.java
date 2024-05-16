package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.DirewolfConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IPackHunter;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSpawnChestDetectParticle;
import anightdazingzoroark.prift.server.message.RiftSpawnDetectParticle;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Direwolf extends RiftCreature implements IPackHunter {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/direwolf"));
    private static final DataParameter<Boolean> PACK_BUFFING = EntityDataManager.createKey(Direwolf.class, DataSerializers.BOOLEAN);
    private int packBuffCooldown;
    private int sniffCooldown;
    private RiftCreaturePart hipsPart;

    public Direwolf(World worldIn) {
        super(worldIn, RiftCreatureType.DIREWOLF);
        this.minCreatureHealth = DirewolfConfig.getMinHealth();
        this.maxCreatureHealth = DirewolfConfig.getMaxHealth();
        this.setSize(1f, 1.55f);
        this.experienceValue = 10;
        this.favoriteFood = DirewolfConfig.direwolfFavoriteFood;
        this.tamingFood = DirewolfConfig.direwolfTamingFood;
        this.speed = 0.25D;
        this.isRideable = true;
        this.attackWidth = 2.5f;
        this.maxRightClickCooldown = 1800f;
        this.saddleItem = DirewolfConfig.direwolfSaddleItem;
        this.attackDamage = DirewolfConfig.damage;
        this.healthLevelMultiplier = DirewolfConfig.healthMultiplier;
        this.damageLevelMultiplier = DirewolfConfig.damageMultiplier;
        this.densityLimit = DirewolfConfig.direwolfDensityLimit;
        this.packBuffCooldown = 0;
        this.sniffCooldown = 0;
        this.targetList = RiftUtil.creatureTargets(DirewolfConfig.direwolfTargets, DirewolfConfig.direwolfTargetBlacklist, true);
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
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftPackBuff(this, 2.28f, 0.76f, 90f));
        this.tasks.addTask(4, new RiftControlledAttack(this, 0.28F, 0.28F));
        this.tasks.addTask(4, new RiftControlledPackBuff(this, 2.28f, 0.76f));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 0.6F, 0.48F));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(10, new RiftHerdMemberFollow(this));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //manage pack buff cooldown
        if (this.packBuffCooldown > 0) this.packBuffCooldown--;
        if (this.getRightClickCooldown() > 0) this.setRightClickCooldown(this.getRightClickCooldown() - 1);

        //manage sniffing cooldown
        if (this.sniffCooldown > 0) this.sniffCooldown--;
    }

    //sniffCooldown

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 1f, 0, 0.8f, 0.7f * scale, 0.7f * scale, 1.5f);
            this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.6f, scale, 0.7f * scale, 1f);
            this.hipsPart = new RiftCreaturePart(this, -0.9f, 0, 0.6f, 0.9f * scale, 0.7f * scale, 1f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.hipsPart != null) this.hipsPart.onUpdate();

        if (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden() && this.hipsPart != null) {
            this.hipsPart.setPositionAndUpdate(this.hipsPart.posX, this.hipsPart.posY - 0.3f, this.hipsPart.posZ);
        }
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.hipsPart != null) {
            this.world.removeEntityDangerously(this.hipsPart);
            this.hipsPart = null;
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
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.25f);
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.375f) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.375f) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.875, zOffset);
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
            if (!this.isActing()) {
                UUID ownerID =  this.getOwnerId();
                List<Direwolf> tamedPackList = this.world.getEntitiesWithinAABB(Direwolf.class, this.herdBoundingBox(), new Predicate<RiftCreature>() {
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
        if (control == 3 && this.sniffCooldown == 0 && this.headPart != null) {
            if (!this.headPart.isUnderwater()) {
                this.sniffCooldown = 100;
                //for all entities nearby (except those that are submerged)
                AxisAlignedBB mobDetectAABB = new AxisAlignedBB(this.posX - DirewolfConfig.direwolfMobSniffRange, this.posY - DirewolfConfig.direwolfMobSniffRange, this.posZ - DirewolfConfig.direwolfMobSniffRange, this.posX + DirewolfConfig.direwolfMobSniffRange, this.posY + DirewolfConfig.direwolfMobSniffRange, this.posZ + DirewolfConfig.direwolfMobSniffRange);
                for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, mobDetectAABB, null)) {
                    if (entityLivingBase != this && entityLivingBase != this.getOwner() && !RiftUtil.entityIsUnderwater(entityLivingBase) && RiftUtil.isAppropriateSize(entityLivingBase, MobSize.safeValueOf(DirewolfConfig.direwolfMaxSniffSize))) {
                        RiftMessages.WRAPPER.sendToAll(new RiftSpawnDetectParticle((EntityPlayer)this.getControllingPassenger(), (int)entityLivingBase.posX, (int)entityLivingBase.posY, (int)entityLivingBase.posZ));
                    }
                }
                //for chests
                for (int x = -DirewolfConfig.direwolfBlockSniffRange; x <= DirewolfConfig.direwolfBlockSniffRange; x++) {
                    for (int y = -DirewolfConfig.direwolfBlockSniffRange; y <= DirewolfConfig.direwolfBlockSniffRange; y++) {
                        for (int z = -DirewolfConfig.direwolfBlockSniffRange; z <= DirewolfConfig.direwolfBlockSniffRange; z++) {
                            BlockPos pos = this.getPosition().add(x, y, z);
                            if (this.isSniffableBlock(this.world.getBlockState(pos).getBlock(), this.world.getBlockState(pos))) {
                                RiftMessages.WRAPPER.sendToAll(new RiftSpawnChestDetectParticle((EntityPlayer)this.getControllingPassenger(), pos.getX(), pos.getY(), pos.getZ()));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isSniffableBlock(Block block, IBlockState blockState) {
        boolean flag = false;
        for (String blockEntry : DirewolfConfig.direwolfSniffableBlocks) {
            if (flag) break;
            int blockIdFirst = blockEntry.indexOf(":");
            int blockIdSecond = blockEntry.indexOf(":", blockIdFirst + 1);
            int blockData = Integer.parseInt(blockEntry.substring(blockIdSecond + 1));
            flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == -1 || block.getMetaFromState(blockState) == blockData);
        }
        return flag;
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
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::direwolfMovement));
        data.addAnimationController(new AnimationController(this, "attacking", 0, this::direwolfAttack));
        data.addAnimationController(new AnimationController(this, "pack_buff", 0, this::direwolfPackBuff));
    }

    private <E extends IAnimatable> PlayState direwolfMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.direwolf.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.direwolf.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState direwolfAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.direwolf.attack", false));
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState direwolfPackBuff(AnimationEvent<E> event) {
        if (this.isPackBuffing()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.direwolf.howl", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.DIREWOLF_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DIREWOLF_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DIREWOLF_DEATH;
    }

    public SoundEvent getCallSound() {
        return RiftSounds.DIREWOLF_HOWL;
    }
}
