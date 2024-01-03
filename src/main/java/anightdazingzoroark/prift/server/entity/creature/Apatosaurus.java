package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.items.RiftLargeWeaponItem;
import anightdazingzoroark.prift.server.message.RiftManageUtilizingControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.Sys;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Apatosaurus extends RiftCreature {
    private static final DataParameter<Byte> WEAPON = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> CATAPULT_USE = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> CATAPULT_COOLDOWN = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.VARINT);

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
        this.dataManager.register(CATAPULT_USE, 0);
        this.dataManager.register(CATAPULT_COOLDOWN, 0);
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
        EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
        if (control == 0) {
            if (rider.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) {
                switch (this.getWeapon()) {
                    case CANNON:
                        this.manageCannonFiring();
                        break;
                    case MORTAR:
                        this.manageMortarFiring();
                        break;
                    case CATAPULT:
                        this.manageCatapultFiring(holdAmount);
                        break;
                }
            }
            else {
                if (this.getEnergy() > 0) {
                    if (target == null) {
                        if (!this.isActing()) this.setAttacking(true);
                    }
                    else {
                        if (!this.isActing()) {
                            this.ssrTarget = target;
                            this.setAttacking(true);
                        }
                    }
                }
                else ((EntityPlayer)this.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("reminder.insufficient_energy", this.getName()), false);
            }
        }
    }

    private void manageCannonFiring() {
        EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        int indexToRemove = -1;
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CANNONBALL)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            RiftCannonball cannonball = new RiftCannonball(this.world, this, rider);
            cannonball.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, 1.6F, 1.0F);
            this.world.spawnEntity(cannonball);
            this.creatureInventory.getStackInSlot(indexToRemove).setCount(0);
        }
    }

    private void manageMortarFiring() {
        //get nearest entity first
        //should be in the front
        AxisAlignedBB detectionBox = new AxisAlignedBB(this.posX - 16, 0, this.posZ - 16, this.posX + 16, this.posY + 16, this.posZ + 16);
        double dist = detectionBox.maxX - detectionBox.minX;
        Vec3d vec3d = this.getPositionEyes(1.0F);
        Vec3d vec3d1 = this.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist);
        double d1 = dist;
        EntityLivingBase pointedEntity = null;
        EntityPlayer rider = (EntityPlayer)this.getControllingPassenger();
        UUID userId = rider.getUniqueID();
        List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, detectionBox.expand(vec3d1.x * dist, vec3d1.y * dist, vec3d1.z * dist).grow(1.0D, 1.0D, 1.0D), new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase input) {
                if (input instanceof EntityTameable) {
                    EntityTameable inpTameable = (EntityTameable)input;
                    if (inpTameable.isTamed()) {
                        return !userId.equals(inpTameable.getOwnerId());
                    }
                    else return true;
                }
                return true;
            }
        });
        double d2 = d1;
        for (EntityLivingBase potentialTarget : list) {
            AxisAlignedBB axisalignedbb = potentialTarget.getEntityBoundingBox().grow((double) potentialTarget.getCollisionBorderSize() + 2F);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (potentialTarget != this && potentialTarget != rider) {
                if (axisalignedbb.contains(vec3d)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = potentialTarget;
                        d2 = 0.0D;
                    }
                }
                else if (raytraceresult != null) {
                    double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        if (potentialTarget.getLowestRidingEntity() == rider.getLowestRidingEntity() && !rider.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = potentialTarget;
                            }
                        }
                        else {
                            pointedEntity = potentialTarget;
                            d2 = d3;
                        }
                    }
                }
            }
        }

        //firing logic
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        int indexToRemove = -1;
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.MORTAR_SHELL)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            RiftMortarShell mortarShell = new RiftMortarShell(this.world, this, rider);
            mortarShell.shoot(this, pointedEntity);
            this.world.spawnEntity(mortarShell);
            this.creatureInventory.getStackInSlot(indexToRemove).setCount(0);
        }
    }

    private void manageCatapultFiring(int holdAmount) {}

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
        if (this.isBeingRidden()) {
            EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
            return rider.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE) && this.getWeapon().equals(RiftLargeWeaponType.CATAPULT);
        }
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
