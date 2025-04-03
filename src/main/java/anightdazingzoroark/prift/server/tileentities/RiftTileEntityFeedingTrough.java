package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.server.blocks.RiftFeedingTroughBlock;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.inventory.FeedingTroughContainer;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public class RiftTileEntityFeedingTrough extends TileEntity implements IAnimatable, ITickable {
    private final AnimationFactory factory = new AnimationFactory(this);
    public static final int INV_SIZE = 9;
    private int feedCountdown = 0;
    private ItemStackHandler itemStackHandler = new ItemStackHandler(INV_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            RiftTileEntityFeedingTrough.this.markDirty();
        }
    };

    @Override
    public void update() {
        //get all nearby creatures
        if (!this.world.isRemote) {
            this.feedCountdown++;
            if (this.feedCountdown >= 60) {
                List<RiftCreature> creatures = this.world.getEntitiesWithinAABB(RiftCreature.class, this.getFeedingRange(), new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature creature) {
                        return creature.isTamed() && !creature.isSleeping();
                    }
                });

                //start feeding them
                if (!creatures.isEmpty()) {
                    for (RiftCreature creature : creatures) {
                        IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                        if (itemHandler != null) {
                            for (int k = 0; k < INV_SIZE; ++k) {
                                ItemStack itemInSlot = itemHandler.getStackInSlot(k);
                                if (!itemHandler.getStackInSlot(k).isEmpty() && creature.isFavoriteFood(itemInSlot) && creature.getHealth() < creature.getMaxHealth()) {
                                    creature.eatFoodForHealing(itemInSlot);
                                }
                                if (!itemHandler.getStackInSlot(k).isEmpty() && creature.isEnergyRegenItem(itemInSlot) && creature.getEnergy() < creature.getMaxEnergy()) {
                                    creature.eatFoodForEnergyRegen(itemInSlot);
                                }
                            }
                        }
                    }
                }
                this.feedCountdown = 0;
            }
        }
    }

    private AxisAlignedBB getFeedingRange() {
        return new AxisAlignedBB(this.getPos().getX() - 16, this.getPos().getY() - 16, this.getPos().getZ() - 16, this.getPos().getX() + 16, this.getPos().getY() + 16, this.getPos().getZ() + 16);
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(RiftFeedingTroughBlock.FACING);
    }

    //for inventory stuff
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("items")) {
            this.itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("items", this.itemStackHandler.serializeNBT());
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
        }
        return super.getCapability(capability, facing);
    }

    public boolean inventoryIsEmpty() {
        IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler != null) {
            for (int k = 0; k < INV_SIZE; ++k) {
                if (!itemHandler.getStackInSlot(k).isEmpty()) return false;
            }
        }
        return true;
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public void registerControllers(AnimationData animationData) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
