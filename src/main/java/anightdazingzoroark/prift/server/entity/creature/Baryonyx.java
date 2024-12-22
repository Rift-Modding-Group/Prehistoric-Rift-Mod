package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.BaryonyxConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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

public class Baryonyx extends RiftWaterCreature {
    private static final DataParameter<Boolean> LEFT_CLAW = EntityDataManager.<Boolean>createKey(Baryonyx.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT_CLAW = EntityDataManager.<Boolean>createKey(Baryonyx.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/baryonyx"));
    private RiftCreaturePart mainHeadPart;
    private RiftCreaturePart neckPart;
    private RiftCreaturePart frontBodyPart;
    private RiftCreaturePart leftLegPart;
    private RiftCreaturePart rightLegPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;
    private RiftCreaturePart tail3Part;
    private RiftCreaturePart tail4Part;

    public Baryonyx(World worldIn) {
        super(worldIn, RiftCreatureType.BARYONYX);
        this.setSize(1.25f, 2.75f);
        this.experienceValue = 20;
        this.favoriteFood = ((BaryonyxConfig)RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((BaryonyxConfig)RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = ((BaryonyxConfig)RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;
        this.speed = 0.25D;
        this.waterSpeed = 5D;
        this.targetList = RiftUtil.creatureTargets(((BaryonyxConfig)RiftConfigHandler.getConfig(this.creatureType)).general.targetWhitelist, ((BaryonyxConfig)RiftConfigHandler.getConfig(this.creatureType)).general.targetBlacklist, true);

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 1.25f, 0.9f, 1f, 1f);
        this.headPart = new RiftCreaturePart(this, 4f, 0, 2.125f, 0.75f, 0.5f, 1.5f);
        this.mainHeadPart = new RiftCreaturePart(this, 3, 0, 2.125f, 0.625f, 0.625f, 1.5f);
        this.neckPart = new RiftCreaturePart(this, 2.25f, 0, 1.625f, 0.5f, 1f, 1.5f);
        this.frontBodyPart = new RiftCreaturePart(this, 1.25f, 0, 1.25f, 0.9f, 0.9f, 1f);
        this.leftLegPart = new RiftCreaturePart(this, 0.75f, -140f, 0, 0.5f, 1.3f, 0.5f);
        this.rightLegPart = new RiftCreaturePart(this, 0.75f, 140f, 0, 0.5f, 1.3f, 0.5f);
        this.tail0Part = new RiftCreaturePart(this, -1f, 0, 1.5f, 0.6f, 0.7f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.75f, 0, 1.5f, 0.6f, 0.7f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -2.5f, 0, 1.45f, 0.5f, 0.7f, 0.5f);
        this.tail3Part = new RiftCreaturePart(this, -3.25f, 0, 1.45f, 0.5f, 0.6f, 0.5f);
        this.tail4Part = new RiftCreaturePart(this, -4f, 0, 1.4f, 0.5f, 0.6f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.bodyPart,
            this.headPart,
            this.mainHeadPart,
            this.neckPart,
            this.frontBodyPart,
            this.leftLegPart,
            this.rightLegPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part,
            this.tail3Part,
            this.tail4Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEFT_CLAW, false);
        this.dataManager.register(RIGHT_CLAW, false);
        this.setCanPickUpLoot(true);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //remove poison
        if (this.getActivePotionEffect(MobEffects.POISON) != null) this.removePotionEffect(MobEffects.POISON);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        //this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(2, new RiftBaryonyxControlledClawAttack(this));
        this.tasks.addTask(3, new RiftAttack.BaryonyxAttack(this, 1.0D));
        this.tasks.addTask(4, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        //this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftGoToWater(this, 16, 1.0D));
        this.tasks.addTask(7, new RiftWanderWater(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden() && !this.isInWater()) ? -0.75f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.mainHeadPart != null) this.mainHeadPart.setPositionAndUpdate(this.mainHeadPart.posX, this.mainHeadPart.posY + sitOffset, this.mainHeadPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.frontBodyPart != null) this.frontBodyPart.setPositionAndUpdate(this.frontBodyPart.posX, this.frontBodyPart.posY + sitOffset, this.frontBodyPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
        if (this.tail3Part != null) this.tail3Part.setPositionAndUpdate(this.tail3Part.posX, this.tail3Part.posY + sitOffset, this.tail3Part.posZ);
        if (this.tail4Part != null) this.tail4Part.setPositionAndUpdate(this.tail4Part.posX, this.tail4Part.posY + sitOffset, this.tail4Part.posZ);
    }

    @Override
    public boolean isAmphibious() {
        return true;
    }

    public boolean attackUsingClaw(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()) / 2f);
        if (flag) {
            this.applyEnchantments(this, entityIn);
            if (entityIn instanceof EntityLivingBase) ((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, 5 * 20, 1));
        }
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    public boolean isUsingLeftClaw() {
        return this.dataManager.get(LEFT_CLAW);
    }

    public void setUsingLeftClaw(boolean value) {
        this.dataManager.set(LEFT_CLAW, value);
        this.setActing(value);
    }

    public boolean isUsingRightClaw() {
        return this.dataManager.get(RIGHT_CLAW);
    }

    public void setUsingRightClaw(boolean value) {
        this.dataManager.set(RIGHT_CLAW, value);
        this.setActing(value);
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
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.5f, 1.5f);
    }

    public float attackWidth() {
        return 6f;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY + 0.125f, this.posZ);
    }

    public void controlClawAttack() {
        boolean breakFlag = false;

        //attack entity
        if (this.forcedAttackTarget != null && RiftUtil.checkForNoAssociations(this, this.forcedAttackTarget)) {
            breakFlag = this.attackUsingClaw(this.forcedAttackTarget);
        }

        //break blocks
        if (this.forcedBreakPos != null && !breakFlag) {
            IBlockState blockState = this.world.getBlockState(this.forcedBreakPos);
            if (blockState.getMaterial() != Material.AIR && this.checkBasedOnStrength(blockState)) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos toBreakPos = this.forcedBreakPos.add(x, y, z);
                            IBlockState toBreakState = this.world.getBlockState(toBreakPos);
                            if (toBreakState.getMaterial() != Material.AIR && this.checkBasedOnStrength(toBreakState)) {
                                this.world.destroyBlock(toBreakPos, true);
                            }
                        }
                    }
                }
            }
        }

