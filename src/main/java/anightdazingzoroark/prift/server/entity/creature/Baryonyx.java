package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Baryonyx extends RiftWaterCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/baryonyx"));

    public Baryonyx(World worldIn) {
        super(worldIn, RiftCreatureType.BARYONYX);
        this.setSize(1.25f, 2.75f);
        this.experienceValue = 20;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.25D;
        this.waterSpeed = 5D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        //remove poison
        if (this.getActivePotionEffect(MobEffects.POISON) != null) this.removePotionEffect(MobEffects.POISON);
    }

    /*
    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(3, new RiftCreatureWarnTarget(this, 1.25f, 0.5f));
        this.tasks.addTask(4, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(5, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(6, new RiftWaterCreatureFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(7, new RiftGoToWater(this, 16, 1.0D));
        this.tasks.addTask(8, new RiftWanderWater(this, 1.0D));
        this.tasks.addTask(9, new RiftWander(this, 1.0D));
    }
     */

    @Override
    public boolean isAmphibious() {
        return true;
    }

    @Override
    public int slotCount() {
        return 27;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.5f, 1.5f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(2, Arrays.asList(CreatureMove.BITE, CreatureMove.POISON_CLAW, CreatureMove.TAIL_WHIP));
        possibleMoves.add(2, Arrays.asList(CreatureMove.BITE, CreatureMove.POISON_CLAW, CreatureMove.KICK));
        possibleMoves.add(1, Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.POISON_CLAW, CreatureMove.KICK));
        possibleMoves.add(1, Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.POISON_CLAW, CreatureMove.TAIL_WHIP));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setNumberOfAnims(2)
                .setChargeUpToUseSound(RiftSounds.GENERIC_SCRATCH_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.KICK, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(10D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(7.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_KICK_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.HEAD, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_HEAD_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.TAIL, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(2.5D)
                .defineUseDurationLength(7.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_TAIL_MOVE)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 6f;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.BARYONYX_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.BARYONYX_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.BARYONYX_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.BARYONYX_WARN;
    }
}
