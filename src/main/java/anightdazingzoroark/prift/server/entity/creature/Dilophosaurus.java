package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.DilophosaurusConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IRangedAttacker;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpit;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Dilophosaurus extends RiftCreature implements IRangedAttacker {
    private static final DataParameter<Boolean> LEFT_CLAW = EntityDataManager.<Boolean>createKey(Dilophosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RIGHT_CLAW = EntityDataManager.<Boolean>createKey(Dilophosaurus.class, DataSerializers.BOOLEAN);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Dilophosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.DILOPHOSAURUS);
        this.setSize(1f, 1.75f);
        this.experienceValue = 20;
        this.favoriteFood = ((DilophosaurusConfig)RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((DilophosaurusConfig)RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = ((DilophosaurusConfig)RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;
        this.speed = 0.2D;
        this.targetList = RiftUtil.creatureTargets(((DilophosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.targetWhitelist, ((DilophosaurusConfig)RiftConfigHandler.getConfig(this.creatureType)).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 2f, 0, 1.7f, 1f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.9f, 1f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 1.5f, 0, 1.2f, 0.7f, 0.7f, 1.5f);
        this.hipPart = new RiftCreaturePart(this, 0, 0, 0.7f, 1f, 1f, 1f);
        this.tail0Part = new RiftCreaturePart(this, -0.9f, 0, 1f, 0.7f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.5f, 0, 0.95f, 0.6f, 0.6f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -2.1f, 0, 0.9f, 0.6f, 0.6f, 0.5f);

        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.hipPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEFT_CLAW, false);
        this.dataManager.register(RIGHT_CLAW, false);
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftRangedAttack(this, false, 1.0D, 2.64F, 0.72F));
        this.tasks.addTask(4, new RiftAttack.DilophosaurusAttack(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
        this.tasks.addTask(9, new RiftLookAround(this));
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1f};
    }

    @Override
    public float attackWidth() {
        return 2f;
    }

    @Override
    public float rangedWidth() {
        return 12f;
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
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        DilophosaurusSpit dilophosaurusSpit = new DilophosaurusSpit(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - dilophosaurusSpit.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        dilophosaurusSpit.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 1F);
        dilophosaurusSpit.setDamage(2D + (double)(this.getLevel())/10D);
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(dilophosaurusSpit);
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {

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
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "frillSetup", 0, this::dilophosaurusFrillSetup));
        data.addAnimationController(new AnimationController(this, "sacSetup", 0, this::dilophosaurusSacSetup));
        data.addAnimationController(new AnimationController(this, "movement", 0, this::dilophosaurusMovement));
        data.addAnimationController(new AnimationController(this, "clawAttack", 0, this::dilophosaurusClawAttack));
        data.addAnimationController(new AnimationController(this, "spitAttack", 0, this::dilophosaurusSpitAttack));
    }

    private <E extends IAnimatable> PlayState dilophosaurusFrillSetup(AnimationEvent event) {
        if (!this.isSleeping() && !this.isRangedAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.frill_setup", true));
            return PlayState.CONTINUE;
        }
        else {
            event.getController().clearAnimationCache();
            return PlayState.STOP;
        }
    }

    private <E extends IAnimatable> PlayState dilophosaurusSacSetup(AnimationEvent event) {
        if (!this.isSleeping() && !this.isRangedAttacking()) event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.sac_setup", true));
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState dilophosaurusMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.sitting", true));
                return PlayState.CONTINUE;
            }
            if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.walk", true));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState dilophosaurusClawAttack(AnimationEvent<E> event) {
        if (this.isUsingLeftClaw()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.claw_two", false));
        }
        else if (this.isUsingRightClaw()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.claw_one", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState dilophosaurusSpitAttack(AnimationEvent<E> event) {
        if (this.isRangedAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dilophosaurus.spit", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }
}
