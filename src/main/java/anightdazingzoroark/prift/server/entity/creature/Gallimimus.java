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

public class Gallimimus extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/gallimimus"));

    public Gallimimus(World worldIn) {
        super(worldIn, RiftCreatureType.GALLIMIMUS);
        this.setSize(1.25f, 1.5f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 10;
        this.speed = 0.5D;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.targetList = RiftUtil.creatureTargets(RiftConfigHandler.getConfig(this.creatureType).general.targetWhitelist, RiftConfigHandler.getConfig(this.creatureType).general.targetBlacklist, false);
    }

    /*
    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets(this, true, true));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftProtectOwner(this));
        this.targetTasks.addTask(3, new RiftAttackForOwner(this));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftFleeFromEntities(this, 1f));
        this.tasks.addTask(4, new RiftCreatureUseMoveMounted(this));
        this.tasks.addTask(5, new RiftCreatureWarnTarget(this, 1.25f, 0.5f));
        this.tasks.addTask(6, new RiftCreatureUseMoveUnmounted(this));
        this.tasks.addTask(7, new RiftFollowOwner(this, 1.0D, 8.0F, 4.0F));
        this.tasks.addTask(9, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(10, new RiftHerdDistanceFromOtherMembers(this, 1D));
        this.tasks.addTask(11, new RiftHerdMemberFollow(this));
        this.tasks.addTask(12, new RiftWander(this, 0.5D));
        this.tasks.addTask(13, new RiftLookAround(this));
    }
     */

    @Override
    public double herdFollowRange() {
        return 4D;
    }

    @Override
    public int slotCount() {
        return 9;
    }

    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Arrays.asList(CreatureMove.HEADBUTT, CreatureMove.LEAP, CreatureMove.KICK));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.HEAD, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_HEAD_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.LEAP, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(6D)
                .defineRecoverFromUseLength(1D)
                .setChargeUpToUseSound(RiftSounds.GALLIMIMUS_LEAP)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.KICK, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(12.5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_KICK_MOVE)
                .finalizePoints());
        return moveMap;
    }

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 0.9f};
    }

    @Override
    public float attackWidth() {
        return 2f;
    }

    @Override
    public boolean fleesFromDanger() {
        return true;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.GALLIMIMUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.GALLIMIMUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.GALLIMIMUS_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.GALLIMIMUS_WARN;
    }
}
