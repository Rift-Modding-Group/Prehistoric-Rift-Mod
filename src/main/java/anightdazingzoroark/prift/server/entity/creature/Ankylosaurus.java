package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.AnkylosaurusConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.List;

public class Ankylosaurus extends RiftCreature implements IHerder, IHarvestWhenWandering {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/ankylosaurus"));
    private static final DataParameter<Boolean> START_HIDING = EntityDataManager.<Boolean>createKey(Ankylosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> STOP_HIDING = EntityDataManager.<Boolean>createKey(Ankylosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HIDING = EntityDataManager.<Boolean>createKey(Ankylosaurus.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> CAN_HARVEST = EntityDataManager.createKey(Ankylosaurus.class, DataSerializers.BOOLEAN);
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
    protected boolean forceShellFlag;

    public Ankylosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.ANKYLOSAURUS);
        this.setSize(2f, 2.5f);
        this.favoriteFood = ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteFood;
        this.tamingFood = ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.15D;
        this.isRideable = true;
        this.saddleItem = ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.saddleItem;

        this.headPart = new RiftCreaturePart(this, "head",2f, 0, 0.7f, 0.5f, 0.5f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, "body", 0, 0, 0.5f, 1.25f, 0.9f, 0.25f)
                .setImmuneToMelee()
                .setImmuneToProjectile();
        this.leftFrontLegPart = new RiftCreaturePart(this, 1.5f, 30f, 0, 0.35f, 0.7f, 0.5f);
        this.rightFrontLegPart = new RiftCreaturePart(this, 1.5f, -30f, 0, 0.35f, 0.7f, 0.5f);
        this.leftBackLegPart = new RiftCreaturePart(this, 1.5f, 150f, 0, 0.35f, 0.7f, 0.5f);
        this.rightBackLegPart = new RiftCreaturePart(this, 1.5f, -150f, 0, 0.35f, 0.7f, 0.5f);
        this.tail0 = new RiftCreaturePart(this, "tail", -1.75f, 0, 0.7f, 0.5f, 0.5f, 0.5f);
        this.tail1 = new RiftCreaturePart(this,"tail",  -2.625f, 0, 0.75f, 0.4f, 0.4f, 0.5f);
        this.tail2 = new RiftCreaturePart(this,"tail",  -3.375f, 0, 0.8f, 0.35f, 0.35f, 0.5f);
        this.tail3 = new RiftCreaturePart(this,"tail",  -4f, 0, 0.75f, 0.3f, 0.3f, 0.5f);
        this.tailClub = new RiftCreaturePart(this,"tail",  -4.375f, 0, 0.7f, 0.35f, 0.35f, 0.5f);
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
        this.dataManager.register(START_HIDING, false);
        this.dataManager.register(STOP_HIDING, false);
        this.dataManager.register(HIDING, false);
        this.dataManager.register(CAN_HARVEST, false);
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
        this.tasks.addTask(0, new RiftAnkylosaurusHideInShell(this));
        //this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftControlledAttack(this, 1.2F, 0.6F));
        this.tasks.addTask(5, new RiftAttack(this, 1.0D, 1.2F, 0.6F));
        this.tasks.addTask(6, new RiftHarvestOnWander(this, 1.2f, 0.6f));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(8, new RiftHerdDistanceFromOtherMembers(this, 3D));
        this.tasks.addTask(9, new RiftHerdMemberFollow(this));
        //this.tasks.addTask(10, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(11, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    public void updateParts() {
        super.updateParts();

        //disable parts when hiding
        for (RiftCreaturePart part : this.hitboxArray) {
            if (this.isSitting() && !this.isHiding()) {
                part.setDisabled(!part.partName.equals("body") && !part.partName.equals("head"));
            }
            else if (this.isHiding()) {
                part.setDisabled(!part.partName.equals("body"));
            }
        }

        //change positions when sitting or hiding
        float sitOffset = ((this.isSitting() && !this.isBeingRidden()) || this.isHiding()) ? -0.75f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
    }

    public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        RiftCreaturePart riftPart = (RiftCreaturePart) part;
        if (riftPart.partName.equals("body") && this.isHiding()) {
            Entity attackedEntity = source.getImmediateSource();
            if (attackedEntity != null && !source.isExplosion() && !source.isProjectile()) {
                attackedEntity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
            }
        }
        return super.attackEntityFromPart(part, source, damage);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getImmediateSource() instanceof EntityLivingBase && !(source.getImmediateSource() instanceof EntityPlayer)) {
            //make it so that anything trying to attack the mobs main hitbox ends up attacking the nearest hitbox instead
            Entity attacker = source.getImmediateSource();
            RiftCreaturePart closestPart = null;
            float closestDist = RiftUtil.funnyNumber;
            for (RiftCreaturePart testPart : this.hitboxArray) {
                if (attacker.getDistance(testPart) <= closestDist && !testPart.isDisabled()) {
                    closestPart = testPart;
                    closestDist = attacker.getDistance(testPart);
                }
            }
            if (closestPart != null) {
                if (closestPart.partName.equals("body") && this.isHiding() && !source.isExplosion() && !source.isProjectile()) {
                    attacker.attackEntityFrom(DamageSource.causeThornsDamage(this), 2f);
                }
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.resetSpeed();
        this.writeHarvestWanderDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readHarvestWanderDataFromNBT(compound);
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
        float offset = this.isStartHiding() || this.isHiding() ? -0.875f : -0.25f;
        float xOffset = (float)(this.posX + (-0.125) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (-0.125) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + offset, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {
        if (control == 0) {
            if (this.getEnergy() > 0) {
                if (!this.isActing() && !this.isStartHiding() && !this.isHiding() && !this.isStopHiding()) {
                    this.forcedAttackTarget = target;
                    this.forcedBreakPos = pos;
                    this.setAttacking(true);
                }
            }
            else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
        }
        if (control == 1) {
            boolean isHidingTest = !this.isHiding() || this.getHealth()/this.getMaxHealth() >= 0.5;
            if (!this.isActing() && !this.isStartHiding() && !this.isStopHiding() && isHidingTest) this.forceShellFlag = true;
        }
    }


    @Override
    public List<String> blocksToHarvest() {
        return ((AnkylosaurusConfig) RiftConfigHandler.getConfig(this.creatureType)).general.harvestableBlocks;
    }

    public int harvestRange() {
        return 6;
    }

    @Override
    public AxisAlignedBB breakRange() {
        return new AxisAlignedBB(-1, 0, -1, 1, 2, 1);
    }

    public void setHarvesting(boolean value) {
        this.setAttacking(value);
    }

    public boolean isHarvesting() {
        return this.isAttacking();
    }

    public void setCanHarvest(boolean value) {
        this.dataManager.set(CAN_HARVEST, value);
    }

    public boolean canHarvest() {
        return this.dataManager.get(CAN_HARVEST);
    }

    public void setStartHiding(boolean value) {
        this.dataManager.set(START_HIDING, value);
    }

    public boolean isStartHiding() {
        return this.dataManager.get(START_HIDING);
    }

    public void setStopHiding(boolean value) {
        this.dataManager.set(STOP_HIDING, value);
    }

    public boolean isStopHiding() {
        return this.dataManager.get(STOP_HIDING);
    }

    public void setHiding(boolean value) {
        this.dataManager.set(HIDING, value);
    }

    public boolean isHiding() {
        return this.dataManager.get(HIDING);
    }

    public void setForceShellFlag(boolean value) {
        this.forceShellFlag = value;
    }

    public boolean getForceShellFlag() {
        return this.forceShellFlag;
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
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "movement", 0, this::ankylosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::ankylosaurusAttack));
        data.addAnimationController(new AnimationController(this, "shellMode", 0, this::ankylosaurusShell));
    }

    private <E extends IAnimatable> PlayState ankylosaurusMovement(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget() && !this.isStartHiding() && !this.isHiding() && !this.isStopHiding()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.sitting", true));
                return PlayState.CONTINUE;
            }
            if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.walk", true));
                return PlayState.CONTINUE;
            }
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

    private <E extends IAnimatable> PlayState ankylosaurusShell(AnimationEvent<E> event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof RiftJournalScreen)) {
            if (this.isStartHiding()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.enter_shell", true));
                return PlayState.CONTINUE;
            }
            else if (this.isHiding()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.shell_mode", true));
                return PlayState.CONTINUE;
            }
            else if (this.isStopHiding()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ankylosaurus.exit_shell", true));
                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.ANKYLOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.ANKYLOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.ANKYLOSAURUS_DEATH;
    }
}
