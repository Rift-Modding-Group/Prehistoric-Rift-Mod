package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.*;
import anightdazingzoroark.rift.server.entity.ai.RiftAttack;
import anightdazingzoroark.rift.server.entity.ai.RiftGetTargets;
import anightdazingzoroark.rift.server.entity.ai.RiftPickUpItems;
import anightdazingzoroark.rift.server.entity.ai.RiftTyrannosaurusRoar;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tyrannosaurus extends RiftCreature implements IAnimatable {
    public static final ResourceLocation LOOT = LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/tyrannosaurus"));
    private static final Predicate<EntityLivingBase> WEAKNESS_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(RiftConfig.apexAffectedBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && !blacklist.contains("minecraft:player") && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !((RiftCreature) entity).isApexPredator() && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
            else {
                if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && !((RiftCreature) entity).isApexPredator() && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
        }
    };
    private static final Predicate<EntityLivingBase> WEAKNESS_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(RiftConfig.apexAffectedBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && blacklist.contains("minecraft:player") && entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else if (entity instanceof RiftCreature) {
                    return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !((RiftCreature) entity).isApexPredator() && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS);
                }
                else {
                    return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !entity.getActivePotionEffects().contains(MobEffects.WEAKNESS) && !(entity instanceof RiftEgg);
                }
            }
            else {
                return false;
            }
        }
    };
    private static final DataParameter<Boolean> ROARING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_ROAR = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private List<EntityAINearestAttackableTarget> attackableTargets = new ArrayList<>();
    public int roarCooldownTicks;
    private final EntityAIWander wanderTask = new EntityAIWander(this, 1.0D);
    private final EntityAIFollowOwner followOwner = new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F);
    private final RiftGetTargets getTargetTask = new RiftGetTargets(this, RiftConfig.tyrannosaurusTargets, true);
    private final EntityAIHurtByTarget hurtByTargetTask = new EntityAIHurtByTarget(this, false, new Class[0]);

    public Tyrannosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.TYRANNOSAURUS);
        this.setSize(3.25F, 5F);
        this.roarCooldownTicks = 0;
        this.isRideable = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CAN_ROAR, Boolean.valueOf(true));
        this.dataManager.register(ROARING, Boolean.valueOf(false));
        this.dataManager.register(APEX, Boolean.valueOf(true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(160D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, false, 0.5F, 0.5F));
        this.tasks.addTask(4, new EntityAILookIdle(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanRoar();
        this.manageApplyWeakness();
        this.manageAttributesByAge();
        this.manageTasksByTameStatus();
    }

    private void manageCanRoar() {
        this.roarCooldownTicks++;
        if (this.roarCooldownTicks >= 200) {
            this.setCanRoar(true);
            this.roarCooldownTicks = 0;
        }
    }

    private void manageApplyWeakness() {
        Predicate<EntityLivingBase> targetPredicate = RiftConfig.tyrannosaurusRoarTargetsWhitelist ? WEAKNESS_WHITELIST : WEAKNESS_BLACKLIST;
        for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(), targetPredicate)) {
            if (this.isTamed() && !entityLivingBase.getUniqueID().equals(this.getOwnerId())) {
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
            }
            else if (this.isTamed() && entityLivingBase instanceof EntityTameable) {
                if (!((EntityTameable) entityLivingBase).getOwnerId().equals(this.getOwnerId())) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
                }
            }
            else if (!this.isTamed()) {
                entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
            }
        }
    }

    private void manageAttributesByAge() {
        if (this.isChild()) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4D);
        }
        else {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(160D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(35.0D);
        }
    }

    private void manageTasksByTameStatus() {
        if (!this.isTamed()) {
            this.targetTasks.addTask(2, this.getTargetTask);
            this.targetTasks.addTask(0, new RiftTyrannosaurusRoar(this));
            this.targetTasks.addTask(1, this.hurtByTargetTask);
            this.targetTasks.addTask(3, new RiftPickUpItems(this, RiftConfig.tyrannosaurusFavoriteFood, true));
            this.tasks.addTask(3, this.wanderTask);
        }
        else {
            if (!this.world.isRemote) {
                switch (this.tameStatus) {
                    case SIT:
                        if (this.getAttackTarget() == null) this.setSitting(true);
                        this.tasks.removeTask(this.wanderTask);
                        this.tasks.removeTask(this.followOwner);
                        break;
                    case STAND:
                        this.setSitting(false);
                        this.tasks.removeTask(this.wanderTask);
                        this.tasks.addTask(3, this.followOwner);
                        break;
                    case WANDER:
                        this.setSitting(false);
                        this.tasks.addTask(3, this.wanderTask);
                        this.tasks.removeTask(this.followOwner);
                        break;
                }
                switch (this.tameBehavior) {
                    case ASSIST:
                        this.targetTasks.addTask(1, this.hurtByTargetTask);
                        this.targetTasks.addTask(2, this.defendOwner);
                        this.targetTasks.addTask(3, this.attackForOwner);
                        this.targetTasks.removeTask(this.getAggressiveModeTargets);
                        if (this.getAttackTarget() != null) this.setSitting(false);
                        break;
                    case NEUTRAL:
                        this.targetTasks.addTask(1, this.hurtByTargetTask);
                        this.targetTasks.removeTask(this.defendOwner);
                        this.targetTasks.removeTask(this.attackForOwner);
                        this.targetTasks.removeTask(this.getAggressiveModeTargets);
                        if (this.getAttackTarget() != null) this.setSitting(false);
                        break;
                    case AGGRESSIVE:
                        this.targetTasks.addTask(1, this.hurtByTargetTask);
                        this.targetTasks.removeTask(this.defendOwner);
                        this.targetTasks.removeTask(this.attackForOwner);
                        this.targetTasks.addTask(2, this.getAggressiveModeTargets);
                        if (this.getAttackTarget() != null) this.setSitting(false);
                        break;
                    case PASSIVE:
                        this.targetTasks.removeTask(this.hurtByTargetTask);
                        this.targetTasks.removeTask(this.defendOwner);
                        this.targetTasks.removeTask(this.attackForOwner);
                        this.targetTasks.removeTask(this.getAggressiveModeTargets);
                        break;
                }
            }
        }
    }

    protected AxisAlignedBB getTargetableArea() {
        return this.getEntityBoundingBox().grow(16.0D, 16.0D, 16.0D);
    }

    @Override
    public float getRenderSizeModifier() {
        if (this.isChild()) {
            return 0.5f;
        }
        else {
            return 3.25f;
        }
    }

    public boolean isFavoriteFood(ItemStack stack) {
        List<String> favoriteFoodList = Arrays.asList(RiftConfig.tyrannosaurusFavoriteFood);
        for (String foodItem : favoriteFoodList) {
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(foodItem))) return true;
        }
        return false;
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        ItemStack itemstack = itemEntity.getItem();
        Item item = itemstack.getItem();
        EntityEquipmentSlot entityequipmentslot = getSlotForItemStack(itemstack);

        if (Arrays.asList(RiftConfig.tyrannosaurusFavoriteFood).contains(Item.REGISTRY.getNameForObject(item).toString()) && this.canEquipItem(itemstack)) {
            this.setItemStackToSlot(entityequipmentslot, new ItemStack(Items.AIR));
            this.onItemPickup(itemEntity, itemstack.getCount());
            itemEntity.setDead();
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    public void setRoaring(boolean value) {
        this.dataManager.set(ROARING, Boolean.valueOf(value));
    }

    public boolean isRoaring() {
        return this.dataManager.get(ROARING);
    }

    public void setCanRoar(boolean value) {
        this.dataManager.set(CAN_ROAR, Boolean.valueOf(value));
    }

    public boolean canRoar() {
        return this.dataManager.get(CAN_ROAR);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::tyrannosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attacking", 0, this::tyrannosaurusAttack));
        data.addAnimationController(new AnimationController(this, "roaring", 0, this::tyrannosaurusRoar));
    }

    private <E extends IAnimatable> PlayState tyrannosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState tyrannosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.attack", false));
            return PlayState.CONTINUE;
        }
        else {
            event.getController().clearAnimationCache();
            return PlayState.CONTINUE;
        }
    }

    private <E extends IAnimatable> PlayState tyrannosaurusRoar(AnimationEvent<E> event) {
        if (this.isRoaring()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.roar", false));
            return PlayState.CONTINUE;
        }
        else {
            event.getController().clearAnimationCache();
            return PlayState.CONTINUE;
        }
    }
}
