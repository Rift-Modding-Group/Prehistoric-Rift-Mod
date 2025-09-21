package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftClimber;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Utahraptor extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/utahraptor"));

    public Utahraptor(World worldIn) {
        super(worldIn, RiftCreatureType.UTAHRAPTOR);
        this.setSize(1.25f, 1.5f);
        this.experienceValue = 10;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.speed = 0.35D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    /*
    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftPickUpFavoriteFoods(this,true));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureWarnTarget(this, 1.25f, 0.5f));
        this.tasks.addTask(5, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(6, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(8, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(9, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(10, new RiftHerdMemberFollow(this));
        this.tasks.addTask(11, new RiftWander(this, 1.0D));
        this.tasks.addTask(12, new RiftLookAround(this));
    }
     */

    public void fall(float distance, float damageMultiplier) {}

    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateRiftClimber(this, worldIn);
    }

    @Override
    public double herdFollowRange() {
        return 4D;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.POUNCE, CreatureMove.PACK_CALL));
        possibleMoves.add(1, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.POUNCE, CreatureMove.KICK));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(1.5D)
                .defineRecoverFromUseLength(1D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_SCRATCH_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.ROAR, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(30D)
                .setChargeUpToUseSound(RiftSounds.UTAHRAPTOR_CALL)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.LEAP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(6D)
                .defineRecoverFromUseLength(1D)
                .setChargeUpToUseSound(RiftSounds.UTAHRAPTOR_LEAP)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.KICK, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(2.5D)
                .setChargeUpToUseSound(RiftSounds.UTAHRAPTOR_LEAP)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2f;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1f};
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.UTAHRAPTOR_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.UTAHRAPTOR_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.UTAHRAPTOR_DEATH;
    }

    @Override
    public SoundEvent getWarnSound() {
        return RiftSounds.UTAHRAPTOR_WARN;
    }
}
