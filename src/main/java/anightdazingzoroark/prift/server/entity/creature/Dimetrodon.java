package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.config.UtahraptorConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Dimetrodon extends RiftCreature {
    public Dimetrodon(World worldIn) {
        super(worldIn, RiftCreatureType.DIMETRODON);
        this.minCreatureHealth = DimetrodonConfig.getMinHealth();
        this.maxCreatureHealth = DimetrodonConfig.getMaxHealth();
        this.setSize(1f, 1f);
        this.favoriteFood = DimetrodonConfig.dimetrodonFavoriteFood;
        this.tamingFood = DimetrodonConfig.dimetrodonTamingFood;
        this.experienceValue = 3;
        this.speed = 0.20D;
        this.isRideable = false;
        this.attackWidth = 3f;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(DimetrodonConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, DimetrodonConfig.dimetrodonTargets, DimetrodonConfig.dimetrodonTargetBlacklist, false, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, DimetrodonConfig.dimetrodonFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, 0.52F, 0.52F));
        this.tasks.addTask(3, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(5, new RiftWander(this, 1.0D));
        this.tasks.addTask(6, new RiftLookAround(this));
    }

    @Override
    public void resetParts(float scale) {

    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.4f, 1.25f);
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {

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
        data.addAnimationController(new AnimationController(this, "movement", 0, this::dimetrodonMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::dimetrodonAttack));
    }

    private <E extends IAnimatable> PlayState dimetrodonMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState dimetrodonAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dimetrodon.attack", false));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }
}
