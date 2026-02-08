package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.entity.*;
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

public class Anomalocaris extends RiftWaterCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/anomalocaris"));

    public Anomalocaris(World worldIn) {
        super(worldIn, RiftCreatureType.ANOMALOCARIS);
        this.setSize(2f, 0.75f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 10;
        this.isRideable = true;
        this.speed = 0.2D;
        this.waterSpeed = 5D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{1f, 2f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.LIFE_DRAIN, CreatureMove.CLOAK));
        possibleMoves.add(1, Arrays.asList(CreatureMove.LEECH, CreatureMove.LIFE_DRAIN, CreatureMove.CLOAK));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_SCRATCH_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.GRAB, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(9D)
                .defineRecoverFromUseLength(8D)
                .setChargeUpSound(RiftSounds.ANOMALOCARIS_CLOAKING)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 3f;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public boolean isAmphibious() {
        return false;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.ANOMALOCARIS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.ANOMALOCARIS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.ANOMALOCARIS_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.ANOMALOCARIS_WARN;
    }
}
