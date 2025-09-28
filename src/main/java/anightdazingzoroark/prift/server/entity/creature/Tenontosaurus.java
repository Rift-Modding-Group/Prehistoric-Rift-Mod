package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Tenontosaurus extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/tenontosaurus"));

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
        return RiftSounds.TENONTOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.TENONTOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.TENONTOSAURUS_DEATH;
    }
}
