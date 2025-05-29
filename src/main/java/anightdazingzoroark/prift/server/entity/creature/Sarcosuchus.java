package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Sarcosuchus extends RiftWaterCreature {
    private static final DataParameter<Boolean> SPINNING = EntityDataManager.<Boolean>createKey(Sarcosuchus.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/sarcosuchus"));
    private RiftCreaturePart snoutPart;
    private RiftCreaturePart frontBodyPart;
    private RiftCreaturePart tail0;
    private RiftCreaturePart tail1;
    private RiftCreaturePart tail2;
    private RiftCreaturePart tail3;
    private RiftCreaturePart tail4;

    public Sarcosuchus(World worldIn) {
        super(worldIn, RiftCreatureType.SARCOSUCHUS);
        this.setSize(1.25f, 0.5f);
        this.experienceValue = 10;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 10D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.125f, 0.75f, 0.675f, 1f);
        this.headPart = new RiftCreaturePart(this, 1.625f, 0, 0.125f, 0.625f, 0.625f, 1.5f);
        this.snoutPart = new RiftCreaturePart(this, 2.5f, 0, 0.2f, 0.55f, 0.5f, 1.5f);
        this.frontBodyPart = new RiftCreaturePart(this, 0.75f, 0, 0.125f, 0.75f, 0.65f, 1f);
        this.tail0 = new RiftCreaturePart(this, -0.75f, 0, 0.125f, 0.75f, 0.65f, 0.5f);
        this.tail1 = new RiftCreaturePart(this, -1.625f, 0, 0.2f, 0.575f, 0.525f, 0.5f);
        this.tail2 = new RiftCreaturePart(this, -2.375f, 0, 0.225f, 0.525f, 0.475f, 0.5f);
        this.tail3 = new RiftCreaturePart(this, -3.125f, 0, 0.225f, 0.525f, 0.475f, 0.5f);
        this.tail4 = new RiftCreaturePart(this, -3.75f, 0, 0.25f, 0.475f, 0.425f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.bodyPart,
            this.headPart,
            this.snoutPart,
            this.frontBodyPart,
            this.tail0,
            this.tail1,
            this.tail2,
            this.tail3,
            this.tail4
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
        this.dataManager.register(SPINNING, false);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(4, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(6, new RiftGoToWater(this, 16, 1.0D));
        this.tasks.addTask(7, new RiftWanderWater(this, 1.0D));
        this.tasks.addTask(8, new RiftWander(this, 1.0D));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sitOffset = (this.isSitting() && !this.isBeingRidden()) ? -0.175f : 0;
        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sitOffset, this.bodyPart.posZ);
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sitOffset, this.headPart.posZ);
        if (this.snoutPart != null) this.snoutPart.setPositionAndUpdate(this.snoutPart.posX, this.snoutPart.posY + sitOffset, this.snoutPart.posZ);
        if (this.frontBodyPart != null) this.frontBodyPart.setPositionAndUpdate(this.frontBodyPart.posX, this.frontBodyPart.posY + sitOffset, this.frontBodyPart.posZ);
        if (this.tail0 != null) this.tail0.setPositionAndUpdate(this.tail0.posX, this.tail0.posY + sitOffset, this.tail0.posZ);
        if (this.tail1 != null) this.tail1.setPositionAndUpdate(this.tail1.posX, this.tail1.posY + sitOffset, this.tail1.posZ);
        if (this.tail2 != null) this.tail2.setPositionAndUpdate(this.tail2.posX, this.tail2.posY + sitOffset, this.tail2.posZ);
        if (this.tail3 != null) this.tail3.setPositionAndUpdate(this.tail3.posX, this.tail3.posY + sitOffset, this.tail3.posZ);
        if (this.tail4 != null) this.tail4.setPositionAndUpdate(this.tail4.posX, this.tail4.posY + sitOffset, this.tail4.posZ);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1.5f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.LUNGE, CreatureMove.DEATH_ROLL));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(4D)
                .defineChargeUpToUseLength(1D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.CHARGE, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.SARCOSUCHUS_CHARGE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.SPIN, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineUseDurationLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .setUseDurationSound(RiftSounds.SARCOSUCHUS_DEATH_ROLL)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.GRAB, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 3f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX - (0.3) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ - (0.3) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY - 1.375, zOffset);
    }

    @Override
    public boolean canBeKnockedBack() {
        return true;
    }

    @Override
    public boolean isAmphibious() {
        return true;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.SARCOSUCHUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.SARCOSUCHUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.SARCOSUCHUS_DEATH;
    }
}
