package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.AnkylosaurusConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Ankylosaurus extends RiftCreature implements IHerder {
    private static final DataParameter<Boolean> HIDING = EntityDataManager.<Boolean>createKey(Ankylosaurus.class, DataSerializers.BOOLEAN);
    private RiftCreaturePart leftFrontLegPart;
    private RiftCreaturePart rightFrontLegPart;
    private RiftCreaturePart leftBackLegPart;
    private RiftCreaturePart rightBackLegPart;
    private RiftCreaturePart tail0;
    private RiftCreaturePart tail1;
    private RiftCreaturePart tail2;
    private RiftCreaturePart tail3;
    private RiftCreaturePart tailClub;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Ankylosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.ANKYLOSAURUS);
        this.setSize(2f, 2.5f);
        this.favoriteFood = ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.15D;
        this.isRideable = true;
        this.saddleItem = ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, 2f, 0, 0.7f, 0.5f, 0.5f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.5f, 1.25f, 0.9f, 0.5f);
        this.leftFrontLegPart = new RiftCreaturePart(this, 1.5f, 30f, 0, 0.35f, 0.7f, 0.5f);
        this.rightFrontLegPart = new RiftCreaturePart(this, 1.5f, -30f, 0, 0.35f, 0.7f, 0.5f);
        this.leftBackLegPart = new RiftCreaturePart(this, 1.5f, 150f, 0, 0.35f, 0.7f, 0.5f);
        this.rightBackLegPart = new RiftCreaturePart(this, 1.5f, -150f, 0, 0.35f, 0.7f, 0.5f);
        this.tail0 = new RiftCreaturePart(this, -1.75f, 0, 0.7f, 0.5f, 0.5f, 0.5f);
        this.tail1 = new RiftCreaturePart(this, -2.625f, 0, 0.75f, 0.4f, 0.4f, 0.5f);
        this.tail2 = new RiftCreaturePart(this, -3.375f, 0, 0.8f, 0.35f, 0.35f, 0.5f);
        this.tail3 = new RiftCreaturePart(this, -4f, 0, 0.75f, 0.3f, 0.3f, 0.5f);
        this.tailClub = new RiftCreaturePart(this, -4.375f, 0, 0.7f, 0.35f, 0.35f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
                this.headPart,
                this.bodyPart,
                this.leftFrontLegPart,
                this.rightFrontLegPart,
                this.leftBackLegPart,
                this.rightBackLegPart,
                this.tail0,
                this.tail1,
                this.tail2,
                this.tail3,
                this.tailClub
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HIDING, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftControlledAttack(this, 0.72F, 0.48F));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 1.2F, 0.6F));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(8, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(9, new RiftHerdMemberFollow(this));
        this.tasks.addTask(10, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(11, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public void updateParts() {
        super.updateParts();
    }

    @Override
    public void removeParts() {
        super.removeParts();
        if (this.leftFrontLegPart != null) {
            this.world.removeEntityDangerously(this.leftFrontLegPart);
            this.leftFrontLegPart = null;
        }
        if (this.rightFrontLegPart != null) {
            this.world.removeEntityDangerously(this.rightFrontLegPart);
            this.rightFrontLegPart = null;
        }
        if (this.leftBackLegPart != null) {
            this.world.removeEntityDangerously(this.leftBackLegPart);
            this.leftBackLegPart = null;
        }
        if (this.rightBackLegPart != null) {
            this.world.removeEntityDangerously(this.rightBackLegPart);
            this.rightBackLegPart = null;
        }
        if (this.tail0 != null) {
            this.world.removeEntityDangerously(this.tail0);
            this.tail0 = null;
        }
        if (this.tail1 != null) {
            this.world.removeEntityDangerously(this.tail1);
            this.tail1 = null;
        }
        if (this.tail2 != null) {
            this.world.removeEntityDangerously(this.tail2);
            this.tail2 = null;
        }
        if (this.tail3 != null) {
            this.world.removeEntityDangerously(this.tail3);
            this.tail3 = null;
        }
        if (this.tailClub != null) {
            this.world.removeEntityDangerously(this.tailClub);
            this.tailClub = null;
        }
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.5f, 2.125f);
    }

    @Override
    public boolean canDoHerding() {
        return !this.isTamed();
    }

    public RiftCreature getHerder() {
        return this;
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
    }

    public int getHerdSize() {
        return this.herdSize;
    }

    public void setHerdSize(int value) {
        this.herdSize = value;
    }

    public double followRange() {
        return 6D;
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
    public float attackWidth() {
        return 6f;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target, BlockPos pos) {

    }

    public void setHiding(boolean value) {
        this.dataManager.set(HIDING, value);
    }

    public boolean isHiding() {
        return this.dataManager.get(HIDING);
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
        data.addAnimationController(new AnimationController(this, "movement", 0, this::ankylosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::ankylosaurusAttack));
    }

    private <E extends IAnimatable> PlayState ankylosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget()))) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState ankylosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.attack", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }
}
