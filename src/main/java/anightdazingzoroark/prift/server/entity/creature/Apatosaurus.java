package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Apatosaurus extends RiftCreature {
    private static final DataParameter<Byte> WEAPON = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BYTE);

    public Apatosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.APATOSAURUS);
        this.minCreatureHealth = ApatosaurusConfig.getMinHealth();
        this.maxCreatureHealth = ApatosaurusConfig.getMaxHealth();
        this.setSize(3f, 3f);
        this.favoriteFood = ApatosaurusConfig.apatosaurusFavoriteFood;
        this.tamingFood = ApatosaurusConfig.apatosaurusBreedingFood;
        this.experienceValue = 50;
        this.speed = 0.15D;
        this.isRideable = true;
        this.attackWidth = 4.5f;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WEAPON, (byte)0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ApatosaurusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 3F, 3F));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 3F, 3F));
        this.tasks.addTask(4, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftWander(this, 1.0D));
        this.tasks.addTask(7, new RiftLookAround(this));
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 54;
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

    public int getWeapon() {
        return this.dataManager.get(WEAPON);
    }

    public void setWeapon(int value) {
        this.dataManager.set(WEAPON, (byte)value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::apatosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::apatosaurusAttack));
    }

    private <E extends IAnimatable> PlayState apatosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget()) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState apatosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.stomp", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }
}
