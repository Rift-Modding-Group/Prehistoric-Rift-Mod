package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public abstract class RiftLargeWeapon extends EntityAnimal implements IAnimatable {
    private static final DataParameter<Boolean> USING_LEFT_CLICK = EntityDataManager.createKey(RiftLargeWeapon.class, DataSerializers.BOOLEAN);
    public RiftLargeWeaponInventory weaponInventory;
    public final RiftLargeWeaponType weaponType;
    private final int slotCount = 5;
    private AnimationFactory factory = new AnimationFactory(this);
    public final Item weaponItem;
    public final Item ammoItem;

    public RiftLargeWeapon(World worldIn, RiftLargeWeaponType weaponType, Item weaponItem, Item ammoItem) {
        super(worldIn);
        this.initInventory();
        this.weaponType = weaponType;
        this.weaponItem = weaponItem;
        this.ammoItem = ammoItem;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(USING_LEFT_CLICK, Boolean.FALSE);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.world.isRemote) this.setControls();
    }

    @SideOnly(Side.CLIENT)
    public void setControls() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (this.isBeingRidden()) {
            if (this.getPassengers().get(0).equals(player)) {
                if (settings.keyBindAttack.isKeyDown() && !this.isUsingLeftClick()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftLaunchLWeaponProjectile(this));
                }
                RiftMessages.WRAPPER.sendToServer(new RiftManageUtilizingControl(this, settings.keyBindAttack.isKeyDown()));
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!player.isSneaking()) RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this));
        else RiftMessages.WRAPPER.sendToServer(new RiftOpenWeaponInventory(this));
        return false;
    }

    private void initInventory() {
        int inventorySize = this.slotCount;
        this.weaponInventory = new RiftLargeWeapon.RiftLargeWeaponInventory("weaponInventory", inventorySize, this);
        this.weaponInventory.setCustomName(this.getName());
        if (this.weaponInventory != null) {
            for (int i = 0; i < inventorySize; ++i) {
                ItemStack itemStack = this.weaponInventory.getStackInSlot(i);
                if (!itemStack.isEmpty()) this.weaponInventory.setInventorySlotContents(i, itemStack.copy());
            }
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (this.weaponInventory != null) {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                int inventorySize = this.slotCount;
                if (j < inventorySize) this.weaponInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        } else {
            NBTTagList nbtTagList = compound.getTagList("Items", 10);
            this.initInventory();
            for (int i = 0; i < nbtTagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbtTagList.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;
                this.weaponInventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.weaponInventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.weaponInventory.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }
        compound.setTag("Items", nbttaglist);
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

    public abstract Vec3d riderPos();

    @Override
    public abstract void registerControllers(AnimationData data);

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public class RiftLargeWeaponInventory extends ContainerHorseChest {
        public RiftLargeWeaponInventory(String inventoryTitle, int slotCount, RiftLargeWeapon weapon) {
            super(inventoryTitle, slotCount);
            this.addInventoryChangeListener(new RiftLargeWeapon.RiftWeaponInvListener(weapon));
        }

        public void setInventoryFromData(RiftChangeWeaponInvFromMenu.RiftWeaponInvData data) {
            ItemStack[] contents = data.getInventoryContents();

            if (contents.length != getSizeInventory()) {
                throw new IllegalArgumentException("Invalid inventory size");
            }

            for (int i = 0; i < getSizeInventory(); i++) {
                setInventorySlotContents(i, contents[i]);
            }
        }
    }

    class RiftWeaponInvListener implements IInventoryChangedListener {
        RiftLargeWeapon weapon;

        public RiftWeaponInvListener(RiftLargeWeapon weapon) {
            this.weapon = weapon;
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {}
    }
}
