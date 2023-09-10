package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.*;
import anightdazingzoroark.rift.server.entity.ai.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    private static final Predicate<EntityLivingBase> ROAR_BLACKLIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(RiftConfig.tyrannosaurusRoarTargetBlacklist);
            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && !blacklist.contains("minecraft:player");
                }
                else {
                    return entity.isEntityAlive() && !blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
                }
            }
            else {
                return entity.isEntityAlive() && !(entity instanceof RiftEgg);
            }
        }
    };
    private static final Predicate<EntityLivingBase> ROAR_WHITELIST = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            List<String> blacklist = Arrays.asList(RiftConfig.tyrannosaurusRoarTargetBlacklist);

            if (!blacklist.isEmpty()) {
                if (entity instanceof EntityPlayer) {
                    return entity.isEntityAlive() && blacklist.contains("minecraft:player");
                }
                else {
                    return entity.isEntityAlive() && blacklist.contains(EntityList.getKey(entity).toString()) && !(entity instanceof RiftEgg);
                }
            }
            else {
                return false;
            }
        }
    };
    private static final DataParameter<Boolean> ROARING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_ROAR = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    public int roarCooldownTicks;
    private final EntityAIWander wanderTask = new EntityAIWander(this, 1.0D);
    private final EntityAIFollowOwner followOwner = new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F);
    private final RiftGetTargets getTargetTask = new RiftGetTargets(this, RiftConfig.tyrannosaurusTargets, true);
    private final EntityAIHurtByTarget hurtByTargetTask = new EntityAIHurtByTarget(this, false, new Class[0]);
    private final EntityAILookIdle lookAroundTask = new EntityAILookIdle(this);

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
        this.targetTasks.addTask(0, new RiftTyrannosaurusRoar(this));
        this.tasks.addTask(1, new RiftControlledAttack(this, 0.52F, 0.24F));
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, false, 0.52F, 0.24F));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanRoar();
        if (!this.isChild()) this.manageApplyWeakness();
        this.manageAttributesByAge();
        this.manageTasksByTameStatus();
        this.manageTasksByIsRidden();
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
        for (EntityLivingBase entityLivingBase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEffectCastArea(), targetPredicate)) {
            if (this.isTamed() && entityLivingBase instanceof EntityPlayer) {
                if (!entityLivingBase.getUniqueID().equals(this.getOwnerId())) {
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
                }
            }
            else if (this.isTamed() && entityLivingBase instanceof EntityTameable) {
                if (((EntityTameable) entityLivingBase).isTamed()) {
                    if (!((EntityTameable) entityLivingBase).getOwnerId().equals(this.getOwnerId())) {
                        entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 1));
                    }
                }
                else {
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
            this.targetTasks.addTask(1, this.hurtByTargetTask);
            this.targetTasks.addTask(3, new RiftPickUpItems(this, RiftConfig.tyrannosaurusFavoriteFood, true));
            this.tasks.addTask(3, this.wanderTask);
        }
        else {
            if (!this.world.isRemote) {
                switch (this.getTameStatus()) {
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
                switch (this.getTameBehavior()) {
                    case ASSIST:
                        if (this.isBeingRidden()) {
                            this.targetTasks.removeTask(this.hurtByTargetTask);
                            this.targetTasks.removeTask(this.defendOwner);
                            this.targetTasks.removeTask(this.attackForOwner);
                            this.targetTasks.removeTask(this.getAggressiveModeTargets);
                        }
                        else {
                            this.targetTasks.addTask(1, this.hurtByTargetTask);
                            this.targetTasks.addTask(2, this.defendOwner);
                            this.targetTasks.addTask(3, this.attackForOwner);
                            this.targetTasks.removeTask(this.getAggressiveModeTargets);
                            if (this.getAttackTarget() != null) this.setSitting(false);
                        }
                        break;
                    case NEUTRAL:
                        if (this.isBeingRidden()) {
                            this.targetTasks.removeTask(this.hurtByTargetTask);
                            this.targetTasks.removeTask(this.defendOwner);
                            this.targetTasks.removeTask(this.attackForOwner);
                            this.targetTasks.removeTask(this.getAggressiveModeTargets);
                        }
                        else {
                            this.targetTasks.addTask(1, this.hurtByTargetTask);
                            this.targetTasks.removeTask(this.defendOwner);
                            this.targetTasks.removeTask(this.attackForOwner);
                            this.targetTasks.removeTask(this.getAggressiveModeTargets);
                            if (this.getAttackTarget() != null) this.setSitting(false);
                        }
                        break;
                    case AGGRESSIVE:
                        if (this.isBeingRidden()) {
                            this.targetTasks.removeTask(this.hurtByTargetTask);
                            this.targetTasks.removeTask(this.defendOwner);
                            this.targetTasks.removeTask(this.attackForOwner);
                            this.targetTasks.removeTask(this.getAggressiveModeTargets);
                        }
                        else {
                            this.targetTasks.addTask(1, this.hurtByTargetTask);
                            this.targetTasks.removeTask(this.defendOwner);
                            this.targetTasks.removeTask(this.attackForOwner);
                            this.targetTasks.addTask(2, this.getAggressiveModeTargets);
                            if (this.getAttackTarget() != null) this.setSitting(false);
                        }
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

    private void manageTasksByIsRidden() {
        if (this.isBeingRidden()) {
            this.tasks.removeTask(this.lookAroundTask);
        }
        else {
            this.tasks.addTask(4, this.lookAroundTask);
        }
    }

    protected AxisAlignedBB getEffectCastArea() {
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

    @Override
    public boolean isFavoriteFood(ItemStack stack) {
        List<String> favoriteFoodList = Arrays.asList(RiftConfig.tyrannosaurusFavoriteFood);
        for (String foodItem : favoriteFoodList) {
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(foodItem))) return true;
        }
        return false;
    }

    @Override
    protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
        if (!this.isTamed()) {
            ItemStack itemstack = itemEntity.getItem();
            Item item = itemstack.getItem();
            EntityEquipmentSlot entityequipmentslot = getSlotForItemStack(itemstack);

            if (Arrays.asList(RiftConfig.tyrannosaurusFavoriteFood).contains(Item.REGISTRY.getNameForObject(item).toString()) && this.canEquipItem(itemstack)) {
                this.setItemStackToSlot(entityequipmentslot, new ItemStack(Items.AIR));
                this.onItemPickup(itemEntity, itemstack.getCount());
                itemEntity.setDead();
            }
        }
    }

    //stuff below this comment is for roar stuff
    public void roar(float strength) {
        Predicate<EntityLivingBase> targetPredicate = RiftConfig.tyrannosaurusRoarTargetsWhitelist ? ROAR_WHITELIST : ROAR_BLACKLIST;
        for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getRoarArea((double)strength * 6d), targetPredicate)) {
            if (entity != this) {
                if (this.isTamed() && entity instanceof EntityTameable) {
                    if (!((EntityTameable) entity).getOwner().equals(this.getOwner())) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
                        this.roarKnockback(entity, strength);
                    }
                }
                else if (this.isTamed() && entity instanceof EntityPlayer) {
                    if (!this.getOwner().equals(entity)) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
                        this.roarKnockback(entity, strength);
                    }
                }
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), 2f);
                this.roarKnockback(entity, strength);
            }
        }
        this.roarBreakBlocks(strength);
    }

    private void roarKnockback(Entity target, float strength) {
        double d0 = this.posX - target.posX;
        double d1 = this.posZ - target.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        ((EntityLivingBase)target).knockBack(this, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
    }

    protected AxisAlignedBB getRoarArea(double targetDistance) {
        return this.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    private void roarBreakBlocks(float strength) {
        List<BlockPos> affectedBlockPositions = Lists.<BlockPos>newArrayList();
        boolean canBreak = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this);
        if (canBreak) {
            if (!this.world.isRemote) {
                Set<BlockPos> set = Sets.<BlockPos>newHashSet();
                int i = 16;
                for (int j = 0; j < 16; ++j) {
                    for (int k = 0; k < 16; ++k) {
                        for (int l = 0; l < 16; ++l) {
                            if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                                double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                                double d1 = Math.abs((double)((float)k / 15.0F * 2.0F - 1.0F));
                                double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                                d0 = d0 / d3;
                                d1 = d1 / d3;
                                d2 = d2 / d3;
                                float f = (strength * 4) * (0.7F + this.world.rand.nextFloat() * 0.6F);
                                double d4 = this.posX;
                                double d6 = this.posY;
                                double d8 = this.posZ;

                                for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                    BlockPos blockpos = new BlockPos(d4, d6, d8);
                                    IBlockState iblockstate = this.world.getBlockState(blockpos);
                                    Block block = iblockstate.getBlock();

                                    if (iblockstate.getMaterial() != Material.AIR) {
                                        if (RiftUtil.blockWeakerThanWood(block)) {
                                            f -= 0.24F;
                                        }
                                        else {
                                            f -= (1200F + 0.3F) * 0.3F;
                                        }

                                        if (f > 0.0F) {
                                            set.add(blockpos);
                                        }
                                    }

                                    d4 += d0 * 0.30000001192092896D;
                                    d6 += d1 * 0.30000001192092896D;
                                    d8 += d2 * 0.30000001192092896D;
                                }
                            }
                        }
                    }
                }
                affectedBlockPositions.addAll(set);
                for (BlockPos blockPos : affectedBlockPositions) {
                    this.world.destroyBlock(blockPos, false);
                }
            }
        }
    }
    //end of roar stuff

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
    public boolean isApexPredator() {
        return true;
    }

    @Override
    public boolean canBeSaddled() {
        return true;
    }

    @Override
    public int slotCount() {
        return 54;
    }

    @Override
    public Vec3d riderPos() {
        return new Vec3d(this.posX, this.posY + 2.125, this.posZ);
    }

    @Override
    public void controlInput(int control) {
        if (control == 0) {
            if (!this.isRoaring() && !this.isAttacking()) this.setAttacking(true);
        }
        if (control == 1) {
            if (this.canRoar() && !this.isRoaring() && !this.isAttacking()) this.setRoaring(true);
        }
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::tyrannosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attacking", 0, this::tyrannosaurusAttack));
        data.addAnimationController(new AnimationController(this, "roaring", 0, this::tyrannosaurusRoar));
    }

    private <E extends IAnimatable> PlayState tyrannosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden()) {
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
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState tyrannosaurusRoar(AnimationEvent<E> event) {
        if (this.isRoaring()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.roar", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }
}
