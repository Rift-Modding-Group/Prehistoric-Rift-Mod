package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.DimetrodonConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.TameStatusType;
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
    private RiftCreaturePart neckPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

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
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.headPart = new RiftCreaturePart(this, 1.25f, 0, 0.45f, 0.5f * scale, 0.425f * scale, 1.5f);
            this.bodyPart = new RiftMainBodyPart(this, 0, 0, 0.3f, scale, 0.5f * scale, 1f);
            this.neckPart = new RiftCreaturePart(this, 0.75f, 0, 0.45f, 0.375f * scale, 0.375f * scale, 1.5f);
            this.tail0Part = new RiftCreaturePart(this, -0.8f, 0, 0.4f, 0.375f * scale, 0.375f * scale, 0.5f);
            this.tail1Part = new RiftCreaturePart(this, -1.2f, 0, 0.35f, 0.375f * scale, 0.375f * scale, 0.5f);
            this.tail2Part = new RiftCreaturePart(this, -1.6f, 0, 0.3f, 0.375f * scale, 0.375f * scale, 0.5f);
        }
    }

    @Override
    public void updateParts() {
        super.updateParts();
        if (this.neckPart != null) this.neckPart.onUpdate();
        if (this.tail0Part != null) this.tail0Part.onUpdate();
        if (this.tail1Part != null) this.tail1Part.onUpdate();
        if (this.tail2Part != null) this.tail2Part.onUpdate();

        float sitOffset = (this.getTameStatus().equals(TameStatusType.SIT) && !this.isBeingRidden()) ? -0.25f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
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
    public int slotCount() {
        return 9;
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
