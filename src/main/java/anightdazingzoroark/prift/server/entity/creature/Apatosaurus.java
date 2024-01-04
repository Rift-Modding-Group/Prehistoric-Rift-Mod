package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.SSRCompatUtils;
import anightdazingzoroark.prift.config.ApatosaurusConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.ai.*;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.items.RiftLargeWeaponItem;
import anightdazingzoroark.prift.server.message.*;
import com.google.common.base.Predicate;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Loader;
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
    public static final ResourceLocation LOOT =  LootTableList.register(new ResourceLocation(RiftInitialize.MODID, "entities/apatosaurus"));
    private static final DataParameter<Byte> WEAPON = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> LAUNCHING = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LOADED = EntityDataManager.createKey(Apatosaurus.class, DataSerializers.BOOLEAN);
    private int launchTick;

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
        this.launchTick = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WEAPON, (byte)RiftLargeWeaponType.NONE.ordinal());
        this.dataManager.register(LAUNCHING, false);
        this.dataManager.register(CHARGING, false);
        this.dataManager.register(LOADED, false);
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
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.manageWeaponCooldown();
        this.manageLoaded();
        this.manageCatapultAnims();
    }

    private void manageWeaponCooldown() {
        if (this.getLeftClickCooldown() > 0) this.setLeftClickCooldown(this.getLeftClickCooldown() - 1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (this.isBeingRidden()) {
            if (this.getControllingPassenger().equals(player)) {
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 0, settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown()));
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, 1, !settings.keyBindAttack.isKeyDown() && settings.keyBindUseItem.isKeyDown()));

                if (settings.keyBindAttack.isKeyDown() && !this.isActing()) {
                    if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) {
                        if (ShoulderInstance.getInstance().doShoulderSurfing()) {
                            Entity toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                            if (player.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) {
                                RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                            }
                            else {
                                if (toBeAttacked != null) {
                                    int targetId = toBeAttacked.getEntityId();
                                    RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,0));
                                }
                                else {
                                    RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,0));
                                }
                            }
                        }
                        else {
                            if (player.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) {
                                if (this.getLeftClickCooldown() == 0) RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 0));
                            }
                        }
                    }
                    else {
                        if (player.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) {
                            if (this.getLeftClickCooldown() == 0) RiftMessages.WRAPPER.sendToServer(new RiftIncrementControlUse(this, 0));
                        }
                        else {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1, 0));
                        }
                    }
                }
                else if (!settings.keyBindUseItem.isKeyDown() && !this.canUseRightClick()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseClick(this, 1, true));
                }
                else if (!settings.keyBindAttack.isKeyDown() && !settings.keyBindUseItem.isKeyDown()) {
                    Entity toBeAttacked = null;
                    if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) toBeAttacked = SSRCompatUtils.getEntities(this.attackWidth * (64D/39D)).entityHit;
                    if (this.hasLeftClickChargeBar()) {
                        if (this.getLeftClickUse() > 0) {
                            if (toBeAttacked != null) {
                                int targetId = toBeAttacked.getEntityId();
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, targetId,0, this.getLeftClickUse()));
                            }
                            else {
                                RiftMessages.WRAPPER.sendToServer(new RiftMountControl(this, -1,0, this.getLeftClickUse()));
                            }
                        }
                    }
                }
            }
        }
    }

    private void manageLoaded() {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            boolean flag1 = false;
            boolean flag2 = this.isBeingRidden() ? (this.getControllingPassenger() instanceof EntityPlayer ? ((EntityPlayer)this.getControllingPassenger()).isCreative() : false) : false;
            for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
                if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                    if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CATAPULT_BOULDER)) {
                        flag1 = true;
                        break;
                    }
                }
            }
            this.setLoaded(flag1 || flag2);
        }
        else this.setLoaded(false);
    }

    private void manageCatapultAnims() {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            if (!this.world.isRemote) {
                EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
                if (!this.isCharging() && this.isUsingLeftClick() && rider.getHeldItemMainhand().getItem().equals(RiftItems.COMMAND_CONSOLE)) this.setCharging(true);
                else if (this.isCharging() && !this.isUsingLeftClick()) this.setCharging(false);

                if (this.isLaunching()) {
                    this.launchTick++;
                    if (this.launchTick > 7) {
                        this.setLaunching(false);
                        this.launchTick = 0;
                    }
                }
            }
        }
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
                if (this.getLeftClickCooldown() == 0) {
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
            this.setLeftClickUse(0);
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
            this.setLeftClickCooldown(30);
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
            this.setLeftClickCooldown(30);
        }
    }

    private void manageCatapultFiring(int holdAmount) {
        EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
        boolean flag1 = false;
        boolean flag2 = rider.isCreative();
        int indexToRemove = -1;
        for (int x = this.creatureInventory.getSizeInventory() - 1; x >= 0; x--) {
            if (!this.creatureInventory.getStackInSlot(x).isEmpty()) {
                if (this.creatureInventory.getStackInSlot(x).getItem().equals(RiftItems.CATAPULT_BOULDER)) {
                    flag1 = true;
                    indexToRemove = x;
                    break;
                }
            }
        }
        if (flag1 || flag2) {
            this.setLaunching(true);
            RiftCatapultBoulder boulder = new RiftCatapultBoulder(this.world, this, rider);
            float velocity = RiftUtil.clamp((float) holdAmount * 0.015f + 1.5f, 1.5f, 3f);
            float power = RiftUtil.clamp(0.03f * holdAmount + 3f, 3f, 6f);
            boulder.setPower(power);
            boulder.shoot(this, RiftUtil.clamp(this.rotationPitch, -180f, 0f), this.rotationYaw, 0.0F, velocity, 1.0F);
            this.world.spawnEntity(boulder);
            this.creatureInventory.getStackInSlot(indexToRemove).setCount(0);
            this.setLeftClickCooldown(Math.max(holdAmount * 2, 60));
        }
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
        return !this.getWeapon().equals(RiftLargeWeaponType.NONE);
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

    public boolean isLaunching() {
        return this.dataManager.get(LAUNCHING);
    }

    public void setLaunching(boolean value) {
        this.dataManager.set(LAUNCHING, value);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    public void setCharging(boolean value) {
        this.dataManager.set(CHARGING, value);
    }

    public boolean isLoaded() {
        return this.dataManager.get(LOADED);
    }

    public void setLoaded(boolean value) {
        this.dataManager.set(LOADED, value);
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "movement", 0, this::apatosaurusMovement));
        data.addAnimationController(new AnimationController(this, "attack", 0, this::apatosaurusAttack));
        data.addAnimationController(new AnimationController(this, "weaponResize", 0, this::apatosaurusWeaponSize));
        data.addAnimationController(new AnimationController(this, "catapultCharge", 0, this::apatosaurusCatapultCharge));
        data.addAnimationController(new AnimationController(this, "catapultLaunch", 0, this::apatosaurusCatapultLaunch));
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

    private <E extends IAnimatable> PlayState apatosaurusCatapultCharge(AnimationEvent<E> event) {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            if (this.isCharging()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.charge_catapult", true));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }

    private <E extends IAnimatable> PlayState apatosaurusCatapultLaunch(AnimationEvent<E> event) {
        if (this.getWeapon().equals(RiftLargeWeaponType.CATAPULT)) {
            if (this.isLaunching()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.apatosaurus.launch_catapult", false));
                return PlayState.CONTINUE;
            }
        }
        event.getController().clearAnimationCache();
        return PlayState.STOP;
    }
}
