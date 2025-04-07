package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.client.ui.RiftJournalScreen;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.SaurophaganaxConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Saurophaganax extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/saurophaganax"));
    private RiftCreaturePart neckPart;
    private RiftCreaturePart bodyFrontPart;
    private RiftCreaturePart tail0Part;
    private RiftCreaturePart tail1Part;
    private RiftCreaturePart tail2Part;

    public Saurophaganax(World worldIn) {
        super(worldIn, RiftCreatureType.SAUROPHAGANAX);
        this.setSize(2f, 3f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 20;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.25D;
        this.targetList = RiftUtil.creatureTargets((RiftConfigHandler.getConfig(this.creatureType)).general.targetWhitelist);

        this.headPart = new RiftCreaturePart(this, 3.5f, 0, 2f, 0.6f, 0.6f, 1.5f);
        this.bodyPart = new RiftCreaturePart(this, 0f, 0, 1.125f, 1f, 0.8f, 1f);
        this.neckPart = new RiftCreaturePart(this, 2.5f, 0, 1.7f, 0.5f, 0.65f, 1.5f);
        this.bodyFrontPart = new RiftCreaturePart(this, 1.5f, 0, 1.125f, 0.8f, 0.8f, 1f);
        this.tail0Part = new RiftCreaturePart(this, -1.5f, 0, 1.4f, 0.6f, 0.6f, 0.5f);
        this.tail1Part = new RiftCreaturePart(this, -2.5f, 0, 1.4f, 0.5f, 0.5f, 0.5f);
        this.tail2Part = new RiftCreaturePart(this, -3.5f, 0, 1.4f, 0.5f, 0.5f, 0.5f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.headPart,
            this.bodyPart,
            this.neckPart,
            this.bodyFrontPart,
            this.tail0Part,
            this.tail1Part,
            this.tail2Part
        };
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftGetTargets(this, false, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
        this.tasks.addTask(10, new RiftLookAround(this));
    }

    @Override
    public void updateParts() {
        super.updateParts();

        float sleepOffsetBody = this.isSleeping() ? -1.2f : 0;
        float sleepOffsetNeck = this.isSleeping() ? -1.7f : 0;
        float sleepOffsetHead = this.isSleeping() ? -2f : 0;

        float sitOffset = (this.isSitting() && !this.isBeingRidden() && !this.isSleeping()) ? -0.6f : 0;

        if (this.bodyPart != null) this.bodyPart.setPositionAndUpdate(this.bodyPart.posX, this.bodyPart.posY + sleepOffsetBody + sitOffset, this.bodyPart.posZ);
        if (this.bodyFrontPart != null) this.bodyFrontPart.setPositionAndUpdate(this.bodyFrontPart.posX, this.bodyFrontPart.posY + sleepOffsetBody + sitOffset, this.bodyFrontPart.posZ);
        if (this.tail0Part != null) this.tail0Part.setPositionAndUpdate(this.tail0Part.posX, this.tail0Part.posY + sleepOffsetBody + sitOffset, this.tail0Part.posZ);
        if (this.tail1Part != null) this.tail1Part.setPositionAndUpdate(this.tail1Part.posX, this.tail1Part.posY + sleepOffsetBody + sitOffset, this.tail1Part.posZ);
        if (this.tail2Part != null) this.tail2Part.setPositionAndUpdate(this.tail2Part.posX, this.tail2Part.posY + sleepOffsetBody + sitOffset, this.tail2Part.posZ);
        if (this.neckPart != null) this.neckPart.setPositionAndUpdate(this.neckPart.posX, this.neckPart.posY + sleepOffsetNeck + sitOffset, this.neckPart.posZ);
        if (this.headPart != null) this.headPart.setPositionAndUpdate(this.headPart.posX, this.headPart.posY + sleepOffsetHead + sitOffset, this.headPart.posZ);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.2f, 2f};
    }

    @Override
    public int slotCount() {
        return 27;
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Arrays.asList(CreatureMove.BITE, CreatureMove.HEADBUTT, CreatureMove.LIGHT_BLAST);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Arrays.asList(CreatureMove.BITE, CreatureMove.HEADBUTT, CreatureMove.LIGHT_BLAST);
    }

    @Override
    public Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.HEAD, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveType.ROAR, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(22.5)
                .defineRecoverFromUseLength(7.5)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 3.5f;
    }

    @Override
    public Vec3d riderPos() {
        float xOffset = (float)(this.posX + (0.5) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (0.5) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + 0.35, zOffset);
    }

    @Override
    public boolean isNocturnal() {
        return true;
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.SAUROPHAGANAX_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.SAUROPHAGANAX_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.SAUROPHAGANAX_DEATH;
    }
}
