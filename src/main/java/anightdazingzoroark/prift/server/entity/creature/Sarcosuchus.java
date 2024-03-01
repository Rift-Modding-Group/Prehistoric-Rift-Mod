package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
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

public class Sarcosuchus extends RiftWaterCreature {
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.<Boolean>createKey(Sarcosuchus.class, DataSerializers.BOOLEAN);

    public Sarcosuchus(World worldIn) {
        super(worldIn, RiftCreatureType.SARCOSUCHUS);
        this.setSize(1.25f, 1.25f);
        this.minCreatureHealth = SarcosuchusConfig.getMinHealth();
        this.maxCreatureHealth = SarcosuchusConfig.getMaxHealth();
        this.experienceValue = 10;
        this.favoriteFood = SarcosuchusConfig.sarcosuchusFavoriteFood;
        this.tamingFood = SarcosuchusConfig.sarcosuchusTamingFood;
        this.isRideable = true;
        this.attackWidth = 3f;
        this.saddleItem = SarcosuchusConfig.sarcosuchusSaddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 10D;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
        this.dataManager.register(SPINNING, false);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, SarcosuchusConfig.sarcosuchusTargets, SarcosuchusConfig.sarcosuchusTargetBlacklist, true, true, true));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, SarcosuchusConfig.sarcosuchusFavoriteFood, true));

        this.tasks.addTask(1, new RiftMate(this));

        this.tasks.addTask(2, new RiftAttack.SarcosuchusAttack(this, 4.0D, 0.52f, 0.52f));

//        this.tasks.addTask(3, new RiftFollowOwner(this, 1.0D, 6.0F, 2.0F));
        this.tasks.addTask(4, new RiftMoveToHomePos(this, 1.0D));

        this.tasks.addTask(5, new RiftGoToWater(this, 16, 1.0D));
        this.tasks.addTask(6, new RiftWanderWater(this, 1.0D));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(SarcosuchusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    @Override
    public void resetParts(float scale) {}

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1.5f);
    }

    public boolean isSpinning() {
        return this.dataManager.get(SPINNING);
    }

    public void setIsSpinning(boolean value) {
        this.dataManager.set(SPINNING, value);
        this.setActing(value);
    }

    public boolean attackEntityUsingSpin(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()/4D)));
        if (flag) this.applyEnchantments(this, entityIn);
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY - 1.375, this.posZ);
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
    public boolean isAmphibious() {
        return true;
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

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::sarcosuchusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::sarcosuchusAttack));
    }

    private <E extends IAnimatable> PlayState sarcosuchusMovement(AnimationEvent<E> event) {
        if (this.isInWater()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.swim", true));
            return PlayState.CONTINUE;
        }
        else {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.sitting", true));
                return PlayState.CONTINUE;
            }
            if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.walk", true));
                return PlayState.CONTINUE;
            }
            event.getController().clearAnimationCache();
            return PlayState.STOP;
        }
    }

    private <E extends IAnimatable> PlayState sarcosuchusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.attack", false));
            return PlayState.CONTINUE;
        }
        if (this.isSpinning()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sarcosuchus.spin_attack", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }
}
