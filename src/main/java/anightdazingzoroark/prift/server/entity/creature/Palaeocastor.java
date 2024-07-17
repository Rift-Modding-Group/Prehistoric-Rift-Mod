package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.PalaeocastorConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.nbt.NBTTagCompound;
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

public class Palaeocastor extends RiftCreature implements IImpregnable {
    public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(Megaloceros.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> PREGNANCY_TIMER = EntityDataManager.createKey(Megaloceros.class, DataSerializers.VARINT);

    public Palaeocastor(World worldIn) {
        super(worldIn, RiftCreatureType.PALAEOCASTOR);
        this.setSize(0.75f, 0.75f);
        this.minCreatureHealth = PalaeocastorConfig.getMinHealth();
        this.maxCreatureHealth = PalaeocastorConfig.getMaxHealth();
        this.favoriteFood = PalaeocastorConfig.palaeocastorFavoriteFood;
        this.tamingFood = PalaeocastorConfig.palaeocastorTamingFood;
        this.speed = 0.25D;
        this.attackWidth = 2f;
        this.attackDamage = PalaeocastorConfig.damage;
        this.experienceValue = 3;
        this.healthLevelMultiplier = PalaeocastorConfig.healthMultiplier;
        this.damageLevelMultiplier = PalaeocastorConfig.damageMultiplier;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PREGNANT, false);
        this.dataManager.register(PREGNANCY_TIMER, 0);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, false));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(4, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //manage birthin related stuff
        if (!this.world.isRemote) this.createBaby(this);
    }

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.125f, scale * 0.625f, scale * 0.625f, 1f);
            this.headPart = new RiftCreaturePart(this, 0.75f, 0, 0.25f, scale * 0.5f, scale * 0.5f, 1.5f);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("PregnancyTime", this.getPregnancyTimer());
        compound.setBoolean("IsPregnancy", this.isPregnant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setPregnant(compound.getBoolean("IsPregnancy"), compound.getInteger("PregnancyTime"));
    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.25f, 1f);
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {

    }

    public void setPregnant(boolean value, int timer) {
        this.dataManager.set(PREGNANT, value);
        this.dataManager.set(PREGNANCY_TIMER, timer);
    }

    public boolean isPregnant() {
        return this.dataManager.get(PREGNANT);
    }

    public void setPregnancyTimer(int value) {
        this.dataManager.set(PREGNANCY_TIMER, value);
    }

    public int getPregnancyTimer() {
        return this.dataManager.get(PREGNANCY_TIMER);
    }

    @Override
    public int slotCount() {
        return 9;
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
        data.addAnimationController(new AnimationController(this, "movement", 0, this::palaeocastorMovement));
    }

    private <E extends IAnimatable> PlayState palaeocastorMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.palaeocastor.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget()))) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.palaeocastor.walk", true));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }
}
