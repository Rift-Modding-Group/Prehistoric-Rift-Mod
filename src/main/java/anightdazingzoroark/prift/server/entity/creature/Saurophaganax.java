package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.SaurophaganaxConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
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

public class Saurophaganax extends RiftCreature {
    private static final DataParameter<Boolean> USING_LIGHT_BLAST = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LIGHT_BLAST_CHARGE = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.VARINT);

    public Saurophaganax(World worldIn) {
        super(worldIn, RiftCreatureType.SAUROPHAGANAX);
        this.setSize(2f, 3f);
        this.minCreatureHealth = SaurophaganaxConfig.getMinHealth();
        this.maxCreatureHealth = SaurophaganaxConfig.getMaxHealth();
        this.favoriteFood = SaurophaganaxConfig.saurophaganaxFavoriteFood;
        this.tamingFood = SaurophaganaxConfig.saurophaganaxFavoriteFood;
        this.experienceValue = 20;
        this.isRideable = true;
        this.saddleItem = SaurophaganaxConfig.saurophaganaxSaddleItem;
        this.speed = 0.25D;
        this.attackWidth = 3.5F;
        this.attackDamage = SaurophaganaxConfig.damage;
        this.healthLevelMultiplier = SaurophaganaxConfig.healthMultiplier;
        this.damageLevelMultiplier = SaurophaganaxConfig.damageMultiplier;
        this.densityLimit = SaurophaganaxConfig.saurophaganaxDensityLimit;
        this.targetList = RiftUtil.creatureTargets(SaurophaganaxConfig.saurophaganaxTargets);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
        this.dataManager.register(USING_LIGHT_BLAST, false);
        this.dataManager.register(LIGHT_BLAST_CHARGE, 0);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, false, true));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 0.52F, 0.24F));
        this.tasks.addTask(6, new RiftWander(this, 1.0D));
        this.tasks.addTask(7, new RiftLookAround(this));
    }

    @Override
    public void resetParts(float scale) {

    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.2f, 2f);
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

    public boolean isUsingLightBlast() {
        return this.dataManager.get(USING_LIGHT_BLAST);
    }

    public void setUsingLightBlast(boolean value) {
        this.dataManager.set(USING_LIGHT_BLAST, value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::saurophaganaxMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::saurophaganaxAttack));
        data.addAnimationController(new AnimationController(this, "lightBlast", 0, this::saurophaganaxLightBlast));
    }

    private <E extends IAnimatable> PlayState saurophaganaxMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState saurophaganaxAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.attack", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState saurophaganaxLightBlast(AnimationEvent<E> event) {
        if (this.isUsingLightBlast()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.light_blast", false));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.setup", true));
        }
        return PlayState.CONTINUE;
    }
}
