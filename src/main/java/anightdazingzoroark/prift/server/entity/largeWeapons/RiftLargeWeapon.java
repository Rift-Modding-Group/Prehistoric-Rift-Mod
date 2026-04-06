package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.client.ui.UIPanelNames;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.inventory.LargeWeaponInventoryHandler;
import anightdazingzoroark.prift.server.message.*;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.factory.EntityGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

public abstract class RiftLargeWeapon extends EntityAnimal implements IAnimatable, IGuiHolder<EntityGuiData> {
    private static final DataParameter<Boolean> USING_LEFT_CLICK = EntityDataManager.createKey(RiftLargeWeapon.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LEFT_CLICK_USE = EntityDataManager.createKey(RiftLargeWeapon.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LEFT_CLICK_COOLDOWN = EntityDataManager.createKey(RiftLargeWeapon.class, DataSerializers.VARINT);
    public final LargeWeaponInventoryHandler weaponInventory = new LargeWeaponInventoryHandler(5);
    public final RiftLargeWeaponType weaponType;
    private AnimationFactory factory = new AnimationFactory(this);
    public final Item weaponItem;
    public final Item ammoItem;

    public RiftLargeWeapon(World worldIn, RiftLargeWeaponType weaponType, Item weaponItem, Item ammoItem) {
        super(worldIn);
        this.weaponType = weaponType;
        this.weaponItem = weaponItem;
        this.ammoItem = ammoItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(USING_LEFT_CLICK, Boolean.FALSE);
        this.dataManager.register(LEFT_CLICK_USE, 0);
        this.dataManager.register(LEFT_CLICK_COOLDOWN, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) this.setControls();
        this.weaponCooldown();
    }

    @SideOnly(Side.CLIENT)
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (this.isBeingRidden()) {
            if (this.getPassengers().get(0).equals(player)) {
                if (settings.keyBindAttack.isKeyDown() && !this.isUsingLeftClick() && this.getLeftClickCooldown() == 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftLaunchLWeaponProjectile(this));
                }
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, settings.keyBindAttack.isKeyDown()));
            }
        }
    }

    private void weaponCooldown() {
        if (this.getLeftClickCooldown() > 0) this.setLeftClickCooldown(this.getLeftClickCooldown() - 1);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!player.isSneaking()) RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
        else {
            if (!player.world.isRemote) EntityGuiFactory.INSTANCE.open(player, this);
        }
        return false;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.weaponInventory.deserializeNBT(compound.getCompoundTag("Items"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("Items", this.weaponInventory.serializeNBT());
    }

    public abstract void launchProjectile(EntityPlayer player, int charge);

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) return false;
        else if (!this.world.isRemote && !this.isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() != null && this.isPassenger(source.getTrueSource())) return false;
            else {
                if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                    if (!source.isCreativePlayer()) this.dropItemWithOffset(this.weaponItem, 1, 0.0F);
                }
                this.setDead();
                return true;
            }
        }
        return true;
    }
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);

        this.rotationYaw = passenger.rotationYaw;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch = passenger.rotationPitch * 0.5f;
        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.renderYawOffset = this.rotationYaw;

        passenger.setPosition(riderPos().x, riderPos().y, riderPos().z);
        ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
        if (this.isDead) passenger.dismountRidingEntity();
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    public boolean isUsingLeftClick() {
        return this.dataManager.get(USING_LEFT_CLICK);
    }

    public void setUsingLeftClick(boolean value) {
        this.dataManager.set(USING_LEFT_CLICK, value);
    }

    public int getLeftClickUse() {
        return this.dataManager.get(LEFT_CLICK_USE);
    }

    public void setLeftClickUse(int value) {
        this.dataManager.set(LEFT_CLICK_USE, value);
    }

    public int getLeftClickCooldown() {
        return Math.max(0, this.dataManager.get(LEFT_CLICK_COOLDOWN));
    }

    public void setLeftClickCooldown(int value) {
        this.dataManager.set(LEFT_CLICK_COOLDOWN, Math.max(0, value));
    }

    public int maxCooldown() {
        return 30;
    }

    public abstract Vec3d riderPos();

    @Override
    public abstract void registerControllers(AnimationData data);

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public ModularPanel buildUI(EntityGuiData entityGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
        RiftLargeWeapon largeWeapon = (RiftLargeWeapon) entityGuiData.getGuiHolder();
        if (largeWeapon == null) return new ModularPanel(UIPanelNames.LARGE_WEAPON_SCREEN);

        String playerName = entityGuiData.getPlayer().getName();

        syncManager.registerSlotGroup("largeWeaponInventory", largeWeapon.weaponInventory.getSlots());
        SlotGroupWidget.Builder weaponInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                        new ModularSlot(largeWeapon.weaponInventory, index).slotGroup("largeWeaponInventory")
                                .filter(itemStack -> {
                                    return itemStack.getItem() == largeWeapon.ammoItem;
                                })
                        )
                )
                .matrix("IIIII");


        return new ModularPanel(UIPanelNames.LARGE_WEAPON_SCREEN)
                .padding(7, 7).height(131)
                .child(Flow.column().childPadding(5).coverChildrenHeight()
                        //weapon inventory
                        .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                .child(Flow.column().widthRel(1f).coverChildrenHeight()
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(IKey.str(largeWeapon.getName()).asWidget().left(0))
                                        )
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(weaponInvBuilder.build().left(0))
                                        )
                                )
                        )
                        //player inventory
                        .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                .child(Flow.column().widthRel(1f).coverChildren()
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(IKey.str(playerName).asWidget().left(0))
                                        )
                                        .child(SlotGroupWidget.playerInventory(false))
                                )
                        )
                );
    }
}
