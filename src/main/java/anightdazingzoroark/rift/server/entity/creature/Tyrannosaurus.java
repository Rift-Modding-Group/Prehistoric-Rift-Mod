package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.ai.RiftAttack;
import anightdazingzoroark.rift.server.entity.ai.RiftPickUpItems;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;


import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class Tyrannosaurus extends RiftCreature implements IAnimatable {
    private static final Predicate<Entity> ROAR_BLACKLIST = new Predicate<Entity>() {
        @Override
        public boolean apply(@Nullable Entity entity) {
            String[] blacklist = RiftConfig.tyrannosaurusRoarTargetBlacklist;
            if (entity instanceof EntityPlayer) {
                return entity.isEntityAlive() && !Arrays.asList(blacklist).contains("minecraft:player");
            }
            else {
                return entity.isEntityAlive() && !Arrays.asList(blacklist).contains(EntityList.getKey(entity).toString());
            }
        }
    };
    private static final Predicate<Entity> ROAR_WHITELIST = new Predicate<Entity>() {
        @Override
        public boolean apply(@Nullable Entity entity) {
            String[] blacklist = RiftConfig.tyrannosaurusRoarTargetBlacklist;
            if (entity instanceof EntityPlayer) {
                return entity.isEntityAlive() && Arrays.asList(blacklist).contains("minecraft:player");
            }
            else {
                return entity.isEntityAlive() && Arrays.asList(blacklist).contains(EntityList.getKey(entity).toString());
            }
        }
    };
    private static final DataParameter<Boolean> ROARING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAN_ROAR = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private AnimationFactory factory = new AnimationFactory(this);
    public int roarCooldownTicks;

    public Tyrannosaurus(World worldIn) {
        super(worldIn);
        this.setSize(3.25F, 5F);
        this.roarCooldownTicks = 0;
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
        this.targetTasks.addTask(0, new TyrannosaurusRoar(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        for (String target : RiftConfig.tyrannosaurusTargets) {
            if (!"minecraft:player".equals(target)) {
                this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityList.getClass(new ResourceLocation(target)), true));
            }
            else {
                this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
            }
        }
        this.targetTasks.addTask(3, new RiftPickUpItems(this, RiftConfig.tyrannosaurusFavoriteFood, true));
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, false, 0.5F, 0.5F));
        this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(4, new EntityAILookIdle(this));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageCanRoar();
    }

    public void manageCanRoar() {
        this.roarCooldownTicks++;
        if (this.roarCooldownTicks >= 200) {
            this.setCanRoar(true);
            this.roarCooldownTicks = 0;
        }
    }

    @Override
    public float getRenderSizeModifier() {
        return 3.25f;
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
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tyrannosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        else {
            return PlayState.STOP;
        }
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

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    class TyrannosaurusRoar extends EntityAIBase {
        protected final Tyrannosaurus mob;
        private int useTick;
        private int roarTick;

        public TyrannosaurusRoar(Tyrannosaurus mob) {
            this.mob = mob;
            this.useTick = 0;
            this.roarTick = 0;
        }

        @Override
        public boolean shouldExecute() {
            return this.mob.hurtTime > 0 && new Random().nextInt(4) == 0 && this.mob.canRoar();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.roarTick <= 40;
        }

        @Override
        public void startExecuting() {
            this.mob.setRoaring(true);
        }

        @Override
        public void resetTask() {
            this.roarTick = 0;
            this.mob.setRoaring(false);
            this.mob.setCanRoar(false);
        }

        @Override
        public void updateTask() {
            this.roarTick++;
            if (this.roarTick == 10 && this.mob.isEntityAlive()) {
                Predicate<Entity> targetPredicate = RiftConfig.tyrannosaurusRoarTargetsWhitelist ? ROAR_WHITELIST : ROAR_BLACKLIST;
                for (Entity entity : this.mob.world.getEntitiesWithinAABB(Entity.class, this.getTargetableArea(12.0D), targetPredicate)) {
                    if (entity != this.mob) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this.mob), 2f);
                        this.strongKnockback(entity);
                    }
                }
            }
        }

        private void strongKnockback(Entity target) {
            double d0 = this.mob.posX - target.posX;
            double d1 = this.mob.posZ - target.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            ((EntityLivingBase)target).knockBack(this.mob, 1.5f, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            return this.mob.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
        }
    }
}
