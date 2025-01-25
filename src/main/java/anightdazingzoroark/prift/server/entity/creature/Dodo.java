package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.DodoConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Dodo extends RiftCreature implements IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dodo"));
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Dodo(World worldIn) {
        super(worldIn, RiftCreatureType.DODO);
        this.setSize(0.75f, 0.75f);
        this.favoriteFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteFood;
        this.tamingFood = RiftConfigHandler.getConfig(this.creatureType).general.favoriteMeals;
        this.experienceValue = 3;
        this.speed = 0.25D;
        this.isRideable = false;

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.25f, 1f,  0.55f, 1f);
        this.headPart = new RiftCreaturePart(this, 0.35f, 0, 0.5f, 0.5f,  0.5f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
                this.headPart,
                this.bodyPart,
        };
    }

    protected void initEntityAI() {
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftLandDwellerSwim(this));
        this.tasks.addTask(3, new RiftHerdDistanceFromOtherMembers(this, 1.5D));
        this.tasks.addTask(4, new RiftHerdMemberFollow(this));
        this.tasks.addTask(5, new RiftGoToLandFromWater(this, 16, 1.0D));
        this.tasks.addTask(6, new RiftWander(this, 1.0D));
        this.tasks.addTask(7, new RiftLookAround(this));
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
    }

    public void fall(float distance, float damageMultiplier) {}

    @Override
    public boolean canDoHerding() {
        return true;
    }

    public RiftCreature getHerder() {
        return this;
    }

    public RiftCreature getHerdLeader() {
        return this.herdLeader;
    }

    public void setHerdLeader(RiftCreature creature) {
        this.herdLeader = creature;
    }

    public int getHerdSize() {
        return this.herdSize;
    }

    public void setHerdSize(int value) {
        this.herdSize = value;
    }

    public double followRange() {
        return 3D;
    }

    public float attackWidth() {
        return 0;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, Entity target, BlockPos pos) {}

    @Override
    public boolean canNaturalRegen() {
        return false;
    }

    @Override
    public boolean hasLeftClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasRightClickChargeBar() {
        return false;
    }

    @Override
    public boolean hasSpacebarChargeBar() {
        return false;
    }

    //move related stuff starts here
    @Override
    public List<CreatureMove> learnableMoves() {
        return Collections.singletonList(CreatureMove.BOUNCE);
    }

    @Override
    public List<CreatureMove> initialMoves() {
        return Collections.singletonList(CreatureMove.BOUNCE);
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