        //reset
        this.forcedAttackTarget = null;
        this.forcedBreakPos = null;
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
            if (this.getEnergy() > 0) {
                if (!this.isActing()) {
                    this.forcedAttackTarget = target;
                    this.forcedBreakPos = pos;
                    if (RiftUtil.randomInRange(0, 1) == 0) this.setUsingLeftClaw(true);
                    else this.setUsingRightClaw(true);
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
        data.addAnimationController(new AnimationController(this, "movement", 0, this::baryonyxMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::baryonyxAttack));
        data.addAnimationController(new AnimationController(this, "armPose", 0, this::baryonyxArmPose));
    }

    private <E extends IAnimatable> PlayState baryonyxMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isInWater()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.swim", true));
                return PlayState.CONTINUE;
            }
            else {
                if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.sitting", true));
                    return PlayState.CONTINUE;
                }
                if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.walk", true));
                    return PlayState.CONTINUE;
                }
                return PlayState.STOP;
            }
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState baryonyxAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.bite", false));
        }
        else if (this.isUsingLeftClaw()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.claw_two", false));
        }
        else if (this.isUsingRightClaw()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.claw_one", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState baryonyxArmPose(AnimationEvent<E> event) {
        if (!this.isUsingLeftClaw() && !this.isUsingRightClaw() && this.isInWater()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.baryonyx.swim_arms", true));
            return PlayState.CONTINUE;
        }
        else event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.BARYONYX_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.BARYONYX_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.BARYONYX_DEATH;
    }
}
