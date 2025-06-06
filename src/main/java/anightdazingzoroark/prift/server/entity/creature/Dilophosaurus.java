package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.*;

public class Dilophosaurus extends RiftCreature implements ITurretModeUser {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dilophosaurus"));
    private static final DataParameter<Boolean> TURRET_MODE = EntityDataManager.createKey(Dilophosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> TURRET_TARGET = EntityDataManager.createKey(Dilophosaurus.class, DataSerializers.BYTE);
    private RiftCreaturePart neckPart;
    private RiftCreaturePart hipPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Dilophosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.DILOPHOSAURUS);
        this.setSize(1f, 1.75f);
        this.experienceValue = 20;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.2D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.headPart = new RiftCreaturePart(this, 2f, 0, 1.7f, 1f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0.8f, 0, 0.9f, 1f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 1.5f, 0, 1.2f, 0.7f, 0.7f, 1.5f);
        this.hipPart = new RiftCreaturePart(this, 0, 0, 0.7f, 1f, 1f, 1f);
        this.tail0Part = new RiftCreaturePart(this, -0.9f, 0, 1f, 0.7f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -1.5f, 0, 0.95f, 0.6f, 0.6f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -2.1f, 0, 0.9f, 0.6f, 0.6f, 0.5f);

        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.hipPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TURRET_MODE, false);
        this.dataManager.register(TURRET_TARGET, (byte) TurretModeTargeting.HOSTILES.ordinal());
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(0, new RiftTurretModeTargeting(this, true));
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(5, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(6, new RiftFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(7, new RiftWander(this, 1.0D));
        this.tasks.addTask(8, new RiftLookAround(this));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.45f : 0;
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sitOffset, this.neckPart.posZ);
        if (this.hipPart != null) this.hipPart.setPositionAndUpdate(this.hipPart.posX, this.hipPart.posY + sitOffset, this.hipPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sitOffset, this.tail2Part.posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeTurretModeDataToNBT(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readTurretModeDataFromNBT(compound);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.POISON_SPIT, CreatureMove.POISON_TRAP));
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.POISON_SPIT, CreatureMove.POISON_TRAP));
        possibleMoves.add(1, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.POISON_SPIT, CreatureMove.VENOM_BOMB));
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.POISON_SPIT, CreatureMove.VENOM_BOMB));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setNumberOfAnims(2)
                .setChargeUpToUseSound(RiftSounds.GENERIC_SCRATCH_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.RANGED, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(25D)
                .defineRecoverFromUseLength(10D)
                .setChargeUpToUseSound(RiftSounds.DILOPHOSAURUS_SPIT)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(7.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(10D)
                .setChargeUpToUseSound(RiftSounds.DILOPHOSAURUS_SPIT)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    @Override
    public float attackWidth() {
        return 2f;
    }

    @Override
    public float rangedWidth() {
        return 12f;
    }

    @Override
    public boolean isTurretMode() {
        return this.dataManager.get(TURRET_MODE);
    }

    @Override
    public void setTurretMode(boolean value) {
        this.dataManager.set(TURRET_MODE, value);
    }

    @Override
    public TurretModeTargeting getTurretTargeting() {
        return TurretModeTargeting.values()[this.dataManager.get(TURRET_TARGET)];
    }

    @Override
    public void setTurretModeTargeting(TurretModeTargeting turretModeTargeting) {
        this.dataManager.set(TURRET_TARGET, (byte) turretModeTargeting.ordinal());
    }

    @Override
    public int slotCount() {
        return 18;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (0.05) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (0.05) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 0.75, zOffset);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "frillSetup", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (currentCreatureMove() == null || (currentCreatureMove() != null && currentCreatureMove().moveAnimType != CreatureMove.MoveAnimType.RANGED)) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".frill_setup", true));
                    return PlayState.CONTINUE;
                }
                else {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
            }
        }));
        data.addAnimationController(new AnimationController(this, "sacSetup", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (currentCreatureMove() == null || (currentCreatureMove() != null && currentCreatureMove().moveAnimType != CreatureMove.MoveAnimType.RANGED)) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".sac_setup", true));
                    return PlayState.CONTINUE;
                }
                else {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
            }
        }));
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.DILOPHOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DILOPHOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DILOPHOSAURUS_DEATH;
    }

    public SoundEvent rangedAttackSound() {
        return RiftSounds.DILOPHOSAURUS_SPIT;
    }
}
