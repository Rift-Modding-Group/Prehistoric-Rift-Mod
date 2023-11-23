package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.config.TriceratopsConfig;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import anightdazingzoroark.rift.server.entity.ai.*;
import anightdazingzoroark.rift.server.entity.creatureinterface.IChargingMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Triceratops extends RiftCreature implements IChargingMob {
    public Triceratops(World worldIn) {
        super(worldIn, RiftCreatureType.TRICERATOPS);
        this.setSize(2f, 2f);
        this.favoriteFood = TriceratopsConfig.triceratopsFavoriteFood;
        this.tamingFood = TriceratopsConfig.triceratopsTamingFood;
        this.experienceValue = 20;
        this.speed = 0.15D;
        this.attackWidth = 4.875f;
        this.chargeWidth = 12f;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(25.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 0.72F, 0.48F));
        this.tasks.addTask(2, new RiftChargeAttack(this, 1.75f, 0.24f, 4f, 5f));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 0.72F, 0.48F));
        this.tasks.addTask(4, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(5, new RiftHerdMemberFollow(this, 10D, 2D, 1D));
        this.tasks.addTask(6, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (-0.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY, zOffset);
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
    public boolean canDoHerding() {
        return !this.isTamed();
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
        data.addAnimationController(new AnimationController(this, "movement", 0, this::triceratopsMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::triceratopsAttack));
    }

    private <E extends IAnimatable> PlayState triceratopsMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving() || (this.isSitting() && this.hasTarget())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState triceratopsAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.triceratops.attack", false));
        }
        else event.getController().clearAnimationCache();
        return PlayState.CONTINUE;
    }
}
