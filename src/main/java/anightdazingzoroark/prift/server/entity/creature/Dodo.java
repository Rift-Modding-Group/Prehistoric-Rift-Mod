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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Dodo extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dodo"));

    public Dodo(World worldIn) {
        super(worldIn, RiftCreatureType.DODO);
        this.setSize(0.75f, 0.75f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 3;
        this.speed = 0.25D;
        this.isRideable = false;
    }

    /*
    protected void initEntityAI() {
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftHerdDistanceFromOtherMembers(this, 1.5D));
        this.tasks.addTask(4, new RiftHerdMemberFollow(this));
        this.tasks.addTask(5, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(6, new RiftWander(this, 1.0D));
        this.tasks.addTask(7, new RiftLookAround(this));
    }
     */

    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
    }

    public void fall(float distance, float damageMultiplier) {}

    @Override
    public double herdFollowRange() {
        return 3D;
    }

    public float attackWidth() {
        return 0;
    }

    @Override
    public boolean canNaturalRegen() {
        return false;
    }

    //move related stuff starts here
    @Override
    public WeightedList<List<CreatureMove>> possibleStartingMoves() {
        WeightedList<List<CreatureMove>> possibleMoves = new WeightedList<>();
        possibleMoves.add(1, Collections.singletonList(CreatureMove.BOUNCE));
        return possibleMoves;
    }

    @Override
    public Map<CreatureMove.MoveAnimType, RiftCreatureMoveAnimator> animatorsForMoveType() {
        return Collections.emptyMap();
    }
    //move related stuff ends here

    @Override
    public float[] ageScaleParams() {
        return new float[]{0.5f, 0.75f};
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::dodoMovement));
    }

    private <E extends IAnimatable> PlayState dodoMovement(AnimationEvent<E> event) {
        if (event.isMoving() && this.onGround) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dodo.walk", true));
            return PlayState.CONTINUE;
        }
        else if (!this.onGround) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dodo.fall", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    protected SoundEvent getAmbientSound() {
        return RiftSounds.DODO_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return RiftSounds.DODO_HURT;
    }

    protected SoundEvent getDeathSound() {
        return RiftSounds.DODO_DEATH;
    }
}
