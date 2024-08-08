package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSounds;
import anightdazingzoroark.prift.config.DodoConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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

public class Dodo extends RiftCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/dodo"));

    public Dodo(World worldIn) {
        super(worldIn, RiftCreatureType.DODO);
        this.setSize(0.75f, 0.75f);
        this.breedingFood = ((DodoConfig)RiftConfigHandler.getConfig(this.creatureType)).general.breedingFood;
        this.experienceValue = 3;
        this.speed = 0.25D;
        this.isRideable = false;
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

    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.bodyPart = new RiftCreaturePart(this, 0, 0, 0.25f, scale,  0.55f * scale, 1f);
            this.headPart = new RiftCreaturePart(this, 0.35f, 0, 0.5f, 0.5f * scale,  0.5f * scale, 1f);
        }
    }

    public void fall(float distance, float damageMultiplier) {}

    @Override
    public boolean canDoHerding() {
        return true;
    }

    public double followRange() {
        return 3D;
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

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

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 0.5f, 0.75f);
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
