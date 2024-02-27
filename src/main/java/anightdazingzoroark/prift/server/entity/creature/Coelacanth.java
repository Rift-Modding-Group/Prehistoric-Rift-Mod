package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.config.CoelacanthConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

public class Coelacanth extends RiftWaterCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/coelacanth"));
    private RiftCreaturePart bodyFront0;
    private RiftCreaturePart bodyFront1;
    private RiftCreaturePart bodyFront2;
    private RiftCreaturePart bodyBack0;
    private RiftCreaturePart bodyBack1;
    private RiftCreaturePart bodyBack2;

    public Coelacanth(World worldIn) {
        super(worldIn, RiftCreatureType.COELACANTH);
        this.minCreatureHealth = CoelacanthConfig.getMinHealth();
        this.maxCreatureHealth = CoelacanthConfig.getMaxHealth();
        this.setSize(0.5f, 1f);
        this.experienceValue = 3;
        this.speed = 0.5D;
    }

    protected void initEntityAI() {
        this.tasks.addTask(3, new RiftHerdMemberFollow(this));
        this.tasks.addTask(4, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void resetParts(float scale) {
        if (scale > this.oldScale) {
            this.oldScale = scale;
            this.removeParts();
            this.bodyFront0 = new RiftCreaturePart(this, 1.1f, 0, 0.15f, 0.375f * scale, 0.8f * scale, 1f);
            this.bodyFront1 = new RiftCreaturePart(this, 0.72f, 0, 0.15f, 0.375f * scale, 0.8f * scale, 1f);
            this.bodyFront2 = new RiftCreaturePart(this, 0.36f, 0, 0.15f, 0.375f * scale, 0.8f * scale, 1f);
            this.bodyBack0 = new RiftCreaturePart(this, -1.1f, 0, 0.15f, 0.375f * scale, 0.8f * scale, 1f);
            this.bodyBack1 = new RiftCreaturePart(this, -0.72f, 0, 0.15f, 0.375f * scale, 0.8f * scale, 1f);
            this.bodyBack2 = new RiftCreaturePart(this, -0.36f, 0, 0.15f, 0.375f * scale, 0.8f * scale, 1f);
        }
    }

    @Override
    public void updateParts() {
        if (this.bodyFront0 != null) this.bodyFront0.onUpdate();
        if (this.bodyFront1 != null) this.bodyFront1.onUpdate();
        if (this.bodyFront2 != null) this.bodyFront2.onUpdate();
        if (this.bodyBack0 != null) this.bodyBack0.onUpdate();
        if (this.bodyBack1 != null) this.bodyBack1.onUpdate();
        if (this.bodyBack2 != null) this.bodyBack2.onUpdate();
    }

    @Override
    public void removeParts() {
        if (this.bodyFront0 != null) {
            this.world.removeEntityDangerously(this.bodyFront0);
            this.bodyFront0 = null;
        }
        if (this.bodyFront1 != null) {
            this.world.removeEntityDangerously(this.bodyFront1);
            this.bodyFront1 = null;
        }
        if (this.bodyFront2 != null) {
            this.world.removeEntityDangerously(this.bodyFront2);
            this.bodyFront2 = null;
        }
        if (this.bodyBack0 != null) {
            this.world.removeEntityDangerously(this.bodyBack0);
            this.bodyBack0 = null;
        }
        if (this.bodyBack1 != null) {
            this.world.removeEntityDangerously(this.bodyBack1);
            this.bodyBack1 = null;
        }
        if (this.bodyBack2 != null) {
            this.world.removeEntityDangerously(this.bodyBack2);
            this.bodyBack2 = null;
        }
    }

    @Override
    public boolean canDoHerding() {
        return this.isInWater();
    }

    public double followRange() {
        return 2D;
    }

    @Override
    public float getRenderSizeModifier() {
        return 1f;
    }

    @Override
    public float getEyeHeight() { return this.height * 0.05f; }

    @Override
    public Vec3d riderPos() {
        return null;
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
    public boolean isAmphibious() {
        return false;
    }

    public boolean canFlop() {
        return true;
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::coelacanthMovement));
    }

    private <E extends IAnimatable> PlayState coelacanthMovement(AnimationEvent<E> event) {
        if (this.isInWater()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.coelacanth.move", true));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.coelacanth.flop", true));
        }
        return PlayState.CONTINUE;
    }
}
