package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.world.World;

import java.util.*;

public class Tenontosaurus extends RiftCreature {
    public Tenontosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.TENONTOSAURUS);
        this.setSize(2f, 2f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 20;
        this.speed = 0.2D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));

        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(4, new RiftCreatureWarnTarget(this, 2.25f, 0.5f));
        this.tasks.addTask(5, new RiftBreakBlockWhilePursuingTarget(this));
        this.tasks.addTask(6, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(8, new RiftFollowOwner(this, 1.0D, 8.0F, 6.0F));
        this.tasks.addTask(11, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(12, new RiftWander(this, 1.0D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }

    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.HYPNOSIS_POWDER, CreatureMove.POISON_POWDER));
        possibleMoves.add(1, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.HYPNOSIS_POWDER, CreatureMove.ITCHING_POWDER));
        possibleMoves.add(1, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.HYPNOSIS_POWDER, CreatureMove.PARALYZING_POWDER));
        possibleMoves.add(1, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.HYPNOSIS_POWDER, CreatureMove.RAGE_POWDER));
        possibleMoves.add(1, Arrays.asList(CreatureMove.TAIL_SLAP, CreatureMove.HYPNOSIS_POWDER, CreatureMove.SLEEP_POWDER));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> toReturn = new HashMap<>();
        toReturn.put(CreatureMove.MoveAnimType.TAIL, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(7.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(10D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_TAIL_MOVE)
                .finalizePoints());
        toReturn.put(CreatureMove.MoveAnimType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(20D)
                //.defineUseDurationLength(15D)
                .defineRecoverFromUseLength(5D)
                .finalizePoints());
        return toReturn;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1.75f};
    }

    @Override
    public float attackWidth() {
        return 3.5f;
    }
}
