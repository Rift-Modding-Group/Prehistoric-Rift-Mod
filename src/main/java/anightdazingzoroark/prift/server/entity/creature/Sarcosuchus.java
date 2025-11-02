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

    public Sarcosuchus(World worldIn) {
        super(worldIn, RiftCreatureType.SARCOSUCHUS);
        this.setSize(1.25f, 0.9f);
        this.experienceValue = 10;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 10D;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
        this.dataManager.register(SPINNING, false);
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

    public SoundEvent getWarnSound() {
        return RiftSounds.SARCOSUCHUS_WARN;
    }
}
