package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.SaurophaganaxConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
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

public class Saurophaganax extends RiftCreature {
    private static final DataParameter<Boolean> USING_LIGHT_BLAST = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LIGHT_BLAST_CHARGE = EntityDataManager.createKey(Saurophaganax.class, DataSerializers.VARINT);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart bodyFrontPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

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
        this.tasks.addTask(0, new RiftSleepAtDay(this));
        this.tasks.addTask(2, new RiftSaurophaganaxUseLightBlast(this));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 0.52F, 0.24F));
        this.tasks.addTask(6, new RiftWander(this, 1.0D));
        this.tasks.addTask(7, new RiftLookAround(this));
    }

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.removeParts();
            this.oldScale = scale;
            this.headPart = new RiftCreaturePart(this, 3.5f, 0, 2f, 0.6f * scale, 0.6f * scale, 1.5f);
            this.bodyPart = new RiftMainBodyPart(this, 0f, 0, 1.125f, scale, 0.8f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 2.5f, 0, 1.7f, 0.5f * scale, 0.65f * scale, 1.5f);
            this.bodyFrontPart = new RiftCreaturePart(this, 1.5f, 0, 1.125f, 0.8f * scale, 0.8f * scale, 1f);
            this.tail0Part = new RiftCreaturePart(this, -1.5f, 0, 1.4f, 0.6f * scale, 0.6f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -2.5f, 0, 1.4f, 0.5f * scale, 0.5f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -3.5f, 0, 1.4f, 0.5f * scale, 0.5f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.bodyFrontPart != null) this.bodyFrontPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();

        boolean sleeping = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) || this.isSleeping();
        float sleepOffsetBody = sleeping ? -1.2f : 0;
        float sleepOffsetNeck = sleeping ? -1.7f : 0;
        float sleepOffsetHead = sleeping ? -2f : 0;
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sleepOffsetBody, this.bodyPart.posZ);
        if (this.bodyFrontPart != null) this.bodyFrontPart.setPositionAndUpdate(this.bodyFrontPart.posX, this.bodyFrontPart.posY + sleepOffsetBody, this.bodyFrontPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sleepOffsetBody, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sleepOffsetBody, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sleepOffsetBody, this.tail2Part.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sleepOffsetNeck, this.neckPart.posZ);
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sleepOffsetHead, this.headPart.posZ);

    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.neckPart != null) {
            this.world.removeEntityDangerously(this.neckPart);
            this.neckPart = null;
        }
        if (this.bodyFrontPart != null) {
            this.world.removeEntityDangerously(this.bodyFrontPart);
            this.bodyFrontPart = null;
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
    public boolean attackEntityAsMob(Entity entityIn) {
        if (!(entityIn instanceof EntityPlayer)) {
            if (this.getTargetList().contains(EntityList.getKey(entityIn).toString())) {
                this.setLightBlastCharge(this.lightBlastCharge() + 1);
            }
        }
        return super.attackEntityAsMob(entityIn);
    }

    public void attackWithLightBlast(EntityLivingBase entityIn) {
        entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())/4F);
        entityIn.setFire(30);
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

    public int lightBlastCharge() {
        return this.dataManager.get(LIGHT_BLAST_CHARGE);
    }

    public void setLightBlastCharge(int value) {
        this.dataManager.set(LIGHT_BLAST_CHARGE, value);
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::saurophaganaxMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::saurophaganaxAttack));
        data.addAnimationController(new AnimationController(this, "lightBlast", 0, this::saurophaganaxLightBlast));
    }

    private <E extends IAnimatable> PlayState saurophaganaxMovement(AnimationEvent<E> event) {
        if (!this.isSleeping() && this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.sitting", true));
            return PlayState.CONTINUE;
        }
        if (!this.isSleeping() && (event.isMoving() || (this.isSitting() && this.hasTarget()))) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.walk", true));
            return PlayState.CONTINUE;
        }
        if (this.isSleeping()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.saurophaganax.sleeping", true));
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
