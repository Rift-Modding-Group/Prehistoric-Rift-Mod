package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.*;

public class Direwolf extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/direwolf"));

    public Direwolf(World worldIn) {
        super(worldIn, RiftCreatureType.DIREWOLF);
        this.setSize(1f, 1.55f);
        this.experienceValue = 10;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.speed = 0.25D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    @Override
    public double herdFollowRange() {
        return 4D;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1.25f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.PACK_CALL, CreatureMove.SNIFF));
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.SNARL, CreatureMove.SNIFF));
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.POWER_BLOW, CreatureMove.SNIFF));
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
        moveMap.put(CreatureMove.MoveAnimType.ROAR, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(30D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.DIREWOLF_HOWL)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(20D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.DIREWOLF_SNIFF)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.GROWL, new RiftCreatureMoveAnimator(this)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(2.5D)
                .defineRecoverFromUseLength(2.5D)
                .setChargeUpToUseSound(RiftSounds.DIREWOLF_GROWL)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.BLOW, new RiftCreatureMoveAnimator(this)
                .defineStartMoveDelayLength(5D)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(17.5D)
                .defineRecoverFromUseLength(2.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BLOW_MOVE)
                .setChargeUpToUseParticles("blow", 32, this.getHeadHitbox().posX, this.getHeadHitbox().posY, this.getHeadHitbox().posZ, this.getLookVec().x, this.getLookVec().y, this.getLookVec().z)
                .finalizePoints()
        );
        return moveMap;
    }
    //move related stuff ends here

    public float attackWidth() {
        return 2.5f;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return RiftSounds.DIREWOLF_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DIREWOLF_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DIREWOLF_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.DIREWOLF_WARN;
    }
}
