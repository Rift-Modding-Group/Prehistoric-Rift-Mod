package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.items.RiftLargeWeaponItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.Sys;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class Apatosaurus extends RiftCreature {
    private static final DataParameter<Byte> WEAPON = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BYTE);

    public Apatosaurus(World worldIn) {
        super(worldIn, RiftCreatureType.APATOSAURUS);
        this.minCreatureHealth = ApatosaurusConfig.getMinHealth();
        this.maxCreatureHealth = ApatosaurusConfig.getMaxHealth();
        this.setSize(3f, 3f);
        this.favoriteFood = ApatosaurusConfig.apatosaurusFavoriteFood;
        this.tamingFood = ApatosaurusConfig.apatosaurusBreedingFood;
        this.experienceValue = 50;
        this.speed = 0.15D;
        this.isRideable = true;
        this.attackWidth = 4.5f;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WEAPON, (byte)RiftLargeWeaponType.NONE.ordinal());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ApatosaurusConfig.damage);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16D);
    }

    protected void initEntityAI() {
        this.targetTasks.addTask(1, new RiftHurtByTarget(this, false));
        this.tasks.addTask(1, new RiftMate(this));
        this.tasks.addTask(2, new RiftControlledAttack(this, 3F, 3F));
        this.tasks.addTask(3, new RiftAttack(this, 1.0D, 3F, 3F));
        this.tasks.addTask(4, new RiftFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new RiftMoveToHomePos(this, 1.0D));
        this.tasks.addTask(6, new RiftWander(this, 1.0D));
        this.tasks.addTask(7, new RiftLookAround(this));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("Weapon", (byte) this.getWeapon().ordinal());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Weapon")) this.setWeapon(RiftLargeWeaponType.values()[compound.getByte("Weapon")]);
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
        float xOffset = (float)(this.posX + (1) * Math.cos((this.rotationYaw + 90) * Math.PI / 180));
        float zOffset = (float)(this.posZ + (1) * Math.sin((this.rotationYaw + 90) * Math.PI / 180));
        return new Vec3d(xOffset, this.posY + 1.25, zOffset);
    }

    @Override
    public void controlInput(int control, int holdAmount, EntityLivingBase target) {

    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isTamed()) {
            if (itemstack.getItem() instanceof RiftLargeWeaponItem && this.getWeapon().equals(RiftLargeWeaponType.NONE)) {
                if (itemstack.getItem().equals(RiftItems.CANNON)) {
                    this.setWeapon(RiftLargeWeaponType.CANNON);
                    this.consumeItemFromStack(player, itemstack);
                    return true;
                }
                else if (itemstack.getItem().equals(RiftItems.MORTAR)) {
                    this.setWeapon(RiftLargeWeaponType.MORTAR);
                    this.consumeItemFromStack(player, itemstack);
                    return true;
                }
                else if (itemstack.getItem().equals(RiftItems.CATAPULT)) {
                    this.setWeapon(RiftLargeWeaponType.CATAPULT);
                    this.consumeItemFromStack(player, itemstack);
                    return true;
                }
            }
            else if (itemstack.getItem().equals(RiftItems.WRENCH) && !this.getWeapon().equals(RiftLargeWeaponType.NONE)) {
                if (!player.capabilities.isCreativeMode) {
                    switch (this.getWeapon()) {
                        case CANNON:
                            player.inventory.addItemStackToInventory(new ItemStack(RiftItems.CANNON));
                            break;
                        case MORTAR:
                            player.inventory.addItemStackToInventory(new ItemStack(RiftItems.MORTAR));
                            break;
                        case CATAPULT:
                            player.inventory.addItemStackToInventory(new ItemStack(RiftItems.CATAPULT));
                            break;
                    }
                }
                this.setWeapon(RiftLargeWeaponType.NONE);
                return true;
            }
        }
        return super.processInteract(player, hand);
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
    public void refreshInventory() {
        ItemStack saddle = this.creatureInventory.getStackInSlot(0);
        if (!this.world.isRemote) this.setSaddled(saddle.getItem() == RiftItems.APATOSAURUS_PLATFORM && !saddle.isEmpty());
    }

    public RiftLargeWeaponType getWeapon() {
        return RiftLargeWeaponType.values()[this.dataManager.get(WEAPON).byteValue()];
    }

    public void setWeapon(RiftLargeWeaponType value) {
        this.dataManager.set(WEAPON, (byte) value.ordinal());
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::apatosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::apatosaurusAttack));
        data.addAnimationController(new AnimationController(this, "weaponResize", 0, this::apatosaurusWeaponSize));
    }

    private <E extends IAnimatable> PlayState apatosaurusMovement(AnimationEvent<E> event) {
        if (this.isSitting() && !this.isBeingRidden() && !this.hasTarget()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.sitting", true));
            return PlayState.CONTINUE;
        }
        if ((event.isMoving() || (this.isSitting() && this.hasTarget())) && !this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.walk", true));
            return PlayState.CONTINUE;
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState apatosaurusAttack(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.stomp", false));
        }
        else {
            event.getController().clearAnimationCache();
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState apatosaurusWeaponSize(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.weapon_size_change", true));
        return PlayState.CONTINUE;
    }
}
