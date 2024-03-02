package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.config.MegapiranhaConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ResourceLocation;
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

public class Megapiranha extends RiftWaterCreature {
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/megapiranha"));

    public Megapiranha(World worldIn) {
        super(worldIn, RiftCreatureType.MEGAPIRANHA);
        this.setSize(0.5f, 0.75f);
        this.minCreatureHealth = MegapiranhaConfig.getMinHealth();
        this.maxCreatureHealth = MegapiranhaConfig.getMaxHealth();
        this.experienceValue = 3;
        this.favoriteFood = MegapiranhaConfig.megapiranhaFavoriteFood;
        this.speed = 0.35D;
        this.waterSpeed = 4D;
        this.attackWidth = 2f;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.setCanPickUpLoot(true);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, MegapiranhaConfig.megapiranhaTargets, MegapiranhaConfig.megapiranhaTargetBlacklist, true, true, true));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, MegapiranhaConfig.megapiranhaFavoriteFood, true));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 4.0D, true));
//        this.tasks.addTask(3, new RiftHerdDistanceFromOtherMembers(this, 2D));
        this.tasks.addTask(4, new RiftHerdMemberFollow(this));
        this.tasks.addTask(5, new RiftWanderWater(this, 1.0D));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(MegapiranhaConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    @Override
    public void resetParts(float scale) {}

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
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {}

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::megapiranhaMovement));
    }

    private <E extends IAnimatable> PlayState megapiranhaMovement(AnimationEvent<E> event) {
        if (this.isInWater()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megapiranha.move", true));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.megapiranha.flop", true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }
}
