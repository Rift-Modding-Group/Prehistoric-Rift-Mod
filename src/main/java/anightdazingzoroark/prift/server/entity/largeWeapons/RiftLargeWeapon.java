package anightdazingzoroark.prift.server.entity.largeWeapons;

import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.message.RiftChangeInventoryFromMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public abstract class RiftLargeWeapon extends EntityAnimal implements IAnimatable {
    private static final DataParameter<Float> DAMAGE = EntityDataManager.<Float>createKey(RiftLargeWeapon.class, DataSerializers.FLOAT);
    public RiftLargeWeaponInventory weaponInventory;
    public final RiftLargeWeaponType weaponType;
    private final int slotCount = 5;
    private AnimationFactory factory = new AnimationFactory(this);

    public RiftLargeWeapon(World worldIn, RiftLargeWeaponType weaponType) {
        super(worldIn);
        this.initInventory();
        this.weaponType = weaponType;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(DAMAGE, 0.0F);
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
        }
        else {
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
//    public boolean attackEntityFrom(DamageSource source, float amount)
//    {
//        if (this.isEntityInvulnerable(source)) return false;
//        else if (!this.world.isRemote && !this.isDead)
//        {
//            if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() != null && this.isPassenger(source.getTrueSource())) return false;
//            else {
//                this.setDamage(this.getDamage() + amount * 10.0F);
//                this.markVelocityChanged();
//                boolean flag = source.getTrueSource() instanceof EntityPlayer && ((EntityPlayer)source.getTrueSource()).capabilities.isCreativeMode;
//
//                if (flag || this.getDamage() > 40.0F) {
//                    if (!flag && this.world.getGameRules().getBoolean("doEntityDrops")) {
//                        this.dropItemWithOffset(this.weaponType.getItem(), 1, 0.0F);
//                    }
//
//                    this.setDead();
//                }
//
//                return true;
//            }
//        }
//        else return true;
//    }

    public float getDamage() {
        return this.dataManager.get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, damage);
    }
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

        public void setInventoryFromData(RiftChangeInventoryFromMenu.RiftCreatureInvData data) {
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
