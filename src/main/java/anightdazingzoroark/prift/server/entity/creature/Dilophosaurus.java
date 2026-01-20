package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.WeightedList;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.*;

public class Dilophosaurus extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dilophosaurus"));

    public Dilophosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.DILOPHOSAURUS);
        this.setSize(1f, 1.75f);
        this.experienceValue = 20;
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.isRideable = true;
        this.saddleItem = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        this.speed = 0.2D;
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

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.3f, 1f};
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(3, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.POISON_SPIT, CreatureMove.POISON_TRAP));
        possibleMoves.add(3, Arrays.asList(CreatureMove.BITE, CreatureMove.POISON_SPIT, CreatureMove.POISON_TRAP));
        possibleMoves.add(1, Arrays.asList(CreatureMove.SCRATCH, CreatureMove.POISON_SPIT, CreatureMove.VENOM_BOMB));
        possibleMoves.add(1, Arrays.asList(CreatureMove.BITE, CreatureMove.POISON_SPIT, CreatureMove.VENOM_BOMB));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> moveMap = new HashMap<>();
        moveMap.put(CreatureMove.MoveAnimType.CLAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setNumberOfAnims(2)
                .setChargeUpToUseSound(RiftSounds.GENERIC_SCRATCH_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.JAW, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(2.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(5D)
                .setChargeUpToUseSound(RiftSounds.GENERIC_BITE_MOVE)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.RANGED, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(5D)
                .defineChargeUpToUseLength(5D)
                .defineUseDurationLength(25D)
                .defineRecoverFromUseLength(10D)
                .setChargeUpToUseSound(RiftSounds.DILOPHOSAURUS_SPIT)
                .finalizePoints());
        moveMap.put(CreatureMove.MoveAnimType.STATUS, new RiftCreatureMoveAnimator(this)
                .defineChargeUpLength(7.5D)
                .defineChargeUpToUseLength(2.5D)
                .defineRecoverFromUseLength(10D)
                .setChargeUpToUseSound(RiftSounds.DILOPHOSAURUS_SPIT)
                .finalizePoints());
        return moveMap;
    }
    //move related stuff ends here

    @Override
    public float attackWidth() {
        return 2f;
    }

    @Override
    public float rangedWidth() {
        return 12f;
    }

    @Override
    public int slotCount() {
        return 18;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(new AnimationController(this, "frillSetup", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if ((currentCreatureMove() == null || (currentCreatureMove() != null && currentCreatureMove().moveAnimType != CreatureMove.MoveAnimType.RANGED)) && !isWarning()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".frill_setup", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
                else {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
            }
        }));
        data.addAnimationController(new AnimationController(this, "sacSetup", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if ((currentCreatureMove() == null || (currentCreatureMove() != null && currentCreatureMove().moveAnimType != CreatureMove.MoveAnimType.RANGED)) && !isWarning()) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation."+creatureType.toString().toLowerCase()+".sac_setup", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
                else {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
            }
        }));
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.DILOPHOSAURUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DILOPHOSAURUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DILOPHOSAURUS_DEATH;
    }

    public SoundEvent getWarnSound() {
        return RiftSounds.DILOPHOSAURUS_WARN;
    }
}
