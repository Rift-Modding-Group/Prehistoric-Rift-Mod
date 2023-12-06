package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.UtahraptorConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureinterface.ILeapingMob;
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

public class Utahraptor extends RiftCreature implements ILeapingMob {
    public Utahraptor(World worldIn) {
        super(worldIn, RiftCreatureType.UTAHRAPTOR);
        this.minCreatureHealth = UtahraptorConfig.getMinHealth();
        this.maxCreatureHealth = UtahraptorConfig.getMaxHealth();
        this.setSize(1.25f, 1.5f);
        this.experienceValue = 3;
        this.favoriteFood = UtahraptorConfig.utahraptorFavoriteFood;
        this.tamingFood = UtahraptorConfig.utahraptorTamingFood;
        this.speed = 0.35D;
        this.isRideable = true;
        this.attackWidth = 2f;
        this.leapWidth = 16f;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(UtahraptorConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, UtahraptorConfig.utahraptorTargets, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, UtahraptorConfig.utahraptorFavoriteFood, true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.28F, 0.28F));
        this.tasks.addTask(3, new RiftLeapAttack(this, 1.5f));
        this.tasks.addTask(4, new RiftAttack(this, 1.0D, 0.28F, 0.28F));
        this.tasks.addTask(5, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(6, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    public void fall(float distance, float damageMultiplier) {}

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.3f, 1f);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::utahraptorMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::utahraptorAttack));
    }

    private <E extends IAnimatable> PlayState utahraptorMovement(AnimationEvent<E> event) {
        if (event.isMoving() && this.onGround) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.utahraptor.walk", true));
            return PlayState.CONTINUE;
        }
        else if (!this.onGround) {
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
}
