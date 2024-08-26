package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.interfaces.IHerder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
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

public class Coelacanth extends RiftWaterCreature implements IHerder {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/coelacanth"));
    private RiftCreaturePart bodyFront0;
    private RiftCreaturePart bodyFront1;
    private RiftCreaturePart bodyFront2;
    private RiftCreaturePart bodyBack0;
    private RiftCreaturePart bodyBack1;
    private RiftCreaturePart bodyBack2;
    protected int herdSize = 1;
    protected RiftCreature herdLeader;

    public Coelacanth(World worldIn) {
        super(worldIn, RiftCreatureType.COELACANTH);
        this.setSize(0.5f, 0.5f);
        this.experienceValue = 3;
        this.speed = 0.5D;

        this.bodyPart = new RiftCreaturePart(this, 0, 0, 0f, 0.5f, 1f, 1f);
        this.bodyFront0 = new RiftCreaturePart(this, 1.1f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyFront1 = new RiftCreaturePart(this, 0.72f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyFront2 = new RiftCreaturePart(this, 0.36f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyBack0 = new RiftCreaturePart(this, -1.1f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyBack1 = new RiftCreaturePart(this, -0.72f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.bodyBack2 = new RiftCreaturePart(this, -0.36f, 0, 0.15f, 0.375f, 0.8f, 1f);
        this.hitboxArray = new RiftCreaturePart[]{
            this.bodyPart,
            this.bodyFront0,
            this.bodyFront1,
            this.bodyFront2,
            this.bodyBack0,
            this.bodyBack1,
            this.bodyBack2
        };
    }

    protected void initEntityAI() {
        this.tasks.addTask(3, new RiftHerdMemberFollow(this));
        this.tasks.addTask(4, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void updateParts() {
        super.updateParts();
    }

    @Override
    public void removeParts() {
        super.removeParts();
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
        return 2D;
    }

    @Override
    public float getRenderSizeModifier() {
        return 1f;
    }

    public float attackWidth() {
        return 0;
    }

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
    public void controlInput(int control, int holdAmount, EntityLivingBase target, BlockPos pos) {}

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
