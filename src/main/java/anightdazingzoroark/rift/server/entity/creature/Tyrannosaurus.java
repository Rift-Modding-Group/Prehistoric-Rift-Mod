package anightdazingzoroark.rift.server.entity.creature;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.ai.RiftAttack;
import anightdazingzoroark.rift.server.entity.ai.RiftPickUpItems;
import com.google.common.base.Predicate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class Tyrannosaurus extends RiftCreature implements IAnimatable {
    private static final DataParameter<Boolean> ROARING = EntityDataManager.<Boolean>createKey(Tyrannosaurus.class, DataSerializers.BOOLEAN);
    private AnimationFactory factory = new AnimationFactory(this);

    public Tyrannosaurus(World worldIn) {
        super(worldIn);
        this.setSize(3.25F, 5F);
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

    @Override
    protected void initEntityAI() {
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        for (String target : RiftConfig.tyrannosaurusTargets) {
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityList.getClass(new ResourceLocation(target)), true));
        }
        this.targetTasks.addTask(3, new RiftPickUpItems(this, RiftConfig.tyrannosaurusFavoriteFood, true));
        this.tasks.addTask(2, new RiftAttack(this, 1.0D, false, 0.5F, 0.5F));
        this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(4, new EntityAILookIdle(this));
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

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::tyrannosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attacking", 0, this::tyrannosaurusAttack));
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

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    class TyrannosaurusRoar extends EntityAIBase {
        protected final Tyrannosaurus mob;
        private int roarTick;
        private int roarCooldown;

        public TyrannosaurusRoar(Tyrannosaurus mob) {
            this.mob = mob;
            this.roarCooldown = 0;
        }

        @Override
        public boolean shouldExecute() {
            if (this.roarCooldown >= 100) {
                int roarChance = new Random().nextInt(4);
                return this.mob.getLastAttackedEntity() != null && roarChance == 0;
            }
            else {
                return false;
            }
        }
    }
}
