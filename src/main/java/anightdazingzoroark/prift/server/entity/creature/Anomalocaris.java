package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.AnomalocarisConfig;
import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.ai.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Anomalocaris extends RiftWaterCreature {
    public Anomalocaris(World worldIn) {
        super(worldIn, RiftCreatureType.ANOMALOCARIS);
        this.setSize(2f, 0.75f);
        this.minCreatureHealth = AnomalocarisConfig.getMinHealth();
        this.maxCreatureHealth = AnomalocarisConfig.getMaxHealth();
        this.favoriteFood = AnomalocarisConfig.anomalocarisFavoriteFood;
        this.tamingFood = AnomalocarisConfig.anomalocarisTamingFood;
        this.experienceValue = 10;
        this.isRideable = true;
        this.saddleItem = AnomalocarisConfig.anomalocarisSaddleItem;
        this.speed = 0.2D;
        this.waterSpeed = 5D;
        this.attackWidth = 3f;
        this.attackDamage = AnomalocarisConfig.damage;
        this.healthLevelMultiplier = AnomalocarisConfig.healthMultiplier;
        this.damageLevelMultiplier = AnomalocarisConfig.damageMultiplier;
        this.densityLimit = AnomalocarisConfig.anomalocarisDensityLimit;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.targetTasks.addTask(2, new RiftAggressiveModeGetTargets(this, true));
        this.targetTasks.addTask(2, new RiftGetTargets.RiftGetTargetsWater(this, AnomalocarisConfig.anomalocarisTargets, AnomalocarisConfig.anomalocarisTargetBlacklist, true, true, true));
        this.targetTasks.addTask(3, new RiftPickUpItems(this, AnomalocarisConfig.anomalocarisFavoriteFood, true));
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(7, new RiftWanderWater(this, 1.0D));
    }

    @Override
    public void resetParts(float scale) {

    }

    @Override
    public float getRenderSizeModifier() {
        return RiftUtil.setModelScale(this, 1f, 2f);
    }

    @Override
    public Vec3d riderPos() {
        return null;
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {

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
    public void registerControllers(AnimationData data) {

    }

    @Override
    public boolean isAmphibious() {
        return false;
    }
}
