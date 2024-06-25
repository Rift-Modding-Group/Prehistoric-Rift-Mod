package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public abstract class TileEntitySemiManualBase extends TileEntity implements IAnimatable, ITickable, ISidedInventory {
    private final AnimationFactory factory = new AnimationFactory(this);
    protected NonNullList<ItemStack> itemStackHandler = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
    private boolean playResetAnim;
    private int resetAnimTime;

    @Override
    public void update() {
        //manage reset anim
        if (!this.world.isRemote) {
            if (this.canDoResetAnim()) {
                this.setResetAnimTime(this.getResetAnimTime() + 1);
                if (this.getResetAnimTime() >= 10) {
                    this.setPlayResetAnim(false);
                    this.setResetAnimTime(0);
                }
            }
        }
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(BlockSemiManualBase.FACING);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.itemStackHandler = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.itemStackHandler);
        this.playResetAnim = compound.getBoolean("playResetAnim");
        this.resetAnimTime = compound.getInteger("resetAnimTime");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.itemStackHandler);
        compound.setBoolean("playResetAnim", this.playResetAnim);
        compound.setInteger("resetAnimTime", this.resetAnimTime);
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == EnumFacing.DOWN) return (T) new SidedInvWrapper(this, EnumFacing.DOWN);
            else if (facing == EnumFacing.UP)  return (T) new SidedInvWrapper(this, EnumFacing.UP);
            else if (facing == EnumFacing.EAST)  return (T) new SidedInvWrapper(this, EnumFacing.EAST);
            else if (facing == EnumFacing.WEST)  return (T) new SidedInvWrapper(this, EnumFacing.WEST);
            else if (facing == EnumFacing.NORTH)  return (T) new SidedInvWrapper(this, EnumFacing.NORTH);
            else if (facing == EnumFacing.SOUTH)  return (T) new SidedInvWrapper(this, EnumFacing.SOUTH);
            else return (T) new SidedInvWrapper(this, EnumFacing.NORTH);
        }
        return super.getCapability(capability, facing);
    }

    public boolean canDoResetAnim() {
        return this.playResetAnim;
    }

    public void setPlayResetAnim(boolean value) {
        this.playResetAnim = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getResetAnimTime() {
        return this.resetAnimTime;
    }

    public void setResetAnimTime(int value) {
        this.resetAnimTime = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.itemStackHandler = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.itemStackHandler);
        this.playResetAnim = tag.getBoolean("playResetAnim");
        this.resetAnimTime = tag.getInteger("resetAnimTime");
    }

    //inventory stuff starts here
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) return new int[]{1};
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (index == 1) return direction == EnumFacing.DOWN;
        return true;
    }

    @Override
    public int getSizeInventory() {
        return this.itemStackHandler.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStackHandler) {
            if (!itemstack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.itemStackHandler.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.itemStackHandler, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.itemStackHandler, index);
    }

    public void insertItemToSlot(int slot, ItemStack itemStack) {
        int newCount = this.getStackInSlot(slot).getCount() + itemStack.getCount();
        itemStack.setCount(newCount);
        this.setInventorySlotContents(slot, itemStack);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.itemStackHandler.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !isInvalid() && player.getDistanceSq(this.pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != 1;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.itemStackHandler.clear();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
    //inventory stuff ends here

    public ItemStack getInputItem() {
        return this.getStackInSlot(0);
    }

    public TileEntitySemiManualTopBase getTopTEntity() {
        return (TileEntitySemiManualTopBase)this.world.getTileEntity(this.pos.up());
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "reset", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent animationEvent) {
                if (canDoResetAnim()) {
                    animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("animation.semi_manual_extractor.release", false));
                    return PlayState.CONTINUE;
                }
                animationEvent.getController().clearAnimationCache();
                return PlayState.STOP;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(1, 2, 1));
    }
}